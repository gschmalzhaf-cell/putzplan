package com.putzplan.app.logic

import com.putzplan.app.data.Task
import java.util.Calendar
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

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
            // Gewichteter gleitender Durchschnitt ab dem 3. Abschluss, damit neue Messungen schneller wirken
            val neuerSchnitt: Int = when {
                bisher == null -> dauerMin
                t.erledigt <= 2 -> Math.round((bisher * (t.erledigt - 1) + dauerMin).toDouble() / t.erledigt).toInt()
                else -> (bisher * 0.65 + dauerMin * 0.35).roundToInt()
            }
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
// 2.2 / 2.6 — Intelligenter & dynamischer Plan
// ============================================================================

object PlanIntelligenz {

    data class SmartRunde(
        val titel: String,
        val untertitel: String,
        val gesamtMin: Int,
        val taskIds: List<String>
    )

    data class Gewohnheiten(
        val wochenStreak: Int,
        val lieblingsTag: String?,
        val lieblingsZeit: String?
    )

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
            abs(it.days - median)
        } ?: return null
        if (passend.key == task.intervall) return null

        val aktuellTage = Konstanten.INTERVALL_MAP[task.intervall]?.days ?: 7
        val toleranz = maxOf(2.0, passend.days * 0.35)
        if (abs(passend.days - median) > toleranz || abs(passend.days - aktuellTage) < 2) return null

        val begruendung = "Du erledigst „${task.titel}“ real im Schnitt alle " +
            "${Math.round(median)} Tage – „${passend.label}“ passt besser zu deinem Rhythmus."
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
     * Zeitbudget-Modus (2.6: 0/1-Knapsack-Optimierung):
     * Wählt die optimale Kombination offener Aufgaben bis budgetMin,
     * die den höchsten Prioritäts-Nutzen erzielt und die Zeit gut ausnutzt.
     */
    fun <T> zeitplanAuswaehlen(
        zeilen: List<T>,
        budgetMin: Int,
        prioritaet: (T) -> Double,
        dauer: (T) -> Int
    ): List<T> {
        if (budgetMin <= 0 || zeilen.isEmpty()) return emptyList()
        val kandidaten = zeilen.filter { dauer(it) in 1..budgetMin }
        if (kandidaten.isEmpty()) return emptyList()

        // Normalisierte positive Gewichtung: Priorität + kleiner Bonus für gute Zeitausnutzung
        val minPrio = kandidaten.minOf(prioritaet)
        val gewichte = kandidaten.map { z ->
            (prioritaet(z) - minPrio + 20.0) + dauer(z) * 0.5
        }
        val n = kandidaten.size
        val dp = Array(n + 1) { DoubleArray(budgetMin + 1) }

        for (i in 1..n) {
            val d = dauer(kandidaten[i - 1])
            val w = gewichte[i - 1]
            for (b in 0..budgetMin) {
                dp[i][b] = dp[i - 1][b]
                if (d <= b && dp[i - 1][b - d] + w > dp[i][b]) {
                    dp[i][b] = dp[i - 1][b - d] + w
                }
            }
        }

        val auswahl = mutableListOf<T>()
        var restBudget = budgetMin
        for (i in n downTo 1) {
            if (dp[i][restBudget] != dp[i - 1][restBudget]) {
                val item = kandidaten[i - 1]
                auswahl += item
                restBudget -= dauer(item)
            }
        }
        return auswahl.sortedByDescending(prioritaet)
    }

    /**
     * 2.6 — Smart-Runde (Synergie-Erkennung):
     * Erkennt, ob mehrere fällige Aufgaben dasselbe Hilfsmittel nutzen (z. B. Staubsauger
     * in mehreren Räumen) oder im selben Raum liegen, damit sie effizient am Stück erledigt
     * werden können.
     */
    fun <T> smartBatchVorschlag(
        zeilen: List<T>,
        idOf: (T) -> String,
        raumOf: (T) -> String,
        dauerOf: (T) -> Int,
        hilfsmittelOf: (T) -> List<String>
    ): SmartRunde? {
        if (zeilen.size < 2) return null

        // 1. Hilfsmittel-Synergie über mehrere Aufgaben (z. B. Staubsauger, Glasreiniger, Wischmopp)
        val ignorierteMittel = setOf("tuch", "lappen", "wasser", "schwamm", "feuchtes tuch", "mikrofasertuch")
        val nachMittel = mutableMapOf<String, MutableList<T>>()
        for (z in zeilen) {
            for (m in hilfsmittelOf(z)) {
                val norm = m.trim()
                if (norm.length >= 4 && norm.lowercase() !in ignorierteMittel) {
                    nachMittel.getOrPut(norm) { mutableListOf() }.add(z)
                }
            }
        }
        val bestesMittel = nachMittel.entries
            .filter { it.value.size >= 2 }
            .maxByOrNull { it.value.size }

        if (bestesMittel != null) {
            val gruppe = bestesMittel.value.take(4)
            val raumZahl = gruppe.map(raumOf).distinct().size
            val gesamtMin = gruppe.sumOf(dauerOf)
            val raumInfo = if (raumZahl > 1) "in $raumZahl Räumen" else "in ${raumOf(gruppe.first())}"
            return SmartRunde(
                titel = "${bestesMittel.key}-Runde",
                untertitel = "${gruppe.size} Aufgaben $raumInfo · ca. $gesamtMin Min. am Stück",
                gesamtMin = gesamtMin,
                taskIds = gruppe.map(idOf)
            )
        }

        // 2. Raum-Fokus: mehrere fällige Aufgaben im selben Raum
        val besterRaum = zeilen.groupBy(raumOf)
            .entries
            .filter { it.value.size >= 2 }
            .maxByOrNull { it.value.size }

        if (besterRaum != null) {
            val gruppe = besterRaum.value.take(4)
            val gesamtMin = gruppe.sumOf(dauerOf)
            return SmartRunde(
                titel = "${besterRaum.key} in einem Rutsch",
                untertitel = "${gruppe.size} fällige Aufgaben im selben Raum · ca. $gesamtMin Min.",
                gesamtMin = gesamtMin,
                taskIds = gruppe.map(idOf)
            )
        }

        return null
    }

    /**
     * 2.6 — Gewohnheits-Analyse aus den realen Abschluss-Zeitpunkten:
     * Ermittelt die aktuelle Wochen-Streak, den stärksten Putztag und die bevorzugte Tageszeit.
     */
    fun gewohnheitsAnalyse(erledigtZeitpunkte: List<Long>, jetzt: Long = System.currentTimeMillis()): Gewohnheiten {
        if (erledigtZeitpunkte.isEmpty()) {
            return Gewohnheiten(wochenStreak = 0, lieblingsTag = null, lieblingsZeit = null)
        }

        val wochentagNamen = mapOf(
            Calendar.MONDAY to "Montag",
            Calendar.TUESDAY to "Dienstag",
            Calendar.WEDNESDAY to "Mittwoch",
            Calendar.THURSDAY to "Donnerstag",
            Calendar.FRIDAY to "Freitag",
            Calendar.SATURDAY to "Samstag",
            Calendar.SUNDAY to "Sonntag"
        )

        val tagCounts = mutableMapOf<Int, Int>()
        val zeitCounts = mutableMapOf<String, Int>()
        val aktiveWochen = mutableSetOf<Long>()

        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY

        fun wochenIndex(ts: Long): Long {
            cal.timeInMillis = ts
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val offset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            return cal.timeInMillis / (7L * 86400000L)
        }

        for (ts in erledigtZeitpunkte) {
            cal.timeInMillis = ts
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            tagCounts[dow] = (tagCounts[dow] ?: 0) + 1

            val stunde = cal.get(Calendar.HOUR_OF_DAY)
            val tageszeit = when (stunde) {
                in 5..11 -> "Vormittags"
                in 12..14 -> "Mittags"
                in 15..18 -> "Nachmittags"
                else -> "Abends"
            }
            zeitCounts[tageszeit] = (zeitCounts[tageszeit] ?: 0) + 1
            aktiveWochen += wochenIndex(ts)
        }

        val aktuelleWoche = wochenIndex(jetzt)
        var streak = 0
        var pruefWoche = if (aktuelleWoche in aktiveWochen) aktuelleWoche else aktuelleWoche - 1
        while (pruefWoche in aktiveWochen) {
            streak++
            pruefWoche--
        }

        val lieblingsTag = if (erledigtZeitpunkte.size >= 3) {
            tagCounts.maxByOrNull { it.value }?.key?.let { wochentagNamen[it] }
        } else null

        val lieblingsZeit = if (erledigtZeitpunkte.size >= 3) {
            zeitCounts.maxByOrNull { it.value }?.key
        } else null

        return Gewohnheiten(
            wochenStreak = streak,
            lieblingsTag = lieblingsTag,
            lieblingsZeit = lieblingsZeit
        )
    }

    /** Tageszeitabhängige Begrüßung für den Wochenplan-Hero. */
    fun tagesGruss(stunde: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String = when (stunde) {
        in 5..10 -> "Guten Morgen ☀️"
        in 11..13 -> "Moin 👋"
        in 14..17 -> "Guten Nachmittag ✨"
        else -> "Guten Abend 🌙"
    }

    private fun median(werte: List<Double>): Double {
        val s = werte.sorted()
        val n = s.size
        return if (n % 2 == 1) s[n / 2] else (s[n / 2 - 1] + s[n / 2]) / 2.0
    }
}
