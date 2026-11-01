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

val Creme = Color(0xFFFAF3E7)
val CremeTief = Color(0xFFF3E9D8)
val Tinte = Color(0xFF3A3344)

val Mint = Color(0xFF7FC8A9)
val MintDunkel = Color(0xFF2E6B52)
val Sonnengelb = Color(0xFFF2C94C)
val Koralle = Color(0xFFEB6B4D)
val Lavendel = Color(0xFF9B8CE0)

val Midnight = Color(0xFF241B3A)
val MidnightTief = Color(0xFF2E2149)
val Plum = Color(0xFF6C4FB3)

private val Hell = lightColorScheme(
    primary = MintDunkel,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F0E4),
    onPrimaryContainer = Color(0xFF1E4A38),
    secondary = Plum,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E1FB),
    onSecondaryContainer = Color(0xFF3D2E6B),
    tertiary = Color(0xFFB3541E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4D6),
    onTertiaryContainer = Color(0xFF6B2E0F),
    error = Koralle,
    onError = Color.White,
    errorContainer = Color(0xFFFFDED6),
    onErrorContainer = Color(0xFF7A2A12),
    background = Creme,
    onBackground = Tinte,
    surface = Creme,
    onSurface = Tinte,
    surfaceVariant = CremeTief,
    onSurfaceVariant = Color(0xFF6B5F52),
    surfaceDim = Color(0xFFEADFCE),
    surfaceBright = Color(0xFFFFFBF4),
    surfaceContainerLowest = Color(0xFFFFFDF9),
    surfaceContainerLow = Color(0xFFF8F0E3),
    surfaceContainer = Color(0xFFF3EADB),
    surfaceContainerHigh = Color(0xFFEDE2CF),
    surfaceContainerHighest = Color(0xFFE7DAC5),
    outline = Color(0xFFB3A48E),
    outlineVariant = Color(0xFFD9CCB6),
    inverseSurface = Color(0xFF3A3344),
    inverseOnSurface = Creme,
    inversePrimary = Mint
)

private val Dunkel = darkColorScheme(
    primary = Color(0xFFA8D8C3),
    onPrimary = Color(0xFF14342A),
    primaryContainer = Color(0xFF2E6B52),
    onPrimaryContainer = Color(0xFFD9F0E4),
    secondary = Color(0xFFB9A7F0),
    onSecondary = Color(0xFF2A1F4D),
    secondaryContainer = Plum,
    onSecondaryContainer = Color(0xFFE9E1FB),
    tertiary = Color(0xFFFFB48F),
    onTertiary = Color(0xFF5A2410),
    tertiaryContainer = Color(0xFF7A3A1B),
    onTertiaryContainer = Color(0xFFFFE4D6),
    error = Color(0xFFFF9E85),
    onError = Color(0xFF5A1608),
    errorContainer = Color(0xFF8C3A22),
    onErrorContainer = Color(0xFFFFDED6),
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
    inversePrimary = MintDunkel
)

private val Formen = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
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
