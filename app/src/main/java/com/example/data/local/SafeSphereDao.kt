package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EmergencySettings
import com.example.data.model.JourneyRecord
import com.example.data.model.SafeSphereUser
import com.example.data.model.SafeZone
import com.example.data.model.TimelineEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeSphereDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUser(userId: String = "user_default"): Flow<SafeSphereUser?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: SafeSphereUser)

    @Query("SELECT * FROM safe_zones ORDER BY id ASC")
    fun getAllSafeZones(): Flow<List<SafeZone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeZone(safeZone: SafeZone)

    @Query("DELETE FROM safe_zones WHERE id = :id")
    suspend fun deleteSafeZone(id: Long)

    @Query("SELECT * FROM journeys ORDER BY id DESC LIMIT 1")
    fun getLatestJourney(): Flow<JourneyRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneyRecord): Long

    @Update
    suspend fun updateJourney(journey: JourneyRecord)

    @Query("SELECT * FROM timeline_events ORDER BY id ASC")
    fun getTimelineEvents(): Flow<List<TimelineEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvent(event: TimelineEvent)

    @Query("DELETE FROM timeline_events")
    suspend fun clearTimeline()

    @Query("SELECT * FROM emergency_settings WHERE id = 1 LIMIT 1")
    fun getEmergencySettings(): Flow<EmergencySettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencySettings(settings: EmergencySettings)
}
