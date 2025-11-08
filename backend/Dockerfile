# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /workspace

# Cache deps first
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# Then copy sources and build a fat jar (spring-boot:repackage is automatic if plugin present)
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app

# Add a non-root user
RUN useradd -r -s /usr/sbin/nologin spring
USER spring

# Copy the built jar (wildcard to avoid version mismatch)
COPY --from=build /workspace/target/*\.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=production
EXPOSE 8080

# Optional HEALTHCHECK (only if you actually have spring-boot-actuator on classpath)
USER root
RUN set -eux; apt-get update && apt-get install -y --no-install-recommends curl ca-certificates && rm -rf /var/lib/apt/lists/*
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1
USER spring

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
