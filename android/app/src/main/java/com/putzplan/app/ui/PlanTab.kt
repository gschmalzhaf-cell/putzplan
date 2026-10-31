package com.putzplan.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                title = { Text("🧹 Putzplan") },
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
                    text = { Text("Aufgabe") }
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
        Box(Modifier.padding(padding)) {
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
    var raum by remember { mutableStateOf("alle") }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        RaumFilter(raum) { raum = it; vm.setRaumFilter(it) }
        Spacer(Modifier.height(8.dp))
        Text(
            if (plan.allesErledigt) "Alles erledigt ✅"
            else "⏰ ${plan.gesamtMinutenOffen} Min. offen (überfällig/heute)",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        if (plan.zeilen.isEmpty() && !plan.lade) {
            Text("Keine Aufgaben für diesen Raum.")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(plan.zeilen, key = { it.id }) { z ->
                TaskKarte(z, vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaumFilter(auswahl: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }) {
        Text("Raum: ${if (auswahl == "alle") "Alle" else auswahl}")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Alle / Ganze Wohnung") }, onClick = {
            onChange("alle"); expanded = false
        })
        Konstanten.RAEUME.forEach { r ->
            DropdownMenuItem(text = { Text(r) }, onClick = { onChange(r); expanded = false })
        }
    }
}

@Composable
fun TaskKarte(z: PlanUi.PlanZeileUi, vm: PutzplanViewModel) {
    var zeigeAnleitung by remember { mutableStateOf(false) }
    var zeigeDauerDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                z.ueberfaellig -> MaterialTheme.colorScheme.errorContainer
                z.tageBis <= 2 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(z.titel, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                listOf(
                    z.raum,
                    z.intervallLabel,
                    "⏱ ${z.dauerMin} Min.",
                    if (z.ueberfaellig) "überfällig seit ${-z.tageBis} T."
                    else "fällig in ${z.tageBis} T.",
                    "${z.erledigtZaehler}× erledigt"
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { zeigeAnleitung = true }) { Text("📖 Anleitung") }
                TextButton(onClick = { zeigeDauerDialog = true }) { Text("✔ Erledigt") }
                TextButton(onClick = { vm.ueberspringen(z.id) }) { Text("↷ später") }
                TextButton(onClick = { vm.loeschen(z.id) }) { Text("🗑") }
            }
        }
    }

    if (zeigeAnleitung) AnleitungDialog(z) { zeigeAnleitung = false }
    if (zeigeDauerDialog) DauerDialog(z.dauerMin) { dauer ->
        zeigeDauerDialog = false
        vm.erledigt(z.id, dauer)
    }
}

@Composable
fun AnleitungDialog(z: PlanUi.PlanZeileUi, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(z.titel) },
        text = {
            Column {
                Text("${z.raum} · ${z.intervallLabel} · ⏱ ca. ${z.dauerMin} Min.")
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
    AlertDialog(
        onDismissRequest = { onFertig(null) },
        title = { Text("Dauer in Minuten") },
        text = {
            OutlinedTextField(
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

@Composable
fun NeuTaskDialog(vm: PutzplanViewModel, onDismiss: () -> Unit) {
    var titel by remember { mutableStateOf("") }
    var raum by remember { mutableStateOf("Ganze Wohnung") }
    var intervall by remember { mutableStateOf("woechentlich") }
    var dauer by remember { mutableStateOf("15") }
    var hilfsmittel by remember { mutableStateOf("") }
    var anleitung by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neue Aufgabe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(titel, { titel = it }, label = { Text("Titel") })
                RaumDropdown(raum) { raum = it }
                IntervallDropdown(intervall) { intervall = it }
                OutlinedTextField(
                    dauer, { dauer = it.filter { c -> c.isDigit() } },
                    label = { Text("Dauer (Min.)") }
                )
                OutlinedTextField(hilfsmittel, { hilfsmittel = it }, label = { Text("Hilfsmittel (Komma)") })
                OutlinedTextField(anleitung, { anleitung = it }, label = { Text("Anleitung") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (titel.isNotBlank()) {
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
fun RaumDropdown(auswahl: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }, Modifier.fillMaxWidth()) {
        Text("Raum: $auswahl")
    }
    DropdownMenu(expanded, { expanded = false }) {
        Konstanten.RAEUME.forEach { r ->
            DropdownMenuItem(text = { Text(r) }, onClick = { onChange(r); expanded = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervallDropdown(auswahl: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }, Modifier.fillMaxWidth()) {
        Text("Intervall: ${Konstanten.intervallLabel(auswahl)}")
    }
    DropdownMenu(expanded, { expanded = false }) {
        Konstanten.INTERVALLE.forEach { i ->
            DropdownMenuItem(text = { Text(i.label) }, onClick = { onChange(i.key); expanded = false })
        }
    }
}

@Composable
fun EinstellungenDialog(vm: PutzplanViewModel, onDismiss: () -> Unit) {
    var key by remember(vm.apiKey) { mutableStateOf(vm.apiKey.collectAsState().value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Einstellungen") },
        text = {
            Column {
                Text("Mistral API-Key für die Foto-Analyse (pixtral):")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(key, { key = it }, label = { Text("API-Key") })
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
