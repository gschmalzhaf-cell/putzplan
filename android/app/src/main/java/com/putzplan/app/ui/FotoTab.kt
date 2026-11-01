package com.putzplan.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveFileRenameOutline
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
    val uebernommen by vm.uebernommeneFotoTitel.collectAsState()
    var raum by remember { mutableStateOf("") }

    val fotos by vm.ausgewaehlteFotos.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        kotlinx.coroutines.MainScope().launch {
            uris.take(8).forEach { uri ->
                val bytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                }
                if (bytes != null && !vm.fotoHinzufuegen(bytes)) {
                    android.widget.Toast.makeText(
                        context, "Bild nicht lesbar oder Limit (8) erreicht", android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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
        RaumDropdown(raeume, raum, onChange = { raum = it }, onRename = { alt, neu -> vm.raumUmbenennen(alt, neu) })
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Fotos auswählen / aufnehmen (max. 8)")
        }
        if (fotos.isNotEmpty()) {
            Text("${fotos.size} Foto(s) ausgew\u00e4hlt", style = MaterialTheme.typography.labelLarge)
            androidx.compose.foundation.layout.FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fotos.forEachIndexed { index, bytes ->
                    val bmp = remember(bytes) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                    androidx.compose.foundation.layout.Box {
                        if (bmp != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bmp,
                                contentDescription = "Foto ${index + 1}",
                                modifier = Modifier.size(96.dp)
                            )
                        }
                        androidx.compose.material3.IconButton(
                            onClick = { vm.fotoEntfernen(index) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Foto ${index + 1} entfernen",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (raum.isNotBlank()) vm.fotoAnalysierenAlle(raum, fotos) },
                    enabled = raum.isNotBlank() && analyse !is FotoAnalyseUi.Laeuft,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Alle ${fotos.size} Fotos analysieren")
                }
                TextButton(onClick = { vm.fotosLeeren() }) { Text("Leeren") }
            }
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
                        val erkannt = a.analyse.raum.ifBlank { a.raum }
                        Text(
                            "Erkannter Raum: $erkannt",
                            style = MaterialTheme.typography.titleMedium
                        )
                        val zielRaum = raum.ifBlank { "Sonstiges" }
                        if (erkannt.isNotBlank() && !erkannt.equals(zielRaum, ignoreCase = true)
                            && raeume.any { it.equals(zielRaum, ignoreCase = true) }) {
                            Text(
                                "Mistral hat \u201e$erkannt\u201c erkannt statt \u201e$zielRaum\u201c.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(onClick = { vm.raumUmbenennen(zielRaum, erkannt) }) {
                                Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null)
                                Spacer(Modifier.padding(2.dp))
                                Text("Raum \u201e$zielRaum\u201c in \u201e$erkannt\u201c umbenennen")
                            }
                        }
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
                        if (a.analyse.veraenderungen.isNotEmpty()) {
                            Text("Ver\u00e4nderungen zum letzten Scan", style = MaterialTheme.typography.titleSmall)
                            a.analyse.veraenderungen.forEach { v ->
                                val label = when (v.typ) {
                                    "hinzugefuegt" -> "Neu hinzugekommen"
                                    "entfernt" -> "Weggefallen"
                                    "veraendert" -> "Ver\u00e4ndert"
                                    else -> v.typ
                                }
                                val farbe = when (v.typ) {
                                    "hinzugefuegt" -> MaterialTheme.colorScheme.primary
                                    "entfernt" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                                Text(
                                    "\u2022 [$label] ${v.objekt}" +
                                        (if (v.bemerkung.isNotBlank()) " \u2013 ${v.bemerkung}" else ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = farbe
                                )
                            }
                        } else {
                            Text(
                                "Keine Ver\u00e4nderungen zum letzten Scan erkannt.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                            val bereits = aufgabe.titel in uebernommen
                            FilledTonalButton(
                                onClick = {
                                    vm.fotoAufgabeUebernehmen(
                                        aufgabe,
                                        a.analyse.raum.ifBlank { a.raum }.ifBlank { "Sonstiges" }
                                    )
                                },
                                enabled = !bereits
                            ) {
                                Icon(
                                    if (bereits) Icons.Default.Check else Icons.Default.AddTask,
                                    contentDescription = null
                                )
                                Spacer(Modifier.padding(2.dp))
                                Text(if (bereits) "Im Plan übernommen" else "In Plan übernehmen")
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
