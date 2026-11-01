package com.putzplan.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PutzplanDao {

    @Query("SELECT * FROM tasks ORDER BY titel")
    fun alleTasks(): Flow<List<Task>>

    @Query("SELECT DISTINCT raum FROM tasks WHERE raum != '' ORDER BY raum")
    fun distinkteRaeume(): Flow<List<String>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun taskById(id: String): Task?

    @Insert
    suspend fun insertTask(task: Task)

    @Insert
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM history WHERE taskId = :id")
    suspend fun deleteHistoryForTask(id: String)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun taskCount(): Int

    @Query("SELECT * FROM history ORDER BY zeitpunkt DESC LIMIT :limit")
    fun history(limit: Int = 50): Flow<List<HistoryEntry>>

    @Insert
    suspend fun insertHistory(entry: HistoryEntry)

    @Query("SELECT COUNT(*) FROM history WHERE aktion = 'erledigt'")
    suspend fun anzahlErledigt(): Int

    @Query("SELECT raum, COUNT(*) as anzahl FROM history WHERE aktion = 'erledigt' GROUP BY raum")
    suspend fun erledigtProRaum(): List<RaumStat>

    @Insert
    suspend fun insertFotoErkenntnis(e: FotoErkenntnis)

    @Query("SELECT COUNT(*) FROM foto_erkenntnisse")
    suspend fun fotoErkenntnisCount(): Int

    @Query("SELECT * FROM history ORDER BY zeitpunkt DESC LIMIT 10")
    suspend fun letzteAktionen(): List<HistoryEntry>
}

data class RaumStat(val raum: String, val anzahl: Int)

data class Stats(
    val gesamtErledigt: Int,
    val proRaum: Map<String, Int>,
    val taskCount: Int,
    val fotoErkenntnisse: Int
)
