# Putzplan (IT-gestützt, lernend)

Kleine Web-App ohne externe Abhängigkeiten (Node.js ≥ 18). Läuft auch als installierbare App (PWA) auf Android – siehe [ANDROID.md](ANDROID.md).

## Start

```bash
# optional: Mistral-Vision für echte Objekterkennung
MISTRAL_API_KEY=... MISTRAL_VISION_MODEL=pixtral-large-latest node server.js
# ohne Key: npm start  (Foto-Tab nutzt dann die lokale Heuristik)
# Browser: http://127.0.0.1:3000
```

## Funktionen

- **Wochenplan** (`GET /api/plan`): alle fälligen/überfälligen Aufgaben, filterbar nach Raum (`?raum=Bad`) oder ganze Wohnung. Jede Aufgabe kann abgehakt werden.
- **Anleitungen auf Wunsch** (`GET /api/anleitung/:id`, Button „📖 Anleitung"): Schritt-für-Schritt-Anweisung mit Hilfsmitteln und Zeitschätzung pro Aufgabe – quer durch die Wohnung oder zimmerweise.
- **Foto-Analyse mit Mistral (Vision)**: Tab „Foto-Analyse" – das Foto wird als Base64-Data-URL an die Mistral-API (`POST /v1/chat/completions`, Modell z. B. `pixtral-large-latest`, einstellbar via `MISTRAL_VISION_MODEL`) gesendet. Das Modell erkennt Objekte, Oberflächen und Verschmutzungen, antwortet als JSON mit Beobachtungen und Aufgaben inkl. Zeitschätzung, Hilfsmitteln, Anleitung, Intervall und Priorität. Vorschläge können per Klick in den Plan übernommen werden. Ohne `MISTRAL_API_KEY` greift automatisch ein lokaler Heuristik-Fallback im Browser (Helligkeit, Kontrast, Kanten-, Dunkelanteil, Farbton).
- **Intervalle**: täglich, wöchentlich, alle 2 Wochen, monatlich, vierteljährlich, halbjährlich, jährlich. Fälligkeit wird aus dem letzten Abschluss + Intervall berechnet.
- **Lernen/Verlauf**: Jeder Abschluss wird protokolliert (`/api/history`), die tatsächliche Dauer fließt in die Zeitschätzung ein (gleitender Durchschnitt, Anpassung der Schätzung). Häufig erledigte Aufgaben bleiben häufig (Intervall bleibt klein); Aufgaben, die 3× übersprungen werden, rutschen automatisch in ein selteneres Intervall.

## API

| Methode | Pfad | Beschreibung |
|---|---|---|
| GET | `/api/plan?raum=` | Plan mit Fälligkeit |
| GET | `/api/raeume` | Räume + Intervalle |
| GET | `/api/tasks/:id` | Aufgabe inkl. Stats |
| GET | `/api/anleitung/:id` | Anleitung + Hilfsmittel |
| POST | `/api/tasks/:id/erledigt` | abhaken, Body: `{dauerMin}` |
| POST | `/api/tasks/:id/ueberspringen` | verschieben/Intervall erhöhen |
| POST | `/api/tasks` | neue Aufgabe |
| DELETE | `/api/tasks/:id` | löschen |
| GET | `/api/history` | Verlauf |
| GET | `/api/stats` | Statistik |
| POST | `/api/foto` | Foto-Metadaten melden |
| GET | `/api/foto-analyse/status` | prüft, ob `MISTRAL_API_KEY` gesetzt ist |
| POST | `/api/foto-analyse` | Bild (Base64-Data-URL) an Mistral Vision, JSON-Ergebnis mit Aufgaben |

Daten liegen in `data/db.json`.

## Test

```bash
npm test
```
