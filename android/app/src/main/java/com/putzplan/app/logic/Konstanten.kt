package com.putzplan.app.logic

object Konstanten {
    data class Intervall(val key: String, val label: String, val days: Int)

    val INTERVALLE: List<Intervall> = listOf(
        Intervall("taeglich", "Täglich", 1),
        Intervall("woechentlich", "Wöchentlich", 7),
        Intervall("zweiwoechentlich", "Alle 2 Wochen", 14),
        Intervall("monatlich", "Monatlich", 30),
        Intervall("quartal", "Vierteljährlich", 91),
        Intervall("halbjaehrlich", "Halbjährlich", 182),
        Intervall("jaehrlich", "Jährlich", 365)
    )

    val INTERVALL_MAP: Map<String, Intervall> = INTERVALLE.associateBy { it.key }

    fun intervallLabel(key: String): String =
        INTERVALL_MAP[key]?.label ?: key

    fun istGueltigesIntervall(key: String): Boolean = INTERVALL_MAP.containsKey(key)
}
