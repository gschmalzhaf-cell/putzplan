package com.putzplan.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ---- Putzplan 2.0 — eigene Design-Sprache (frei von Material-3-Expressive-Vorgaben) ----

val Tinte = Color(0xFF2B2B33)      // AufPastell-Tinte für Text auf Pastell
val TinteSanft = Color(0xFF6E6E78)  // Sekundärtext
val Flaeche = Color(0xFFF2F5F1)     // warmes Off-White als Basis
val Linie = Color(0xFFE6E6EC)

val Mint = Color(0xFF4E9B72)       // Primär
val MintHell = Color(0xFFDFF3E8)
val Aqua = Color(0xFF4FB3A6)
val Himmel = Color(0xFF5B93D6)     // Sekundär
val HimmelHell = Color(0xFFE1EEFB)
val Lavendel = Color(0xFF9A8AE0)
val LavendelHell = Color(0xFFEEE9FB)
val Sonnen = Color(0xFFF5B841)     // Tertiär (Pause)
val SonnenHell = Color(0xFFFFF1D0)
val Koralle = Color(0xFFE06D6D)    // Fehler
val KoralleHell = Color(0xFFFFE3E3)

val Midnight = Color(0xFF241B3A)
val MidnightTief = Color(0xFF2E2149)
val Plum = Color(0xFF6C4FB3)

private val Hell = lightColorScheme(
    primary = Mint,
    onPrimary = Color.White,
    primaryContainer = MintHell,
    onPrimaryContainer = Color(0xFF1E4A38),
    secondary = Himmel,
    onSecondary = Color.White,
    secondaryContainer = HimmelHell,
    onSecondaryContainer = Color(0xFF1F4C77),
    tertiary = Sonnen,
    onTertiary = Tinte,
    tertiaryContainer = SonnenHell,
    onTertiaryContainer = Color(0xFF6B4E12),
    error = Koralle,
    onError = Color.White,
    errorContainer = KoralleHell,
    onErrorContainer = Color(0xFF7A2424),
    background = Flaeche,
    onBackground = Tinte,
    surface = Flaeche,
    onSurface = Tinte,
    surfaceVariant = Color(0xFFE9EDE9),
    onSurfaceVariant = TinteSanft,
    surfaceDim = Color(0xFFE3E7E3),
    surfaceBright = Color(0xFFFCFDFC),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAF7),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFE9EDE9),
    surfaceContainerHighest = Linie,
    outline = Color(0xFFC9CDD4),
    outlineVariant = Linie,
    inverseSurface = Tinte,
    inverseOnSurface = Flaeche,
    inversePrimary = Color(0xFFA8D8C3)
)

private val Dunkel = darkColorScheme(
    primary = Color(0xFFA8D8C3),
    onPrimary = Color(0xFF14342A),
    primaryContainer = Color(0xFF2E6B52),
    onPrimaryContainer = MintHell,
    secondary = Color(0xFFA9C8EE),
    onSecondary = Color(0xFF1F4C77),
    secondaryContainer = Color(0xFF3A6AA5),
    onSecondaryContainer = HimmelHell,
    tertiary = Color(0xFFF8D58A),
    onTertiary = Color(0xFF4A3410),
    tertiaryContainer = Color(0xFF8A6A24),
    onTertiaryContainer = SonnenHell,
    error = Color(0xFFFF9E85),
    onError = Color(0xFF5A1608),
    errorContainer = Color(0xFF8C3A22),
    onErrorContainer = KoralleHell,
    background = Midnight,
    onBackground = Color(0xFFEDE7F7),
    surface = Midnight,
    onSurface = Color(0xFFEDE7F7),
    surfaceVariant = MidnightTief,
    onSurfaceVariant = Color(0xFFB9AECB),
    surfaceDim = Midnight,
    surfaceBright = Color(0xFF3A2D5C),
    surfaceContainerLowest = Color(0xFF1B1330),
    surfaceContainerLow = Color(0xFF2A2046),
    surfaceContainer = MidnightTief,
    surfaceContainerHigh = Color(0xFF372A58),
    surfaceContainerHighest = Color(0xFF423468),
    outline = Color(0xFF6B5E8C),
    outlineVariant = Color(0xFF453A66),
    inverseSurface = Color(0xFFEDE7F7),
    inverseOnSurface = Color(0xFF241B3A),
    inversePrimary = Mint
)

private val Formen = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(20.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun PutzplanTheme(dunkel: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dunkel) Dunkel else Hell,
        shapes = Formen,
        content = content
    )
}
