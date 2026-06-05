# syntax=docker/dockerfile:1.7
# ============================================================
# Build stage
# ============================================================
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY gradle gradle
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
RUN --mount=type=cache,target=/root/.gradle ./gradlew --version >/dev/null

COPY config config
COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar -x test -x checkstyleMain -x checkstyleTest -x spotlessCheck

# Extract layered jar so each layer is cacheable independently.
RUN java -Djarmode=layertools -jar build/libs/order-worker.jar extract --destination extracted

# ============================================================
# Runtime stage
# ============================================================
FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system app && useradd --system --gid app --home /app --shell /sbin/nologin app
USER app
WORKDIR /app

COPY --from=builder --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /workspace/extracted/application/ ./

EXPOSE 8081

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
