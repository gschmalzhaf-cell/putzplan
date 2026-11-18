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
            anleitung = "Fenster von innen und außen reinigen: erst Rahmen und Fensterbank feucht abwischen, dann Scheibe mit Glasreiniger und Abzieher in Zickzack-Bahnen trocknen.",
            hinweis = "Frühling – jetzt sind die Fenster dran."
        ),
        SaisonAufgabe(
            titel = "Balkon oder Terrasse wischen", raum = "Balkon", intervall = "quartal", dauerMin = 20,
            hilfsmittel = listOf("Besen", "Eimer", "Allzweckreiniger"),
            anleitung = "Balkon abkehren, dann Boden und Geländer feucht abwischen, Moos und Vogelkot mit Allzweckreiniger lösen.",
            hinweis = "Der Frühling beginnt auf dem Balkon."
        )
    )

    private val sommer = listOf(
        SaisonAufgabe(
            titel = "Rollläden und Jalousien abstauben", raum = "Wohnen", intervall = "halbjaehrlich", dauerMin = 15,
            hilfsmittel = listOf("Staubtuch", "Federbesen"),
            anleitung = "Rollläden ganz runterlassen, mit Federbesen oder feuchtem Tuch Lamelle für Lamelle abnehmen.",
            hinweis = "Mehr Licht im Sommer – saubere Rollläden helfen."
        ),
        SaisonAufgabe(
            titel = "Kühlschrank grundlegend reinigen", raum = "Küche", intervall = "quartal", dauerMin = 30,
            hilfsmittel = listOf("Allzweckreiniger", "Schwamm", "Kühltasche"),
            anleitung = "Kühlschrank ausräumen, abtauen lassen, innen mit warmem Wasser und Reiniger auswischen, Dichtungen nicht vergessen.",
            hinweis = "Im Sommer wird der Kühlschrank richtig gebraucht."
        )
    )

    private val herbst = listOf(
        SaisonAufgabe(
            titel = "Teppiche grundlegend reinigen", raum = "Wohnen", intervall = "jaehrlich", dauerMin = 40,
            hilfsmittel = listOf("Teppichklopfer", "Staubsauger", "Teppichschaum"),
            anleitung = "Teppich draußen ausklopfen, gründlich absaugen, Flecken mit Teppichschaum behandeln und trocknen lassen.",
            hinweis = "Herbst – die Teppiche kommen bald wieder zum Einsatz."
        ),
        SaisonAufgabe(
            titel = "Heizkörper entstauben", raum = "Bad", intervall = "halbjaehrlich", dauerMin = 15,
            hilfsmittel = listOf("Heizkörperbürste", "Feuchtes Tuch"),
            anleitung = "Heizkörper ausschalten, Zwischenräume mit Heizkörperbürste auskehren, Außenflächen feucht abwischen.",
            hinweis = "Vor der Heizsaison die Heizkörper entstauben."
        )
    )

    private val winter = listOf(
        SaisonAufgabe(
            titel = "Vorratsschrank ausmisten", raum = "Küche", intervall = "quartal", dauerMin = 20,
            hilfsmittel = listOf("Feuchtes Tuch", "Mülleimer"),
            anleitung = "Vorräte rausnehmen, Abgelaufenes entsorgen, Regale auswischen und alles sortiert zurückstellen.",
            hinweis = "Winter-Vorrat checken und ausmisten."
        ),
        SaisonAufgabe(
            titel = "Schränke ausmisten", raum = "Schlafen", intervall = "jaehrlich", dauerMin = 30,
            hilfsmittel = listOf("Staubtuch", "Mülleimer", "Spendenbeutel"),
            anleitung = "Kleidung rausnehmen, aussortieren (spenden/entsorgen), Regale abstauben und Ordnung schaffen.",
            hinweis = "Neues Jahr, aufgeräumte Schränke."
        )
    )

    /** Saisonale Vorschläge für den aktuellen Monat (1-12). */
    fun saisonVorschlaege(monat: Int): List<SaisonAufgabe> = when (monat) {
        3, 4, 5 -> fruehjahr
        6, 7, 8 -> sommer
        9, 10, 11 -> herbst
        else -> winter
    }
}
