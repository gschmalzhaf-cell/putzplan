package com.putzplan.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.putzplan.app.ui.theme.AufPastell
import com.putzplan.app.ui.theme.raumFarbe

@Composable
fun Raumbadge(
    raum: String,
    modifier: Modifier = Modifier,
    dunkel: Boolean = isSystemInDarkTheme()
) {
    val farbe = raumFarbe(raum, dunkel)
    val textFarbe = if (dunkel) Color.White else AufPastell
    SuggestionChip(
        onClick = {},
        enabled = false,
        modifier = modifier,
        label = { Text(raum, maxLines = 1, overflow = TextOverflow.Ellipsis, color = textFarbe) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            disabledContainerColor = farbe,
            disabledLabelColor = textFarbe
        ),
        border = null
    )
}

@Composable
fun FortschrittsBalken(
    erledigtWoche: Int,
    offenAufgaben: Int,
    offenMinuten: Int,
    allesErledigt: Boolean,
    modifier: Modifier = Modifier
) {
    val gesamt = erledigtWoche + offenAufgaben
    val ratio = when {
        allesErledigt -> 1f
        gesamt == 0 -> 0f
        else -> (erledigtWoche.toFloat() / gesamt).coerceIn(0f, 1f)
    }
    val emoji = when {
        allesErledigt -> "🎉"
        gesamt == 0 -> "✨"
        ratio >= 0.7f -> "😄"
        ratio >= 0.3f -> "😺"
        else -> "💪"
    }
    val text = when {
        allesErledigt -> "Alle fälligen Aufgaben erledigt!"
        gesamt == 0 -> "Noch keine Aufgaben offen – leg los!"
        offenMinuten > 0 -> "Noch $offenAufgaben offen (~$offenMinuten Min.) – weiter so!"
        else -> "Alles im grünen Bereich!"
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Wochenfortschritt",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$erledigtWoche / $gesamt Aufgaben",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    "${(ratio * 100).toInt()} %",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            Text(
                "$text $emoji",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 2.3 - Streak-Intelligenz: Badge, der mit genau einer weiteren Aufgabe
 * erreicht wird (fuer die "Noch 1 Aufgabe bis ..."-Hinweiskarte).
 */
fun naechsterBadge(gesamtErledigt: Int): Pair<String, String>? {
    val schwellen = listOf(1 to "Erster Putz", 7 to "Blitz-Woche", 14 to "Fenster-Profi", 30 to "Monats-Champion", 100 to "Putz-Legende")
    val treffer = schwellen.firstOrNull { it.first == gesamtErledigt + 1 }
    return treffer?.let { "\uD83C\uDFC1" to it.second }
}

fun streakBadges(gesamtErledigt: Int): List<Pair<String, String>> {
    val liste = mutableListOf<Pair<String, String>>()
    if (gesamtErledigt >= 1) liste += "\uD83C\uDF31" to "Erster Putz"
    if (gesamtErledigt >= 7) liste += "\u26A1" to "Blitz-Woche"
    if (gesamtErledigt >= 14) liste += "\uD83E\uDDFD" to "Fenster-Profi"
    if (gesamtErledigt >= 30) liste += "\uD83C\uDFC6" to "Monats-Champion"
    if (gesamtErledigt >= 100) liste += "\uD83D\uDC51" to "Putz-Legende"
    return liste
}

@Composable
fun DringlichkeitBadge(text: String) {
    SuggestionChip(
        onClick = {},
        enabled = false,
        label = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
            disabledLabelColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        border = null
    )
}

@Composable
fun StatKachel(wert: String, label: String) {
    Column(
        Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            wert,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
