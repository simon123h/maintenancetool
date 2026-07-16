# Schulungsplanung

[![Build Status](https://github.com/simon123h/schulungsplanung/actions/workflows/ci.yml/badge.svg)](https://github.com/simon123h/schulungsplanung/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/simon123h/schulungsplanung/graph/badge.svg?token=863JJN33QK)](https://codecov.io/gh/simon123h/schulungsplanung)

Eine einfache, moderne Webanwendung zur Planung, Buchung und Verwaltung von Schulungsterminen.

---

## Funktionen

- **Schulungs- & Vorlagenverwaltung**: Komfortable Erstellung wiederverwendbarer Schulungsvorlagen sowie die Terminierung, Organisation und Überwachung konkreter Schulungstermine.
- **Teilnehmer- & Buchungsmanagement**: Selbstregistrierung für Teilnehmer, Registrierung von Dritten durch berechtigte Accounts sowie strukturierte Statusverfolgung und Teilnahmeerfassung.
- **Kryptografisch signierte PDF-Zertifikate**: Einfache, sichere digitale Signierung der Zertifikats-PDFs und automatische Bereitstellung der Zertifikate an alle Teilnehmenden einer Schulung
- **Automatisierter E-Mail-Versand**: Vollautomatische Benachrichtigungen bei Buchungsbestätigungen, Stornierungen sowie bei der Zertifikatsbereitstellung inklusive direkter Download-Links für die Teilnehmenden.
- **Praktikables Admin-Dashboard**:
  - Zentrale Übersicht über den Status aller Schulungen
  - Interaktive Teilnehmerverwaltung pro Schulung mit direktem Status-Toggle und Möglichkeit zur Zertifikatsgenerierung.
- **Authentifizierung**: wahlweise LDAP-Anbindung oder SAML2-Authentifizierung

---

## Installation

**Voraussetzungen**:

Der Betrieb der Anwendung hat die folgenden Anforderungen:

- Java 21+
- LDAP-Server oder SAML2-IdP für die Authentifizierung
- SMTP-E-Mail-Server
- MS SQL Server 2022+ (optional, alternativer Betrieb mit dateibasierter H2-Datenbank möglich)

Die Anwendung wird als WAR-Datei ausgeliefert. Dabei handlet es sich um eine **Self-Contained Executive WAR**, welche das Spring Boot 4 Backend und das Vue.js 3 Frontend vereint. Die Anwendung kann in einem Webserver (z.B. Apache Tomcat 10+) gehostet oder auch direkt mit dem folgendem Befehl gestartet werden:

```shell
java -jar schulungsplanung-*.war
```

Weitere Details sind in der [Betriebsanleitung](docs/hosting/README.md) dokumentiert.

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

Führen Sie folgenden Befehl auf im **Root-Verzeichnis** des Projekts aus. Dieser startet gleichzeitig das Spring Boot Backend auf Port `8080` (inklusive In-Memory Test-LDAP und H2-Datenbank) sowie den Vite-Entwicklungsserver des Frontends auf Port `3000`:

```bash
npm run dev
```

Öffnen Sie anschließend [http://localhost:3000](http://localhost:3000) im Browser.

---

## Entwicklungs- und Hilfsskripte

Auf der Hauptebene stehen praktische npm-Skripte für den lokalen Entwicklungs-Workflow zur Verfügung:

| Befehl              | Beschreibung                                                            |
| :------------------ | :---------------------------------------------------------------------- |
| `npm run dev`       | Startet Backend, Frontend-Dev-Server und Hilfe-Vorschau parallel        |
| `npm run test`      | Führt die vollständige Maven-Backend-Testsuite aus                      |
| `npm run format`    | Formatiert Java-Backend (Spotless) und Frontend-Code (Prettier)         |
| `npm run lint`      | Validiert Code-Formatierung und führt ESLint statische Code-Analyse aus |
| `npm run build`     | Bereitet eine saubere Produktion-WAR inklusive Frontend-Assets vor      |
| `npm run db:update` | Führt anstehende Liquibase-Datenbankmigrationen lokal aus               |
| `npm run clean`     | Entfernt alle generierten Maven-Build-Artefakte                         |

---

## Dokumentation

Die gesamte Dokumentation liegt versioniert im Repository vor und kann direkt eingesehen werden:

- [**Anforderungen & User Stories:**](docs/requirements/README.md)  
  Fachliches Konzept nach dem **req42-Standard**.
- [**Systemarchitektur:**](docs/architecture/README.md)  
  Technische Architektur nach dem **arc42-Template** (inkl. ER- und Bausteinsicht).
- [**Betrieb & Deployment:**](docs/hosting/README.md)  
  Produktionskonfigurationen für MS SQL Server, SMTP-E-Mail, SAML2/LDAP SSO und systemd Service-Vorgaben.
- [**Entwicklungs-Handbuch:**](docs/dev/README.md)  
  Detaillierte lokale Setup-Anleitungen, Zugangsdaten des Test-LDAPs und Komfort-Skripte.

---

## Lizenz

Dieses Projekt ist unter den Bedingungen der [LICENSE.md](LICENSE.md) lizenziert. Bitte lesen Sie das Lizenzdokument für nähere Informationen.
