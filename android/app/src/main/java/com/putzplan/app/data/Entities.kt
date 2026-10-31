package com.putzplan.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val titel: String,
    val raum: String,
    val intervall: String,
    val dauerMin: Int,
    val hilfsmittel: String,
    val anleitung: String,
    val erstelltAm: Long,
    val erstelltAus: String,
    val erledigt: Int,
    val uebersprungen: Int,
    val letzteDauerMin: Int?,
    val durchschnittDauerMin: Int?,
    val letzterAbschluss: Long?,
    val naechstesFaellig: Long
)

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val titel: String,
    val raum: String,
    val aktion: String,
    val dauerMin: Int?,
    val zeitpunkt: Long
)

@Entity(tableName = "foto_erkenntnisse")
data class FotoErkenntnis(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val raum: String,
    val model: String,
    val zeitpunkt: Long,
    val analyseJson: String
)
