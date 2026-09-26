package com.example.data.repository

import com.example.data.local.SafeSphereDao
import com.example.data.model.EmergencySettings
import com.example.data.model.JourneyRecord
import com.example.data.model.SafeSphereUser
import com.example.data.model.SafeZone
import com.example.data.model.TimelineEvent
import kotlinx.coroutines.flow.Flow

class SafeSphereRepository(private val dao: SafeSphereDao) {
    val currentUser: Flow<SafeSphereUser?> = dao.getUser()
    val allSafeZones: Flow<List<SafeZone>> = dao.getAllSafeZones()
    val latestJourney: Flow<JourneyRecord?> = dao.getLatestJourney()
    val timelineEvents: Flow<List<TimelineEvent>> = dao.getTimelineEvents()
    val emergencySettings: Flow<EmergencySettings?> = dao.getEmergencySettings()

    suspend fun saveUser(user: SafeSphereUser) = dao.insertUser(user)

    suspend fun addSafeZone(safeZone: SafeZone) = dao.insertSafeZone(safeZone)

    suspend fun removeSafeZone(id: Long) = dao.deleteSafeZone(id)

    suspend fun insertJourney(journey: JourneyRecord) = dao.insertJourney(journey)

    suspend fun updateJourney(journey: JourneyRecord) = dao.updateJourney(journey)

    suspend fun addTimelineEvent(event: TimelineEvent) = dao.insertTimelineEvent(event)

    suspend fun clearTimeline() = dao.clearTimeline()

    suspend fun updateEmergencySettings(settings: EmergencySettings) = dao.insertEmergencySettings(settings)
}
