package com.putzplan.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.putzplan.app.logic.Konstanten

@Composable
fun FotoTab(vm: PutzplanViewModel) {
    val context = LocalContext.current
    val analyse by vm.fotoAnalyse.collectAsState()
    var raum by remember { mutableStateOf("Ganze Wohnung") }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) vm.fotoAnalysieren(raum, bytes)
    }

    Column(
        Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Foto aus der Wohnung analysieren", style = MaterialTheme.typography.titleLarge)
        Text(
            "Foto eines Raums aufnehmen. Mistral Vision erkennt Objekte und Verschmutzungen " +
                "und leitet daraus Aufgaben mit Zeitschätzung, Hilfsmitteln und Priorität ab.",
            style = MaterialTheme.typography.bodyMedium
        )
        RaumDropdown(raum) { raum = it }
        Button(onClick = { launcher.launch("image/*") }) {
            Text("📷 Foto auswählen / aufnehmen")
        }

        when (val a = analyse) {
            is FotoAnalyseUi.Laeuft -> {
                CircularProgressIndicator()
                Text("Analysiere Foto …")
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
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Erkannter Raum: ${a.analyse.raum.ifBlank { a.raum }}")
                        if (a.analyse.beobachtungen.isNotEmpty()) {
                            Text("Beobachtungen", style = MaterialTheme.typography.titleSmall)
                            a.analyse.beobachtungen.forEach { Text("• $it") }
                        }
                        if (a.analyse.aufgaben.isNotEmpty()) {
                            Text("Vorgeschlagene Aufgaben", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
                a.analyse.aufgaben.forEach { aufgabe ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text(aufgabe.titel, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${aufgabe.bereich} · ⏱ ${aufgabe.dauerMin} Min. · " +
                                    "${Konstanten.intervallLabel(aufgabe.intervall)} · " +
                                    "Priorität ${(aufgabe.prioritaet * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (aufgabe.hilfsmittel.isNotEmpty()) {
                                Text("Hilfsmittel: ${aufgabe.hilfsmittel.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            if (aufgabe.anleitung.isNotBlank()) {
                                Text(aufgabe.anleitung, style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { vm.fotoAufgabeUebernehmen(aufgabe, a.raum) }) {
                                Text("In Plan übernehmen")
                            }
                        }
                    }
                }
            }
            FotoAnalyseUi.Idle -> {}
        }
    }
}
