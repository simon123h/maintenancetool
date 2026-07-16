# Betriebs- und Hosting-Dokumentation (Betriebsdoku)

Dieses Dokument beschreibt den Betrieb, das Deployment und die Konfiguration der Maintenance-Tool-Anwendung in der Produktionsumgebung.

---

## 1. Systemvoraussetzungen & Laufzeitumgebung

Die Anwendung ist als **Single Deployment Artifact** (Executable FAT WAR) konzipiert, das sowohl die Java-Backend-Anwendung als auch die vorkompilierten statischen Frontend-Dateien beherbergt.

| Komponente | Empfohlene Voraussetzung |
| :--- | :--- |
| **Java Runtime (JRE)** | Java 21 oder neuer (z. B. Eclipse Temurin) |
| **Arbeitsspeicher** | Mindestens 1 GB RAM frei für die JVM (Xmx512m empfohlen) |
| **Datenbank** | Microsoft SQL Server (2016 oder neuer) |
| **Speicherplatz** | ~100 MB für die WAR-Datei + Platz für die lokale H2-Datenbank (falls kein SQL Server genutzt wird) |
| **Netzwerk** | Port `8080` (Standard, frei konfigurierbar) |

---

## 2. Deployment-Modell

Die Anwendung wird als ausführbare WAR-Datei ausgeliefert. Der integrierte Tomcat-Server ermöglicht den direkten Start auf dem Server ohne externen Webserver-Container.

### Manueller Start (Service-Start)
Das Deployment erfolgt über das Ausführen des JAR/WAR-Archivs im Vorder- oder Hintergrund:

```bash
java -jar maintenance-tool.war
```

> [!TIP]
> Für den produktiven Betrieb wird dringend empfohlen, die Anwendung als System-Service (z. B. via **systemd** unter Linux oder als Windows-Service) einzurichten.

### Linux systemd Service-Beispiel (`/etc/systemd/system/maintenance-tool.service`):
```ini
[Unit]
Description=Maintenance Tool Webapplikation
After=syslog.target network.target

[Service]
User=maintenancetool
Type=simple
WorkingDirectory=/var/lib/maintenancetool
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar -Dspring.profiles.active=prod /var/lib/maintenancetool/maintenance-tool.war
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

---

## 3. Container-basiertes Deployment (Containerfile / Docker)

Die Anwendung ist hervorragend für den Betrieb in Container-Umgebungen (Docker, Podman, Kubernetes, OpenShift) vorbereitet. Im Root-Verzeichnis befindet sich ein optimiertes Multi-Stage **`Containerfile`**.

### Vorteile der Containerisierung:
*   **Multi-Stage Build**: Der Sourcecode wird innerhalb eines temporären Java-Builders kompiliert. Das endgültige Laufzeit-Image enthält nur das schlanke JRE-System (auf Alpine-Linux-Basis) ohne Build-Tools, Node.js oder Quellcode.
*   **Sicherheitsoptimiert**: Der Container läuft standardmäßig unter einem unprivilegierten Systembenutzer (`appuser`), um Container-Escape-Szenarien vorzubeugen.
*   **Ressourcen-optimiert**: Das Image nutzt Garbage Collection (G1GC) und JVM Container-Support, um sich dynamisch an CPU/Arbeitsspeicher-Limits des Containers anzupassen.

### Image lokal bauen:
```bash
docker build -t maintenance-tool:latest .
```

### Container starten:
```bash
# Startet den Container im Hintergrund auf Port 8080
docker run -d \
  -p 8080:8080 \
  --name maintenance-tool-app \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:sqlserver://sql.meinefirma.de:1433;databaseName=maintenancedb \
  -e SPRING_DATASOURCE_USERNAME=app_user \
  -e SPRING_DATASOURCE_PASSWORD=StrongPassword123 \
  -v /var/lib/maintenancetool/storage:/app/storage \
  maintenance-tool:latest
```

---

## 4. Konfiguration der Anwendung

Die Konfiguration der Anwendung erfolgt primär über zwei Wege:
1. **Externe Konfigurationsdatei (`maintenance-tool.properties`)**: Legen Sie eine Datei namens `maintenance-tool.properties` im aktuellen Arbeitsverzeichnis (neben der `.war` Datei) oder unter `/etc/maintenancetool/maintenance-tool.properties` ab. Diese Datei wird beim Start automatisch erkannt und überschreibt die internen Standardwerte.
2. **Umgebungsvariablen (Environment Variables)**: Da Spring Boot Umgebungsvariablen automatisch auf Konfigurationsschlüssel mappt, können Sie jeden Parameter flexibel beim Starten (z. B. im systemd-Service oder in Docker-Containern) übergeben.

### Wichtigste Konfigurationsparameter:

| Eigenschaft in `maintenance-tool.properties` | Umgebungsvariable | Beschreibung | Standard / Beispiel |
| :--- | :--- | :--- | :--- |
| `server.port` | `SERVER_PORT` | Port, auf dem die Webanwendung lauscht | `8080` |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | JDBC-Verbindungs-URL (z. B. MS SQL Server oder H2) | `jdbc:h2:file:/var/lib/maintenancetool/storage/maintenancedb;...` |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME`| Datenbank-Benutzername | `sa` (für H2) / `maintenancetool_app` |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD`| Datenbank-Passwort | *(keines bei H2)* / *PROD_PASSWORT* |
| `app.security.auth-method` | `APP_SECURITY_AUTH_METHOD` | Authentifizierungs-Verfahren (`SAML2` / `LDAP`) | `SAML2` |

---

## 5. Datenbank-Migrationen (Liquibase)

Die Datenbank-Migrationen werden über **Liquibase** gesteuert.
*   **Automatisches Bootstrapping:** Die Anwendung ist so vorkonfiguriert, dass alle ausstehenden Liquibase-Datenbankänderungen **automatisch bei jedem Anwendungsstart** ausgeführt werden (`spring.liquibase.enabled=true`).
*   **Sicherheitshinweis:** Stellen Sie sicher, dass der konfigurierte Datenbankbenutzer in Produktion die Berechtigung besitzt, Tabellen anzulegen, zu ändern (DDL) und Daten zu manuell zu bearbeiten (DML).
*   **Manueller Audit:** Liquibase legt automatisch die Tabellen `DATABASECHANGELOG` und `DATABASECHANGELOGLOCK` in Ihrer MS SQL-Datenbank an, um den Migrationsstatus zu tracken.

---

## 6. Konfiguration der Authentifizierung

In Produktion kann zwischen **SAML2** (Single Sign-On gegen IDP) und **LDAP** gewählt werden. Dies wird über die Property `app.security.auth-method` gesteuert.

### Szenario A: SAML2 (Standard)
Der Server agiert als SAML2 Service Provider (SP) und benötigt die Metadaten des Identity Providers (IDP).
Konfigurieren Sie folgende Parameter in der Produktionsumgebung:
*   `spring.security.saml2.relyingparty.registration.[registrationId].assertingparty.metadata-uri`: URL oder lokaler Pfad zur XML-Metadatendatei Ihres Firmen-IDPs.

### Szenario B: LDAP-Authentifizierung
Falls kein SAML2 gewünscht ist, aktivieren Sie LDAP über `app.security.auth-method=LDAP`.
Geben Sie die Verbindungsdaten zu Ihrem Active Directory / LDAP-Server an:
*   `spring.ldap.urls`: z.B. `ldaps://ldap.meinefirma.de:636`
*   `spring.ldap.base`: z.B. `dc=meinefirma,dc=de`
*   `spring.ldap.username`: Service-Konto zum Suchen im Verzeichnis.
*   `spring.ldap.password`: Passwort des Service-Kontos.

---

## 7. Datensicherung & Wartung

### Backup-Strategie
Für eine vollständige Datensicherung müssen die Datenbankinhalte gesichert werden:
1.  **Datenbank (MS SQL Server):** Regelmäßige Backups der SQL-Datenbank (z. B. täglich nachts) über standardisierte SQL-Server-Wartungspläne.
2.  **Lokaler Dateispeicher (H2-Ausweichlösung):** Falls die H2-Datenbank betrieben wird, sichern Sie die Datenbankdatei unter dem konfigurierten Storage-Ordner (z. B. `maintenancedb.mv.db`).

### Logdateien (Log-Rotation)
Protokolle werden mittels **Logback** gesteuert:
*   Die Logs werden auf der Konsole und im Verzeichnis `${user.dir}/logs` ausgegeben.
*   Konfiguriert ist ein **Size- and Time-based Rolling Policy** (max. 10 MB pro Logdatei, automatisches ZIP-Archivieren, Vorhaltezeit: 30 Tage, max. 1 GB Gesamtkapazität).
