package com.putzplan.app.logic

/**
 * Offline-Assistent für das schnelle Anlegen neuer Aufgaben.
 * Erkennt gängige Haushaltsaufgaben anhand von Schlüsselwörtern im Titel
 * und schlägt realistische Werte für Raum, Intervall, Dauer, Hilfsmittel
 * und Schritt-für-Schritt-Anleitung vor.
 */
object AufgabenAssistent {

    data class AufgabenVorlage(
        val stichwoerter: List<String>,
        val raum: String,
        val intervall: String,
        val dauerMin: Int,
        val hilfsmittel: List<String>,
        val anleitung: String
    )

    private val vorlagen = listOf(
        AufgabenVorlage(
            stichwoerter = listOf("staubsaugen", "saugen", "staubsauger"),
            raum = "Wohnen",
            intervall = "woechentlich",
            dauerMin = 20,
            hilfsmittel = listOf("Staubsauger", "Fugendüse"),
            anleitung = "Erst Ecken, Fußleisten und unter Möbeln saugen, dann die Hauptflächen in ruhigen Bahnen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("wischen", "boden", "feucht wischen", "mopp"),
            raum = "Küche",
            intervall = "woechentlich",
            dauerMin = 20,
            hilfsmittel = listOf("Wischmopp", "Eimer", "Bodenreiniger"),
            anleitung = "Boden vorab saugen oder fegen, dann nebelfeucht in S-Bahnen von hinten nach vorn zur Tür wischen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("dusche", "badewanne", "duschkabine"),
            raum = "Bad",
            intervall = "woechentlich",
            dauerMin = 15,
            hilfsmittel = listOf("Badreiniger", "Schwamm", "Abzieher", "Mikrofasertuch"),
            anleitung = "Armaturen und Wände einsprühen, kurz einwirken lassen, mit Schwamm reinigen, klar abspülen und trocken abziehen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("wc", "toilette", "klo"),
            raum = "Bad",
            intervall = "woechentlich",
            dauerMin = 10,
            hilfsmittel = listOf("WC-Reiniger", "Toilettenbürste", "Feuchtes Tuch"),
            anleitung = "WC-Reiniger unter den Rand geben, einwirken lassen, schrubben und danach Brille, Deckel und Spülkasten abwischen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("waschbecken", "armatur"),
            raum = "Bad",
            intervall = "woechentlich",
            dauerMin = 5,
            hilfsmittel = listOf("Badreiniger", "Mikrofasertuch"),
            anleitung = "Becken und Armatur abwischen, Kalkreste am Auslauf entfernen und trocken nachpolieren."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("spiegel", "glas"),
            raum = "Bad",
            intervall = "woechentlich",
            dauerMin = 5,
            hilfsmittel = listOf("Glasreiniger", "Mikrofasertuch"),
            anleitung = "Sparsam einsprühen und mit sauberem Mikrofasertuch streifenfrei von oben nach unten polieren."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("küche", "arbeitsfläche", "arbeitsplatte", "spüle"),
            raum = "Küche",
            intervall = "taeglich",
            dauerMin = 10,
            hilfsmittel = listOf("Allzweckreiniger", "Schwamm", "Geschirrtuch"),
            anleitung = "Krümel entfernen, Arbeitsfläche und Spüle feucht abwischen und Armatur abtrocknen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("herd", "kochfeld", "ceran", "induktion"),
            raum = "Küche",
            intervall = "woechentlich",
            dauerMin = 10,
            hilfsmittel = listOf("Fettlöser", "Ceranfeldschaber", "Mikrofasertuch"),
            anleitung = "Eingebranntes vorsichtig lösen, Reiniger auftragen, feucht nachwischen und trocken polieren."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("backofen", "ofen"),
            raum = "Küche",
            intervall = "monatlich",
            dauerMin = 30,
            hilfsmittel = listOf("Backofenreiniger", "Schwamm", "Küchenpapier"),
            anleitung = "Bleche herausnehmen, Innenraum und Glas einsprühen, einwirken lassen und gründlich feucht auswischen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("kühlschrank", "gefrierfach"),
            raum = "Küche",
            intervall = "monatlich",
            dauerMin = 25,
            hilfsmittel = listOf("Allzweckreiniger", "Schwamm", "Trockentuch"),
            anleitung = "Abgelaufenes aussortieren, Fächer mit warmem Wasser und etwas Reiniger auswischen, Dichtungen prüfen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("mikrowelle"),
            raum = "Küche",
            intervall = "zweiwoechentlich",
            dauerMin = 10,
            hilfsmittel = listOf("Schüssel mit Wasser & Zitrone", "Mikrofasertuch"),
            anleitung = "Schüssel mit Wasser und Zitronenscheibe 3 Min. erhitzen, Dampf kurz wirken lassen und innen mühelos auswischen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("spülmaschine", "geschirrspüler"),
            raum = "Küche",
            intervall = "monatlich",
            dauerMin = 10,
            hilfsmittel = listOf("Maschinenreiniger", "Schwamm"),
            anleitung = "Sieb herausnehmen und ausspülen, Türdichtungen abwischen und ggf. heißes Reinigungsprogramm starten."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("müll", "altpapier", "pfand", "gelber sack"),
            raum = "Küche",
            intervall = "woechentlich",
            dauerMin = 5,
            hilfsmittel = listOf("Müllbeutel"),
            anleitung = "Beutel zubinden, rausbringen, Eimer bei Bedarf kurz auswischen und neuen Beutel einsetzen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("bett", "bettwäsche", "beziehen"),
            raum = "Schlafen",
            intervall = "zweiwoechentlich",
            dauerMin = 15,
            hilfsmittel = listOf("Frische Bettwäsche"),
            anleitung = "Alte Bezüge abziehen, Matratze kurz lüften und frisches Laken sowie Kissen- und Deckenbezüge aufziehen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("staub", "abstauben", "regal", "kommode", "tv", "fernseher"),
            raum = "Wohnen",
            intervall = "woechentlich",
            dauerMin = 15,
            hilfsmittel = listOf("Staubtuch", "Mikrofasertuch"),
            anleitung = "Immer von oben nach unten abstauben (Regale, Lampen, Fensterbänke), Elektronik nur trocken abwischen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("fenster", "fensterscheibe"),
            raum = "Wohnen",
            intervall = "quartal",
            dauerMin = 40,
            hilfsmittel = listOf("Glasreiniger", "Fensterabzieher", "Mikrofasertuch"),
            anleitung = "Zuerst Rahmen und Fensterbank feucht abwischen, danach Scheiben reinigen und streifenfrei abziehen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("waschmaschine", "wäsche", "trockner"),
            raum = "Bad",
            intervall = "monatlich",
            dauerMin = 10,
            hilfsmittel = listOf("Feuchtes Tuch", "Waschmaschinenreiniger"),
            anleitung = "Einspülfach ausspülen, Gummidichtung auswischen, Flusensieb prüfen und Tür zum Trocknen offen lassen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("pflanzen", "gießen", "blumen"),
            raum = "Wohnen",
            intervall = "woechentlich",
            dauerMin = 10,
            hilfsmittel = listOf("Gießkanne"),
            anleitung = "Erde mit dem Finger prüfen, bedarfsgerecht gießen und vertrocknete Blätter entfernen."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("schreibtisch", "büro", "tastatur", "monitor"),
            raum = "Arbeitszimmer",
            intervall = "woechentlich",
            dauerMin = 10,
            hilfsmittel = listOf("Mikrofasertuch", "Bildschirmreiniger"),
            anleitung = "Unterlagen sortieren, Tischfläche abwischen und Tastatur sowie Monitor vorsichtig entstauben."
        ),
        AufgabenVorlage(
            stichwoerter = listOf("flur", "schuhe", "eingang", "fußmatte"),
            raum = "Flur",
            intervall = "woechentlich",
            dauerMin = 10,
            hilfsmittel = listOf("Staubsauger", "Feuchtes Tuch"),
            anleitung = "Schuhe ordentlich ausrichten, Fußmatte ausschütteln und Eingangsbereich saugen oder wischen."
        )
    )

    /**
     * Sucht anhand des eingegebenen Titels nach einer passenden Vorlage.
     * Gibt null zurück, wenn weniger als 3 Zeichen eingegeben wurden oder kein Treffer vorliegt.
     */
    fun vorschlagFuer(titel: String): AufgabenVorlage? {
        val norm = titel.trim().lowercase()
        if (norm.length < 3) return null
        return vorlagen.firstOrNull { vorlage ->
            vorlage.stichwoerter.any { kw -> norm.contains(kw) || (norm.length >= 4 && kw.startsWith(norm)) }
        }
    }
}
