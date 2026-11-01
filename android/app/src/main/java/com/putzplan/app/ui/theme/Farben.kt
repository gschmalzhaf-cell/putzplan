package com.putzplan.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

val PastellPalette = listOf(
    Color(0xFFB8E6C9), // Mint
    Color(0xFFFFE29A), // Sonnengelb
    Color(0xFFAED9FF), // Himmelblau
    Color(0xFFFFC4D6), // Rosa
    Color(0xFFD9CCFF), // Lavendel
    Color(0xFFFFC9A3), // Aprikose
    Color(0xFFB5E8E0), // Aqua
    Color(0xFFFFB3B3)  // Koralle hell
)

val PastellDunkelPalette = listOf(
    Color(0xFF4E9B72), // Mint dunkel
    Color(0xFFC79A2E), // Sonnengelb dunkel
    Color(0xFF5B93D6), // Himmelblau dunkel
    Color(0xFFD67BA0), // Rosa dunkel
    Color(0xFF9A8AE0), // Lavendel dunkel
    Color(0xFFD6894F), // Aprikose dunkel
    Color(0xFF4FB3A6), // Aqua dunkel
    Color(0xFFE06D6D)  // Koralle dunkel
)

fun raumFarbe(raum: String, dunkel: Boolean = false): Color {
    val idx = abs(raum.hashCode()) % PastellPalette.size
    return if (dunkel) PastellDunkelPalette[idx] else PastellPalette[idx]
}

val AufPastell = Color(0xFF2B2B33)
