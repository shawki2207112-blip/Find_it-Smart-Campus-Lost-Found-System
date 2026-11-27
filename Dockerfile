# ---- Stage 1: build (dependencies are cached in their own layer) ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Stage 2: runtime ----
FROM eclipse-temurin:17-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system findit \
    && useradd --system --gid findit --no-create-home findit
WORKDIR /app
COPY --from=build /build/target/findit.jar /app/findit.jar
USER findit

# server.port=${PORT:8080} in application.yml honours the PORT variable set by Render.
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fs "http://localhost:${PORT:-8080}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/findit.jar"]
