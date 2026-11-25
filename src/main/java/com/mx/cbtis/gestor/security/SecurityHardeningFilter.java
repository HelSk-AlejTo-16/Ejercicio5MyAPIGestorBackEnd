package com.mx.cbtis.gestor.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHardeningFilter extends OncePerRequestFilter {

    private final String internalApiKey;
    private final String metricsToken;
    private final boolean rateLimitEnabled;
    private final int loginMaxPerWindow;
    private final int apiMaxPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, long[]> counters = new ConcurrentHashMap<>();

    @Autowired
    public SecurityHardeningFilter(
            @Value("${security.internal-api-key:}") String internalApiKey,
            @Value("${security.metrics-token:}") String metricsToken,
            @Value("${security.rate-limit.enabled:true}") boolean rateLimitEnabled,
            @Value("${security.rate-limit.login-max:20}") int loginMaxPerWindow,
            @Value("${security.rate-limit.api-max:3000}") int apiMaxPerWindow) {
        this(internalApiKey, metricsToken, rateLimitEnabled, loginMaxPerWindow, apiMaxPerWindow, 60_000L);
    }

    public SecurityHardeningFilter(
            String internalApiKey,
            String metricsToken,
            boolean rateLimitEnabled,
            int loginMaxPerWindow,
            int apiMaxPerWindow,
            long windowMillis) {
        this.internalApiKey = internalApiKey.trim();
        this.metricsToken = metricsToken.trim();
        this.rateLimitEnabled = rateLimitEnabled;
        this.loginMaxPerWindow = loginMaxPerWindow;
        this.apiMaxPerWindow = apiMaxPerWindow;
        this.windowMillis = windowMillis;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        applySecurityHeaders(response);

        String path = request.getRequestURI();
        if ("/actuator/health".equals(path) || "/actuator/info".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/actuator/")) {
            if (!metricsToken.isEmpty() && !validBearerToken(request.getHeader("Authorization"), metricsToken)) {
                writeJsonError(response, 401, "{\"error\":\"No autorizado para metricas\"}");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        if (!internalApiKey.isEmpty() && !validApiKey(request.getHeader("X-Internal-Api-Key"), internalApiKey)) {
            writeJsonError(response, 401, "{\"error\":\"API Key interna invalida o ausente\"}");
            return;
        }

        if (rateLimitEnabled) {
            boolean isAuthEndpoint = "/getUsuarioLogin".equals(path) || "/addUsuario".equals(path);
            int limit = isAuthEndpoint ? loginMaxPerWindow : apiMaxPerWindow;
            String bucket = (isAuthEndpoint ? "auth:" : "api:") + resolveClientIp(request);
            if (!allowRequest(bucket, limit, System.currentTimeMillis())) {
                response.setHeader("Retry-After", "60");
                writeJsonError(response, 429, "{\"error\":\"Demasiadas peticiones, intente mas tarde\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    static void applySecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
    }

    boolean allowRequest(String bucketKey, int limit, long nowMillis) {
        long[] state = counters.compute(bucketKey, (k, cur) -> {
            if (cur == null || (nowMillis - cur[0]) >= windowMillis) {
                return new long[] { nowMillis, 1L };
            }
            cur[1]++;
            return cur;
        });
        return state[1] <= limit;
    }

    static boolean validBearerToken(String authHeader, String expectedToken) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String provided = authHeader.substring(7);
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expectedToken.getBytes(StandardCharsets.UTF_8));
    }

    static boolean validApiKey(String headerValue, String expectedKey) {
        if (headerValue == null) {
            return false;
        }
        return MessageDigest.isEqual(
                headerValue.getBytes(StandardCharsets.UTF_8),
                expectedKey.getBytes(StandardCharsets.UTF_8));
    }

    static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static void writeJsonError(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }
}
