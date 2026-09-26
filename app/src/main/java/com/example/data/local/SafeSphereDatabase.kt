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
    version = 1,
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

            // Default safe zones from screenshot
            dao.insertSafeZone(
                SafeZone(
                    name = "Home",
                    type = "Home",
                    address = "Oak Ridge Ave, Seattle",
                    radiusMeters = 500,
                    expectedSchedule = "All Day"
                )
            )
            dao.insertSafeZone(
                SafeZone(
                    name = "School",
                    type = "School",
                    address = "Lincoln High School, Seattle",
                    radiusMeters = 300,
                    expectedSchedule = "8:30 AM - 4:30 PM"
                )
            )
            dao.insertSafeZone(
                SafeZone(
                    name = "Library",
                    type = "Library",
                    address = "Central District Library",
                    radiusMeters = 1000,
                    expectedSchedule = "Weekend Afternoons"
                )
            )
            dao.insertSafeZone(
                SafeZone(
                    name = "Park",
                    type = "Park",
                    address = "Woodland Park",
                    radiusMeters = 1500,
                    expectedSchedule = "Flexible"
                )
            )

            // Initial journey
            dao.insertJourney(
                JourneyRecord(
                    studentName = "Alex",
                    origin = "School",
                    destination = "Home",
                    expectedArrival = "5:00 PM",
                    currentEta = "12 min",
                    remainingDistance = "2.4 km",
                    batteryPercent = 84,
                    progressPercent = 64
                )
            )

            // Initial timeline events matching screenshot
            dao.insertTimelineEvent(
                TimelineEvent(
                    title = "Journey Started",
                    timeFormatted = "4:12 PM",
                    locationOrStatus = "School",
                    iconType = "car"
                )
            )
            dao.insertTimelineEvent(
                TimelineEvent(
                    title = "Location Shared",
                    timeFormatted = "4:15 PM",
                    locationOrStatus = "Active",
                    iconType = "location"
                )
            )
            dao.insertTimelineEvent(
                TimelineEvent(
                    title = "Safe Zone Entered",
                    timeFormatted = "4:32 PM",
                    locationOrStatus = "Home (Zone)",
                    iconType = "safe_zone"
                )
            )
            dao.insertTimelineEvent(
                TimelineEvent(
                    title = "Camera Check",
                    timeFormatted = "4:45 PM",
                    locationOrStatus = "Verified",
                    iconType = "camera"
                )
            )
            dao.insertTimelineEvent(
                TimelineEvent(
                    title = "Journey Completed",
                    timeFormatted = "5:00 PM",
                    locationOrStatus = "Arrived Home",
                    iconType = "flag"
                )
            )
        }
    }
}
