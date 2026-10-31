import { spawn } from 'node:child_process';
import { setTimeout as sleep } from 'node:timers/promises';
import { mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';

const PORT = 3987;
const BASE = `http://127.0.0.1:${PORT}`;
const tmp = mkdtempSync(path.join(tmpdir(), 'putzplan-test-'));

const server = spawn('node', ['server.js'], {
  cwd: path.resolve(new URL('..', import.meta.url).pathname),
  env: { ...process.env, PORT: String(PORT), DATA_DIR_OVERRIDE: tmp },
  stdio: 'pipe'
});

let failed = 0;
function check(name, cond) {
  console.log((cond ? 'PASS' : 'FAIL') + ' - ' + name);
  if (!cond) failed++;
}

async function api(p, opts = {}) {
  const res = await fetch(BASE + p, {
    method: opts.method || 'GET',
    headers: opts.body ? { 'Content-Type': 'application/json' } : undefined,
    body: opts.body ? JSON.stringify(opts.body) : undefined
  });
  return { status: res.status, body: await res.json() };
}

try {
  await sleep(800);

  let r = await api('/api/plan');
  check('Plan liefert Zeilen', r.status === 200 && r.body.zeilen.length >= 10);
  check('Plan hat gesamtMinuten', typeof r.body.gesamtMinuten === 'number');

  r = await api('/api/plan?raum=Bad');
  check('Raumfilter Bad enthaelt Bad + Ganze Wohnung', r.body.zeilen.every(z => z.raum === 'Bad' || z.raum === 'Ganze Wohnung'));

  const first = (await api('/api/plan')).body.zeilen[0];
  r = await api('/api/anleitung/' + first.id);
  check('Anleitung enthaelt Hilfsmittel + Text', Array.isArray(r.body.hilfsmittel) && typeof r.body.anleitung === 'string' && r.body.anleitung.length > 0);

  const vor = (await api('/api/tasks/' + first.id)).body;
  r = await api('/api/tasks/' + first.id + '/erledigt', { method: 'POST', body: { dauerMin: 33 } });
  const nach = r.body.task;
  check('Erledigen erhoeht Zaehler', nach.stats.erledigt === vor.stats.erledigt + 1);
  check('Erledigen lernt Dauer', nach.stats.durchschnittDauerMin === 33);
  check('Naechstes Faellig in der Zukunft', new Date(nach.naechstesFaellig) > new Date());

  const intervallVor = nach.intervall;
  for (let i = 0; i < 3; i++) await api('/api/tasks/' + first.id + '/ueberspringen', { method: 'POST', body: {} });
  r = await api('/api/tasks/' + first.id);
  check('Ueberspringen verschiebt Intervall nach hinten', r.body.intervall !== intervallVor);

  r = await api('/api/tasks', { method: 'POST', body: { titel: 'Testaufgabe', intervall: 'monatlich', dauerMin: 12, raum: 'Flur', hilfsmittel: ['Tuch'], anleitung: 'Abwischen.' } });
  check('Neue Aufgabe anlegbar', r.status === 200 && r.body.task.titel === 'Testaufgabe');
  const neueId = r.body.task.id;

  r = await api('/api/tasks/' + neueId, { method: 'DELETE' });
  check('Aufgabe loeschbar', r.body.ok === true);
  r = await api('/api/tasks/' + neueId);
  check('Geloeschte Aufgabe weg (404)', r.status === 404);

  r = await api('/api/tasks', { method: 'POST', body: { titel: 'x', intervall: 'ungueltig' } });
  check('Ungueltiges Intervall abgelehnt (400)', r.status === 400);

  r = await api('/api/history?limit=10');
  check('History enthaelt erledigt + uebersprungen', r.body.history.some(h => h.aktion === 'erledigt') && r.body.history.some(h => h.aktion === 'uebersprungen'));

  r = await api('/api/stats');
  check('Stats liefern Zaehler', r.body.gesamtErledigt >= 1 && r.body.tasks >= 10);

  r = await api('/api/foto', { method: 'POST', body: { raum: 'Küche', merkmale: {} } });
  check('Foto-Endpunkt antwortet', r.body.ok === true);

  r = await api('/api/foto-analyse/status');
  check('Foto-Analyse-Status meldet Key-Verfuegbarkeit', typeof r.body.mistralVerbunden === 'boolean');

  r = await api('/api/foto-analyse', { method: 'POST', body: { raum: 'Küche', imageDataUrl: 'data:image/png;base64,AAAA' } });
  if (!process.env.MISTRAL_API_KEY) {
    check('Ohne Key: 503 mit Heuristik-Fallback', r.status === 503 && r.body.fallback === 'heuristik');
  } else {
    check('Mit Key: Analyse liefert Aufgaben-Array', r.status === 200 && r.body.ok === true && Array.isArray(r.body.analyse.aufgaben));
  }

  r = await api('/api/foto-analyse', { method: 'POST', body: { raum: 'Küche' } });
  check('Ohne Bild: 400', r.status === 400);

  const res = await fetch(BASE + '/');
  check('Startseite wird ausgeliefert', res.status === 200 && (await res.text()).includes('Putzplan'));
} finally {
  server.kill();
  rmSync(tmp, { recursive: true, force: true });
}

console.log(failed === 0 ? '\nAlle Tests bestanden.' : `\n${failed} Test(s) fehlgeschlagen.`);
process.exit(failed === 0 ? 0 : 1);
