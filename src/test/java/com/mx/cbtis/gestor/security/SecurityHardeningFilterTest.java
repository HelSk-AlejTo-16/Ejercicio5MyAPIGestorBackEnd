package com.mx.cbtis.gestor.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHardeningFilterTest {

    @Test
    @DisplayName("Aplica cabeceras HTTP de seguridad y permite probes publicos /actuator/health e /info")
    void allowsPublicActuatorProbesAndSetsHeaders() throws Exception {
        SecurityHardeningFilter filter = new SecurityHardeningFilter(
                "secret-api-key", "secret-metrics-token", true, 2, 5, 60_000L);

        MockHttpServletRequest reqHealth = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse resHealth = new MockHttpServletResponse();
        MockFilterChain chainHealth = new MockFilterChain();
        filter.doFilter(reqHealth, resHealth, chainHealth);

        assertEquals(200, resHealth.getStatus());
        assertEquals("nosniff", resHealth.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", resHealth.getHeader("X-Frame-Options"));
        assertEquals("strict-origin-when-cross-origin", resHealth.getHeader("Referrer-Policy"));
        assertTrue(resHealth.getHeader("Strict-Transport-Security").contains("max-age=31536000"));
        assertTrue(resHealth.getHeader("Content-Security-Policy").contains("frame-ancestors 'none'"));

        MockHttpServletRequest reqInfo = new MockHttpServletRequest("GET", "/actuator/info");
        MockHttpServletResponse resInfo = new MockHttpServletResponse();
        filter.doFilter(reqInfo, resInfo, new MockFilterChain());
        assertEquals(200, resInfo.getStatus());
    }

    @Test
    @DisplayName("Protege /actuator/prometheus con Bearer token y permite cuando el token coincide o esta vacio")
    void enforcesMetricsBearerToken() throws Exception {
        SecurityHardeningFilter protectedFilter = new SecurityHardeningFilter(
                "", "prom-token-123", false, 10, 10);

        // Sin header Authorization -> 401
        MockHttpServletRequest reqNoAuth = new MockHttpServletRequest("GET", "/actuator/prometheus");
        MockHttpServletResponse resNoAuth = new MockHttpServletResponse();
        protectedFilter.doFilter(reqNoAuth, resNoAuth, new MockFilterChain());
        assertEquals(401, resNoAuth.getStatus());

        // Con esquema distinto de Bearer -> 401
        MockHttpServletRequest reqBasic = new MockHttpServletRequest("GET", "/actuator/prometheus");
        reqBasic.addHeader("Authorization", "Basic abcdef");
        MockHttpServletResponse resBasic = new MockHttpServletResponse();
        protectedFilter.doFilter(reqBasic, resBasic, new MockFilterChain());
        assertEquals(401, resBasic.getStatus());

        // Con Bearer incorrecto -> 401
        MockHttpServletRequest reqWrong = new MockHttpServletRequest("GET", "/actuator/prometheus");
        reqWrong.addHeader("Authorization", "Bearer wrong-token");
        MockHttpServletResponse resWrong = new MockHttpServletResponse();
        protectedFilter.doFilter(reqWrong, resWrong, new MockFilterChain());
        assertEquals(401, resWrong.getStatus());

        // Con Bearer correcto -> 200
        MockHttpServletRequest reqOk = new MockHttpServletRequest("GET", "/actuator/prometheus");
        reqOk.addHeader("Authorization", "Bearer prom-token-123");
        MockHttpServletResponse resOk = new MockHttpServletResponse();
        protectedFilter.doFilter(reqOk, resOk, new MockFilterChain());
        assertEquals(200, resOk.getStatus());

        // Con metricsToken vacio -> permite /actuator/prometheus en modo abierto
        SecurityHardeningFilter openFilter = new SecurityHardeningFilter("", "", false, 10, 10);
        MockHttpServletRequest reqOpen = new MockHttpServletRequest("GET", "/actuator/prometheus");
        MockHttpServletResponse resOpen = new MockHttpServletResponse();
        openFilter.doFilter(reqOpen, resOpen, new MockFilterChain());
        assertEquals(200, resOpen.getStatus());
    }

    @Test
    @DisplayName("Exige X-Internal-Api-Key en endpoints de negocio cuando esta configurada")
    void enforcesInternalApiKeyZeroTrust() throws Exception {
        SecurityHardeningFilter filter = new SecurityHardeningFilter(
                "zero-trust-key", "", false, 10, 10);

        // Sin cabecera -> 401
        MockHttpServletRequest reqMissing = new MockHttpServletRequest("POST", "/buscarProyectos");
        MockHttpServletResponse resMissing = new MockHttpServletResponse();
        filter.doFilter(reqMissing, resMissing, new MockFilterChain());
        assertEquals(401, resMissing.getStatus());

        // Con cabecera incorrecta -> 401
        MockHttpServletRequest reqInvalid = new MockHttpServletRequest("POST", "/buscarProyectos");
        reqInvalid.addHeader("X-Internal-Api-Key", "clave-falsa");
        MockHttpServletResponse resInvalid = new MockHttpServletResponse();
        filter.doFilter(reqInvalid, resInvalid, new MockFilterChain());
        assertEquals(401, resInvalid.getStatus());

        // Con cabecera valida -> 200
        MockHttpServletRequest reqValid = new MockHttpServletRequest("POST", "/buscarProyectos");
        reqValid.addHeader("X-Internal-Api-Key", "zero-trust-key");
        MockHttpServletResponse resValid = new MockHttpServletResponse();
        filter.doFilter(reqValid, resValid, new MockFilterChain());
        assertEquals(200, resValid.getStatus());
    }

    @Test
    @DisplayName("Aplica Rate Limiting anti-fuerza bruta en login, registro y API general, y reinicia ventana")
    void enforcesRateLimitingAndWindowReset() throws Exception {
        SecurityHardeningFilter filter = new SecurityHardeningFilter(
                "", "", true, 1, 2, 50L);

        // Endpoint de login (/getUsuarioLogin): limite = 1
        MockHttpServletRequest login1 = new MockHttpServletRequest("POST", "/getUsuarioLogin");
        login1.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        MockHttpServletResponse resLogin1 = new MockHttpServletResponse();
        filter.doFilter(login1, resLogin1, new MockFilterChain());
        assertEquals(200, resLogin1.getStatus());

        MockHttpServletRequest login2 = new MockHttpServletRequest("POST", "/addUsuario");
        login2.addHeader("X-Forwarded-For", "203.0.113.10");
        MockHttpServletResponse resLogin2 = new MockHttpServletResponse();
        filter.doFilter(login2, resLogin2, new MockFilterChain());
        assertEquals(429, resLogin2.getStatus());
        assertEquals("60", resLogin2.getHeader("Retry-After"));

        // Endpoint general (/buscarProyectos) con X-Forwarded-For en blanco usa remoteAddr: limite = 2
        MockHttpServletRequest api1 = new MockHttpServletRequest("POST", "/buscarProyectos");
        api1.addHeader("X-Forwarded-For", "   ");
        api1.setRemoteAddr("192.0.2.55");
        MockHttpServletResponse resApi1 = new MockHttpServletResponse();
        filter.doFilter(api1, resApi1, new MockFilterChain());
        assertEquals(200, resApi1.getStatus());

        MockHttpServletRequest api2 = new MockHttpServletRequest("POST", "/buscarProyectos");
        api2.setRemoteAddr("192.0.2.55");
        MockHttpServletResponse resApi2 = new MockHttpServletResponse();
        filter.doFilter(api2, resApi2, new MockFilterChain());
        assertEquals(200, resApi2.getStatus());

        MockHttpServletRequest api3 = new MockHttpServletRequest("POST", "/buscarProyectos");
        api3.setRemoteAddr("192.0.2.55");
        MockHttpServletResponse resApi3 = new MockHttpServletResponse();
        filter.doFilter(api3, resApi3, new MockFilterChain());
        assertEquals(429, resApi3.getStatus());

        // Verificar reinicio determinista de ventana de tiempo en allowRequest
        assertTrue(filter.allowRequest("test-window", 1, 1_000L));
        assertFalse(filter.allowRequest("test-window", 1, 1_010L));
        assertTrue(filter.allowRequest("test-window", 1, 1_060L));
    }
}
