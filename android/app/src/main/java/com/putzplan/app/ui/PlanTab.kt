package com.putzplan.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putzplan.app.logic.Konstanten

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PutzplanApp(vm: PutzplanViewModel = viewModel()) {
    var tab by remember { mutableStateOf(0) }
    var zeigeNeuDialog by remember { mutableStateOf(false) }
    var zeigeEinstellungen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Putzplan", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { zeigeEinstellungen = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
                    }
                }
            )
        },
        floatingActionButton = {
            if (tab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { zeigeNeuDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Neue Aufgabe") }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    label = { Text("Wochenplan") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    label = { Text("Foto") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Verlauf") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> PlanTab(vm)
                1 -> FotoTab(vm)
                else -> VerlaufTab(vm)
            }
        }
    }

    if (zeigeNeuDialog) NeuTaskDialog(vm) { zeigeNeuDialog = false }
    if (zeigeEinstellungen) EinstellungenDialog(vm) { zeigeEinstellungen = false }
}

@Composable
fun PlanTab(vm: PutzplanViewModel) {
    val plan by vm.plan.collectAsState()
    val raeume by vm.raeume.collectAsState()
    var raum by remember { mutableStateOf("alle") }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = raum == "alle",
                    onClick = { raum = "alle"; vm.setRaumFilter("alle") },
                    label = { Text("Alle") }
                )
            }
            items(raeume) { r ->
                FilterChip(
                    selected = raum == r,
                    onClick = { raum = r; vm.setRaumFilter(r) },
                    label = { Text(r) }
                )
            }
        }
        Surface(
            color = if (plan.allesErledigt) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (plan.allesErledigt) {
                    Text(
                        "Alles erledigt",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        "Aktuell fällig",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "${plan.gesamtMinutenOffen} Min. offen (überfällig/heute)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (plan.zeilen.isEmpty() && !plan.lade) {
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (raum == "alle") "Noch keine Aufgaben angelegt."
                    else "Keine Aufgaben für diesen Raum.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Lege über „Neue Aufgabe“ unten rechts die erste Aufgabe an – der Raum wird dabei automatisch erstellt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 4.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plan.zeilen, key = { it.id }) { z ->
                TaskKarte(z, vm)
            }
        }
    }
}

@Composable
fun TaskKarte(z: PlanUi.PlanZeileUi, vm: PutzplanViewModel) {
    var zeigeAnleitung by remember { mutableStateOf(false) }
    var zeigeDauerDialog by remember { mutableStateOf(false) }
    var zeigeLoeschDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                z.ueberfaellig -> MaterialTheme.colorScheme.surfaceContainerHigh
                z.tageBis <= 2 -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Raumbadge(z.raum)
                if (z.ueberfaellig) DringlichkeitBadge("Überfällig")
            }
            Text(z.titel, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOf(
                        z.intervallLabel,
                        "${z.dauerMin} Min.",
                        if (z.ueberfaellig) "überfällig seit ${-z.tageBis} T."
                        else "fällig in ${z.tageBis} T.",
                        "${z.erledigtZaehler}× erledigt"
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { zeigeDauerDialog = true }) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("Erledigt")
                }
                TextButton(onClick = { zeigeAnleitung = true }) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("Anleitung")
                }
                TextButton(onClick = { vm.ueberspringen(z.id) }) {
                    Icon(Icons.Default.Update, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("später")
                }
                TextButton(onClick = { zeigeLoeschDialog = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Löschen")
                }
            }
        }
    }

    if (zeigeAnleitung) AnleitungDialog(z) { zeigeAnleitung = false }
    if (zeigeDauerDialog) DauerDialog(z.dauerMin) { dauer ->
        zeigeDauerDialog = false
        vm.erledigt(z.id, dauer)
    }
    if (zeigeLoeschDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { zeigeLoeschDialog = false },
            title = { Text("Aufgabe löschen?") },
            text = { Text("„${z.titel}“ wird unwiderruflich entfernt, inklusive Verlaufseinträgen.") },
            confirmButton = {
                TextButton(onClick = {
                    zeigeLoeschDialog = false
                    vm.loeschen(z.id)
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { zeigeLoeschDialog = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
fun AnleitungDialog(z: PlanUi.PlanZeileUi, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(z.titel) },
        text = {
            Column {
                Text("${z.raum} · ${z.intervallLabel} · ca. ${z.dauerMin} Min.")
                Spacer(Modifier.height(8.dp))
                Text("Hilfsmittel", style = MaterialTheme.typography.titleSmall)
                if (z.hilfsmittel.isEmpty()) Text("–")
                else z.hilfsmittel.forEach { Text("• $it") }
                Spacer(Modifier.height(8.dp))
                Text("Schritt-für-Schritt", style = MaterialTheme.typography.titleSmall)
                Text(z.anleitung.ifBlank { "Keine Anleitung hinterlegt." })
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}

@Composable
fun DauerDialog(schaetzung: Int, onFertig: (Int?) -> Unit) {
    var text by remember { mutableStateOf(schaetzung.toString()) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onFertig(null) },
        title = { Text("Dauer in Minuten") },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                label = { Text("Minuten (leer = Schätzung $schaetzung Min.)") }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onFertig(text.toIntOrNull()?.takeIf { it > 0 })
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = { onFertig(null) }) { Text("Abbrechen") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuTaskDialog(vm: PutzplanViewModel, onDismiss: () -> Unit) {
    val raeume by vm.raeume.collectAsState()
    var titel by remember { mutableStateOf("") }
    var raum by remember { mutableStateOf("") }
    var intervall by remember { mutableStateOf("woechentlich") }
    var dauer by remember { mutableStateOf("15") }
    var hilfsmittel by remember { mutableStateOf("") }
    var anleitung by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neue Aufgabe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(titel, { titel = it }, label = { Text("Titel") })
                RaumDropdown(raeume, raum) { raum = it }
                IntervallDropdown(intervall) { intervall = it }
                androidx.compose.material3.OutlinedTextField(
                    dauer, { dauer = it.filter { c -> c.isDigit() } },
                    label = { Text("Dauer (Min.)") }
                )
                androidx.compose.material3.OutlinedTextField(hilfsmittel, { hilfsmittel = it }, label = { Text("Hilfsmittel (Komma)") })
                androidx.compose.material3.OutlinedTextField(anleitung, { anleitung = it }, label = { Text("Anleitung") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (titel.isNotBlank() && raum.isNotBlank()) {
                    vm.neuTask(
                        titel, raum, intervall, dauer.toIntOrNull() ?: 15,
                        hilfsmittel.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                        anleitung
                    )
                }
                onDismiss()
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaumDropdown(raeume: List<String>, auswahl: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var zeigeNeuerRaum by remember { mutableStateOf(false) }
    var freitext by remember { mutableStateOf("") }
    androidx.compose.material3.OutlinedButton(onClick = { expanded = true }, Modifier.fillMaxWidth()) {
        Text("Raum: ${auswahl.ifBlank { "ausw\u00e4hlen" }}")
    }
    androidx.compose.material3.DropdownMenu(expanded, { expanded = false }) {
        raeume.forEach { r ->
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(r) },
                onClick = { onChange(r); expanded = false }
            )
        }
        androidx.compose.material3.DropdownMenuItem(
            text = { Text("Neuer Raum \u2026") },
            onClick = { expanded = false; freitext = ""; zeigeNeuerRaum = true }
        )
    }
    if (zeigeNeuerRaum) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { zeigeNeuerRaum = false },
            title = { Text("Neuer Raum") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    freitext, { freitext = it }, label = { Text("Raumname") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = freitext.trim()
                    if (name.isNotBlank()) {
                        onChange(name)
                        zeigeNeuerRaum = false
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { zeigeNeuerRaum = false }) { Text("Abbrechen") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervallDropdown(auswahl: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material3.OutlinedButton(onClick = { expanded = true }, Modifier.fillMaxWidth()) {
        Text("Intervall: ${Konstanten.intervallLabel(auswahl)}")
    }
    androidx.compose.material3.DropdownMenu(expanded, { expanded = false }) {
        Konstanten.INTERVALLE.forEach { i ->
            androidx.compose.material3.DropdownMenuItem(text = { Text(i.label) }, onClick = { onChange(i.key); expanded = false })
        }
    }
}

@Composable
fun EinstellungenDialog(vm: PutzplanViewModel, onDismiss: () -> Unit) {
    val aktuellerKey by vm.apiKey.collectAsState()
    var key by remember(aktuellerKey) { mutableStateOf(aktuellerKey) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Einstellungen") },
        text = {
            Column {
                Text("Mistral API-Key für die Foto-Analyse (pixtral):")
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(key, { key = it }, label = { Text("API-Key") })
                Text(
                    "Der Key bleibt lokal auf dem Gerät (SharedPreferences).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.setApiKey(key); onDismiss }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}
