FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=secret,id=sentry_token \
    SENTRY_AUTH_TOKEN="$(cat /run/secrets/sentry_token 2>/dev/null || true)" \
    mvn clean package -DskipTests
RUN mvn dependency:copy -Dartifact=io.sentry:sentry-opentelemetry-agent:8.57.0 -DoutputDirectory=/app

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/sentry-opentelemetry-agent-8.57.0.jar sentry-agent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/app/sentry-agent.jar", "-jar", "app.jar"]