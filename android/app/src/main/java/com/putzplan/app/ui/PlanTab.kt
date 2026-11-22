package com.putzplan.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putzplan.app.logic.AufgabenAssistent
import com.putzplan.app.logic.Konstanten
import com.putzplan.app.logic.PlanIntelligenz
import com.putzplan.app.ui.theme.AufPastell
import com.putzplan.app.ui.theme.raumFarbe
import kotlinx.coroutines.delay

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
    val raum by vm.raumFilter.collectAsState()
    val dunkel = isSystemInDarkTheme()
    // 2.2: Zeitbudget-Modus ("Ich habe X Minuten")
    var budget by remember { mutableStateOf<Int?>(null) }
    // 2.3: Streak-Intelligenz & Saison-/Kontext-Modul
    val stats by vm.stats.collectAsState()
    val besuch by vm.besuchModus.collectAsState()
    val taskTitel by vm.taskTitel.collectAsState()
    val monat = remember { java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1 }
    val istWochenende = remember {
        val tag = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        tag == java.util.Calendar.SATURDAY || tag == java.util.Calendar.SUNDAY
    }
    var zeigeSaison by remember { mutableStateOf(false) }
    var zeigeSpaeter by remember { mutableStateOf(false) }
    var nurKurz by remember { mutableStateOf(false) }
    var zeigeSmartRunde by remember { mutableStateOf(false) }
    val gruss = remember { PlanIntelligenz.tagesGruss() }
    val saisonTips = remember(taskTitel, monat) {
        com.putzplan.app.logic.SaisonLogik.saisonVorschlaege(monat)
            .filter { it.titel.trim().lowercase() !in taskTitel }
    }
    // Besuch-Modus & Quick-Wins-Filter (≤ 10 Min.)
    val basisZeilen = if (besuch)
        plan.zeilen.sortedByDescending { it.prioritaet + (if (it.tageBis <= 2) 200.0 else 0.0) }
    else
        plan.zeilen
    val sichtbareZeilen = if (nurKurz) basisZeilen.filter { it.dauerMin <= 10 } else basisZeilen
    val smartRunde = remember(plan.zeilen) {
        PlanIntelligenz.smartBatchVorschlag(
            zeilen = plan.zeilen,
            idOf = { it.id },
            raumOf = { it.raum },
            dauerOf = { it.dauerMin },
            hilfsmittelOf = { it.hilfsmittel }
        )
    }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = raum == "alle",
                    onClick = { vm.setRaumFilter("alle") },
                    label = { Text("Alle") },
                    modifier = Modifier.federPress(ruheElevation = 3.dp)
                )
            }
            items(raeume) { r ->
                FilterChip(
                    selected = raum == r,
                    onClick = { vm.setRaumFilter(r) },
                    label = { Text(r) },
                    modifier = Modifier.federPress(
                        akzent = raumFarbe(r, dunkel),
                        ruheElevation = 3.dp
                    )
                )
            }
        }
        // 2.0: Hero (Begrüßung + Fortschrittsanzeige) wischt beim
        // Scrollen der Aufgaben mit nach oben weg und gibt Platz zurück.
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            gruss,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Dein Wochenplan",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FortschrittsBalken(
                            erledigtWoche = stats?.erledigtDieseWoche ?: 0,
                            offenAufgaben = plan.zeilen.size,
                            offenMinuten = plan.gesamtMinutenOffen,
                            allesErledigt = plan.allesErledigt
                        )
                    }
                }
            }
            // 2.2: adaptive Intervall-Vorschläge (aus dem Lern-System)
            items(plan.vorschlaege, key = { "v-" + it.taskId }) { v ->
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "\uD83E\uDDE0 Intervall-Vorschlag",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(v.begruendung, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { vm.vorschlagUebernehmen(v.taskId) },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Übernehmen (" + v.intervallVorschlagLabel + ")")
                            }
                            TextButton(onClick = { vm.vorschlagAblehnen(v.taskId) }) {
                                Text("Behalten")
                            }
                        }
                    }
                }
            }
            // 2.2 & 2.3: Zeitbudget + Kontext-Chips (Besuch & Saison) kompakt gebündelt
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Text(
                                "Zeitbudget:",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        }
                        items(listOf(5, 10, 20, 30)) { minuten ->
                            FilterChip(
                                selected = budget == minuten,
                                onClick = { budget = if (budget == minuten) null else minuten },
                                label = { Text("$minuten Min.") },
                                modifier = Modifier.federPress(ruheElevation = 2.dp)
                            )
                        }
                    }
                    budget?.let { b ->
                        val auswahl = com.putzplan.app.logic.PlanIntelligenz.zeitplanAuswaehlen(
                            plan.zeilen, b, { it.prioritaet }, { it.dauerMin }
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Dein $b-Minuten-Plan",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (auswahl.isEmpty()) {
                                    Text(
                                        "Keine offene Aufgabe passt in $b Minuten.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    auswahl.forEach { z ->
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "• ${z.titel} (${z.dauerMin} Min., ${z.raum})",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(
                                                onClick = { vm.erledigt(z.id, z.dauerMin) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.TaskAlt,
                                                    contentDescription = "Direkt abhaken",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Erledigt", style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                    Text(
                                        "Gesamt: ${auswahl.sumOf { it.dauerMin }} Min.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Besuch-Chip, Quick-Wins-Chip & Saison-Chip nebeneinander in einer kompakten Zeile
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = besuch,
                            onClick = { vm.besuchModusUmschalten() },
                            label = { Text("\uD83D\uDC4B Besuch?") },
                            modifier = Modifier.federPress(ruheElevation = 2.dp)
                        )
                        FilterChip(
                            selected = nurKurz,
                            onClick = { nurKurz = !nurKurz },
                            label = { Text("⚡ ≤ 10 Min.") },
                            modifier = Modifier.federPress(ruheElevation = 2.dp)
                        )
                        FilterChip(
                            selected = zeigeSaison,
                            onClick = { zeigeSaison = !zeigeSaison },
                            label = {
                                Text(
                                    if (!zeigeSaison && saisonTips.isNotEmpty())
                                        "\uD83C\uDF31 Saison-Tipp (${saisonTips.size})"
                                    else "\uD83C\uDF31 Saison-Tipp"
                                )
                            },
                            modifier = Modifier.federPress(ruheElevation = 2.dp)
                        )
                    }
                    if (besuch) {
                        Text(
                            "Alles in den nächsten 2 Tagen Fällige rutscht nach oben.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (nurKurz) {
                        Text(
                            "Zeigt nur schnelle Aufgaben bis 10 Minuten – perfekt für zwischendurch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // 2.6: Smart-Runde (Hilfsmittel- oder Raum-Synergie)
                    smartRunde?.let { runde ->
                        val rundenTasks = plan.zeilen.filter { it.id in runde.taskIds }
                        if (rundenTasks.size >= 2) {
                            Surface(
                                onClick = { zeigeSmartRunde = !zeigeSmartRunde },
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "✨ Smart-Runde: ${runde.titel}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                runde.untertitel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Icon(
                                            if (zeigeSmartRunde) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (zeigeSmartRunde) "Einklappen" else "Ausklappen",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    if (zeigeSmartRunde) {
                                        HorizontalDivider()
                                        rundenTasks.forEach { z ->
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    "• ${z.titel} (${z.raum}, ${z.dauerMin} Min.)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                TextButton(
                                                    onClick = { vm.erledigt(z.id, z.dauerMin) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.TaskAlt,
                                                        contentDescription = "Direkt abhaken",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("Erledigt", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (zeigeSaison) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (saisonTips.isEmpty()) {
                                    Text(
                                        "Alles Saison-Typische ist schon im Plan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                saisonTips.forEach { a ->
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(a.titel, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "${a.hinweis} · ${a.dauerMin} Min. (${a.raum})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = { vm.saisonTaskAnlegen(a) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Anlegen")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // 2.3: Streak-Intelligenz – "Noch 1 Aufgabe bis ..."
            val badge = stats?.let { naechsterBadge(it.gesamtErledigt) }
            val kuerzeste = plan.zeilen.minByOrNull { it.dauerMin }
            if (badge != null && kuerzeste != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${badge.first} Noch 1 Aufgabe bis „${badge.second}“!",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Die kürzeste offene Aufgabe passt jetzt: ${kuerzeste.titel} " +
                                    "(${kuerzeste.dauerMin} Min., ${kuerzeste.raum})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            // 2.3: Kontext – Wochenende-Hinweis
            val grosse = plan.zeilen.sortedByDescending { it.dauerMin }.take(2)
            if (istWochenende && grosse.isNotEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "\uD83C\uDF89 Wochenende – perfekt für die großen Aufgaben",
                                style = MaterialTheme.typography.titleSmall
                            )
                            grosse.forEach { g ->
                                Text(
                                    "• ${g.titel} (${g.dauerMin} Min., ${g.raum})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            if (plan.zeilen.isEmpty() && !plan.lade) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                when {
                                    plan.allesErledigt -> "Heute steht nichts mehr an! \uD83C\uDF89"
                                    raum == "alle" -> "Noch keine Aufgaben angelegt."
                                    else -> "Keine fälligen Aufgaben für „$raum“."
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (plan.allesErledigt)
                                    "Alle fälligen Aufgaben sind erledigt. Unten kannst du geplante Aufgaben für die nächsten Tage einsehen."
                                else
                                    "Lege über „Neue Aufgabe“ unten rechts oder per Foto-Analyse deine ersten Aufgaben an – der Raum wird dabei automatisch erstellt.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            items(sichtbareZeilen, key = { it.id }) { z ->
                TaskKarte(z, vm)
            }
            // Aufklappbarer Bereich für später fällige Aufgaben (damit erledigte / zukünftige Aufgaben sichtbar bleiben)
            if (plan.spaeterZeilen.isNotEmpty()) {
                item {
                    Surface(
                        onClick = { zeigeSpaeter = !zeigeSpaeter },
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Später fällig (${plan.spaeterZeilen.size})",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Bereits erledigte & kommende Aufgaben im Überblick",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                if (zeigeSpaeter) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (zeigeSpaeter) "Einklappen" else "Ausklappen"
                            )
                        }
                    }
                }
                if (zeigeSpaeter) {
                    items(plan.spaeterZeilen, key = { "spaeter-" + it.id }) { z ->
                        TaskKarte(z, vm)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskKarte(z: PlanUi.PlanZeileUi, vm: PutzplanViewModel) {
    var zeigeAnleitung by remember { mutableStateOf(false) }
    var zeigeDauerDialog by remember { mutableStateOf(false) }
    var zeigeBearbeitenDialog by remember { mutableStateOf(false) }
    var zeigeLoeschDialog by remember { mutableStateOf(false) }
    val dunkel = isSystemInDarkTheme()
    val akzentFarbe = raumFarbe(z.raum, dunkel)
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
                else -> akzentFarbe.copy(alpha = if (dunkel) 0.22f else 0.35f)
                    .compositeOver(MaterialTheme.colorScheme.surface)
            }
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .federPress(
                pressScale = 0.985f,
                akzent = akzentFarbe,
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
                    .background(akzentFarbe, RoundedCornerShape(3.dp))
            )
            Column(
                Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Raumbadge(z.raum, Modifier.weight(1f, fill = false), dunkel = dunkel)
                    if (z.ueberfaellig) DringlichkeitBadge("Überfällig")
                }
                Text(z.titel, style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val faelligkeitText = when {
                        z.ueberfaellig && -z.tageBis == 0L -> "heute fällig"
                        z.ueberfaellig -> "überfällig seit ${-z.tageBis} T."
                        z.tageBis == 1L -> "morgen fällig"
                        else -> "fällig in ${z.tageBis} T."
                    }
                    Text(
                        listOf(
                            z.intervallLabel,
                            "${z.dauerMin} Min." +
                                (z.durchschnittDauerMin?.let { " (Ø $it Min.)" } ?: ""),
                            faelligkeitText,
                            "${z.erledigtZaehler}× erledigt"
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        Text("Später")
                    }
                    IconButton(
                        onClick = { zeigeBearbeitenDialog = true }
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Aufgabe bearbeiten",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { zeigeLoeschDialog = true }
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
    if (zeigeBearbeitenDialog) TaskBearbeitenDialog(z, vm) { zeigeBearbeitenDialog = false }
    if (zeigeDauerDialog) DauerDialog(z.dauerMin) { dauer ->
        zeigeDauerDialog = false
        if (dauer != null) {
            vm.erledigt(z.id, dauer)
        }
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
                }) { Text("Löschen", color = MaterialTheme.colorScheme.error) }
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
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "${z.raum} · ${z.intervallLabel} · ca. ${z.dauerMin} Min.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text("Hilfsmittel", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                if (z.hilfsmittel.isEmpty()) Text("–", style = MaterialTheme.typography.bodyMedium)
                else z.hilfsmittel.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(12.dp))
                Text("Schritt-für-Schritt", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    z.anleitung.ifBlank { "Keine Anleitung hinterlegt." },
                    style = MaterialTheme.typography.bodyMedium
                )
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
        title = { Text("Aufgabe erledigt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Wie viele Minuten hast du gebraucht?",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() } },
                    label = { Text("Minuten (Schätzung: $schaetzung Min.)") },
                    singleLine = true
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 10, 15, 20, 30, 45).forEach { min ->
                        FilterChip(
                            selected = text == min.toString(),
                            onClick = { text = min.toString() },
                            label = { Text("$min Min.") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onFertig(text.toIntOrNull()?.takeIf { it > 0 } ?: schaetzung)
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = { onFertig(null) }) { Text("Abbrechen") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuTaskDialog(vm: PutzplanViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val raeume by vm.raeume.collectAsState()
    val apiKey by vm.apiKey.collectAsState()
    var titel by remember { mutableStateOf("") }
    var raum by remember { mutableStateOf("") }
    var intervall by remember { mutableStateOf("woechentlich") }
    var dauer by remember { mutableStateOf("15") }
    var hilfsmittel by remember { mutableStateOf("") }
    var anleitung by remember { mutableStateOf("") }
    var kiLaedt by remember { mutableStateOf(false) }

    val offlineVorschlag = remember(titel) { AufgabenAssistent.vorschlagFuer(titel) }
    val kannSpeichern = titel.isNotBlank() && raum.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neue Aufgabe") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titel,
                    onValueChange = { titel = it },
                    label = { Text("Titel *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // 2.6: Offline-Assistent schlägt automatisch passende Details vor
                offlineVorschlag?.let { v ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "✨ Smart-Vorschlag",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "${Konstanten.intervallLabel(v.intervall)} · ${v.dauerMin} Min. · ${v.raum}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            FilledTonalButton(
                                onClick = {
                                    if (raum.isBlank()) raum = v.raum
                                    intervall = v.intervall
                                    dauer = v.dauerMin.toString()
                                    hilfsmittel = v.hilfsmittel.joinToString(", ")
                                    anleitung = v.anleitung
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Übernehmen", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                // 2.6: Optionaler KI-Assistent (wenn Mistral API-Key gesetzt ist)
                if (apiKey.isNotBlank() && titel.trim().length >= 3) {
                    TextButton(
                        enabled = !kiLaedt,
                        onClick = {
                            kiLaedt = true
                            vm.kiAufgabeErgaenzen(titel, raum.ifBlank { null }) { res ->
                                kiLaedt = false
                                res.fold(
                                    onSuccess = { a ->
                                        if (raum.isBlank() && a.bereich.isNotBlank()) raum = a.bereich
                                        if (Konstanten.istGueltigesIntervall(a.intervall)) intervall = a.intervall
                                        if (a.dauerMin > 0) dauer = a.dauerMin.toString()
                                        if (a.hilfsmittel.isNotEmpty()) hilfsmittel = a.hilfsmittel.joinToString(", ")
                                        if (a.anleitung.isNotBlank()) anleitung = a.anleitung
                                    },
                                    onFailure = { e ->
                                        Toast.makeText(
                                            context,
                                            "KI-Ergänzung fehlgeschlagen: ${e.message ?: "Fehler"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    ) {
                        if (kiLaedt) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(if (kiLaedt) "KI ergänzt Details …" else "Mit KI ausfüllen")
                    }
                }
                RaumDropdown(
                    raeume = raeume,
                    auswahl = raum,
                    onChange = { raum = it },
                    onRename = { alt, neu -> vm.raumUmbenennen(alt, neu) }
                )
                IntervallDropdown(intervall) { intervall = it }
                OutlinedTextField(
                    value = dauer,
                    onValueChange = { dauer = it.filter { c -> c.isDigit() } },
                    label = { Text("Dauer (Min.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hilfsmittel,
                    onValueChange = { hilfsmittel = it },
                    label = { Text("Hilfsmittel (Komma-getrennt)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = anleitung,
                    onValueChange = { anleitung = it },
                    label = { Text("Anleitung (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = kannSpeichern,
                onClick = {
                    vm.neuTask(
                        titel.trim(),
                        raum.trim(),
                        intervall,
                        dauer.toIntOrNull() ?: 15,
                        hilfsmittel.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                        anleitung.trim()
                    )
                    onDismiss()
                }
            ) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBearbeitenDialog(
    z: PlanUi.PlanZeileUi,
    vm: PutzplanViewModel,
    onDismiss: () -> Unit
) {
    val raeume by vm.raeume.collectAsState()
    var titel by remember(z.id) { mutableStateOf(z.titel) }
    var raum by remember(z.id) { mutableStateOf(z.raum) }
    var intervall by remember(z.id) { mutableStateOf(z.intervallKey) }
    var dauer by remember(z.id) { mutableStateOf(z.dauerMin.toString()) }
    var hilfsmittel by remember(z.id) { mutableStateOf(z.hilfsmittel.joinToString(", ")) }
    var anleitung by remember(z.id) { mutableStateOf(z.anleitung) }

    val kannSpeichern = titel.isNotBlank() && raum.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aufgabe bearbeiten") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titel,
                    onValueChange = { titel = it },
                    label = { Text("Titel *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                RaumDropdown(
                    raeume = raeume,
                    auswahl = raum,
                    onChange = { raum = it },
                    onRename = { alt, neu -> vm.raumUmbenennen(alt, neu) }
                )
                IntervallDropdown(intervall) { intervall = it }
                OutlinedTextField(
                    value = dauer,
                    onValueChange = { dauer = it.filter { c -> c.isDigit() } },
                    label = { Text("Dauer (Min.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hilfsmittel,
                    onValueChange = { hilfsmittel = it },
                    label = { Text("Hilfsmittel (Komma-getrennt)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = anleitung,
                    onValueChange = { anleitung = it },
                    label = { Text("Anleitung") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = kannSpeichern,
                onClick = {
                    vm.taskBearbeiten(
                        taskId = z.id,
                        titel = titel.trim(),
                        raum = raum.trim(),
                        intervall = intervall,
                        dauerMin = dauer.toIntOrNull() ?: z.dauerMin,
                        hilfsmittel = hilfsmittel.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                        anleitung = anleitung.trim()
                    )
                    onDismiss()
                }
            ) { Text("Speichern") }
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
    val dunkel = isSystemInDarkTheme()

    OutlinedButton(
        onClick = { zeigeDialog = true },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .federPress(
                akzent = if (auswahl.isNotBlank()) raumFarbe(auswahl, dunkel) else null,
                form = MaterialTheme.shapes.large,
                ruheElevation = 3.dp
            )
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(
                    if (auswahl.isNotBlank()) raumFarbe(auswahl, dunkel)
                    else MaterialTheme.colorScheme.outline,
                    CircleShape
                )
        )
        Spacer(Modifier.padding(6.dp))
        Text(
            "Raum: ${auswahl.ifBlank { "auswählen" }}",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    val alt = renameAlt
    if (zeigeDialog) {
        AlertDialog(
            onDismissRequest = { zeigeDialog = false; renameAlt = null },
            title = { Text(if (alt == null) "Raum wählen" else "Raum umbenennen") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (alt == null) {
                        if (raeume.isEmpty()) {
                            Text(
                                "Noch keine Räume – lege unten den ersten an.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        raeume.forEach { r ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val farbe = raumFarbe(r, dunkel)
                                val textFarbe = if (dunkel) Color.White else AufPastell
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
                                            color = textFarbe,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (r == auswahl) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "ausgewählt",
                                                tint = textFarbe
                                            )
                                        }
                                    }
                                }
                                if (onRename != null) {
                                    IconButton(onClick = { renameAlt = r; renameText = r }) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = "„$r“ umbenennen",
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
                            "Alle Aufgaben und Verlaufseinträge von „$alt“ werden verschoben.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (alt == null) {
                    TextButton(onClick = { zeigeDialog = false }) { Text("Schließen") }
                } else {
                    Button(
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
                    TextButton(onClick = { renameAlt = null }) { Text("Zurück") }
                }
            }
        )
    }
}

@Composable
fun IntervallDropdown(auswahl: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Intervall: ${Konstanten.intervallLabel(auswahl)}")
        }
        DropdownMenu(expanded, { expanded = false }) {
            Konstanten.INTERVALLE.forEach { i ->
                DropdownMenuItem(
                    text = { Text(i.label) },
                    onClick = { onChange(i.key); expanded = false }
                )
            }
        }
    }
}

@Composable
fun EinstellungenDialog(vm: PutzplanViewModel, onDismiss: () -> Unit) {
    val aktuellerKey by vm.apiKey.collectAsState()
    var key by remember(aktuellerKey) { mutableStateOf(aktuellerKey) }
    var zeigeReport by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Einstellungen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mistral API-Key für die Foto-Analyse:")
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API-Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Der Key bleibt lokal auf deinem Gerät gespeichert (SharedPreferences).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                FilledTonalButton(
                    onClick = { zeigeReport = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📄 Diagnose-Report erstellen")
                }
                Text(
                    "Erstellt einen Markdown-Report zum Kopieren oder Teilen " +
                        "(für Fehleranalyse und Feedback, ohne API-Key).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = { vm.setApiKey(key); onDismiss() }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
    if (zeigeReport) ReportDialog(vm) { zeigeReport = false }
}

/**
 * 2.4 - Report-Funktion: zeigt den generierten Markdown-Report an,
 * zum Kopieren in die Zwischenablage oder Teilen über Android-Share.
 */
@Composable
fun ReportDialog(vm: PutzplanViewModel, onDismiss: () -> Unit) {
    val report by vm.report.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.reportErstellen() }
    AlertDialog(
        onDismissRequest = { vm.reportSchliessen(); onDismiss() },
        title = { Text("Diagnose-Report") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    report ?: "Report wird erstellt …",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(
                    ClipData.newPlainText("Putzplan Report", report ?: "")
                )
                Toast.makeText(context, "Report in Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
            }) { Text("Kopieren") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, report ?: "")
                        putExtra(Intent.EXTRA_SUBJECT, "Putzplan Report")
                    }
                    context.startActivity(Intent.createChooser(send, "Report teilen"))
                }) { Text("Teilen") }
                TextButton(onClick = { vm.reportSchliessen(); onDismiss() }) { Text("Schließen") }
            }
        }
    )
}
