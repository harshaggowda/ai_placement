# api-gateway image. Build context is the REPOSITORY ROOT so the Maven reactor can
# resolve the shared-libraries modules: docker build -f api-gateway/Dockerfile .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY shared-libraries ./shared-libraries
COPY api-gateway ./api-gateway
COPY auth-service ./auth-service
COPY user-service ./user-service
COPY career-service ./career-service
COPY payment-service ./payment-service
COPY ai-orchestrator-service ./ai-orchestrator-service
RUN mvn -B -q -pl api-gateway -am -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S careeros && adduser -S careeros -G careeros
USER careeros
COPY --from=build /workspace/api-gateway/target/api-gateway.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
