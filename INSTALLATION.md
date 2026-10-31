# Installationsanleitung

Der Putzplan ist eine kleine Web-App **ohne externe npm-Abhängigkeiten** – erforderlich ist nur Node.js ≥ 18.

## Voraussetzungen

- [Node.js](https://nodejs.org/) ≥ 18 (enthält `npm`)
- Optional: ein Mistral-API-Key für die Foto-Analyse mit Vision-Modell (ohne Key nutzt die Foto-Analyse automatisch die lokale Heuristik im Browser)

Installation prüfen:

```bash
node --version   # sollte v18.x oder höher zeigen
npm --version
```

## 1. Projekt herunterladen

```bash
git clone https://github.com/gschmalzhaf-cell/putzplan.git
cd putzplan
```

## 2. Starten

Es ist kein `npm install` nötig, da keine Abhängigkeiten bestehen.

```bash
npm start
```

Der Server läuft danach unter <http://127.0.0.1:3000>.

### eigener Port

```bash
PORT=8080 npm start
```

## 3. Foto-Analyse mit Mistral Vision (optional)

Damit die Tab „Foto-Analyse" echte Objekterkennung per Mistral-API nutzt, beim Start zwei Umgebungsvariablen setzen:

```bash
MISTRAL_API_KEY=<dein-key> MISTRAL_VISION_MODEL=pixtral-large-latest npm start
```

- `MISTRAL_API_KEY`: API-Key von Mistral (erhältlich unter <https://console.mistral.ai/>)
- `MISTRAL_VISION_MODEL`: Vision-Modell, z. B. `pixtral-large-latest`

Ob der Key aktiv ist, lässt sich im Browser unter `/api/foto-analyse/status` prüfen (liefert `{"aktiv": true, ...}`).

Ohne `MISTRAL_API_KEY` läuft die App normal; die Foto-Analyse greift dann auf die lokale Heuristik zurück (Helligkeit, Kontrast, Kanten, Dunkelanteil, Farbton).

**Hinweis:** Den API-Key niemals in die Shell-Historie oder in Dateien committen, die ins Repository gelangen. Alternativ in eine nicht eingecheckte Datei auslagern:

```bash
MISTRAL_API_KEY=$(cat ~/.mistral_key) npm start
```

## 4. Daten & Datenverzeichnis

Alle Daten (Aufgaben, Verlauf, Statistik) werden in `data/db.json` gespeichert. Die Datei wird beim ersten Start automatisch mit Beispielaufgaben angelegt.

Eigener Ablageort über Umgebungsvariable:

```bash
DATA_DIR_OVERRIDE=/var/lib/putzplan npm start
```

Für ein frisches Setup genügt es, `data/db.json` zu löschen – beim nächsten Start werden die Standardaufgaben neu erzeugt.

## 5. Tests

```bash
npm test
```

## 6. Produktivbetrieb (Beispiel mit systemd)

`/etc/systemd/system/putzplan.service`:

```ini
[Unit]
Description=Putzplan
After=network.target

[Service]
WorkingDirectory=/opt/putzplan
Environment=PORT=3000
# Environment=MISTRAL_API_KEY=<key>
ExecStart=/usr/bin/node server.js
Restart=on-failure
User=putzplan

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now putzplan
```

## Übersicht der Umgebungsvariablen

| Variable | Default | Bedeutung |
|---|---|---|
| `PORT` | `3000` | HTTP-Port des Servers |
| `MISTRAL_API_KEY` | – | aktiviert Foto-Analyse über Mistral Vision |
| `MISTRAL_VISION_MODEL` | – | verwendetes Vision-Modell (z. B. `pixtral-large-latest`) |
| `DATA_DIR_OVERRIDE` | `./data` | Ablageort für `db.json` |

## Problembehandlung

- **Port bereits belegt** (`EADDRINUSE`): anderen Port mit `PORT=...` wählen oder den belegenden Prozess beenden.
- **Foto-Analyse meldet Fehler**: Key/Modell prüfen (`/api/foto-analyse/status`), Internetverbindung sicherstellen.
- **Daten weg**: `data/db.json` wurde gelöscht oder `DATA_DIR_OVERRIDE` zeigt auf ein anderes Verzeichnis.
