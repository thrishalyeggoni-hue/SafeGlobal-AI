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
    val phone: String = "+91 98765 43210",
    val safeSphereId: String = "safefamily123",
    val displayName: String = "Sarah Sharma",
    val role: UserRole = UserRole.PARENT,
    val gradeClass: String = "10th Grade",
    val profilePhotoUrl: String = "",
    val familyId: String = "SF-8X21P",
    val pairingCode: String = "748291",
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
    val expectedSchedule: String,
    val isActive: Boolean = true
)

@Entity(tableName = "journeys")
data class JourneyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String = "Alex",
    val origin: String = "School",
    val destination: String = "Home",
    val travelMode: TravelMode = TravelMode.CAR,
    val expectedArrival: String = "5:00 PM",
    val currentEta: String = "12 min",
    val remainingDistance: String = "2.4 km",
    val batteryPercent: Int = 84,
    val status: JourneyStatus = JourneyStatus.ACTIVE,
    val riskState: RiskState = RiskState.NORMAL,
    val startTime: String = "4:12 PM",
    val progressPercent: Int = 64
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
    val trustedContactName: String = "Dad",
    val trustedContactPhone: String = "+91 98765 43211",
    val shakeDetectionEnabled: Boolean = true,
    val autoShareLocation: Boolean = true,
    val geofenceAlertsEnabled: Boolean = true,
    val cameraVerificationEnabled: Boolean = true
)
