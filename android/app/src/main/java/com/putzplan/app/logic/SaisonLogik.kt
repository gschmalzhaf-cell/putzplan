package com.putzplan.app.logic

/**
 * 2.3 - Saison-/Kontext-Modul: saisonale Aufgaben-Vorschlaege je Jahreszeit.
 * Die Vorschlaege erscheinen im Plan-Tab und koennen mit einem Tipp angelegt werden.
 */
object SaisonLogik {

    data class SaisonAufgabe(
        val titel: String,
        val raum: String,
        val intervall: String,
        val dauerMin: Int,
        val hilfsmittel: List<String>,
        val anleitung: String,
        val hinweis: String
    )

    private val fruehjahr = listOf(
        SaisonAufgabe(
            titel = "Fenster reinigen", raum = "Wohnen", intervall = "jaehrlich", dauerMin = 45,
            hilfsmittel = listOf("Glasreiniger", "Fensterabzieher", "Mikrofasertuch"),
            anleitung = "Fenster von innen und aussen reinigen: erst Rahmen und Kittbank feucht abwischen, dann Scheibe mit Glasreiniger und Abzieher in Zickzack-Bahnen trocknen.",
            hinweis = "Fruehling - jetzt sind die Fenster dran."
        ),
        SaisonAufgabe(
            titel = "Balkon oder Terrasse wischen", raum = "Balkon", intervall = "quartal", dauerMin = 20,
            hilfsmittel = listOf("Besen", "Eimer", "Allzweckreiniger"),
            anleitung = "Balkon abkehren, dann Boden und Gelaender feucht abwischen, Moos und Vogelkot mit Allzweckreiniger loesen.",
            hinweis = "Der Fruehling beginnt auf dem Balkon."
        )
    )

    private val sommer = listOf(
        SaisonAufgabe(
            titel = "Rolllaeden und Jalousien abstauben", raum = "Wohnen", intervall = "halbjaehrlich", dauerMin = 15,
            hilfsmittel = listOf("Staubtuch", "Federbesen"),
            anleitung = "Rolllaeden ganz runterlassen, mit Federbesen oder feuchtem Tuch Lamelle fuer Lamelle abnehmen.",
            hinweis = "Mehr Licht im Sommer - saubere Rolllaeden helfen."
        ),
        SaisonAufgabe(
            titel = "Kuehlschrank grundlegend reinigen", raum = "Kueche", intervall = "quartal", dauerMin = 30,
            hilfsmittel = listOf("Allzweckreiniger", "Schwamm", "Ausstellkuehlung"),
            anleitung = "Kuehlschrank ausraeumen, abtauen lassen, innen mit warmem Wasser und Reiniger auswischen, Dichtungen nicht vergessen.",
            hinweis = "Im Sommer wird der Kuehlschrank richtig gebraucht."
        )
    )

    private val herbst = listOf(
        SaisonAufgabe(
            titel = "Teppiche grundlegend reinigen", raum = "Wohnen", intervall = "jaehrlich", dauerMin = 40,
            hilfsmittel = listOf("Teppichklopfer", "Staubsauger", "Teppichschaum"),
            anleitung = "Teppich draussen ausklopfen, gründlich absaugen, Flecken mit Teppichschaum behandeln und trocknen lassen.",
            hinweis = "Herbst - die Teppiche kommen bald wieder zum Einsatz."
        ),
        SaisonAufgabe(
            titel = "Heizkoerper entstauben", raum = "Bad", intervall = "halbjaehrlich", dauerMin = 15,
            hilfsmittel = listOf("Heizkoerperbuerste", "Feuchtes Tuch"),
            anleitung = "Heizkoerper ausschalten, Zwischenraeume mit Heizkoerperbuerste auskehren, Aussenflaechen feucht abwischen.",
            hinweis = "Vor der Heizsaison die Heizkoerper entstauben."
        )
    )

    private val winter = listOf(
        SaisonAufgabe(
            titel = "Vorratsschrank ausmisten", raum = "Kueche", intervall = "quartal", dauerMin = 20,
            hilfsmittel = listOf("Feuchtes Tuch", "Muelleimer"),
            anleitung = "Vorraete rausnehmen, abgelaufenes entsorgen, Regale auswischen und alles sortiert zurueckstellen.",
            hinweis = "Winter-Vorrat checken und ausmisten."
        ),
        SaisonAufgabe(
            titel = "Schraenke ausmisten", raum = "Schlafen", intervall = "jaehrlich", dauerMin = 30,
            hilfsmittel = listOf("Staubtuch", "Muelleimer", "Putzbeutel"),
            anleitung = "Kleidung rausnehmen, aussortieren (spenden/entsorgen), Regale abstauben und Ordnung schaffen.",
            hinweis = "Neues Jahr, aufgeraeumte Schraenke."
        )
    )

    /** Saisonale Vorschlaege fuer den aktuellen Monat (1-12). */
    fun saisonVorschlaege(monat: Int): List<SaisonAufgabe> = when (monat) {
        3, 4, 5 -> fruehjahr
        6, 7, 8 -> sommer
        9, 10, 11 -> herbst
        else -> winter
    }
}
