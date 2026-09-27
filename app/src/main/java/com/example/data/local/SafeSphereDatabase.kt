package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.EmergencySettings
import com.example.data.model.JourneyRecord
import com.example.data.model.SafeSphereUser
import com.example.data.model.SafeZone
import com.example.data.model.TimelineEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SafeSphereUser::class,
        SafeZone::class,
        JourneyRecord::class,
        TimelineEvent::class,
        EmergencySettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SafeSphereDatabase : RoomDatabase() {
    abstract fun dao(): SafeSphereDao

    companion object {
        @Volatile
        private var INSTANCE: SafeSphereDatabase? = null

        fun getDatabase(context: Context): SafeSphereDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeSphereDatabase::class.java,
                    "safesphere_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.dao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: SafeSphereDao) {
            dao.insertUser(SafeSphereUser())
            dao.insertEmergencySettings(EmergencySettings())
            // Zero demo data: Safe zones, journeys, and timeline events will be populated dynamically from user activity and parent-child linkage.
        }
    }
}
