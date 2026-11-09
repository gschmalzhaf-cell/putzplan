package com.putzplan.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putzplan.app.logic.Konstanten
import kotlinx.coroutines.delay
import com.putzplan.app.ui.theme.raumFarbe
import com.putzplan.app.ui.theme.AufPastell
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.PaddingValues

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
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Neue Aufgabe") },
                    modifier = Modifier.federPress(
                        akzent = MaterialTheme.colorScheme.primary,
                        form = MaterialTheme.shapes.extraLarge,
                        ruheElevation = 8.dp
                    )
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
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = raum == "alle",
                    onClick = { raum = "alle"; vm.setRaumFilter("alle") },
                    label = { Text("Alle") },
                    modifier = Modifier.federPress(ruheElevation = 3.dp)
                )
            }
            items(raeume) { r ->
                FilterChip(
                    selected = raum == r,
                    onClick = { raum = r; vm.setRaumFilter(r) },
                    label = { Text(r) },
                    modifier = Modifier.federPress(
                        akzent = raumFarbe(r),
                        ruheElevation = 3.dp
                    )
                )
            }
        }
        // 2.0: Hero (Begruessung + Vorschrifts/Fortschrittsanzeige) wischt beim
        // Scrollen der Aufgaben mit nach oben weg und gibt Platz zurueck.
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = 16.dp, vertical = 4.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Moin \uD83D\uDC4B", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Dein Wochenplan", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                        val stats by vm.stats.collectAsState()
                        FortschrittsBalken(
                            erledigt = stats?.gesamtErledigt ?: 0,
                            offen = plan.gesamtMinutenOffen,
                            allesErledigt = plan.allesErledigt
                        )
                    }
                }
            }
            if (plan.zeilen.isEmpty() && !plan.lade) {
                item {
                    Column(
                        Modifier.padding(vertical = 24.dp),
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
            }
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
    val stoppuhr by vm.stoppuhr.collectAsState()
    val zustand = stoppuhr?.takeIf { it.taskId == z.id }
    var sekunden by remember { mutableStateOf(0L) }
    LaunchedEffect(zustand?.taskId, zustand?.laeuft) {
        val s = zustand
        if (s == null) {
            sekunden = 0L
        } else if (s.laeuft) {
            while (true) {
                delay(1000)
                sekunden = s.vergangeneMs() / 1000
            }
        } else {
            sekunden = s.vergangeneMs() / 1000
        }
    }
    val zeitText = "%02d:%02d".format(sekunden / 60, sekunden % 60)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                z.ueberfaellig -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> raumFarbe(z.raum).copy(alpha = 0.35f)
                    .compositeOver(MaterialTheme.colorScheme.surface)
            }
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .federPress(
                pressScale = 0.985f,
                akzent = raumFarbe(z.raum),
                form = MaterialTheme.shapes.large,
                ruheElevation = 3.dp,
                gedruecktElevation = 1.dp
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                Modifier
                    .padding(start = 16.dp)
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(raumFarbe(z.raum), RoundedCornerShape(3.dp))
            )
            Column(Modifier.padding(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Raumbadge(z.raum, Modifier.weight(1f, fill = false))
                if (z.ueberfaellig) DringlichkeitBadge("Überfällig")
            }
            Text(z.titel, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOf(
                        z.intervallLabel,
                        "${z.dauerMin} Min." +
                            (z.durchschnittDauerMin?.let { " (Ø $it Min. gemessen)" } ?: ""),
                        if (z.ueberfaellig) "überfällig seit ${-z.tageBis} T."
                        else "fällig in ${z.tageBis} T.",
                        "${z.erledigtZaehler}× erledigt"
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilledTonalButton(
                    onClick = { zeigeDauerDialog = true },
                    modifier = Modifier.federPress(
                        akzent = MaterialTheme.colorScheme.primary,
                        ruheElevation = 4.dp
                    )
                ) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("Erledigt")
                }
                if (zustand != null) {
                    FilledTonalButton(
                        onClick = { vm.stoppuhrStop(z.id) },
                        modifier = Modifier.federPress(
                            akzent = MaterialTheme.colorScheme.tertiary,
                            ruheElevation = 4.dp
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stoppuhr stoppen")
                        Spacer(Modifier.padding(2.dp))
                        Text(zeitText, style = MaterialTheme.typography.titleMedium)
                    }
                    if (zustand.laeuft) {
                        TextButton(
                            onClick = { vm.stoppuhrPause(z.id) },
                            modifier = Modifier.federPress(ruheElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Stoppuhr pausieren")
                            Spacer(Modifier.padding(2.dp))
                            Text("Pause")
                        }
                    } else {
                        TextButton(
                            onClick = { vm.stoppuhrFortsetzen(z.id) },
                            modifier = Modifier.federPress(ruheElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Stoppuhr fortsetzen")
                            Spacer(Modifier.padding(2.dp))
                            Text("Fortsetzen")
                        }
                    }
                } else {
                    TextButton(
                        onClick = { vm.stoppuhrStart(z.id) },
                        modifier = Modifier.federPress(ruheElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Stoppuhr starten")
                        Spacer(Modifier.padding(2.dp))
                        Text("Stoppuhr")
                    }
                }
                TextButton(
                    onClick = { zeigeAnleitung = true },
                    modifier = Modifier.federPress(ruheElevation = 2.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("Anleitung")
                }
                TextButton(
                    onClick = { vm.ueberspringen(z.id) },
                    modifier = Modifier.federPress(ruheElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Update, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("später")
                }
                TextButton(
                    onClick = { zeigeLoeschDialog = true },
                    modifier = Modifier.federPress(
                        akzent = MaterialTheme.colorScheme.error,
                        ruheElevation = 2.dp
                    )
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
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
        AlertDialog(
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
    AlertDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neue Aufgabe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(titel, { titel = it }, label = { Text("Titel") })
                RaumDropdown(raeume, raum, onChange = { raum = it }, onRename = { alt, neu -> vm.raumUmbenennen(alt, neu) })
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
fun RaumDropdown(
    raeume: List<String>,
    auswahl: String,
    onChange: (String) -> Unit,
    onRename: ((alt: String, neu: String) -> Unit)? = null
) {
    var zeigeDialog by remember { mutableStateOf(false) }
    var renameAlt by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var neuerRaum by remember { mutableStateOf("") }

    OutlinedButton(
        onClick = { zeigeDialog = true },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .federPress(
                akzent = if (auswahl.isNotBlank()) raumFarbe(auswahl) else null,
                form = MaterialTheme.shapes.large,
                ruheElevation = 3.dp
            )
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(
                    if (auswahl.isNotBlank()) raumFarbe(auswahl)
                    else MaterialTheme.colorScheme.outline,
                    CircleShape
                )
        )
        Spacer(Modifier.padding(6.dp))
        Text(
            "Raum: ${auswahl.ifBlank { "ausw\u00e4hlen" }}",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    val alt = renameAlt
    if (zeigeDialog) {
        AlertDialog(
            onDismissRequest = { zeigeDialog = false; renameAlt = null },
            title = { Text(if (alt == null) "Raum w\u00e4hlen" else "Raum umbenennen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (alt == null) {
                        if (raeume.isEmpty()) {
                            Text(
                                "Noch keine R\u00e4ume \u2013 lege unten den ersten an.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        raeume.forEach { r ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val farbe = raumFarbe(r)
                                Surface(
                                    onClick = { onChange(r); zeigeDialog = false },
                                    color = farbe,
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier
                                        .weight(1f)
                                        .federPress(
                                            pressScale = 0.98f,
                                            akzent = farbe,
                                            form = MaterialTheme.shapes.large,
                                            ruheElevation = 3.dp
                                        )
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            r,
                                            Modifier.weight(1f),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = AufPastell,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (r == auswahl) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "ausgew\u00e4hlt",
                                                tint = AufPastell
                                            )
                                        }
                                    }
                                }
                                if (onRename != null) {
                                    IconButton(onClick = { renameAlt = r; renameText = r }) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = "\u201e$r\u201c umbenennen",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider()
                        Text(
                            "Neuer Raum",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                neuerRaum,
                                { neuerRaum = it },
                                Modifier.weight(1f),
                                label = { Text("Raumname") },
                                singleLine = true
                            )
                            FilledTonalButton(
                                enabled = neuerRaum.isNotBlank(),
                                onClick = {
                                    onChange(neuerRaum.trim())
                                    neuerRaum = ""
                                    zeigeDialog = false
                                },
                                modifier = Modifier.federPress(
                                    akzent = MaterialTheme.colorScheme.primary,
                                    ruheElevation = 3.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.padding(2.dp))
                                Text("Anlegen")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            renameText,
                            { renameText = it },
                            Modifier.fillMaxWidth(),
                            label = { Text("Neuer Raumname") },
                            singleLine = true
                        )
                        Text(
                            "Alle Aufgaben und Verlaufseintr\u00e4ge von \u201e$alt\u201c werden verschoben.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (alt == null) {
                    TextButton(onClick = { zeigeDialog = false }) { Text("Abbrechen") }
                } else {
                    TextButton(
                        onClick = {
                            val name = renameText.trim()
                            if (name.isNotBlank() && name != alt) onRename?.invoke(alt, name)
                            renameAlt = null
                            zeigeDialog = false
                        }
                    ) { Text("Umbenennen") }
                }
            },
            dismissButton = {
                if (alt != null) {
                    TextButton(onClick = { renameAlt = null }) { Text("Zur\u00fcck") }
                }
            }
        )
    }
}
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
    val aktuellerKey by vm.apiKey.collectAsState()
    var key by remember(aktuellerKey) { mutableStateOf(aktuellerKey) }
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
