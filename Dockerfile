# Etapa 1: Compilación y empaquetado con Maven y JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B && \
    find target -maxdepth 1 -name "*.jar" ! -name "*.original" -exec cp {} /app/app.jar \;

# Etapa 2: Imagen de ejecución ligera con usuario no-root
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/app.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx300m -XX:+UseSerialGC"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD sh -c 'wget -q -O /dev/null http://localhost:${PORT:-8081}/actuator/health || exit 1'

ENTRYPOINT ["java", "-jar", "app.jar"]
