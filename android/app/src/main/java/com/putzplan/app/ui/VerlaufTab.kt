package com.putzplan.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VerlaufTab(vm: PutzplanViewModel) {
    val history by vm.history.collectAsState()
    val stats by vm.stats.collectAsState()
    val dunkel = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Verlauf & Statistik",
                style = MaterialTheme.typography.titleLarge
            )
        }
        stats?.let { s ->
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "${s.gesamtErledigt}",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Column {
                                    Text(
                                        "Aufgaben erledigt 🎉",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Wochenziel: ${s.erledigtDieseWoche} von " +
                                            "${maxOf(s.erledigtDieseWoche, s.geplantDieseWoche)} " +
                                            "Aufgaben diese Woche",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (s.proRaum.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                s.proRaum.entries.sortedByDescending { it.value }.forEach { (raum, anzahl) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Raumbadge(raum, dunkel = dunkel)
                                        Text(
                                            "${anzahl}×",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        val badges = streakBadges(s.gesamtErledigt)
                        if (badges.isNotEmpty()) {
                            Text(
                                "Erfolge",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                badges.forEach { (emoji, name) ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(emoji, fontSize = 18.sp)
                                            Text(name, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) { StatKachel(s.gesamtErledigt.toString(), "Erledigt") }
                    Card(
                        Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) { StatKachel(s.taskCount.toString(), "Im Plan") }
                    Card(
                        Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) { StatKachel(s.fotoErkenntnisse.toString(), "Foto-Scans") }
                }
            }
        }
        item {
            Text(
                "Letzte Aktionen",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (history.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Noch keine Aktionen im Verlauf. Sobald du Aufgaben erledigst oder überspringst, erscheinen sie hier.",
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        items(history, key = { it.id }) { h ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (h.aktion) {
                        "erledigt" -> Icons.Default.TaskAlt
                        "foto" -> Icons.Default.PhotoCamera
                        "intervall-angepasst" -> Icons.Default.AutoFixHigh
                        else -> Icons.Default.SkipNext
                    }
                    val iconTint = when (h.aktion) {
                        "erledigt" -> MaterialTheme.colorScheme.primary
                        "foto", "intervall-angepasst" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val aktionLabel = when (h.aktion) {
                        "erledigt" -> "Erledigt"
                        "uebersprungen" -> "Später"
                        "intervall-angepasst" -> "Intervall gelernt"
                        "foto" -> "Foto-Scan"
                        else -> h.aktion
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = aktionLabel,
                        tint = iconTint
                    )
                    Spacer(Modifier.padding(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            h.titel,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val zeit = SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMAN)
                            .format(Date(h.zeitpunkt))
                        Text(
                            "${h.raum} · $aktionLabel · $zeit" +
                                (h.dauerMin?.let { " · $it Min." } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { vm.historyLoeschen(h.id) }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Eintrag löschen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
