# Maintenance Tool

[![Build Status](https://github.com/simon123h/schulungsplanung/actions/workflows/ci.yml/badge.svg)](https://github.com/simon123h/schulungsplanung/actions/workflows/ci.yml)

Eine einfache, moderne Webanwendung zur Planung von Wartungsfenstern, Verwaltung von Applikationsbenutzern und automatisierten Benachrichtigungen per E-Mail.

---

## Funktionen

- **Anwendungsverwaltung**: Verwalten Sie die Liste Ihrer gehosteten Web-Applikationen und die zugeordneten Benutzer.
- **Wartungsplanung**: Planen und koordinieren Sie Wartungsfenster für jede Anwendung.
- **E-Mail-Benachrichtigungen**: Automatischer E-Mail-Versand basierend auf konfigurierbaren E-Mail-Templates (mit anpassbarer Vorwarnzeit und Statusverfolgung im H2/SQL-Backend).
- **Authentifizierung**: Wahlweise LDAP-Anbindung oder SAML2-Authentifizierung.

---

## Installation

**Voraussetzungen**:
- Java 21+
- LDAP-Server oder SAML2-IdP für die Authentifizierung
- SMTP-E-Mail-Server
- MS SQL Server 2022+ (optional, alternativer Betrieb mit dateibasierter H2-Datenbank möglich)

Die Anwendung wird als WAR-Datei ausgeliefert. Dabei handelt es sich um eine **Self-Contained Executable WAR**, welche das Spring Boot Backend und das Vue.js 3 Frontend vereint. Die Anwendung kann direkt mit dem folgendem Befehl gestartet werden:

```shell
java -jar maintenance-tool-*.war
```

---

## Schnellstart in die Entwicklung

Um die Anwendung lokal auszuführen oder zu testen, stellen Sie sicher, dass **Java 21** und **Node.js 20+** auf Ihrem System installiert sind.

### 1. Repository klonen und Abhängigkeiten installieren

Installieren Sie die Abhängigkeiten sowohl auf Root-Ebene als auch im Frontend-Unterverzeichnis:

```bash
# Frontend-Abhängigkeiten installieren
npm --prefix frontend install

# Entwicklungsabhängigkeiten auf Root-Ebene installieren
npm install
```

### 2. Entwicklungsumgebung starten (Backend + Frontend)

Führen Sie folgenden Befehl im **Root-Verzeichnis** des Projekts aus. Dieser startet gleichzeitig das Spring Boot Backend auf Port `8080` (inklusive In-Memory Test-LDAP und H2-Datenbank) sowie den Vite-Entwicklungsserver des Frontends auf Port `3000`:

```bash
npm run dev
```

Öffnen Sie anschließend [http://localhost:3000](http://localhost:3000) im Browser.

---

## Entwicklungs- und Hilfsskripte

Auf der Hauptebene stehen praktische npm-Skripte für den lokalen Entwicklungs-Workflow zur Verfügung:

| Befehl              | Beschreibung                                                            |
| :------------------ | :---------------------------------------------------------------------- |
| `npm run dev`       | Startet Backend und Frontend-Dev-Server parallel                        |
| `npm run test`      | Führt die vollständige Maven-Backend-Testsuite aus                      |
| `npm run format`    | Formatiert Java-Backend (Spotless) und Frontend-Code (Prettier)         |
| `npm run lint`      | Validiert Code-Formatierung und führt ESLint aus                        |
| `npm run build`     | Bereitet eine saubere Produktion-WAR vor                                |
| `npm run db:update` | Führt anstehende Liquibase-Datenbankmigrationen lokal aus               |
| `npm run clean`     | Entfernt alle generierten Maven-Build-Artefakte                         |

---

## Dokumentation

Die Dokumentation liegt versioniert im Repository vor:
- [**Entwicklungs-Handbuch:**](docs/dev/README.md) Detaillierte lokale Setup-Anleitungen, Zugangsdaten des Test-LDAPs und Komfort-Skripte.

---

## Lizenz

Dieses Projekt ist unter den Bedingungen der [LICENSE.md](LICENSE.md) lizenziert.
