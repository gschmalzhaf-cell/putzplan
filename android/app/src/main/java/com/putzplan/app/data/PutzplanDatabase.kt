package com.putzplan.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Task::class, HistoryEntry::class, FotoErkenntnis::class],
    version = 2,
    exportSchema = false
)
abstract class PutzplanDatabase : RoomDatabase() {
    abstract fun dao(): PutzplanDao

    companion object {
        // v2: Lern-Felder fuer adaptive Intervall-Vorschlaege
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN intervallVorschlag TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN vorschlagBegruendung TEXT")
            }
        }

        @Volatile
        private var INSTANCE: PutzplanDatabase? = null

        fun get(context: Context): PutzplanDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PutzplanDatabase::class.java,
                    "putzplan.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
    }
}
