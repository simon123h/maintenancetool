# Beitrags- und Entwicklungsrichtlinien (Contributing Guidelines)

Willkommen im Entwicklungsteam der Maintenance-Tool-App! Dieses Dokument beschreibt die verbindlichen Qualitätsstandards, Programmierrichtlinien und Workflows für alle Entwickler und automatisierten Coding-Assistenten. 

---

## 🚦 1. Git-Workflow & Commit-Richtlinien

### 1.1. Automatische Commits
*   **Keine unaufgeforderten Commits:** Wenn Sie ein automatisiertes Entwicklungswerkzeug oder ein KI-Modell verwenden, darf dieses **niemals eigenständig Commits durchführen (`git commit`)**, es sei denn, der Benutzer hat dies explizit angefordert.

### 1.2. Commit-Nachrichten (Conventional Commits)
Sämtliche Commit-Nachrichten müssen dem **Conventional Commits**-Standard entsprechen. Verwenden Sie folgendes Format:
```text
<typ>(<bereich>): <kurze beschreibung>

[optionale detaillierte beschreibung]
```

**Erlaubte Typen (`<typ>`):**
*   `feat`: Ein neues funktionales Feature im Front- oder Backend.
*   `fix`: Ein Bugfix im Code.
*   `docs`: Änderungen an der Dokumentation (z. B. in `docs/` oder `README.md`).
*   `style`: Änderungen am Code-Styling oder Formatierung ohne funktionale Auswirkung.
*   `refactor`: Code-Restrukturierung (z. B. Aufteilen monolithischer Komponenten), die weder Bugs behebt noch neue Features einführt.
*   `test`: Hinzufügen oder Korrigieren von JUnit- oder Frontend-Tests.
*   `chore`: Aktualisierung von Abhängigkeiten, Build-Skripten (Maven/npm) oder CI/CD-Konfigurationen.

**Beispiele:**
*   `feat(auth): add optional ldap authentication active profile dev`
*   `refactor(frontend): modularize monolithic App.vue into concise components`
*   `docs(architecture): update deployment node views in arc42`

---

## 🎨 2. Frontend-Entwicklungsrichtlinien (Vue.js 3 & TypeScript)

Das gesamte Frontend liegt im Verzeichnis `frontend/`. Für Beitragsänderungen gelten folgende technologische Standards:

*   **Framework-Präferenz:** Verwenden Sie **Vue.js 3** mit der **Composition API** (Verwendung von `<script setup lang="ts">`). Verwenden Sie *kein* React.
*   **Typisierung:** Bevorzugen Sie konsequent **TypeScript** gegenüber JavaScript. Definieren Sie klare Schnittstellen und Typen in [types.ts](file:///home/simon/Code/unpackme/schulungsplanung/frontend/src/types.ts).
*   **KISS-Prinzip & Modularität:** 
    *   Vermeiden Sie monolithische Komponenten. Teilen Sie Ansichten in kleine, überschaubare, wiederverwendbare Komponenten auf.
    *   Nutzen Sie **Scoped Styles** (`<style scoped>`) in Vue-Dateien, um Styling-Kollisionen zu vermeiden.
    *   Verwenden Sie standardisierte, HSL-basierte globale CSS-Variablen aus `style.css` zur Farbdarstellung (Theme-Unterstützung).

---

## ☕ 3. Backend-Entwicklungsrichtlinien (Spring Boot & JPA)

Das Java-Backend im Verzeichnis `src/main/` folgt klassischen Spring Boot-Best-Practices:

*   **Paketstruktur (Package-by-Feature):**
    *   Strukturieren Sie Klassen nach funktionalen Domänen/Features (z. B. `de.maintenancetool.user`, `de.maintenancetool.communication`) anstelle von rein technischen Layern.
*   **Architektur-Schichten (KISS):**
    *   Halten Sie eine klare Trennung zwischen **Entity** (Datenbankmodell), **DTO** (Datenübertragungskapsel) und **Controller** (Schnittstellen-Definition) ein.
    *   Verwenden Sie JPA-Repositories (`Spring Data JPA`) für sämtliche Datenbankoperationen.
*   **Fehlerbehandlung & Exceptions:**
    *   Fangen Sie fachliche Fehler im Backend über einen zentralen `@RestControllerAdvice` (`GlobalExceptionHandler`) ab.
    *   Geben Sie Fehler ausschließlich als einheitliche JSON-Payloads (`ErrorResponse`) mit passendem HTTP-Statuscode an das Frontend zurück.
*   **Datenbank-Migrationen (Liquibase):**
    *   Führen Sie DDL- oder DML-Schema-Änderungen **niemals** manuell aus.
    *   Jede Schema-Änderung muss über Liquibase-Changelogs (`db/changelog/`) deklariert werden. Diese werden beim Anwendungsstart automatisch eingespielt (bootstrapped).
*   **Logging & Auditing:**
    *   Protokollieren Sie relevante Geschäftsereignisse und Exceptions mit SLF4J-Loggern.
    *   Die Konfiguration wird über `logback-spring.xml` gesteuert und beinhaltet eine automatische, zeit- und größenbasierte Log-Rotation für Produktionsserver.

---

## 📝 4. Dokumentations-Richtlinien (Documentation-as-Code)

Die Dokumentation des Projekts befindet sich im Ordner `docs/`. Jede Code-Änderung, die das Verhalten oder die Bereitstellung beeinflusst, muss in der zugehörigen Dokumentation nachgezogen werden.

*   **Entwicklungs-Handbuch:** Muss unter [docs/dev/](file:///home/simon/Code/unpackme/schulungsplanung/docs/dev/README.md) dokumentiert werden.
*   **Diagramme:** Erstellen Sie technische Flussdiagramme, ER-Modelle, Bausteinsichten und Bereitstellungsdiagramme ausschließlich als **Mermaid.js**-Codeblöcke direkt in den Markdown-Dateien.

---

## 🧪 5. Testing-Richtlinien

Sichern Sie alle Geschäftsregeln und Workflows im Backend durch automatisierte Tests ab:

*   **Test-Framework:** Verwenden Sie **JUnit 5** und Mockito.
*   **Integrationstests:** Nutzen Sie `@SpringBootTest` in Kombination mit der H2-In-Memory-Datenbank. Datenbank-Mocks sind für Integrationstests zu vermeiden; nutzen Sie stattdessen die realen Repositories und lassen Sie das Liquibase-Schema vorab booten.
*   **Verifikation:** Vor jedem Commit oder Push muss sichergestellt sein, dass die gesamte Testsuite fehlerfrei durchläuft (`npm run test` oder `mvn test`).
