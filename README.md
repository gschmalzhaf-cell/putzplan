# Putzplan – Android-App (Kotlin)

Native Android-App für den Putzplan – **ohne Server**, komplett offline-fähig. Alle Daten liegen lokal in einer Room-Datenbank auf dem Gerät.

## Features

- **Wochenplan**: alle fälligen/überfälligen Aufgaben, filterbar nach Raum, abhaken mit Dauer-Eingabe
- **Lern-Logik**:
  - gleitender Durchschnitt der tatsächlichen Dauer
  - überschätzte/unterschätzte Aufgaben passen ihre Schätzung an (×1.1 / ×0.9)
  - 3× übersprungen → automatisch selteneres Intervall
- **Foto-Analyse mit Mistral Vision**: Foto aufnehmen → direkt aus der App an die Mistral-API → Aufgaben mit Priorität, Dauer, Hilfsmitteln → per Klick in den Plan übernehmen. API-Key wird in den App-Einstellungen hinterlegt (lokal gespeichert).
- **Verlauf & Statistik**: erledigte Aufgaben pro Raum, letzte Aktionen
- 15 Seed-Aufgaben beim ersten Start

## Voraussetzungen

- Aktuelles Android Studio (neueste stabile Version empfohlen)
- Android SDK 37, Java 17

## Bauen & Installieren

```bash
./gradlew assembleDebug       # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug        # direkt auf verbundenem Gerät
```

Oder in Android Studio: Projekt öffnen → Run ▶.

## Release bauen (signiert)

Der Release wird mit dem Keystore in `keystores/putzplan-release.jks` signiert; das Passwort kommt aus der Umgebungsvariablen `ANDROID_KEYSTORE_PASSWORD` (in GitHub als Secret hinterlegt).

```bash
ANDROID_KEYSTORE_PASSWORD=... ./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

Releases werden automatisch gebaut, sobald ein Tag `v*` gepusht wird (siehe `.github/workflows/release.yml`) – die signierte APK wird an den GitHub-Release angehängt.

## Releases

Die aktuelle APK findest du unter [Releases](https://github.com/gschmalzhaf-cell/putzplan/releases) – Datei `putzplan-v<version>.apk` herunterladen, auf dem Handy öffnen und installieren („Installation aus unbekannten Quellen“ muss erlaubt sein).

## Architektur

```
com.putzplan.app
├── data/        Room: Entities, DAO, Database, Repository (Plan-Berechnung)
├── logic/       Konstanten (Räume, Intervalle), PlanLogik (Seed, Lernen, Fälligkeit)
├── net/         MistralVisionClient (Foto-Analyse, kotlinx.serialization)
└── ui/          Compose UI: PlanTab, FotoTab, VerlaufTab, ViewModel, Theme
```
