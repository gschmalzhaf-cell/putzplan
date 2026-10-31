const $ = s => document.querySelector(s);
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => {}));
}

const api = async (pfad, opts = {}) => {
  const res = await fetch(pfad, {
    method: opts.method || 'GET',
    headers: opts.body ? { 'Content-Type': 'application/json' } : undefined,
    body: opts.body ? JSON.stringify(opts.body) : undefined
  });
  return res.json();
};

let raeume = [];
let intervalle = {};

async function init() {
  const meta = await api('/api/raeume');
  raeume = meta.raeume;
  intervalle = meta.intervalle;
  for (const r of raeume) {
    $('#raumFilter').insertAdjacentHTML('beforeend', `<option value="${r}">${r}</option>`);
    $('#fotoRaum').insertAdjacentHTML('beforeend', `<option value="${r}">${r}</option>`);
  }
  $('#raumFilter').addEventListener('change', laden);
  $('#btnNeu').addEventListener('click', neuTaskForm);
  document.querySelectorAll('.tab').forEach(b => b.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(x => x.classList.toggle('active', x === b));
    document.querySelectorAll('.tabpage').forEach(p => p.classList.toggle('active', p.id === 'tab-' + b.dataset.tab));
    if (b.dataset.tab === 'verlauf') ladeVerlauf();
  }));
  $('#fotoInput').addEventListener('change', fotoAnalyse);
  $('#modalClose').addEventListener('click', () => $('#modal').classList.add('hidden'));
  await laden();
}

function statusKlasse(z) {
  if (z.ueberfaellig) return 'ueberfaellig';
  if (z.tageBis <= 2) return 'faellig';
  return 'ok';
}

function faelligText(z) {
  if (z.ueberfaellig) return `<span class="chip warn">überfällig seit ${Math.abs(z.tageBis)} T.</span>`;
  if (z.tageBis <= 0) return '<span class="chip warn">heute fällig</span>';
  return `<span class="chip">fällig in ${z.tageBis} T.</span>`;
}

async function laden() {
  const raum = $('#raumFilter').value;
  const plan = await api('/api/plan?raum=' + encodeURIComponent(raum));
  $('#gesamtInfo').textContent = plan.zeilen.filter(z => z.ueberfaellig || z.tageBis <= 0).length
    ? `⏰ ${plan.gesamtMinuten} Min. offen (überfällig/heute)` : 'Alles erledigt ✅';
  const liste = $('#planListe');
  liste.innerHTML = '';
  if (!plan.zeilen.length) { liste.innerHTML = '<p>Keine Aufgaben für diesen Raum.</p>'; return; }
  for (const z of plan.zeilen) {
    const div = document.createElement('div');
    div.className = 'karte ' + statusKlasse(z);
    div.innerHTML = `
      <div class="info">
        <h3>${escapeHtml(z.titel)}</h3>
        <div class="meta">
          <span class="chip">${escapeHtml(z.raum)}</span>
          <span class="chip">${escapeHtml(z.intervallLabel)}</span>
          <span class="chip">⏱ ${z.dauerMin} Min.</span>
          ${faelligText(z)}
          <span class="chip">${z.stats.erledigt}× erledigt</span>
          ${z.stats.durchschnittDauerMin ? `<span class="chip">Ø ${z.stats.durchschnittDauerMin} Min.</span>` : ''}
        </div>
      </div>
      <div class="aktionen">
        <button data-a="anleitung">📖 Anleitung</button>
        <button data-a="erledigt">✔ Erledigt</button>
        <button class="sekundar" data-a="skip">↷ später</button>
        <button class="sekundar" data-a="loeschen">🗑</button>
      </div>`;
    div.querySelector('[data-a=anleitung]').onclick = () => anleitung(z.id);
    div.querySelector('[data-a=erledigt]').onclick = () => erledigt(z.id, z.dauerMin);
    div.querySelector('[data-a=skip]').onclick = () => ueberspringen(z.id);
    div.querySelector('[data-a=loeschen]').onclick = () => loeschen(z.id);
    liste.appendChild(div);
  }
}

async function anleitung(id) {
  const a = await api('/api/anleitung/' + id);
  $('#modalContent').innerHTML = `
    <h2>${escapeHtml(a.titel)}</h2>
    <p><span class="chip">${escapeHtml(a.raum)}</span> <span class="chip">${escapeHtml(a.intervallLabel)}</span> <span class="chip">⏱ ca. ${a.dauerMin} Min.</span></p>
    <h3>Hilfsmittel</h3>
    <ul class="hilfsmittel-liste">${a.hilfsmittel.map(h => `<li>${escapeHtml(h)}</li>`).join('') || '<li>–</li>'}</ul>
    <h3>Schritt-für-Schritt</h3>
    <p>${escapeHtml(a.anleitung) || 'Keine Anleitung hinterlegt.'}</p>`;
  $('#modal').classList.remove('hidden');
}

async function erledigt(id, dauerMin) {
  const eingabe = prompt('Dauer in Minuten (leer = Schätzung ' + dauerMin + ' Min.):', dauerMin);
  const body = {};
  if (eingabe && !isNaN(Number(eingabe))) body.dauerMin = Number(eingabe);
  await api('/api/tasks/' + id + '/erledigt', { method: 'POST', body });
  await laden();
}

async function ueberspringen(id) {
  await api('/api/tasks/' + id + '/ueberspringen', { method: 'POST', body: {} });
  await laden();
}

async function loeschen(id) {
  if (!confirm('Aufgabe wirklich löschen?')) return;
  await api('/api/tasks/' + id, { method: 'DELETE' });
  await laden();
}

function neuTaskForm() {
  $('#modalContent').innerHTML = `
    <h2>Neue Aufgabe</h2>
    <form id="neuForm">
      <div class="feld"><label>Titel</label><input name="titel" required></div>
      <div class="feld"><label>Raum</label><select name="raum">${raeume.map(r => `<option>${r}</option>`).join('')}</select></div>
      <div class="feld"><label>Intervall</label><select name="intervall">${Object.entries(intervalle).map(([k, v]) => `<option value="${k}">${v.label}</option>`).join('')}</select></div>
      <div class="feld"><label>Dauer (Min.)</label><input name="dauerMin" type="number" min="1" value="15"></div>
      <div class="feld"><label>Hilfsmittel (kommagetrennt)</label><input name="hilfsmittel"></div>
      <div class="feld"><label>Anleitung</label><textarea name="anleitung" rows="3"></textarea></div>
      <button type="submit">Speichern</button>
    </form>`;
  $('#modal').classList.remove('hidden');
  $('#neuForm').onsubmit = async e => {
    e.preventDefault();
    const f = new FormData(e.target);
    await api('/api/tasks', { method: 'POST', body: {
      titel: f.get('titel'), raum: f.get('raum'), intervall: f.get('intervall'),
      dauerMin: Number(f.get('dauerMin')),
      hilfsmittel: f.get('hilfsmittel').split(',').map(s => s.trim()).filter(Boolean),
      anleitung: f.get('anleitung')
    }});
    $('#modal').classList.add('hidden');
    await laden();
  };
}

async function ladeVerlauf() {
  const s = await api('/api/stats');
  $('#statsBox').innerHTML = `
    <div class="eintrag">✅ ${s.gesamtErledigt} erledigt</div>
    <div class="eintrag">📋 ${s.tasks} Aufgaben</div>
    <div class="eintrag">📷 ${s.fotoErkenntnisse} Foto-Erkenntnisse</div>` +
    Object.entries(s.proRaum).map(([r, n]) => `<div class="eintrag">${escapeHtml(r)}: ${n}×</div>`).join('');
  const h = await api('/api/history?limit=50');
  $('#verlaufListe').innerHTML = h.history.map(e =>
    `<div class="eintrag">${e.aktion === 'erledigt' ? '✅' : '↷'} ${escapeHtml(e.titel)} (${escapeHtml(e.raum)})${e.dauerMin ? ' – ' + e.dauerMin + ' Min.' : ''} – ${new Date(e.zeitpunkt).toLocaleString('de-DE')}</div>`
  ).join('') || '<p>Noch keine Aktionen.</p>';
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

async function fotoAnalyse(e) {
  const file = e.target.files[0];
  if (!file) return;
  const raum = $('#fotoRaum').value;
  const reader = new FileReader();
  reader.onload = async () => {
    const dataUrl = reader.result;
    $('#fotoPreview').innerHTML = `<img src="${dataUrl}" alt="Foto">`;
    $('#fotoErgebnis').innerHTML = '<p>Analysiere …</p>';
    const status = await api('/api/foto-analyse/status').catch(() => ({ mistralVerbunden: false }));
    $('#fotoModus').textContent = status.mistralVerbunden
      ? `Vision-Modell: ${status.model} (Mistral).`
      : 'Lokale Heuristik (kein MISTRAL_API_KEY gesetzt).';
    if (status.mistralVerbunden) {
      const r = await api('/api/foto-analyse', { method: 'POST', body: { raum, imageDataUrl: dataUrl } });
      if (r.ok) return zeigeMistralErgebnis(r);
      $('#fotoErgebnis').innerHTML = `<p>⚠ Mistral nicht erreichbar (${escapeHtml(r.error || '')}) – fallback auf lokale Heuristik.</p>`;
    }
    const img = new Image();
    img.onload = () => {
      const analyse = bildAnalyse(img);
      zeigeFotoErgebnis(analyse, aufgabenAusAnalyse(analyse, raum));
    };
    img.src = dataUrl;
  };
  reader.readAsDataURL(file);
}

function zeigeMistralErgebnis(r) {
  const a = r.analyse || {};
  const aufgaben = (a.aufgaben || []).map(v => ({
    titel: v.titel || 'Aufgabe', raum: r.raum, intervall: v.intervall || 'woechentlich',
    dauerMin: Math.max(1, Math.round(Number(v.dauerMin) || 15)),
    hilfsmittel: Array.isArray(v.hilfsmittel) ? v.hilfsmittel.map(String) : [],
    anleitung: [v.bereich ? 'Bereich: ' + v.bereich + '. ' : '', v.anleitung || ''].join(''),
    score: Math.min(1, Math.max(0, Number(v.prioritaet) || 0.5)),
    grund: 'Von ' + r.model + ' erkannt'
  })).sort((x, y) => y.score - x.score);
  const box = $('#fotoErgebnis');
  box.innerHTML = `<h3>Erkannte Beobachtungen (${escapeHtml(r.model)})</h3>
    <div class="ergebnis-karte"><ul>${(a.beobachtungen || []).map(b => `<li>${escapeHtml(b)}</li>`).join('') || '<li>–</li>'}</ul></div>
    <h3>Vorgeschlagene Aufgaben</h3>` +
    (aufgaben.length ? aufgaben.map((v, i) => `
      <div class="ergebnis-karte">
        <h4>${i + 1}. ${escapeHtml(v.titel)} <span class="chip">⏱ ${v.dauerMin} Min.</span> <span class="chip">${escapeHtml(v.intervall)}</span></h4>
        <div class="bar"><div style="width:${Math.round(v.score * 100)}%"></div></div>
        <div class="meta"><span class="chip">Priorität ${Math.round(v.score * 100)}%</span></div>
        <p><b>Hilfsmittel:</b> ${v.hilfsmittel.map(escapeHtml).join(', ') || '–'}</p>
        <p>${escapeHtml(v.anleitung)}</p>
        <button data-i="${i}">Zum Plan hinzufügen</button>
      </div>`).join('') : '<p>Keine Aufgaben erkannt.</p>');
  box.querySelectorAll('button[data-i]').forEach(b => b.onclick = async () => {
    const v = aufgaben[Number(b.dataset.i)];
    await api('/api/tasks', { method: 'POST', body: {
      titel: v.titel, raum: v.raum, intervall: v.intervall, dauerMin: v.dauerMin,
      hilfsmittel: v.hilfsmittel, anleitung: v.anleitung, erstelltAus: 'foto-mistral'
    }});
    b.textContent = '✅ Hinzugefügt';
    b.disabled = true;
  });
}

function bildAnalyse(img) {
  const c = document.createElement('canvas');
  const w = 160, h = 160;
  c.width = w; c.height = h;
  const ctx = c.getContext('2d');
  ctx.drawImage(img, 0, 0, w, h);
  const d = ctx.getImageData(0, 0, w, h).data;
  let sum = 0, hellSum = 0, rot = 0, gruen = 0, blau = 0, kanten = 0, dunkel = 0;
  const lum = [];
  for (let i = 0; i < d.length; i += 4) {
    const r = d[i], g = d[i + 1], b = d[i + 2];
    const l = 0.299 * r + 0.587 * g + 0.114 * b;
    lum.push(l);
    sum += l; hellSum += l;
    rot += r; gruen += g; blau += b;
    if (l < 60) dunkel++;
  }
  const n = lum.length;
  const mittel = sum / n;
  let varianz = 0;
  for (const l of lum) varianz += (l - mittel) ** 2;
  varianz = Math.sqrt(varianz / n);
  for (let y = 1; y < h - 1; y++) for (let x = 1; x < w - 1; x++) {
    const i = y * w + x;
    const gx = Math.abs(lum[i + 1] - lum[i - 1]);
    const gy = Math.abs(lum[i + w] - lum[i - w]);
    if (gx + gy > 60) kanten++;
  }
  const anteil = dunkel / n;
  return {
    mittelHelligkeit: Math.round(mittel),
    kontrast: Math.round(varianz),
    kantenAnteil: +(kanten / n).toFixed(3),
    dunkelAnteil: +anteil.toFixed(3),
    farbton: rot / n > gruen / n && rot / n > blau / n ? 'warm' : (blau / n > gruen / n ? 'kuehl' : 'neutral')
  };
}

function aufgabenAusAnalyse(a, raum) {
  const v = [];
  const basis = (titel, dauerMin, hilfsmittel, anleitung, score, grund) =>
    v.push({ titel, raum, intervall: 'woechentlich', dauerMin, hilfsmittel, anleitung, score, grund });
  if (a.dunkelAnteil > 0.25) basis('Boden wischen (stark verschmutzt)', 40, ['Wischmopp', 'Eimer', 'Bodenreiniger'], 'Zweimal feucht wischen, stark beanspruchte Stellen nachreiben.', 0.9, 'Hoher Dunkel-/Schmutzanteil im Bild');
  else if (a.kantenAnteil > 0.12) basis('Oberflächen & Ecken reinigen', 25, ['Mikrofasertuch', 'Allzweckreiniger'], 'Kanten und Ecken zuerst, dann Flächen von oben nach unten.', 0.7, 'Viele Kanten/Schatten – Staub in Ecken vermutet');
  if (a.mittelHelligkeit < 90) basis('Fenster/Glasflächen reinigen', 35, ['Glasreiniger', 'Abzieher'], 'Einsprühen, abziehen, nachpolieren.', 0.75, 'Bild insgesamt dunkel – Glas/Fenster evtl. verschmutzt');
  if (a.kontrast > 55) basis('Flecken/Verschmutzung punktuell behandeln', 20, ['Fleckentferner', 'Schwamm'], 'Fleck einweichen, auftragen, kreisförmig auswischen.', 0.65, 'Hoher Kontrast – sichtbare Flecken/Heterogenität');
  if (a.farbton === 'warm' && a.mittelHelligkeit > 110) basis('Staub wischen & Oberflächen polieren', 20, ['Mikrofasertuch', 'Staubwedel'], 'Oben nach unten, Möbel polieren.', 0.5, 'Helles, warmes Bild – eher Staub als Schmutz');
  if (raum === 'Küche') v.push({ titel: 'Arbeitsflächen & Spüle reinigen', raum, intervall: 'taeglich', dauerMin: 10, hilfsmittel: ['Allzweckreiniger', 'Schwamm'], anleitung: 'Flächen desinfizieren, Spüle ausspülen und trockenreiben.', score: 0.8, grund: 'Raum Küche erkannt' });
  if (raum === 'Bad') v.push({ titel: 'Bad-Oberflächen desinfizieren', raum, intervall: 'woechentlich', dauerMin: 20, hilfsmittel: ['Badreiniger', 'WC-Bürste', 'Tuch'], anleitung: 'WC, Waschbecken, Dusche reinigen, Spiegel polieren.', score: 0.8, grund: 'Raum Bad erkannt' });
  if (!v.length) v.push({ titel: 'Allgemeine Sichtreinigung', raum, intervall: 'woechentlich', dauerMin: 15, hilfsmittel: ['Mikrofasertuch'], anleitung: 'Sichtbare Stellen abwischen.', score: 0.3, grund: 'Keine Auffälligkeiten erkannt' });
  return v.sort((x, y) => y.score - x.score);
}

function zeigeFotoErgebnis(a, vorschlaege) {
  const box = $('#fotoErgebnis');
  box.innerHTML = `<h3>Analyse</h3>
    <div class="ergebnis-karte">
      Helligkeit Ø ${a.mittelHelligkeit} · Kontrast ${a.kontrast} · Kanten ${(a.kantenAnteil * 100).toFixed(1)}% · Dunkelanteil ${(a.dunkelAnteil * 100).toFixed(1)}% · Farbton ${a.farbton}
    </div>
    <h3>Erkannte Aufgaben (Bewertung)</h3>` +
    vorschlaege.map((v, i) => `
      <div class="ergebnis-karte">
        <h4>${i + 1}. ${escapeHtml(v.titel)} <span class="chip">⏱ ${v.dauerMin} Min.</span></h4>
        <div class="bar"><div style="width:${Math.round(v.score * 100)}%"></div></div>
        <div class="meta"><span class="chip">${escapeHtml(v.grund)}</span> <span class="chip">Wahrscheinlichkeit ${Math.round(v.score * 100)}%</span></div>
        <p><b>Hilfsmittel:</b> ${v.hilfsmittel.map(escapeHtml).join(', ')}</p>
        <p>${escapeHtml(v.anleitung)}</p>
        <button data-i="${i}">Zum Plan hinzufügen</button>
      </div>`).join('');
  box.querySelectorAll('button[data-i]').forEach(b => b.onclick = async () => {
    const v = vorschlaege[Number(b.dataset.i)];
    await api('/api/tasks', { method: 'POST', body: {
      titel: v.titel, raum: v.raum, intervall: v.intervall, dauerMin: v.dauerMin,
      hilfsmittel: v.hilfsmittel, anleitung: v.anleitung, erstelltAus: 'foto'
    }});
    b.textContent = '✅ Hinzugefügt';
    b.disabled = true;
  });
}

init();
