package com.putzplan.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Task::class, HistoryEntry::class, FotoErkenntnis::class],
    version = 1,
    exportSchema = false
)
abstract class PutzplanDatabase : RoomDatabase() {
    abstract fun dao(): PutzplanDao

    companion object {
        @Volatile
        private var INSTANCE: PutzplanDatabase? = null

        fun get(context: Context): PutzplanDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PutzplanDatabase::class.java,
                    "putzplan.db"
                ).build().also { INSTANCE = it }
            }
    }
}
