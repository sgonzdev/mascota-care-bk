# Dockerfile multi-stage reutilizable para todos los microservicios.
# Uso: docker build --build-arg SERVICE=pet-service -t mascotacare/pet-service .

ARG SERVICE
ARG JAVA_VERSION=21

# ---------- Stage 1: Build con Maven ----------
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS builder
ARG SERVICE
WORKDIR /workspace

# Copiar pom y wrapper para cachear dependencias
COPY ${SERVICE}/.mvn/ .mvn/
COPY ${SERVICE}/mvnw ${SERVICE}/pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Compilar
COPY ${SERVICE}/src ./src
RUN ./mvnw -B -q package -DskipTests \
    && cp target/*.jar app.jar

# ---------- Stage 2: Runtime ligero ----------
FROM eclipse-temurin:${JAVA_VERSION}-jre
ARG SERVICE
ENV SERVICE_NAME=${SERVICE}
WORKDIR /app

# Usuario no-root por seguridad
RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=builder --chown=spring:spring /workspace/app.jar app.jar
USER spring

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=4s --retries=10 --start-period=40s \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
