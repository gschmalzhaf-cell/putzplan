package com.putzplan.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        gesamt == 0 || allesErledigt -> "\uD83C\uDF89"   // 🎉 alles geschafft
        ratio >= 0.7f -> "\uD83D\uDE04"                    // 😄 fast fertig
        ratio >= 0.3f -> "\uD83D\uDE3A"                    // 😺 motiviert
        else -> "\uD83D\uDE3E"                             // 😾 gibt Gas
    }
    val text = when {
        gesamt == 0 -> "Noch nichts offen \u2013 leg los!"
        allesErledigt -> "Alles erledigt! $emoji"
        else -> "$offen Min. offen \u2013 weiter so! $emoji"
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 28.sp, textAlign = TextAlign.Center, modifier = Modifier.size(36.dp))
            Spacer(Modifier.padding(6.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text, style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
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
fun StatusBanner(titel: String, text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            titel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
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
