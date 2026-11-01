package com.putzplan.app.data

import com.putzplan.app.logic.Konstanten
import com.putzplan.app.logic.PlanLogik
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class PlanZeile(
    val task: Task,
    val ueberfaellig: Boolean,
    val tageBis: Long
)

data class Plan(
    val zeilen: List<PlanZeile>,
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
            PlanZeile(t, ueberfaellig, tageBis)
        }.sortedWith(compareBy({ !it.ueberfaellig }, { it.tageBis }))

        val minuten = zeilen.filter { it.ueberfaellig }.sumOf { it.task.dauerMin }
        return Plan(zeilen, minuten, gefiltert.isNotEmpty() && zeilen.none { it.ueberfaellig || it.tageBis <= 0 })
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

    suspend fun stats(): Stats = withContext(Dispatchers.IO) {
        Stats(
            gesamtErledigt = dao.anzahlErledigt(),
            proRaum = dao.erledigtProRaum().associate { it.raum to it.anzahl },
            taskCount = dao.taskCount(),
            fotoErkenntnisse = dao.fotoErkenntnisCount()
        )
    }

    fun planFlow(raumFilter: String): Flow<Plan> =
        dao.alleTasks().map { bauePlan(it, raumFilter) }
}
