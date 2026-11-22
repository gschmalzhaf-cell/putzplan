# Gideon's Putzplan

Native, selbstlernende Android-App für deinen Haushalts- und Putzplan – **komplett offline-fähig, ohne Server und ohne Account**. Alle Daten liegen lokal in einer Room-Datenbank auf deinem Gerät.

[![Releases](https://img.shields.io/github/v/release/gschmalzhaf-cell/putzplan?label=Release&color=4E9B72)](https://github.com/gschmalzhaf-cell/putzplan/releases)
[![CI](https://github.com/gschmalzhaf-cell/putzplan/actions/workflows/ci.yml/badge.svg)](https://github.com/gschmalzhaf-cell/putzplan/actions/workflows/ci.yml)

---

## Inhaltsverzeichnis

- [Highlights & Features](#highlights--features)
- [Installation (APK)](#installation-apk)
- [Architektur](#architektur)
- [Entwicklung & Build](#entwicklung--build)
- [Release & Changelog](#release--changelog)
- [Roadmap & Vorschläge für weitere Features & Optimierungen](#roadmap--vorschläge-für-weitere-features--optimierungen)

---

## Highlights & Features

### 📋 Wochenplan & Alltag
- **Dynamische Priorisierung**: Überfällige Aufgaben werden relativ zu ihrer Intervalllänge gewichtet (3 Tage Verzug bei einer täglichen Aufgabe wiegen schwerer als bei einer jährlichen).
- **Wochenfortschritt & Tagesgruß**: Scrollbarer Hero-Bereich mit tageszeitabhängiger Begrüßung und live berechneter Wochenfortschrittsanzeige (erledigte vs. offene Aufgaben & verbleibende Minuten).
- **Stoppuhr mit Pause & Fortsetzen**: Direkte Zeitmessung pro Aufgabe – beim Stoppen wird die reale Dauer direkt ins Lernsystem übernommen.
- **Vollständige Aufgaben- & Raumverwaltung**: Aufgaben anlegen, bearbeiten, überspringen oder löschen; Räume mit eigenen Pastell- und Dark-Mode-Farben umbenennen und filtern.
- **Später fällig im Blick**: Aufklappbarer Bereich für bereits erledigte und kommende Aufgaben.

### 🧠 Intelligenz & Lern-Logik
- **Adaptive Intervalle**: Analysiert die realen Abstände (Median) zwischen deinen Abschlüssen und schlägt automatisch passendere Intervalle vor (mit *Übernehmen* / *Behalten*).
- **Gewichtetes Dauer-Lernen**: Passt Zeitschätzungen per gewichtetem gleitendem Durchschnitt an dein tatsächliches Tempo an; 3× übersprungene Aufgaben werden automatisch seltener eingeplant.
- **Optimaler Zeitbudget-Modus (`5 / 10 / 20 / 30 Min.`)**: Berechnet per 0/1-Knapsack-Optimierung die sinnvollste Kombination offener Aufgaben für deine verfügbare Zeit – inkl. Ein-Tipp-Erledigen.
- **Smart-Runden (Synergie-Erkennung)**: Erkennt automatisch, wenn mehrere fällige Aufgaben dasselbe Hilfsmittel benötigen (z. B. *Staubsauger-Runde*) oder im selben Raum liegen (*Bad in einem Rutsch*).
- **Kontext-Modi**:
  - `👋 Besuch?` – zieht alle in den nächsten 2 Tagen fälligen Aufgaben nach oben.
  - `⚡ ≤ 10 Min.` – filtert auf schnelle „Quick-Wins“ für zwischendurch.
  - `🌱 Saison-Tipp` – schlägt jahreszeitlich passende Sonderaufgaben vor (Frühjahr bis Winter).
  - **Wochenend-Hinweis** – hebt am Samstag/Sonntag größere Aufgaben hervor.
- **Smart-Assistent beim Anlegen**: Offline-Wissensbasis erkennt gängige Aufgaben beim Tippen und schlägt Raum, Intervall, Dauer, Hilfsmittel und Anleitung vor. Bei hinterlegtem Mistral-API-Key steht zusätzlich **„Mit KI ausfüllen“** bereit.

### 📷 Foto-Analyse mit Mistral Vision
- **Kamera & Galerie (bis zu 8 Fotos)**: Direkt aus der App fotografieren oder mehrere Perspektiven eines Raums auswählen.
- **Automatische Aufgaben- & Raum-Erkennung**: Mistral Vision erkennt Objekte, Verschmutzungen sowie **Veränderungen zum letzten Scan** und generiert priorisierte Aufgaben inkl. Duplikat-Erkennung (*„Bereits im Plan“*) und *„Alle übernehmen“*.
- **Foto → Plan-Kopplung**: Erkennt die Analyse starken Schmutz, werden Aufgaben des Raums vorgezogen; wirkt der Raum sauber, werden fällige Aufgaben sanft nach hinten geschoben.
- **Datenschutz**: Der Mistral API-Key wird ausschließlich lokal in den `SharedPreferences` gespeichert.

### 📊 Verlauf, Gewohnheiten & Diagnose
- **Putz-Gewohnheiten**: Automatische Auswertung von **Wochen-Streak**, **stärkstem Putztag & Tageszeit** sowie insgesamt erfasster Putzzeit.
- **Erfolgs-Badges & Streak-Intelligenz**: Motivierende Meilensteine (*Erster Putz* bis *Putz-Legende*) inkl. Hinweis, wenn nur noch eine Aufgabe bis zum nächsten Badge fehlt.
- **Markdown-Diagnose-Report**: In den Einstellungen lässt sich ein strukturierter Report (ohne API-Key) für Fehleranalyse oder Feedback kopieren und teilen.

---

## Installation (APK)

Die jeweils aktuelle signierte APK findest du unter **[GitHub Releases](https://github.com/gschmalzhaf-cell/putzplan/releases)**:

1. Datei `putzplan-v<version>.apk` auf das Android-Gerät herunterladen.
2. APK öffnen und installieren (ggf. *„Installation aus unbekannten Quellen“* erlauben).
3. Die App startet bewusst leer – lege Räume und Aufgaben manuell (mit Smart-Assistent) oder per Foto-Analyse an.

---

## Architektur

Die App ist in **Kotlin** mit **Jetpack Compose (Material 3)**, **Room**, **Coroutines/Flow** und **kotlinx.serialization** umgesetzt:

```text
android/app/src/main/java/com/putzplan/app/
├── MainActivity.kt               Edge-to-Edge Host + Crash-Report-Dialog
├── data/
│   ├── Entities.kt               Room-Entities: Task, HistoryEntry, FotoErkenntnis
│   ├── PutzplanDao.kt            SQL-Queries & Stats-Projektionen
│   ├── PutzplanDatabase.kt       Room-DB (Version 2 mit Migration 1→2)
│   └── Repository.kt             Plan-Berechnung, Lern-Update, Foto→Plan-Kopplung, Report
├── logic/
│   ├── Konstanten.kt             Intervall-Definitionen (Täglich bis Jährlich)
│   ├── PlanLogik.kt              PlanLogik (Lernen) & PlanIntelligenz (Knapsack, Smart-Runden, Gewohnheiten)
│   ├── AufgabenAssistent.kt      Offline-Wissensbasis für Smart-Vorschläge beim Anlegen
│   └── SaisonLogik.kt            Jahreszeitliche Aufgaben-Vorschläge
├── net/
│   └── MistralVisionClient.kt    Mistral Vision (Foto-Analyse) & KI-Aufgaben-Ergänzung
└── ui/
    ├── PutzplanViewModel.kt      StateFlow-Zustandsverwaltung & Stoppuhr-State
    ├── PlanTab.kt                Wochenplan, Hero, Smart-Runden, Dialoge & Einstellungen
    ├── FotoTab.kt                Kamera/Galerie, Vision-Ergebnisse & Duplikat-Schutz
    ├── VerlaufTab.kt             Statistik, Gewohnheiten, Badges & Verlauf
    ├── Bausteine.kt              Raumbadge, FortschrittsBalken, Streak-Badges, StatKachel
    ├── Feder.kt                  Feder-Physik-Modifier für taktiles Feedback
    └── theme/                    Putzplan 2.0 Farb- & Design-System (Hell & Dunkel)
```

---

## Entwicklung & Build

### Voraussetzungen
- Aktuelles Android Studio
- **JDK 17** und **Android SDK 37** (Min-SDK 26 / Android 8.0)

### Lokal bauen
```bash
cd android
./gradlew assembleDebug       # Debug-APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug        # Direkt auf verbundenem Gerät installieren
```

### Signiertes Release bauen
Der Release wird mit dem Keystore unter `keystores/putzplan-release.jks` signiert; das Passwort wird über `ANDROID_KEYSTORE_PASSWORD` übergeben (in GitHub Actions als Secret hinterlegt):

```bash
cd android
ANDROID_KEYSTORE_PASSWORD=... ./gradlew assembleRelease
# Signierte APK: app/build/outputs/apk/release/app-release.apk
```

---

## Release & Changelog

Releases werden vollautomatisch über [`.github/workflows/release.yml`](.github/workflows/release.yml) gebaut, sobald ein Tag `vX.Y.Z` gepusht wird:

1. `versionCode` und `versionName` in `android/app/build.gradle.kts` erhöhen.
2. Änderungen auf `main` mergen und Tag setzen (`git tag vX.Y.Z && git push origin vX.Y.Z`).
3. Der Workflow baut die signierte APK, generiert über [`scripts/changelog.sh`](scripts/changelog.sh) ein gruppiertes Changelog (`feat` / `fix` / `chore` / `docs`) und veröffentlicht beides als GitHub Release.

Manuelles Nachpflegen eines Changelogs:
```bash
scripts/changelog.sh v2.6.0 > /tmp/changelog.md
gh release edit v2.6.0 --repo gschmalzhaf-cell/putzplan --notes-file /tmp/changelog.md
```

---

## 💡 Roadmap & Vorschläge für weitere Features & Optimierungen

Hier sind konkrete, priorisierte Ideen für die nächsten Ausbaustufen von **Gideon's Putzplan**:

### 🎯 Hoher Alltagsnutzen (Quick-Wins)
1. **Home-Screen-Widget (Jetpack Glance)**: Zeigt die 3 dringendsten Aufgaben + Wochenfortschritt direkt auf dem Startbildschirm – mit 1-Tipp-Abhaken ohne die App öffnen zu müssen.
2. **Smarte Benachrichtigungen (WorkManager)**: Lokale Erinnerungen genau zu deiner gelernten **Lieblings-Putzzeit** (z. B. *Samstag 10:00 Uhr*), statt zu starren Uhrzeiten – inkl. „Schlummern“-Aktion.
3. **Backup & Export/Import (JSON)**: Lokaler Export und Import aller Räume, Aufgaben und Verlaufsdaten über Android Storage Access Framework (SAF), ideal bei Gerätewechsel.
4. **Rückgängig-Snackbar (Undo)**: Nach dem Abhaken, Überspringen oder Löschen kurz eine Snackbar mit *„Rückgängig“* einblenden, um versehentliche Tipps sofort zu korrigieren.

### 🧠 Nächste Intelligenz-Stufe
5. **Putzmittel- & Vorrats-Check**: Automatische Übersicht aller benötigten Hilfsmittel der Woche mit Markierung „Nachkaufen“ (optional teilbar als Einkaufsliste).
6. **Urlaubs- & Krankheits-Modus (Plan pausieren)**: Schiebt alle Fälligkeiten um *N* Tage nach hinten, damit nach dem Urlaub keine rote Überfällig-Lawine entsteht.
7. **Wetter-Kontext (optional)**: Lokaler Wetter-Check (z. B. Open-Meteo, ohne API-Key) für Hinweise wie *„Heute trocken & bewölkt – ideal zum Fensterputzen“*.
8. **Spracheingabe für Aufgaben**: Per Mikrofon-Button Aufgaben diktieren (z. B. *„Alle 2 Wochen Bad spiegeln, 10 Minuten“*) und automatisch parsen.

### ⚙️ Technische & Qualitäts-Optimierungen
9. **Automatisierte Unit-Tests in CI**: JUnit-Tests für `PlanLogik`, `PlanIntelligenz` (Knapsack, Adaptive Intervalle, Gewohnheits-Analyse) und `AufgabenAssistent` direkt in `.github/workflows/ci.yml` ausführen.
10. **R8 / Code-Shrinking für Release-Builds**: `isMinifyEnabled = true` mit sauberen ProGuard-Regeln für `kotlinx.serialization` und Room aktivieren, um die APK-Größe deutlich zu reduzieren.
11. **Room-Schema-Export & Migrations-Tests**: `exportSchema = true` aktivieren, damit künftige Datenbank-Migrationen automatisiert getestet werden können.
12. **Vollauflösende Kamera-Aufnahmen (`TakePicture` + `FileProvider`)**: Statt `TakePicturePreview` (Thumbnail) hochauflösende Fotos über `FileProvider` an Mistral Vision übergeben, um feine Details wie Kalk oder Staub noch besser zu erkennen.
