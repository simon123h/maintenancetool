# Entwickler-Dokumentation (Dev-Doku)

Dieses Dokument beschreibt den Entwicklungs-Workflow, das lokale Setup und nützliche Komfort-Skripte für Entwickler der Maintenance-Tool-Webapplikation.

---

## 1. Lokales Setup & Voraussetzungen

Um aktiv an der Anwendung zu entwickeln, werden folgende Werkzeuge auf dem Entwicklungsrechner benötigt:

- **Java Development Kit (JDK) 21** oder höher
- **Node.js 20** (inklusive npm)
- **Maven 3.8+** (bzw. der integrierte Maven Wrapper `mvnw` falls vorhanden)
- Ein Container-Tool wie **Docker** (mit Docker Compose) oder **Podman**
- Eine Java-IDE (z. B. IntelliJ IDEA, Eclipse oder VS Code)

---

## 2. Lokale Container-Infrastruktur (Docker Compose)

Um Abhängigkeiten wie Datenbanken, Authentifizierungsdienste und E-Mail-Mocks nicht lokal installieren zu müssen, steht ein vorkonfiguriertes `docker-compose.yml` im Root-Verzeichnis bereit.

### Enthaltene Dienste:
*   **MS SQL Server 2022**: Die relationale Hauptdatenbank. Auf Port `1433` freigegeben. Ein Initialisierungs-Container erstellt automatisch die Datenbank `maintenancedb`, falls diese noch nicht existiert.
*   **Keycloak 24**: Der IAM-/SSO-Provider für die SAML2-Authentifizierung. Läuft auf Port `8081`. Admin-Zugang: `admin` / `adminpass`.
*   **Mailpit**: Ein lokaler E-Mail-Mock-Server. Fängt alle ausgehenden Benachrichtigungs-Mails der Anwendung ab.
    *   **SMTP-Schnittstelle**: Port `1025`
    *   **Web UI (E-Mail-Postfach)**: Öffnen Sie [http://localhost:8025](http://localhost:8025) im Browser, um gesendete Mails zu prüfen.

### Infrastruktur starten:
```bash
# Startet MS SQL Server, Keycloak und Mailpit im Hintergrund
docker compose up -d
```

### Infrastruktur stoppen:
```bash
# Stoppt die Container und behält die persistenten SQL-Daten
docker compose down

# Stoppt die Container und löscht die Datenbankinhalte vollständig
docker compose down -v
```

---

## 3. Der Entwicklungs-Workflow (Komfort-Skripte)

Auf der **Hauptebene (Root)** des Projekts befindet sich eine zentrale `package.json`. Diese definiert Komfort-Skripte unter Verwendung von `concurrently`, um das gleichzeitige Ausführen von Frontend und Backend zu erleichtern.

### Hauptbefehle im Entwicklungs-Alltag:

#### 1. Lokale Dev-Umgebung starten
```bash
npm run dev
```
*   **Was passiert im Hintergrund?** 
    *   Startet das Spring Boot Backend im Dev-Profil (`mvn spring-boot:run`).
    *   Startet den Vite-Dev-Server für das Vue 3-Frontend (`npm --prefix frontend run dev` auf Port `3000`).
    *   Eventuelle Anfragen des Frontends an `/api/*` werden automatisch an das Backend auf Port `8080` weitergeleitet.

#### 2. Alle Tests ausführen
```bash
npm run test
```
*   **Was passiert im Hintergrund?**
    *   Führt die Java-Testsuite des Backends aus (JUnit 5 und Mockito).

---

## 4. Lokales Test-LDAP (UnboundID) & Benutzerkonten

Für die lokale Entwicklung (`spring.profiles.active=dev`) wird ein eingebetteter In-Memory LDAP-Server über das UnboundID SDK gestartet. Dieser Server liest seine Testdaten aus der Datei `src/main/resources/users.ldif` aus.

### Bereitgestellte Test-Benutzer für lokale Tests:

| Benutzername (UID) | Passwort | E-Mail | Vorgesehene DB-Rolle |
| :--- | :--- | :--- | :--- |
| `admin` | `adminpassword` | `admin@maintenancetool.de` | `ROLE_ADMIN` |
| `manager` | `managerpassword` | `manager@maintenancetool.de` | `ROLE_MANAGER` |
| `user` | `userpassword` | `user@maintenancetool.de` | `ROLE_TEILNEHMER` |

> [!NOTE]
> Die Zuordnung der Rollen findet nicht im LDAP statt. Wenn sich einer dieser User zum ersten Mal lokal einloggt, wird ein `UserAccount`-Eintrag in der H2-In-Memory-Datenbank erzeugt. Als Administrator können Sie im Admin-Panel die Berechtigungsstufen anpassen.

---

## 5. Anwender-Hilfe (Vitepress-Dokumentation)

Die Anwender-Dokumentation ist direkt in die Webapplikation integriert. Sie wird mit **Vitepress** gebaut und ist im laufenden System unter der URL-Route `/help` erreichbar.

### 5.1 Struktur der integrierten Hilfe
Die Dokumentation-as-Code-Inhalte für die Anwender-Hilfe liegen im Quellcode unter `frontend/help/`. Dadurch können sie von Entwicklern im gleichen Git-Workflow gepflegt werden wie der Anwendungscode selbst.

Folgende Dokumente sind im System hinterlegt:
*   [**Einführung (Startseite)**](../../frontend/help/index.md): Grundlegende Einweisung in das Maintenance Tool.
*   [**Anleitung für Administratoren**](../../frontend/help/admin.md): Rechtevergabe im Admin-Panel.
*   [**FAQ & Hilfe**](../../frontend/help/faq.md): Schnelle Antworten auf häufig gestellte Fragen (z. B. Wartungsankündigungen, E-Mails).

### 5.2 Lokale Bearbeitung (Hot-Reload)
*   Um die Hilfe live im Browser mit Hot-Reload zu bearbeiten, wechseln Sie in das Frontend-Verzeichnis und starten Sie das Hilfe-Dev-Skript:
    ```bash
    cd frontend
    npm run help:dev
    ```
*   Die Hilfe ist dann lokal unter `http://localhost:5173/help/` erreichbar.

### 5.3 Bearbeitung & Erweiterung der Hilfe
Da alle Seiten im Markdown-Format (`.md`) verfasst sind, können neue Hilfeartikel oder Aktualisierungen einfach durch das Erstellen/Bearbeiten von Markdown-Dateien im Verzeichnis `frontend/help/` vorgenommen werden.

#### Hinzufügen einer neuen Seite:
1.  Erstellen Sie eine neue Datei, z. B. `frontend/help/neues-thema.md`.
2.  Tragen Sie Ihre Inhalte in Standard-Markdown ein.
3.  Registrieren Sie die neue Seite in der Navigationsstruktur in der Datei `frontend/help/.vitepress/config.ts`:
    ```typescript
    items: [
      { text: 'Einführung', link: '/' },
      { text: 'Neues Thema', link: '/neues-thema' }, // <-- Hinzufügen
      ...
    ]
    ```
4.  Beim nächsten Maven-Build (`mvn clean package`) wird die Hilfeseite automatisch neu generiert und in die ausführbare WAR-Datei integriert.
