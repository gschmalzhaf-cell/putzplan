package com.putzplan.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.putzplan.app.logic.Konstanten

@Composable
fun FotoTab(vm: PutzplanViewModel) {
    val context = LocalContext.current
    val analyse by vm.fotoAnalyse.collectAsState()
    val raeume by vm.raeume.collectAsState()
    var raum by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null && raum.isNotBlank()) vm.fotoAnalysieren(raum, bytes)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Text("Foto aus der Wohnung analysieren", style = MaterialTheme.typography.titleLarge)
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        "Intelligente Vision-Erkennung",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    "Foto eines Raums aufnehmen. Mistral Vision erkennt Objekte und " +
                        "Verschmutzungen und leitet daraus Aufgaben mit Zeitschätzung, " +
                        "Hilfsmitteln und Priorität ab.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text("Zielbereich", style = MaterialTheme.typography.labelLarge)
        RaumDropdown(raeume, raum) { raum = it }
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Foto auswählen / aufnehmen")
        }

        when (val a = analyse) {
            is FotoAnalyseUi.Laeuft -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.padding(8.dp))
                    Text("Analysiere Foto …")
                }
            }
            is FotoAnalyseUi.Fehler -> {
                Text(
                    "Fehler: ${a.meldung}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { vm.fotoAnalyseZuruecksetzen() }) { Text("Erneut versuchen") }
            }
            is FotoAnalyseUi.Ergebnis -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Erkannter Raum: ${a.analyse.raum.ifBlank { a.raum }}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (a.analyse.beobachtungen.isNotEmpty()) {
                            Text("Beobachtungen", style = MaterialTheme.typography.titleSmall)
                            a.analyse.beobachtungen.forEach {
                                Text(
                                    "• $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (a.analyse.aufgaben.isNotEmpty()) {
                            Text("Vorgeschlagene Aufgaben", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
                a.analyse.aufgaben.forEach { aufgabe ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(aufgabe.titel, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${aufgabe.bereich} · ${aufgabe.dauerMin} Min. · " +
                                    "${Konstanten.intervallLabel(aufgabe.intervall)} · " +
                                    "Priorität ${(aufgabe.prioritaet * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (aufgabe.hilfsmittel.isNotEmpty()) {
                                Text(
                                    "Hilfsmittel: ${aufgabe.hilfsmittel.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (aufgabe.anleitung.isNotBlank()) {
                                Text(aufgabe.anleitung, style = MaterialTheme.typography.bodySmall)
                            }
                            FilledTonalButton(onClick = {
                                vm.fotoAufgabeUebernehmen(
                                    aufgabe,
                                    a.analyse.raum.ifBlank { a.raum }.ifBlank { "Sonstiges" }
                                )
                            }) {
                                Icon(Icons.Default.AddTask, contentDescription = null)
                                Spacer(Modifier.padding(2.dp))
                                Text("In Plan übernehmen")
                            }
                        }
                    }
                }
            }
            FotoAnalyseUi.Idle -> {}
        }
        Spacer(Modifier.height(16.dp))
    }
}
