package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    PARENT,
    STUDENT
}

enum class TravelMode {
    CAR,
    BUS,
    WALK
}

enum class JourneyStatus {
    PENDING_APPROVAL,
    APPROVED,
    DECLINED,
    ACTIVE,
    COMPLETED,
    DEVIATION_ALERT,
    EMERGENCY
}

enum class RiskState {
    NORMAL,
    CHECK_REQUIRED,
    ELEVATED,
    EMERGENCY
}

@Entity(tableName = "users")
data class SafeSphereUser(
    @PrimaryKey val id: String = "user_default",
    val phone: String = "",
    val safeSphereId: String = "",
    val displayName: String = "User",
    val role: UserRole = UserRole.PARENT,
    val gradeClass: String = "",
    val profilePhotoUrl: String = "",
    val familyId: String = "",
    val pairingCode: String = "",
    val avatarIndex: Int = 1,
    val themeColorHex: String = "#1652F0",
    val isPhoneVerified: Boolean = true,
    val hasAcceptedConsent: Boolean = true
)

@Entity(tableName = "safe_zones")
data class SafeZone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "Home", "School", "Library", "Park"
    val address: String,
    val radiusMeters: Int,
    val expectedSchedule: String = "24/7 Mon-Sun",
    val isActive: Boolean = true
)

@Entity(tableName = "journeys")
data class JourneyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String = "Student",
    val origin: String = "",
    val destination: String = "",
    val travelMode: TravelMode = TravelMode.CAR,
    val expectedArrival: String = "",
    val currentEta: String = "",
    val remainingDistance: String = "",
    val batteryPercent: Int = 100,
    val status: JourneyStatus = JourneyStatus.COMPLETED,
    val riskState: RiskState = RiskState.NORMAL,
    val startTime: String = "",
    val progressPercent: Int = 0
)

@Entity(tableName = "timeline_events")
data class TimelineEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val journeyId: Long = 1,
    val title: String,
    val timeFormatted: String,
    val locationOrStatus: String,
    val iconType: String, // "car", "location", "safe_zone", "camera", "flag"
    val isCompleted: Boolean = true
)

@Entity(tableName = "emergency_settings")
data class EmergencySettings(
    @PrimaryKey val id: Int = 1,
    val normalPin: String = "4821",
    val duressPin: String = "4822",
    val trustedContactName: String = "",
    val trustedContactPhone: String = "",
    val shakeDetectionEnabled: Boolean = true,
    val autoShareLocation: Boolean = true,
    val geofenceAlertsEnabled: Boolean = true,
    val cameraVerificationEnabled: Boolean = true
)
