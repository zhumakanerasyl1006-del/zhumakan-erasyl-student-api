# Stage 1: Build Spring Boot application
FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN chmod +x ./gradlew && \
    ./gradlew clean bootJar --no-daemon

# Stage 2: Run application
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system appgroup && \
    useradd --system --gid appgroup appuser

COPY --from=builder /app/build/libs/student-api.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
