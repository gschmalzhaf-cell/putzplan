package com.putzplan.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VerlaufTab(vm: PutzplanViewModel) {
    val history by vm.history.collectAsState()
    val stats by vm.stats.collectAsState()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Verlauf & Statistik", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        stats?.let { s ->
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Gesamt erledigt: ${s.gesamtErledigt}")
                    Text("Aufgaben im Plan: ${s.taskCount}")
                    Text("Foto-Analysen: ${s.fotoErkenntnisse}")
                    if (s.proRaum.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        s.proRaum.forEach { (raum, n) -> Text("$raum: $n×") }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Letzte Aktionen", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(history, key = { "${it.titel}-${it.zeitpunkt}" }) { h ->
                Card {
                    Column(Modifier.padding(10.dp)) {
                        Text(
                            if (h.aktion == "erledigt") "✔ ${h.titel}" else "↷ ${h.titel} übersprungen",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val zeit = SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMAN)
                            .format(Date(h.zeitpunkt))
                        Text(
                            "${h.raum} · $zeit" + (h.dauerMin?.let { " · $it Min." } ?: ""),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
