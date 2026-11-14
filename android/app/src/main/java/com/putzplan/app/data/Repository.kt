package com.putzplan.app.data

import com.putzplan.app.logic.Konstanten
import com.putzplan.app.logic.PlanIntelligenz
import com.putzplan.app.logic.PlanLogik
import com.putzplan.app.net.FotoAnalyse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class PlanZeile(
    val task: Task,
    val ueberfaellig: Boolean,
    val tageBis: Long,
    val prioritaet: Double
)

data class Vorschlag(
    val taskId: String,
    val titel: String,
    val raum: String,
    val intervallAktuell: String,
    val intervallVorschlag: String,
    val begruendung: String
)

data class Plan(
    val zeilen: List<PlanZeile>,
    val vorschlaege: List<Vorschlag> = emptyList(),
    val gesamtMinutenOffen: Int,
    val allesErledigt: Boolean
)

class Repository(private val dao: PutzplanDao) {

    val tasks: Flow<List<Task>> = dao.alleTasks()

    val raeume: Flow<List<String>> = dao.distinkteRaeume()

    val history: Flow<List<HistoryEntry>> = dao.history()

    fun bauePlan(tasks: List<Task>, raumFilter: String): Plan {
        val gefiltert = if (raumFilter == "alle" || raumFilter.isEmpty())
            tasks
        else
            tasks.filter { it.raum == raumFilter }

        val jetzt = System.currentTimeMillis()
        val zeilen = gefiltert.map { t ->
            val faellig = if (t.naechstesFaellig > 0) t.naechstesFaellig else t.erstelltAm
            val ueberfaellig = faellig <= jetzt
            val tageBis = (faellig - jetzt) / 86400000L + if (faellig > jetzt) 1 else 0
            // 2.2: dynamische Prioritaet statt starrer Faelligkeits-Sortierung
            PlanZeile(t, ueberfaellig, tageBis, PlanIntelligenz.prioritaet(t, jetzt))
        }.sortedByDescending { it.prioritaet }

        val faelligZeilen = zeilen.filter { it.ueberfaellig || it.tageBis <= 1 }
        val minuten = faelligZeilen.sumOf { it.task.dauerMin }

        // 2.2: adaptive Intervall-Vorschlaege aus dem Lern-Feld der Tasks
        val vorschlaege = gefiltert
            .filter { !it.intervallVorschlag.isNullOrBlank() && it.intervallVorschlag != it.intervall }
            .map { t ->
                Vorschlag(
                    taskId = t.id, titel = t.titel, raum = t.raum,
                    intervallAktuell = t.intervall,
                    intervallVorschlag = t.intervallVorschlag!!,
                    begruendung = t.vorschlagBegruendung ?: ""
                )
            }
        return Plan(faelligZeilen, vorschlaege, minuten, gefiltert.isNotEmpty() && faelligZeilen.isEmpty())
    }

    suspend fun erledigt(taskId: String, dauerMin: Int?) = withContext(Dispatchers.IO) {
        val t = dao.taskById(taskId) ?: return@withContext
        val gelernt = PlanLogik.lerneAusAbschluss(t, dauerMin)
        dao.updateTask(gelernt)
        dao.insertHistory(
            HistoryEntry(
                taskId = t.id, titel = t.titel, raum = t.raum,
                aktion = "erledigt", dauerMin = dauerMin,
                zeitpunkt = System.currentTimeMillis()
            )
        )
        // 2.2: aus realen Abstaenden einen Intervall-Vorschlag lernen
        val vorschlag = PlanIntelligenz.intervallVorschlag(gelernt, dao.erledigtZeiten(taskId))
        val mitVorschlag = if (vorschlag != null)
            gelernt.copy(intervallVorschlag = vorschlag.first, vorschlagBegruendung = vorschlag.second)
        else
            gelernt.copy(intervallVorschlag = null, vorschlagBegruendung = null)
        if (mitVorschlag != gelernt) dao.updateTask(mitVorschlag)
    }

    suspend fun vorschlagUebernehmen(taskId: String) = withContext(Dispatchers.IO) {
        val t = dao.taskById(taskId) ?: return@withContext
        val key = t.intervallVorschlag ?: return@withContext
        val tage = Konstanten.INTERVALL_MAP[key]?.days ?: return@withContext
        val basis = t.letzterAbschluss ?: System.currentTimeMillis()
        val neu = t.copy(
            intervall = key,
            naechstesFaellig = basis + tage * 86400000L,
            intervallVorschlag = null,
            vorschlagBegruendung = null
        )
        dao.updateTask(neu)
        dao.insertHistory(
            HistoryEntry(
                taskId = t.id, titel = t.titel, raum = t.raum,
                aktion = "intervall-angepasst", dauerMin = null,
                zeitpunkt = System.currentTimeMillis()
            )
        )
    }

    suspend fun vorschlagAblehnen(taskId: String) = withContext(Dispatchers.IO) {
        val t = dao.taskById(taskId) ?: return@withContext
        if (t.intervallVorschlag != null || t.vorschlagBegruendung != null) {
            dao.updateTask(t.copy(intervallVorschlag = null, vorschlagBegruendung = null))
        }
    }

    /**
     * 2.2 - Foto->Plan-Kopplung: Eine frische Foto-Analyse verschiebt die
     * Faelligkeiten des Raums dynamisch:
     * - deutlicher Schmutz -> Aufgaben des Raums werden sofort faellig
     * - sauberer Raum      -> faellige Aufgaben werden um halbe Intervallzeit geschoben
     */
    suspend fun fotoAufPlanAnwenden(raum: String, analyse: FotoAnalyse): String? =
        withContext(Dispatchers.IO) {
            val schmutzScore = (analyse.aufgaben.maxOfOrNull { it.prioritaet } ?: 0.0).coerceIn(0.0, 1.0)
            val dringend = schmutzScore >= 0.6 || analyse.beobachtungen.size >= 3
            val sauber = analyse.beobachtungen.isEmpty() || schmutzScore <= 0.2
            if (!dringend && !sauber) return@withContext null

            val jetzt = System.currentTimeMillis()
            var geaendert = 0
            for (t in dao.tasksInRaum(raum)) {
                val faellig = if (t.naechstesFaellig > 0) t.naechstesFaellig else t.erstelltAm
                val neu = when {
                    dringend && faellig > jetzt ->
                        t.copy(naechstesFaellig = jetzt)
                    sauber && faellig <= jetzt -> {
                        val tage = (Konstanten.INTERVALL_MAP[t.intervall]?.days ?: 7).toLong()
                        t.copy(naechstesFaellig = jetzt + (tage / 2).coerceAtLeast(1) * 86400000L)
                    }
                    else -> null
                }
                if (neu != null) {
                    dao.updateTask(neu)
                    geaendert++
                }
            }
            if (geaendert == 0) return@withContext null
            dao.insertHistory(
                HistoryEntry(
                    taskId = "foto", titel = "Foto-Analyse: $raum",
                    raum = raum, aktion = "foto",
                    dauerMin = null, zeitpunkt = jetzt
                )
            )
            if (dringend) "Plan aktualisiert: $geaendert Aufgabe(n) in $raum vorgezogen"
            else "Plan aktualisiert: $geaendert Aufgabe(n) in $raum nach hinten geschoben"
        }

    suspend fun ueberspringen(taskId: String) = withContext(Dispatchers.IO) {
        val t = dao.taskById(taskId) ?: return@withContext
        val neu = PlanLogik.ueberspringen(t)
        dao.updateTask(neu)
        dao.insertHistory(
            HistoryEntry(
                taskId = t.id, titel = t.titel, raum = t.raum,
                aktion = "uebersprungen", dauerMin = null,
                zeitpunkt = System.currentTimeMillis()
            )
        )
    }

    suspend fun loeschen(taskId: String) = withContext(Dispatchers.IO) {
        dao.deleteTask(taskId)
        dao.deleteHistoryForTask(taskId)
    }

    suspend fun raumUmbenennen(alt: String, neu: String) = withContext(Dispatchers.IO) {
        val ziel = neu.trim().ifBlank { alt }
        if (ziel == alt) return@withContext
        dao.renameRaumInTasks(alt, ziel)
        dao.renameRaumInHistory(alt, ziel)
    }

    suspend fun neuTask(
        titel: String, raum: String, intervall: String, dauerMin: Int,
        hilfsmittel: List<String>, anleitung: String, erstelltAus: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (titel.isBlank() || !Konstanten.istGueltigesIntervall(intervall)) return@withContext false
        val raumFix = raum.trim().ifBlank { "Sonstiges" }
        dao.insertTask(
            PlanLogik.neuerTask(titel, raumFix, intervall, dauerMin, hilfsmittel, anleitung, erstelltAus)
        )
        true
    }

    suspend fun fotoErkenntnisSpeichern(raum: String, model: String, analyseJson: String) =
        withContext(Dispatchers.IO) {
            dao.insertFotoErkenntnis(
                FotoErkenntnis(
                    raum = raum, model = model,
                    zeitpunkt = System.currentTimeMillis(), analyseJson = analyseJson
                )
            )
        }

    suspend fun letzteFotoErkenntnis(raum: String): FotoErkenntnis? = withContext(Dispatchers.IO) {
        dao.letzteFotoErkenntnis(raum)
    }

    suspend fun stats(): Stats = withContext(Dispatchers.IO) {
        // 2.3 - Wochenziel: Montag 00:00 als Wochenstart
        val kal = java.util.Calendar.getInstance()
        kal.set(java.util.Calendar.HOUR_OF_DAY, 0); kal.set(java.util.Calendar.MINUTE, 0)
        kal.set(java.util.Calendar.SECOND, 0); kal.set(java.util.Calendar.MILLISECOND, 0)
        kal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val wochenStart = kal.timeInMillis
        kal.add(java.util.Calendar.DAY_OF_YEAR, 7)
        val wochenEnde = kal.timeInMillis
        Stats(
            gesamtErledigt = dao.anzahlErledigt(),
            proRaum = dao.erledigtProRaum().associate { it.raum to it.anzahl },
            taskCount = dao.taskCount(),
            fotoErkenntnisse = dao.fotoErkenntnisCount(),
            erledigtDieseWoche = dao.anzahlErledigtSeit(wochenStart),
            geplantDieseWoche = dao.anzahlFaelligBis(wochenEnde).coerceAtLeast(1)
        )
    }

    fun planFlow(raumFilter: String): Flow<Plan> =
        dao.alleTasks().map { bauePlan(it, raumFilter) }
}
