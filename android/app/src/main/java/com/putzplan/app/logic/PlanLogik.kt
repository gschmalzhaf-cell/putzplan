package com.putzplan.app.logic

import com.putzplan.app.data.Task
import java.util.UUID

object PlanLogik {

    fun seedTasks(): List<Task> {
        val jetzt = System.currentTimeMillis()
        fun seed(
            titel: String, raum: String, intervall: String, dauerMin: Int,
            hilfsmittel: List<String>, anleitung: String, index: Int
        ) = Task(
            id = "seed-${index + 1}",
            titel = titel,
            raum = raum,
            intervall = intervall,
            dauerMin = dauerMin,
            hilfsmittel = hilfsmittel.joinToString(","),
            anleitung = anleitung,
            erstelltAm = jetzt,
            erstelltAus = "seed",
            erledigt = 0,
            uebersprungen = 0,
            letzteDauerMin = null,
            durchschnittDauerMin = null,
            letzterAbschluss = null,
            naechstesFaellig = jetzt
        )

        return listOf(
            seed(
                "Staub wischen (alle Oberflächen)", "Ganze Wohnung", "woechentlich", 30,
                listOf("Mikrofasertuch", "Staubwedel"),
                "Oben nach unten arbeiten. Oberflächen freiräumen, abwischen, Möbel wieder zurückstellen.", 0
            ),
            seed(
                "Boden saugen und wischen", "Ganze Wohnung", "woechentlich", 45,
                listOf("Staubsauger", "Wischmopp", "Eimer", "Reiniger"),
                "Erst saugen, dann feucht wischen. Von hinten nach vorne zur Tür arbeiten.", 1
            ),
            seed(
                "Bad reinigen (WC, Waschbecken, Dusche)", "Bad", "woechentlich", 25,
                listOf("Badreiniger", "Schwamm", "WC-Bürste", "Tuch"),
                "Reiniger einwirken lassen, WC mit Bürste reinigen, Oberflächen abwischen, Spiegel polieren.", 2
            ),
            seed(
                "Küche: Arbeitsflächen desinfizieren", "Küche", "taeglich", 10,
                listOf("Allzweckreiniger", "Tuch"),
                "Flächen feucht abwischen, Schneidebretter separat reinigen.", 3
            ),
            seed(
                "Kühlschrank auswischen", "Küche", "monatlich", 20,
                listOf("Schwamm", "Essigreiniger"),
                "Abgelaufenes entsorgen, Fächer herausnehmen und auswischen, Gummidichtung säubern.", 4
            ),
            seed(
                "Fenster putzen", "Ganze Wohnung", "quartal", 60,
                listOf("Glasreiniger", "Abzieher", "Mikrofasertuch", "Leiter"),
                "Glas einsprühen, mit Abzieher von oben nach unten abziehen, Rahmen mitwischen.", 5
            ),
            seed(
                "Backofen reinigen", "Küche", "quartal", 40,
                listOf("Backofenreiniger", "Handschuhe", "Schwamm"),
                "Reiniger auftragen, einwirken lassen (Herstellerangabe), ausreiben, Nachwischen.", 6
            ),
            seed(
                "Kleiderschrank auswischen", "Schlafzimmer", "halbjaehrlich", 45,
                listOf("Tuch", "Holzpflege"),
                "Kleider ausräumen, Fächer feucht abwischen, trocknen lassen, wieder einräumen.", 7
            ),
            seed(
                "Matratze wenden / lüften", "Schlafzimmer", "monatlich", 15,
                listOf("Staubsauger-Aufsatz"),
                "Bettzeug abziehen, Matratze wenden und absaugen, lüften.", 8
            ),
            seed(
                "Abflüsse reinigen", "Bad", "monatlich", 15,
                listOf("Abflussreiniger", "Spirale", "Handschuhe"),
                "Sieb entfernen, Haare entfernen, Reiniger einfüllen, mit Spirale nachhelfen.", 9
            ),
            seed(
                "Heizkörper entstauben", "Ganze Wohnung", "halbjaehrlich", 30,
                listOf("Heizkörperbürste", "Staubsauger"),
                "Bürste zwischen die Lamellen führen, von oben nach unten absaugen.", 10
            ),
            seed(
                "Türgriffe & Lichtschalter desinfizieren", "Ganze Wohnung", "woechentlich", 10,
                listOf("Desinfektionstuch"),
                "Alle Griffe und Schalter feucht abwischen.", 11
            ),
            seed(
                "Spülmaschine entkalken", "Küche", "quartal", 15,
                listOf("Entkalker", "Reinigungsprogramm"),
                "Entkalker in den Besteckkorb, heißestes Programm leer laufen lassen.", 12
            ),
            seed(
                "Vorhänge waschen", "Wohnzimmer", "halbjaehrlich", 90,
                listOf("Waschmaschine", "Waschmittel"),
                "Pflegesymbole prüfen, Schonprogramm, aufhängen ohne Trockner.", 13
            ),
            seed(
                "Garagen-/Kellerboden fegen", "Abstellraum", "monatlich", 20,
                listOf("Besen", "Schaufel"),
                "Zusammenfegen und entsorgen.", 14
            )
        )
    }

    fun recomputeFaelligkeit(task: Task): Task {
        val base = task.letzterAbschluss ?: task.erstelltAm
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
            val neuerSchnitt = if (t.durchschnittDauerMin != null)
                Math.round((t.durchschnittDauerMin!! * (t.erledigt - 1) + dauerMin).toDouble() / t.erledigt)
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
        return recomputeFaelligkeit(t)
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
        return recomputeFaelligkeit(t)
    }
}
