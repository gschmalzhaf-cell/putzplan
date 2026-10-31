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

val Sage = Color(0xFF3E6852)
val AufSage = Color(0xFFFFFFFF)
val SageContainer = Color(0xFFCEEAD6)
val AufSageContainer = Color(0xFF06351D)
val Lavendel = Color(0xFF5A5C7C)
val LavendelContainer = Color(0xFFD6D7FD)
val AufLavendelContainer = Color(0xFF161935)
val Koralle = Color(0xFFE06D53)
val KoralleContainer = Color(0xFFFFDAD1)
val AufKoralleContainer = Color(0xFF3C0800)

private val Hell = lightColorScheme(
    primary = Sage,
    onPrimary = AufSage,
    primaryContainer = SageContainer,
    onPrimaryContainer = AufSageContainer,
    secondary = Lavendel,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = LavendelContainer,
    onSecondaryContainer = AufLavendelContainer,
    tertiary = Color(0xFF832814),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD1),
    onTertiaryContainer = Color(0xFF3C0800),
    error = Koralle,
    onError = Color(0xFFFFFFFF),
    errorContainer = KoralleContainer,
    onErrorContainer = AufKoralleContainer,
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF171D18),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF171D18),
    surfaceVariant = Color(0xFFDEE4DC),
    onSurfaceVariant = Color(0xFF414943),
    surfaceDim = Color(0xFFD5DCD3),
    surfaceBright = Color(0xFFF5FBF2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F7F2),
    surfaceContainer = Color(0xFFEEF2EC),
    surfaceContainerHigh = Color(0xFFE7ECE5),
    surfaceContainerHighest = Color(0xFFE1E7DE),
    outline = Color(0xFF717973),
    outlineVariant = Color(0xFFC2C8BC),
    inverseSurface = Color(0xFF2B322C),
    inverseOnSurface = Color(0xFFECF3EA),
    inversePrimary = Color(0xFFA3D1B6)
)

private val Dunkel = darkColorScheme(
    primary = Color(0xFFA3D1B6),
    onPrimary = Color(0xFF002113),
    primaryContainer = Color(0xFF254F3A),
    onPrimaryContainer = Color(0xFFBFEDD1),
    secondary = Color(0xFFC2C4E9),
    onSecondary = Color(0xFF2B2E4B),
    secondaryContainer = Color(0xFF424463),
    onSecondaryContainer = Color(0xFFE0E0FF),
    tertiary = Color(0xFFFFB4A3),
    onTertiary = Color(0xFF561F0E),
    tertiaryContainer = Color(0xFF822714),
    onTertiaryContainer = Color(0xFFFFDAD2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101510),
    onBackground = Color(0xFFDEE4DC),
    surface = Color(0xFF101510),
    onSurface = Color(0xFFDEE4DC),
    surfaceVariant = Color(0xFF414943),
    onSurfaceVariant = Color(0xFFC1C8C1),
    surfaceDim = Color(0xFF101510),
    surfaceBright = Color(0xFF363B35),
    surfaceContainerLowest = Color(0xFF0B0F0B),
    surfaceContainerLow = Color(0xFF191D18),
    surfaceContainer = Color(0xFF1D211C),
    surfaceContainerHigh = Color(0xFF272B26),
    surfaceContainerHighest = Color(0xFF323530),
    outline = Color(0xFF8B928B),
    outlineVariant = Color(0xFF414943),
    inverseSurface = Color(0xFFDEE4DC),
    inverseOnSurface = Color(0xFF2B322C),
    inversePrimary = Color(0xFF3E6852)
)

private val Formen = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PutzplanTheme(dunkel: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dunkel) Dunkel else Hell,
        shapes = Formen,
        content = content
    )
}
