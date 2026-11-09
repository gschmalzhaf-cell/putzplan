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
import com.putzplan.app.ui.theme.AufPastell
import com.putzplan.app.ui.theme.raumFarbe

@Composable
fun Raumbadge(raum: String, modifier: Modifier = Modifier, dunkel: Boolean = false) {
    val farbe = raumFarbe(raum, dunkel)
    SuggestionChip(
        onClick = {},
        enabled = false,
        modifier = modifier,
        label = { Text(raum, maxLines = 1, overflow = TextOverflow.Ellipsis, color = AufPastell) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            disabledContainerColor = farbe,
            disabledLabelColor = AufPastell
        ),
        border = null
    )
}

@Composable
fun FortschrittsBalken(erledigt: Int, offen: Int, allesErledigt: Boolean, modifier: Modifier = Modifier) {
    val gesamt = erledigt + offen
    val ratio = if (gesamt == 0) 1f else erledigt.toFloat() / gesamt
    val emoji = when {
        gesamt == 0 || allesErledigt -> "🎉"   // alles geschafft
        ratio >= 0.7f -> "😄"                    // fast fertig
        ratio >= 0.3f -> "😺"                    // motiviert
        else -> "😾"                             // gibt Gas
    }
    val text = when {
        gesamt == 0 -> "Noch nichts offen – leg los!"
        allesErledigt -> "Alles erledigt!"
        else -> "$offen Min. offen – weiter so!"
    }
    // 2.0: weisse Fortschrittskarte im Hero mit grossen Zahlen und Balken
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Fortschritt",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$erledigt / $gesamt Aufgaben",
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
                modifier = Modifier.fillMaxWidth().height(10.dp),
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
