# Putzplan – Native Android-App (Kotlin)

Eigenständige Android-App für den Putzplan – **ohne Node-Server**, komplett offline-fähig. Alle Daten liegen lokal in einer Room-Datenbank auf dem Gerät.

## Features

- **Wochenplan**: alle fälligen/überfälligen Aufgaben, filterbar nach Raum, abhaken mit Dauer-Eingabe
- **Lern-Logik** (identisch zur Web-App portiert):
  - gleitender Durchschnitt der tatsächlichen Dauer
  - überschätzte/unterschätzte Aufgaben passen ihre Schätzung an (×1.1 / ×0.9)
  - 3× übersprungen → automatisch selteneres Intervall
- **Foto-Analyse mit Mistral Vision**: Foto aufnehmen → direkt aus der App an die Mistral-API → Aufgaben mit Priorität, Dauer, Hilfsmitteln → per Klick in den Plan übernehmen. API-Key wird in den App-Einstellungen hinterlegt (lokal gespeichert).
- **Verlauf & Statistik**: erledigte Aufgaben pro Raum, letzte Aktionen
- 15 Seed-Aufgaben beim ersten Start (identisch zur Web-App)

## Voraussetzungen

- Android Studio (Koala oder neuer empfohlen)
- Android SDK 34, Java 17

## Bauen & Installieren

```bash
cd android
./gradlew assembleDebug       # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # direkt auf verbundenem Gerät
```

Oder in Android Studio: Ordner `android/` als Projekt öffnen → Run ▶.

Für die CLI brauchst du einmalig einen Gradle-Wrapper (Android Studio generiert ihn beim ersten Sync automatisch anhand von `gradle/wrapper/gradle-wrapper.properties`), alternativ: `gradle wrapper --gradle-version 8.7`.

## Architektur

```
com.putzplan.app
├── data/        Room: Entities, DAO, Database, Repository (Plan-Berechnung)
├── logic/       Konstanten (Räume, Intervalle), PlanLogik (Seed, Lernen, Fälligkeit)
├── net/         MistralVisionClient (Foto-Analyse, kotlinx.serialization)
└── ui/          Compose UI: PlanTab, FotoTab, VerlaufTab, ViewModel, Theme
```

Min SDK 26 (Android 8.0), Target SDK 34.

## Hinweise

- Der API-Key bleibt ausschließlich lokal auf dem Gerät (SharedPreferences).
- Vor der ersten Foto-Analyse in den Einstellungen (Zahnrad) den Mistral-API-Key eintragen.
