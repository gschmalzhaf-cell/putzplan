package com.putzplan.app.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.putzplan.app.data.PutzplanDatabase
import com.putzplan.app.data.Repository
import com.putzplan.app.data.Stats
import com.putzplan.app.net.FotoAnalyse
import com.putzplan.app.net.FotoAufgabe
import com.putzplan.app.net.MistralVisionClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

private fun com.putzplan.app.data.Plan.toUi() = PlanUi(
    zeilen = zeilen.map { z ->
        PlanUi.PlanZeileUi(
            id = z.task.id,
            titel = z.task.titel,
            raum = z.task.raum,
            intervallLabel = com.putzplan.app.logic.Konstanten.intervallLabel(z.task.intervall),
            dauerMin = z.task.dauerMin,
            hilfsmittel = z.task.hilfsmittel.split(',').filter { it.isNotBlank() },
            anleitung = z.task.anleitung,
            ueberfaellig = z.ueberfaellig,
            tageBis = z.tageBis,
            erledigtZaehler = z.task.erledigt,
            durchschnittDauerMin = z.task.durchschnittDauerMin
        )
    },
    gesamtMinutenOffen = gesamtMinutenOffen,
    allesErledigt = allesErledigt,
    lade = false
)

private fun com.putzplan.app.data.HistoryEntry.toUi() = HistoryUi(
    titel = titel,
    raum = raum,
    aktion = aktion,
    dauerMin = dauerMin,
    zeitpunkt = zeitpunkt
)

data class PlanUi(
    val zeilen: List<PlanZeileUi> = emptyList(),
    val gesamtMinutenOffen: Int = 0,
    val allesErledigt: Boolean = true,
    val lade: Boolean = true
) {
    data class PlanZeileUi(
        val id: String,
        val titel: String,
        val raum: String,
        val intervallLabel: String,
        val dauerMin: Int,
        val hilfsmittel: List<String>,
        val anleitung: String,
        val ueberfaellig: Boolean,
        val tageBis: Long,
        val erledigtZaehler: Int,
        val durchschnittDauerMin: Int?
    )
}

data class HistoryUi(
    val titel: String,
    val raum: String,
    val aktion: String,
    val dauerMin: Int?,
    val zeitpunkt: Long
)

sealed class FotoAnalyseUi {
    object Idle : FotoAnalyseUi()
    object Laeuft : FotoAnalyseUi()
    data class Ergebnis(val raum: String, val analyse: FotoAnalyse) : FotoAnalyseUi()
    data class Fehler(val meldung: String) : FotoAnalyseUi()
}

class PutzplanViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = PutzplanDatabase.get(app).dao()
    private val repo = Repository(dao)
    private val prefs = app.getSharedPreferences("putzplan", Context.MODE_PRIVATE)

    private val _raumFilter = MutableStateFlow("alle")
    val raumFilter: StateFlow<String> = _raumFilter

    val raeume: StateFlow<List<String>> = repo.raeume
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _apiKey = MutableStateFlow(prefs.getString("mistral_key", "") ?: "")
    val apiKey: StateFlow<String> = _apiKey

    private val _fotoAnalyse = MutableStateFlow<FotoAnalyseUi>(FotoAnalyseUi.Idle)
    val fotoAnalyse: StateFlow<FotoAnalyseUi> = _fotoAnalyse

    private val _uebernommeneFotoTitel = MutableStateFlow<Set<String>>(emptySet())
    val uebernommeneFotoTitel: StateFlow<Set<String>> = _uebernommeneFotoTitel

    private val _stats = MutableStateFlow<Stats?>(null)
    val stats: StateFlow<Stats?> = _stats

    @OptIn(ExperimentalCoroutinesApi::class)
    val plan: StateFlow<PlanUi> = _raumFilter
        .flatMapLatest { raum ->
            repo.planFlow(raum).map { p -> p.toUi() }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlanUi())

    val history: StateFlow<List<HistoryUi>> = repo.history
        .map { liste -> liste.map { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch { _stats.value = repo.stats() }
    }

    fun setRaumFilter(raum: String) {
        _raumFilter.value = raum
    }

    fun setApiKey(key: String) {
        val k = key.trim()
        prefs.edit().putString("mistral_key", k).apply()
        _apiKey.value = k
    }

    fun erledigt(taskId: String, dauerMin: Int?) = viewModelScope.launch {
        repo.erledigt(taskId, dauerMin)
        _stats.value = repo.stats()
    }

    fun ueberspringen(taskId: String) = viewModelScope.launch {
        repo.ueberspringen(taskId)
        _stats.value = repo.stats()
    }

    fun loeschen(taskId: String) = viewModelScope.launch {
        repo.loeschen(taskId)
        _stats.value = repo.stats()
    }

    fun neuTask(
        titel: String, raum: String, intervall: String, dauerMin: Int,
        hilfsmittel: List<String>, anleitung: String
    ) = viewModelScope.launch {
        repo.neuTask(titel, raum, intervall, dauerMin, hilfsmittel, anleitung, "manuell")
        _stats.value = repo.stats()
    }

    fun fotoAufgabeUebernehmen(a: FotoAufgabe, raum: String) = viewModelScope.launch {
        val intervall = if (com.putzplan.app.logic.Konstanten.istGueltigesIntervall(a.intervall))
            a.intervall else "woechentlich"
        val ok = repo.neuTask(a.titel, raum, intervall, a.dauerMin, a.hilfsmittel, a.anleitung, "foto")
        if (ok) {
            _uebernommeneFotoTitel.value = _uebernommeneFotoTitel.value + a.titel
            _stats.value = repo.stats()
        }
    }

    fun fotoAnalyseZuruecksetzen() {
        _fotoAnalyse.value = FotoAnalyseUi.Idle
        _uebernommeneFotoTitel.value = emptySet()
    }

    fun fotoAnalysieren(raum: String, imageBytes: ByteArray) {
        _fotoAnalyse.value = FotoAnalyseUi.Laeuft
        viewModelScope.launch {
            if (_apiKey.value.isBlank()) {
                _fotoAnalyse.value = FotoAnalyseUi.Fehler(
                    "Kein Mistral-API-Key gesetzt. In den Einstellungen hinterlegen."
                )
                return@launch
            }
            val dataUrl = zuDataUrl(imageBytes)
            MistralVisionClient(_apiKey.value).analysiereFoto(raum, dataUrl).fold(
                onSuccess = { analyse ->
                    repo.fotoErkenntnisSpeichern(raum, "mistral-vision", "ok")
                    _stats.value = repo.stats()
                    _fotoAnalyse.value = FotoAnalyseUi.Ergebnis(raum, analyse)
                },
                onFailure = { e ->
                    _fotoAnalyse.value = FotoAnalyseUi.Fehler(e.message ?: "Unbekannter Fehler")
                }
            )
        }
    }

    private fun zuDataUrl(bytes: ByteArray): String {
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val skaliert = Bitmap.createScaledBitmap(
            bmp, 1024, (1024.0 * bmp.height / bmp.width).toInt(), true
        )
        val out = ByteArrayOutputStream()
        skaliert.compress(Bitmap.CompressFormat.JPEG, 85, out)
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$b64"
    }
}
