import http from 'node:http';
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import path from 'node:path';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DATA_DIR = process.env.DATA_DIR_OVERRIDE || path.join(__dirname, 'data');
const DB_FILE = path.join(DATA_DIR, 'db.json');
const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '127.0.0.1';

const INTERVALLE = {
  taeglich: { label: 'Täglich', days: 1 },
  woechentlich: { label: 'Wöchentlich', days: 7 },
  zweiwoechentlich: { label: 'Alle 2 Wochen', days: 14 },
  monatlich: { label: 'Monatlich', days: 30 },
  quartal: { label: 'Vierteljährlich', days: 91 },
  halbjaehrlich: { label: 'Halbjährlich', days: 182 },
  jaehrlich: { label: 'Jährlich', days: 365 }
};

const RAEUME = ['Wohnzimmer', 'Küche', 'Bad', 'Schlafzimmer', 'Flur', 'Balkon', 'Abstellraum', 'Ganze Wohnung'];

const seedTasks = () => ([
  { titel: 'Staub wischen (alle Oberflächen)', raum: 'Ganze Wohnung', intervall: 'woechentlich', dauerMin: 30, hilfsmittel: ['Mikrofasertuch', 'Staubwedel'], anleitung: 'Oben nach unten arbeiten. Oberflächen freiräumen, abwischen, Möbel wieder zurückstellen.' },
  { titel: 'Boden saugen und wischen', raum: 'Ganze Wohnung', intervall: 'woechentlich', dauerMin: 45, hilfsmittel: ['Staubsauger', 'Wischmopp', 'Eimer', 'Reiniger'], anleitung: 'Erst saugen, dann feucht wischen. Von hinten nach vorne zur Tür arbeiten.' },
  { titel: 'Bad reinigen (WC, Waschbecken, Dusche)', raum: 'Bad', intervall: 'woechentlich', dauerMin: 25, hilfsmittel: ['Badreiniger', 'Schwamm', 'WC-Bürste', 'Tuch'], anleitung: 'Reiniger einwirken lassen, WC mit Bürste reinigen, Oberflächen abwischen, Spiegel polieren.' },
  { titel: 'Küche: Arbeitsflächen desinfizieren', raum: 'Küche', intervall: 'taeglich', dauerMin: 10, hilfsmittel: ['Allzweckreiniger', 'Tuch'], anleitung: 'Flächen feucht abwischen, Schneidebretter separat reinigen.' },
  { titel: 'Kühlschrank auswischen', raum: 'Küche', intervall: 'monatlich', dauerMin: 20, hilfsmittel: ['Schwamm', 'Essigreiniger'], anleitung: 'Abgelaufenes entsorgen, Fächer herausnehmen und auswischen, Gummidichtung säubern.' },
  { titel: 'Fenster putzen', raum: 'Ganze Wohnung', intervall: 'quartal', dauerMin: 60, hilfsmittel: ['Glasreiniger', 'Abzieher', 'Mikrofasertuch', 'Leiter'], anleitung: 'Glas einsprühen, mit Abzieher von oben nach unten abziehen, Rahmen mitwischen.' },
  { titel: 'Backofen reinigen', raum: 'Küche', intervall: 'quartal', dauerMin: 40, hilfsmittel: ['Backofenreiniger', 'Handschuhe', 'Schwamm'], anleitung: 'Reiniger auftragen, einwirken lassen (Herstellerangabe), ausreiben, Nachwischen.' },
  { titel: 'Kleiderschrank auswischen', raum: 'Schlafzimmer', intervall: 'halbjaehrlich', dauerMin: 45, hilfsmittel: ['Tuch', 'Holzpflege'], anleitung: 'Kleider ausräumen, Fächer feucht abwischen, trocknen lassen, wieder einräumen.' },
  { titel: 'Matratze wenden / lüften', raum: 'Schlafzimmer', intervall: 'monatlich', dauerMin: 15, hilfsmittel: ['Staubsauger-Aufsatz'], anleitung: 'Bettzeug abziehen, Matratze wenden und absaugen, lüften.' },
  { titel: 'Abflüsse reinigen', raum: 'Bad', intervall: 'monatlich', dauerMin: 15, hilfsmittel: ['Abflussreiniger', 'Spirale', 'Handschuhe'], anleitung: 'Sieb entfernen, Haare entfernen, Reiniger einfüllen, mit Spirale nachhelfen.' },
  { titel: 'Heizkörper entstauben', raum: 'Ganze Wohnung', intervall: 'halbjaehrlich', dauerMin: 30, hilfsmittel: ['Heizkörperbürste', 'Staubsauger'], anleitung: 'Bürste zwischen die Lamellen führen, von oben nach unten absaugen.' },
  { titel: 'Türgriffe & Lichtschalter desinfizieren', raum: 'Ganze Wohnung', intervall: 'woechentlich', dauerMin: 10, hilfsmittel: ['Desinfektionstuch'], anleitung: 'Alle Griffe und Schalter feucht abwischen.' },
  { titel: 'Spülmaschine entkalken', raum: 'Küche', intervall: 'quartal', dauerMin: 15, hilfsmittel: ['Entkalker', 'Reinigungsprogramm'], anleitung: 'Entkalker in den Besteckkorb, heißestes Programm leer laufen lassen.' },
  { titel: 'Vorhänge waschen', raum: 'Wohnzimmer', intervall: 'halbjaehrlich', dauerMin: 90, hilfsmittel: ['Waschmaschine', 'Waschmittel'], anleitung: 'Pflegesymbole prüfen, Schonprogramm, aufhängen ohne Trockner.' },
  { titel: 'Garagen-/Kellerboden fegen', raum: 'Abstellraum', intervall: 'monatlich', dauerMin: 20, hilfsmittel: ['Besen', 'Schaufel'], anleitung: 'Zusammenfegen und entsorgen.' }
]);

function defaultDb() {
  const now = new Date().toISOString();
  return {
    tasks: seedTasks().map((t, i) => ({
      id: 'seed-' + (i + 1),
      ...t,
      erstelltAm: now,
      erstelltAus: 'seed',
      stats: { erledigt: 0, uebersprungen: 0, letzteDauerMin: null, durchschnittDauerMin: null, letzterAbschluss: null },
      naechstesFaellig: null
    })),
    history: [],
    fotoErkenntnisse: []
  };
}

let db = null;

async function loadDb() {
  try {
    const raw = await readFile(DB_FILE, 'utf8');
    db = JSON.parse(raw);
  } catch {
    db = defaultDb();
    await saveDb();
  }
}

async function saveDb() {
  await mkdir(DATA_DIR, { recursive: true });
  await writeFile(DB_FILE, JSON.stringify(db, null, 2), 'utf8');
}

function recomputeFaelligkeit(task) {
  const base = task.stats.letzterAbschluss ? new Date(task.stats.letzterAbschluss) : new Date(task.erstelltAm);
  const days = INTERVALLE[task.intervall]?.days || 7;
  task.naechstesFaellig = new Date(base.getTime() + days * 86400000).toISOString();
}

function normalizeIntervall(task) {
  if (INTERVALLE[task.intervall]) return;
  const s = (task.stats.erledigt || 0);
  task.intervall = s >= 8 ? 'woechentlich' : s >= 3 ? 'zweiwoechentlich' : 'monatlich';
}

function lerneAusAbschluss(task, dauerMin) {
  const st = task.stats;
  st.erledigt += 1;
  st.letzterAbschluss = new Date().toISOString();
  if (dauerMin && dauerMin > 0) {
    st.letzteDauerMin = dauerMin;
    st.durchschnittDauerMin = st.durchschnittDauerMin
      ? Math.round((st.durchschnittDauerMin * (st.erledigt - 1) + dauerMin) / st.erledigt)
      : dauerMin;
    if (dauerMin > task.dauerMin * 1.5) task.dauerMin = Math.round(task.dauerMin * 1.1);
    if (dauerMin < task.dauerMin * 0.6 && task.dauerMin > 5) task.dauerMin = Math.max(5, Math.round(task.dauerMin * 0.9));
  }
  normalizeIntervall(task);
  recomputeFaelligkeit(task);
}

function planZeile(task, heute = new Date()) {
  const faellig = task.naechstesFaellig ? new Date(task.naechstesFaellig) : new Date(task.erstelltAm);
  const ueberfaellig = faellig <= heute;
  const tageBis = Math.ceil((faellig - heute) / 86400000);
  return {
    id: task.id, titel: task.titel, raum: task.raum, intervall: task.intervall,
    intervallLabel: INTERVALLE[task.intervall]?.label || task.intervall,
    dauerMin: task.dauerMin, hilfsmittel: task.hilfsmittel, anleitung: task.anleitung,
    naechstesFaellig: task.naechstesFaellig, ueberfaellig, tageBis,
    stats: task.stats
  };
}

function buildPlan({ raum, heute }) {
  const h = heute ? new Date(heute) : new Date();
  let tasks = db.tasks.slice();
  if (raum && raum !== 'alle') {
    tasks = tasks.filter(t => t.raum === raum || t.raum === 'Ganze Wohnung');
  }
  const zeilen = tasks.map(t => planZeile(t, h)).sort((a, b) => {
    if (a.ueberfaellig !== b.ueberfaellig) return a.ueberfaellig ? -1 : 1;
    return a.tageBis - b.tageBis;
  });
  return {
    datum: h.toISOString(),
    gesamtMinuten: zeilen.filter(z => z.ueberfaellig).reduce((s, z) => s + z.dauerMin, 0),
    zeilen
  };
}

const routes = [];
function route(method, pattern, handler) {
  const keys = [];
  const rx = new RegExp('^' + pattern.replace(/:[^/]+/g, m => { keys.push(m.slice(1)); return '([^/]+)'; }) + '$');
  routes.push({ method, rx, keys, handler });
}

route('GET', '/api/plan', (req, res, params, query) => {
  res.end(JSON.stringify(buildPlan({ raum: query.get('raum'), heute: query.get('heute') })));
});

route('GET', '/api/raeume', (req, res) => {
  res.end(JSON.stringify({ raeume: RAEUME, intervalle: INTERVALLE }));
});

route('GET', '/api/tasks/:id', (req, res, params) => {
  const t = db.tasks.find(x => x.id === params.id);
  if (!t) { res.statusCode = 404; return res.end(JSON.stringify({ error: 'nicht gefunden' })); }
  res.end(JSON.stringify({ ...t, zeile: planZeile(t) }));
});

route('GET', '/api/anleitung/:id', (req, res, params) => {
  const t = db.tasks.find(x => x.id === params.id);
  if (!t) { res.statusCode = 404; return res.end(JSON.stringify({ error: 'nicht gefunden' })); }
  res.end(JSON.stringify({
    titel: t.titel, raum: t.raum, dauerMin: t.dauerMin,
    hilfsmittel: t.hilfsmittel, anleitung: t.anleitung,
    intervallLabel: INTERVALLE[t.intervall]?.label
  }));
});

route('POST', '/api/tasks/:id/erledigt', async (req, res, params, query, body) => {
  const t = db.tasks.find(x => x.id === params.id);
  if (!t) { res.statusCode = 404; return res.end(JSON.stringify({ error: 'nicht gefunden' })); }
  const dauerMin = body?.dauerMin ? Math.max(1, Math.round(Number(body.dauerMin))) : null;
  lerneAusAbschluss(t, dauerMin);
  db.history.push({ taskId: t.id, titel: t.titel, raum: t.raum, aktion: 'erledigt', dauerMin, zeitpunkt: new Date().toISOString() });
  await saveDb();
  res.end(JSON.stringify({ ok: true, task: { ...t, zeile: planZeile(t) } }));
});

route('POST', '/api/tasks/:id/ueberspringen', async (req, res, params) => {
  const t = db.tasks.find(x => x.id === params.id);
  if (!t) { res.statusCode = 404; return res.end(JSON.stringify({ error: 'nicht gefunden' })); }
  t.stats.uebersprungen += 1;
  if (t.stats.uebersprungen >= 3 && t.intervall !== 'jaehrlich') {
    const order = Object.keys(INTERVALLE);
    const idx = order.indexOf(t.intervall);
    if (idx < order.length - 1) {
      t.intervall = order[idx + 1];
      t.stats.uebersprungen = 0;
    }
  }
  recomputeFaelligkeit(t);
  db.history.push({ taskId: t.id, titel: t.titel, raum: t.raum, aktion: 'uebersprungen', zeitpunkt: new Date().toISOString() });
  await saveDb();
  res.end(JSON.stringify({ ok: true, task: { ...t, zeile: planZeile(t) } }));
});

route('POST', '/api/tasks', async (req, res, params, query, body) => {
  const b = body || {};
  if (!b.titel || !INTERVALLE[b.intervall]) { res.statusCode = 400; return res.end(JSON.stringify({ error: 'titel und gueltiges intervall noetig' })); }
  const t = {
    id: 't-' + crypto.randomUUID(),
    titel: String(b.titel), raum: RAEUME.includes(b.raum) ? b.raum : 'Ganze Wohnung',
    intervall: b.intervall, dauerMin: Math.max(1, Math.round(Number(b.dauerMin) || 15)),
    hilfsmittel: Array.isArray(b.hilfsmittel) ? b.hilfsmittel.map(String) : [],
    anleitung: b.anleitung ? String(b.anleitung) : '',
    erstelltAm: new Date().toISOString(), erstelltAus: b.erstelltAus || 'manuell',
    stats: { erledigt: 0, uebersprungen: 0, letzteDauerMin: null, durchschnittDauerMin: null, letzterAbschluss: null },
    naechstesFaellig: null
  };
  recomputeFaelligkeit(t);
  db.tasks.push(t);
  await saveDb();
  res.end(JSON.stringify({ ok: true, task: { ...t, zeile: planZeile(t) } }));
});

route('DELETE', '/api/tasks/:id', async (req, res, params) => {
  const i = db.tasks.findIndex(x => x.id === params.id);
  if (i < 0) { res.statusCode = 404; return res.end(JSON.stringify({ error: 'nicht gefunden' })); }
  db.tasks.splice(i, 1);
  await saveDb();
  res.end(JSON.stringify({ ok: true }));
});

route('GET', '/api/history', (req, res, params, query) => {
  const limit = Math.min(200, Number(query.get('limit')) || 50);
  res.end(JSON.stringify({ history: db.history.slice(-limit).reverse() }));
});

route('POST', '/api/foto', async (req, res, params, query, body) => {
  const b = body || {};
  const raum = RAEUME.includes(b.raum) ? b.raum : 'Ganze Wohnung';
  const erkenntnisse = db.fotoErkenntnisse.filter(f => f.raum === raum).slice(-20);
  res.end(JSON.stringify({ ok: true, raum, bisherigeErkenntnisse: erkenntnisse.length }));
});

route('GET', '/api/stats', (req, res) => {
  const erledigt = db.history.filter(h => h.aktion === 'erledigt');
  const proRaum = {};
  for (const h of erledigt) proRaum[h.raum] = (proRaum[h.raum] || 0) + 1;
  res.end(JSON.stringify({
    gesamtErledigt: erledigt.length,
    proRaum,
    tasks: db.tasks.length,
    fotoErkenntnisse: db.fotoErkenntnisse.length,
    letzteAktionen: db.history.slice(-10).reverse()
  }));
});

const MISTRAL_API_KEY = process.env.MISTRAL_API_KEY || '';
const MISTRAL_VISION_MODEL = process.env.MISTRAL_VISION_MODEL || 'pixtral-large-latest';
const MISTRAL_API_URL = 'https://api.mistral.ai/v1/chat/completions';

const FOTO_PROMPT = `Du bist ein Experte für Wohnungsreinigung. Analysiere das Foto eines Raums (Kontext: Raum "{RAUM}").
Erkenne sichtbare Objekte, Oberflächen und Verschmutzungen (Staub, Flecken, Kalk, Fett, Unordnung).
Leite daraus konkrete Reinigungsaufgaben ab. Antworte AUSSCHLIESSLICH als JSON-Objekt mit diesem Schema:
{
  "raum": "<erkannter Raum>",
  "beobachtungen": ["..."],
  "aufgaben": [
    {
      "titel": "kurzer Aufgabentitel",
      "bereich": "z.B. Boden, Fenster, Arbeitsfläche",
      "dauerMin": 15,
      "hilfsmittel": ["..."],
      "anleitung": "Schritt-für-Schritt",
      "intervall": "taeglich|woechentlich|zweiwoechentlich|monatlich|quartal|halbjaehrlich|jaehrlich",
      "prioritaet": 0.0
    }
  ]
}
prioritaet: 0-1, wie dringend/auffällig die Aufgabe auf dem Foto wirkt. dauerMin realistisch schätzen.`;

async function mistralFotoAnalyse(raum, imageDataUrl) {
  const res = await fetch(MISTRAL_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + MISTRAL_API_KEY
    },
    body: JSON.stringify({
      model: MISTRAL_VISION_MODEL,
      messages: [{
        role: 'user',
        content: [
          { type: 'text', text: FOTO_PROMPT.replace('{RAUM}', raum) },
          { type: 'image_url', image_url: { url: imageDataUrl } }
        ]
      }],
      response_format: { type: 'json_object' },
      temperature: 0.2
    }),
    signal: AbortSignal.timeout(60000)
  });
  if (!res.ok) {
    const errText = await res.text().catch(() => '');
    throw new Error('Mistral API Fehler (HTTP ' + res.status + '): ' + errText.slice(0, 200));
  }
  const data = await res.json();
  const text = data.choices?.[0]?.message?.content || '{}';
  const parsed = JSON.parse(text);
  if (!Array.isArray(parsed.aufgaben)) parsed.aufgaben = [];
  return parsed;
}

route('POST', '/api/foto-analyse', async (req, res, params, query, body) => {
  const b = body || {};
  const raum = RAEUME.includes(b.raum) ? b.raum : 'Ganze Wohnung';
  const imageDataUrl = typeof b.imageDataUrl === 'string' && b.imageDataUrl.startsWith('data:image/')
    ? b.imageDataUrl : null;
  if (!imageDataUrl) {
    res.statusCode = 400;
    return res.end(JSON.stringify({ ok: false, error: 'imageDataUrl (data:image/...) fehlt' }));
  }
  if (!MISTRAL_API_KEY) {
    res.statusCode = 503;
    return res.end(JSON.stringify({ ok: false, error: 'MISTRAL_API_KEY nicht gesetzt – lokale Heuristik im Frontend nutzen oder Key konfigurieren.', fallback: 'heuristik' }));
  }
  try {
    const analyse = await mistralFotoAnalyse(raum, imageDataUrl);
    const eintrag = {
      id: 'f-' + crypto.randomUUID(),
      raum,
      model: MISTRAL_VISION_MODEL,
      zeitpunkt: new Date().toISOString(),
      analyse
    };
    db.fotoErkenntnisse.push(eintrag);
    await saveDb();
    res.end(JSON.stringify({ ok: true, ...eintrag }));
  } catch (e) {
    res.statusCode = 502;
    res.end(JSON.stringify({ ok: false, error: String(e.message || e), fallback: 'heuristik' }));
  }
});

route('GET', '/api/foto-analyse/status', (req, res) => {
  res.end(JSON.stringify({ mistralVerbunden: Boolean(MISTRAL_API_KEY), model: MISTRAL_VISION_MODEL }));
});

const MIME = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8', '.json': 'application/manifest+json', '.png': 'image/png', '.svg': 'image/svg+xml', '.webp': 'image/webp', '.webmanifest': 'application/manifest+json' };

async function serveStatic(req, res, pathname) {
  let rel = pathname === '/' ? '/index.html' : pathname;
  const file = path.join(__dirname, 'public', path.normalize(rel).replace(/^(\.\.[/\\])+/, ''));
  try {
    const data = await readFile(file);
    res.setHeader('Content-Type', MIME[path.extname(file)] || 'application/octet-stream');
    res.end(data);
  } catch {
    res.statusCode = 404;
    res.end('not found');
  }
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost');
  const pathname = url.pathname;
  res.setHeader('Content-Type', 'application/json; charset=utf-8');

  if (pathname.startsWith('/api/')) {
    let body = null;
    if (req.method === 'POST' || req.method === 'PUT') {
      let raw = '';
      for await (const chunk of req) raw += chunk;
      try { body = raw ? JSON.parse(raw) : {}; } catch { res.statusCode = 400; return res.end(JSON.stringify({ error: 'ungueltiges JSON' })); }
    }
    for (const r of routes) {
      if (r.method !== req.method) continue;
      const m = r.rx.exec(pathname);
      if (m) {
        const params = {};
        r.keys.forEach((k, i) => params[k] = decodeURIComponent(m[i + 1]));
        try { return await r.handler(req, res, params, url.searchParams, body); }
        catch (e) { res.statusCode = 500; return res.end(JSON.stringify({ error: String(e.message || e) })); }
      }
    }
    res.statusCode = 404;
    return res.end(JSON.stringify({ error: 'unbekannte Route' }));
  }

  if (req.method !== 'GET') { res.statusCode = 405; return res.end('method not allowed'); }
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  await serveStatic(req, res, pathname);
});

await loadDb();
for (const t of db.tasks) { if (!t.naechstesFaellig) { normalizeIntervall(t); recomputeFaelligkeit(t); } }
await saveDb();

server.listen(PORT, HOST, () => {
  if (HOST === '0.0.0.0') {
    console.log(`Putzplan laeuft auf http://<server-ip>:${PORT} (alle Schnittstellen, z. B. fuer Handy im Heimnetz)`);
  } else {
    console.log(`Putzplan laeuft auf http://${HOST}:${PORT}`);
  }
});
