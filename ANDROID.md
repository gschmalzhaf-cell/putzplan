# Putzplan auf dem Android-Handy nutzen

Der Putzplan ist eine **Progressive Web App (PWA)**. Du kannst ihn direkt im Browser auf deinem Android-Gerät benutzen und – ganz ohne Play Store – als App auf dem Homescreen installieren. Die App funktioniert danach auch offline (zuletzt geladene Ansicht), für alle Daten-Änderungen braucht sie aber eine Verbindung zum Server.

## Variante 1: Server im Heimnetz (empfohlen)

Der Putzplan läuft als kleines Serverprogramm auf einem Rechner (Windows, Mac, Linux, Raspberry Pi) im selben WLAN wie dein Handy.

### 1. Server starten (am Rechner)

Der Server lauscht standardmäßig nur auf `127.0.0.1`. Damit das Handy ihn erreicht, starte ihn mit:

```bash
HOST=0.0.0.0 npm start
```

In der Konsole erscheint danach `Putzplan laeuft auf http://<server-ip>:3000 ...`.

### 2. Server-IP herausfinden

- **Linux/Mac**: `hostname -I` bzw. `ipconfig getifaddr en0`
- **Windows**: `ipconfig` → Zeile „IPv4-Adresse“ der WLAN-/Ethernet-Verbindung

### 3. Am Handy öffnen

Im Browser (Chrome, Firefox, …) die Adresse öffnen, z. B. `http://192.168.1.20:3000`.

### 4. Als App installieren (PWA)

1. Seite in **Chrome** öffnen.
2. Menü (⋮) → **„Zum Startbildschirm hinzufügen"** bzw. **„App installieren"**.
3. Der Putzplan erscheint jetzt wie eine native App mit eigenem Icon und öffnet sich ohne Browser-Leiste.

## Variante 2: Server direkt auf dem Handy (Termux)

Für die vollständige Nutzung ohne separaten Rechner läuft der Server direkt auf dem Android-Gerät:

1. **Termux** aus F-Droid installieren (<https://f-droid.org/packages/com.termux/>, die Play-Store-Version ist veraltet).
2. In Termux:

```bash
pkg update && pkg install nodejs git
git clone https://github.com/gschmalzhaf-cell/putzplan.git
cd putzplan
npm start
```

3. Danach im Handy-Browser `http://127.0.0.1:3000` öffnen und wie oben als App installieren.
4. Termux sollte über **Acquire Wake Lock** (Benachrichtigung von Termux antippen) aktiv gehalten werden, sonst beendet Android den Server im Hintergrund.

Die Daten (`data/db.json`) liegen dann nur auf dem Handy.

## Wichtige Hinweise

- **Kein Internet-Zugriff nötig**: Für Variante 1 müssen Rechner und Handy nur im selben WLAN sein. Der Server ist nicht aus dem Internet erreichbar.
- **Foto-Analyse**: Mit `capture="environment"` öffnet der Foto-Upload auf dem Handy direkt die Kamera. Die eigentliche Analyse läuft auf dem Server (Mistral Vision, sofern `MISTRAL_API_KEY` gesetzt) bzw. lokal im Browser als Heuristik-Fallback.
- **Offline-Modus**: Ein Service Worker (`public/sw.js`) cached die App-Oberfläche. Ist der Server (oder das WLAN) nicht erreichbar, lässt sich die Oberfläche noch laden; Aufgaben abhaken und API-Aufrufe erfordern aber eine Verbindung.
- **Sicherheit**: Öffne `HOST=0.0.0.0` nur im eigenen Heimnetz, nicht in öffentlichen WLANs, und richte ggf. keine Portweiterleitung im Router ein.

## Übersicht der relevanten Umgebungsvariablen

| Variable | Default | Bedeutung |
|---|---|---|
| `HOST` | `127.0.0.1` | Netzwerk-Adresse, auf der der Server lauscht. `0.0.0.0` = alle Schnittstellen (Zugriff aus dem Heimnetz) |
| `PORT` | `3000` | HTTP-Port des Servers |

Alle weiteren Variablen (`MISTRAL_API_KEY`, `MISTRAL_VISION_MODEL`, `DATA_DIR_OVERRIDE`) siehe [INSTALLATION.md](INSTALLATION.md).

## Variante 3: Native Android-App (Kotlin)

Für eine echte Android-App ohne Server gibt es unter [`android/`](android/) eine komplett eigenständige Kotlin-App:

- Alle Daten lokal auf dem Gerät (Room-Datenbank), kein Node-Server und kein WLAN-Rechner nötig
- Foto-Analyse direkt aus der App über die Mistral-API (API-Key in den App-Einstellungen)
- Lern-Logik identisch zur Web-App (gleitende Zeitschätzung, Intervall-Anpassung)

Details: [android/README.md](android/README.md)
