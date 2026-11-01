package com.putzplan.app.logic

import com.putzplan.app.data.Task
import java.util.UUID

object PlanLogik {

    fun recomputeFaelligkeit(task: Task, base: Long = task.letzterAbschluss ?: task.erstelltAm): Task {
        val days = Konstanten.INTERVALL_MAP[task.intervall]?.days ?: 7
        return task.copy(naechstesFaellig = base + days * 86400000L)
    }

    fun normalizeIntervall(task: Task): Task {
        if (Konstanten.INTERVALL_MAP.containsKey(task.intervall)) return task
        val s = task.erledigt
        val neu = when {
            s >= 8 -> "woechentlich"
            s >= 3 -> "zweiwoechentlich"
            else -> "monatlich"
        }
        return task.copy(intervall = neu)
    }

    fun lerneAusAbschluss(task: Task, dauerMin: Int?): Task {
        var t = task.copy(erledigt = task.erledigt + 1, letzterAbschluss = System.currentTimeMillis())
        if (dauerMin != null && dauerMin > 0) {
            val bisher = t.durchschnittDauerMin
            val neuerSchnitt: Int = if (bisher != null)
                Math.round((bisher * (t.erledigt - 1) + dauerMin).toDouble() / t.erledigt).toInt()
            else dauerMin
            t = t.copy(
                letzteDauerMin = dauerMin,
                durchschnittDauerMin = neuerSchnitt
            )
            if (dauerMin > t.dauerMin * 1.5) t = t.copy(dauerMin = ((t.dauerMin * 1.1).toInt()))
            if (dauerMin < t.dauerMin * 0.6 && t.dauerMin > 5) t = t.copy(dauerMin = maxOf(5, (t.dauerMin * 0.9).toInt()))
        }
        t = normalizeIntervall(t)
        return recomputeFaelligkeit(t)
    }

    fun ueberspringen(task: Task): Task {
        var t = task.copy(uebersprungen = task.uebersprungen + 1)
        if (t.uebersprungen >= 3 && t.intervall != "jaehrlich") {
            val idx = Konstanten.INTERVALLE.indexOfFirst { it.key == t.intervall }
            if (idx < Konstanten.INTERVALLE.size - 1) {
                t = t.copy(intervall = Konstanten.INTERVALLE[idx + 1].key, uebersprungen = 0)
            }
        }
        return recomputeFaelligkeit(t, System.currentTimeMillis())
    }

    fun neuerTask(
        titel: String, raum: String, intervall: String, dauerMin: Int,
        hilfsmittel: List<String>, anleitung: String, erstelltAus: String
    ): Task {
        val t = Task(
            id = "t-" + UUID.randomUUID().toString(),
            titel = titel,
            raum = raum,
            intervall = intervall,
            dauerMin = maxOf(1, dauerMin),
            hilfsmittel = hilfsmittel.joinToString(","),
            anleitung = anleitung,
            erstelltAm = System.currentTimeMillis(),
            erstelltAus = erstelltAus,
            erledigt = 0, uebersprungen = 0,
            letzteDauerMin = null, durchschnittDauerMin = null, letzterAbschluss = null,
            naechstesFaellig = 0L
        )
        return t.copy(naechstesFaellig = t.erstelltAm)
    }
}
