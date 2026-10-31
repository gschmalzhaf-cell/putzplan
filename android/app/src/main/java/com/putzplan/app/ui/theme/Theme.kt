package com.putzplan.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primär = Color(0xFF0E7C66)
val AufPrimär = Color(0xFFFFFFFF)

private val Hell = lightColorScheme(
    primary = Primär,
    onPrimary = AufPrimär
)

private val Dunkel = darkColorScheme(
    primary = Color(0xFF6FD7C0),
    onPrimary = Color(0xFF00332A)
)

@Composable
fun PutzplanTheme(dunkel: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dunkel) Dunkel else Hell,
        content = content
    )
}
