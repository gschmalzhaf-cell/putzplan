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
                durchschnittDauerMin = neuerSchnitt,
                dauerMin = neuerSchnitt.coerceAtLeast(1)
            )
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

// ============================================================================
// 2.2 — Intelligenter & dynamischer Plan
// ============================================================================

object PlanIntelligenz {

    /** Reale Abstände (Tage) zwischen aufeinanderfolgenden Abschlüssen einer Aufgabe. */
    fun realeAbstaendeTage(erledigtZeiten: List<Long>): List<Double> =
        erledigtZeiten.sorted().zipWithNext { a, b -> (b - a) / 86400000.0 }
            .filter { it > 0.5 }

    /**
     * Adaptive Intervalle: Aus den realen Abständen (Median) wird das passende
     * Intervall abgeleitet. Weicht es deutlich vom aktuellen ab, gibt es einen
     * Vorschlag (Key + Begründung) zurück — sonst null.
     * Lernbereit ab 3 gemessenen Abständen (= 4 Abschlüsse).
     */
    fun intervallVorschlag(
        task: Task,
        erledigtZeiten: List<Long>,
        minAbstaende: Int = 3
    ): Pair<String, String>? {
        val abstaende = realeAbstaendeTage(erledigtZeiten)
        if (abstaende.size < minAbstaende) return null
        val median = median(abstaende)
        val passend = Konstanten.INTERVALLE.minByOrNull {
            Math.abs(it.days - median)
        } ?: return null
        if (passend.key == task.intervall) return null
        // Nur vorschlagen, wenn der Unterschied spürbar ist (> 2 Tage Abweichung)
        if (Math.abs(passend.days - median) > 2 || Math.abs(passend.days - (Konstanten.INTERVALL_MAP[task.intervall]?.days ?: 7)) < 2) return null
        val begruendung = "Du erledigst \u201e${task.titel}\u201c real im Schnitt alle " +
            "${Math.round(median)} Tage \u2013 \u201e${passend.label}\u201c passt besser zu deinem Rhythmus."
        return passend.key to begruendung
    }

    /**
     * Dynamische Priorität: Überfällige Aufgaben zuerst, gewichtet am
     * Intervall (3 Tage Verspätung bei täglichem Intervall wiegt schwerer
     * als bei jährlichem). Überfällig: Score > 100, sonst < 0.
     */
    fun prioritaet(task: Task, jetzt: Long = System.currentTimeMillis()): Double {
        val faellig = if (task.naechstesFaellig > 0) task.naechstesFaellig else task.erstelltAm
        val ivTage = (Konstanten.INTERVALL_MAP[task.intervall]?.days ?: 7).toDouble()
        return if (faellig <= jetzt) {
            val ueberfaelligTage = (jetzt - faellig) / 86400000.0
            100.0 + (ueberfaelligTage / ivTage).coerceAtMost(1.0) * 50.0
        } else {
            val restTage = (faellig - jetzt) / 86400000.0
            -((restTage / ivTage).coerceAtMost(1.0) * 50.0)
        }
    }

    /**
     * Zeitbudget-Modus: wählt aus den offenen Aufgaben die beste Kombination
     * für das verfügbare Budget (greedy nach Priorität).
     */
    fun <T> zeitplanAuswaehlen(
        zeilen: List<T>,
        budgetMin: Int,
        prioritaet: (T) -> Double,
        dauer: (T) -> Int
    ): List<T> {
        val rest = mutableListOf<T>()
        var budget = budgetMin
        for (z in zeilen.sortedByDescending(prioritaet)) {
            val d = dauer(z)
            if (d <= budget) {
                rest += z
                budget -= d
            }
        }
        return rest
    }

    private fun median(werte: List<Double>): Double {
        val s = werte.sorted()
        val n = s.size
        return if (n % 2 == 1) s[n / 2] else (s[n / 2 - 1] + s[n / 2]) / 2.0
    }
}
