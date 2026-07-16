# Multi-stage Containerfile zur Erstellung der Produktiv-Images

# ==========================================
# Phase 1: Build-Umgebung (Java + Maven)
# ==========================================
# Wir nutzen ein glibc-basiertes Image, damit das frontend-maven-plugin
# Node/NPM fehlerfrei herunterladen und ausführen kann.
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Kopieren von pom.xml und package.json für Caching-Zwecke
COPY pom.xml /app/
COPY package.json /app/

# Kopieren aller Quellcodes (Frontend + Backend)
COPY src /app/src
COPY frontend /app/frontend

# Bauen des ausführbaren WAR-Artefakts (Tests werden im CI/CD ausgeführt)
RUN mvn clean package -DskipTests -B

# ==========================================
# Phase 2: Leichtgewichtiges JRE-Laufzeit-Image
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Erstellen eines unprivilegierten System-Benutzers für erhöhte Sicherheit
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Kopieren der gebauten WAR-Datei aus der Build-Phase
COPY --from=builder /app/target/schulungsplanung-*.war /app/app.war

# Setzen der Benutzerberechtigungen auf das Arbeitsverzeichnis
RUN chown -R appuser:appgroup /app

# Zu unprivilegiertem Benutzer wechseln
USER appuser

# Standardmäßig auf Port 8080 lauschen
EXPOSE 8080

# Festlegen der JVM-Startparameter für optimierte Speicher- und GC-Nutzung im Container
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-jar", "/app/app.war"]
