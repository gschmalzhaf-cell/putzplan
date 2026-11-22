package com.putzplan.app.ui

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.putzplan.app.logic.Konstanten
import com.putzplan.app.ui.theme.raumFarbe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FotoTab(vm: PutzplanViewModel) {
    val context = LocalContext.current
    val analyse by vm.fotoAnalyse.collectAsState()
    val raeume by vm.raeume.collectAsState()
    val uebernommen by vm.uebernommeneFotoTitel.collectAsState()
    val vorhandeneTitel by vm.taskTitel.collectAsState()
    val dunkel = isSystemInDarkTheme()
    var raum by remember { mutableStateOf("") }

    val fotos by vm.ausgewaehlteFotos.collectAsState()
    val scope = rememberCoroutineScope()

    val galerieLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        scope.launch {
            uris.take(8).forEach { uri ->
                val bytes = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                }
                if (bytes != null && !vm.fotoHinzufuegen(bytes)) {
                    Toast.makeText(
                        context, "Bild nicht lesbar oder Limit (8) erreicht", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val kameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            if (!vm.fotoHinzufuegenBitmap(bmp)) {
                Toast.makeText(
                    context, "Limit von 8 Fotos erreicht", Toast.LENGTH_SHORT
                ).show()
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
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        "Intelligente Vision-Erkennung",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    "Foto eines Raums aufnehmen oder auswählen. Mistral Vision erkennt Objekte " +
                        "und Verschmutzungen und leitet daraus Aufgaben mit Zeitschätzung, " +
                        "Hilfsmitteln und Priorität ab.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Text("Zielbereich (optional – wird sonst automatisch erkannt)", style = MaterialTheme.typography.labelLarge)
        RaumDropdown(
            raeume = raeume,
            auswahl = raum,
            onChange = { raum = it },
            onRename = { alt, neu -> vm.raumUmbenennen(alt, neu) }
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { kameraLauncher.launch() },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .weight(1f)
                    .federPress(akzent = MaterialTheme.colorScheme.primary, ruheElevation = 4.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Kamera")
            }
            OutlinedButton(
                onClick = { galerieLauncher.launch("image/*") },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .weight(1f)
                    .federPress(akzent = MaterialTheme.colorScheme.secondary, ruheElevation = 3.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Galerie (max. 8)")
            }
        }
        if (fotos.isNotEmpty()) {
            Text("${fotos.size} Foto(s) ausgewählt", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fotos.forEachIndexed { index, bytes ->
                    val bmp = remember(bytes) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                    Box(
                        Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        if (bmp != null) {
                            Image(
                                bitmap = bmp,
                                contentDescription = "Foto ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        IconButton(
                            onClick = { vm.fotoEntfernen(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(26.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Foto ${index + 1} entfernen",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val zielRaum = raum.ifBlank { "Automatisch" }
                Button(
                    onClick = { vm.fotoAnalysierenAlle(zielRaum, fotos) },
                    enabled = analyse !is FotoAnalyseUi.Laeuft,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .weight(1f)
                        .federPress(akzent = MaterialTheme.colorScheme.secondary, ruheElevation = 4.dp)
                ) {
                    Text(
                        if (fotos.size == 1) "Foto analysieren"
                        else "Alle ${fotos.size} Fotos analysieren"
                    )
                }
                TextButton(onClick = { vm.fotosLeeren() }) { Text("Leeren") }
            }
        }

        // 2.2: Hinweis, wie die Foto-Analyse den Plan verschoben hat
        val planHinweis by vm.fotoPlanHinweis.collectAsState()
        planHinweis?.let { hinweis ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    hinweis,
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        when (val a = analyse) {
            is FotoAnalyseUi.Laeuft -> {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                        Text("Analysiere Foto mit Mistral Vision …")
                    }
                }
            }
            is FotoAnalyseUi.Fehler -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Fehler: ${a.meldung}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { vm.fotoAnalyseZuruecksetzen() }) {
                            Text("Schließen")
                        }
                    }
                }
            }
            is FotoAnalyseUi.Ergebnis -> {
                val erkannt = a.analyse.raum.ifBlank { a.raum }.ifBlank { "Sonstiges" }
                val zielRaum = raum.ifBlank { erkannt }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Erkannter Raum: $erkannt",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Raumbadge(erkannt, dunkel = dunkel)
                        }
                        if (raum.isNotBlank() && !erkannt.equals(raum, ignoreCase = true)
                            && raeume.any { it.equals(raum, ignoreCase = true) }) {
                            Text(
                                "Mistral hat „$erkannt“ erkannt statt „$raum“.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(onClick = { vm.raumUmbenennen(raum, erkannt); raum = erkannt }) {
                                Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null)
                                Spacer(Modifier.padding(2.dp))
                                Text("Raum „$raum“ in „$erkannt“ umbenennen")
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
                            Text("Veränderungen zum letzten Scan", style = MaterialTheme.typography.titleSmall)
                            a.analyse.veraenderungen.forEach { v ->
                                val label = when (v.typ) {
                                    "hinzugefuegt" -> "Neu hinzugekommen"
                                    "entfernt" -> "Weggefallen"
                                    "veraendert" -> "Verändert"
                                    else -> v.typ
                                }
                                val farbe = when (v.typ) {
                                    "hinzugefuegt" -> MaterialTheme.colorScheme.primary
                                    "entfernt" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                                Text(
                                    "• [$label] ${v.objekt}" +
                                        (if (v.bemerkung.isNotBlank()) " – ${v.bemerkung}" else ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = farbe
                                )
                            }
                        } else {
                            Text(
                                "Keine Veränderungen zum letzten Scan erkannt.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (a.analyse.aufgaben.isNotEmpty()) {
                            val offeneAufgaben = a.analyse.aufgaben.filter {
                                it.titel !in uebernommen && it.titel.trim().lowercase() !in vorhandeneTitel
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Vorgeschlagene Aufgaben (${a.analyse.aufgaben.size})",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (offeneAufgaben.size > 1) {
                                    FilledTonalButton(
                                        onClick = {
                                            vm.fotoAufgabenAlleUebernehmen(a.analyse.aufgaben, zielRaum)
                                        }
                                    ) {
                                        Icon(Icons.Default.DoneAll, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Alle übernehmen (${offeneAufgaben.size})")
                                    }
                                }
                            }
                        }
                    }
                }
                a.analyse.aufgaben.forEach { aufgabe ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(aufgabe.titel, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${aufgabe.bereich} · ${aufgabe.dauerMin} Min. · " +
                                    "${Konstanten.intervallLabel(aufgabe.intervall)} · " +
                                    "Priorität ${(aufgabe.prioritaet * 100).toInt()} %",
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
                            val bereitsUebernommen = aufgabe.titel in uebernommen
                            val schonImPlan = aufgabe.titel.trim().lowercase() in vorhandeneTitel
                            val bereits = bereitsUebernommen || schonImPlan
                            FilledTonalButton(
                                modifier = Modifier.federPress(
                                    akzent = raumFarbe(zielRaum, dunkel),
                                    ruheElevation = 3.dp
                                ),
                                onClick = {
                                    vm.fotoAufgabeUebernehmen(aufgabe, zielRaum)
                                },
                                enabled = !bereits
                            ) {
                                Icon(
                                    if (bereits) Icons.Default.Check else Icons.Default.AddTask,
                                    contentDescription = null
                                )
                                Spacer(Modifier.padding(2.dp))
                                Text(
                                    when {
                                        bereitsUebernommen -> "Im Plan übernommen"
                                        schonImPlan -> "Bereits im Plan"
                                        else -> "In Plan übernehmen"
                                    }
                                )
                            }
                        }
                    }
                }
            }
            FotoAnalyseUi.Idle -> {}
        }
        Spacer(Modifier.height(24.dp))
    }
}
