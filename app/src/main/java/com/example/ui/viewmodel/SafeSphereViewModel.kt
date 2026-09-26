package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SafeSphereDatabase
import com.example.data.model.EmergencySettings
import com.example.data.model.JourneyRecord
import com.example.data.model.JourneyStatus
import com.example.data.model.RiskState
import com.example.data.model.SafeSphereUser
import com.example.data.model.SafeZone
import com.example.data.model.TimelineEvent
import com.example.data.model.TravelMode
import com.example.data.model.UserRole
import com.example.data.repository.SafeSphereRepository
import com.example.ui.components.NavTab
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    SPLASH,
    LOADING,
    CONSENT,
    PHONE_VERIFY,
    OTP_VERIFY,
    CHOOSE_ROLE,
    CREATE_ID,
    CREATE_PASSWORD,
    COMPLETE_PROFILE,
    PARENT_DASHBOARD,
    STUDENT_DASHBOARD,
    FAMILY_MAP,
    REQUEST_JOURNEY,
    PARENT_APPROVAL,
    SAFETY_TIMELINE,
    EMERGENCY,
    SETTINGS,
    DEMO_SIMULATOR,
    SAFE_ZONES,
    FAMILY_MEMBERS,
    PROFILE
}

class SafeSphereViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SafeSphereRepository

    init {
        val db = SafeSphereDatabase.getDatabase(application)
        repository = SafeSphereRepository(db.dao())
    }

    val currentUser: StateFlow<SafeSphereUser?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SafeSphereUser())

    val safeZones: StateFlow<List<SafeZone>> = repository.allSafeZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestJourney: StateFlow<JourneyRecord?> = repository.latestJourney
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JourneyRecord())

    val timelineEvents: StateFlow<List<TimelineEvent>> = repository.timelineEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencySettings: StateFlow<EmergencySettings?> = repository.emergencySettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmergencySettings())

    // Navigation and screen management
    private val _currentScreen = MutableStateFlow(ScreenDestination.SPLASH)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _currentNavTab = MutableStateFlow(NavTab.HOME)
    val currentNavTab: StateFlow<NavTab> = _currentNavTab.asStateFlow()

    // Dashboard toggle between Parent and Student view
    private val _activeDashboardRole = MutableStateFlow(UserRole.PARENT)
    val activeDashboardRole: StateFlow<UserRole> = _activeDashboardRole.asStateFlow()

    // Theme color accent
    private val _themeAccent = MutableStateFlow(AccentBlue)
    val themeAccent: StateFlow<Color> = _themeAccent.asStateFlow()

    // Form states
    var phoneInput = MutableStateFlow("9876543210")
    var otpInputs = MutableStateFlow(listOf("1", "2", "3", "4", "5", "6"))
    var selectedRole = MutableStateFlow(UserRole.PARENT)
    var safeSphereIdInput = MutableStateFlow("safefamily123")
    var passwordInput = MutableStateFlow("SecurePass123!")
    var fullNameInput = MutableStateFlow("John Doe")
    var gradeClassInput = MutableStateFlow("10th Grade")

    // Journey Request Form
    var journeyFrom = MutableStateFlow("School")
    var journeyTo = MutableStateFlow("Home")
    var journeyMode = MutableStateFlow(TravelMode.CAR)
    var journeyArrivalTime = MutableStateFlow("5:00 PM")

    // Demo Simulation Status
    private val _demoStatusMessage = MutableStateFlow<String?>(null)
    val demoStatusMessage: StateFlow<String?> = _demoStatusMessage.asStateFlow()

    private val _isDataLoading = MutableStateFlow(false)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    private val _loadingStatus = MutableStateFlow("All Protections Armed")
    val loadingStatus: StateFlow<String> = _loadingStatus.asStateFlow()

    fun triggerDataSync() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Syncing Real-time Geofence..."
            delay(1200)
            _loadingStatus.value = "Mutual Telemetry Synced ✓"
            _isDataLoading.value = false
            delay(2000)
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
        when (destination) {
            ScreenDestination.PARENT_DASHBOARD, ScreenDestination.STUDENT_DASHBOARD -> _currentNavTab.value = NavTab.HOME
            ScreenDestination.FAMILY_MEMBERS, ScreenDestination.PARENT_APPROVAL -> _currentNavTab.value = NavTab.FAMILY
            ScreenDestination.FAMILY_MAP -> _currentNavTab.value = NavTab.MAP
            ScreenDestination.SETTINGS, ScreenDestination.PROFILE -> _currentNavTab.value = NavTab.MORE
            else -> {}
        }
    }

    fun selectNavTab(tab: NavTab) {
        _currentNavTab.value = tab
        when (tab) {
            NavTab.HOME -> {
                if (_activeDashboardRole.value == UserRole.PARENT) {
                    _currentScreen.value = ScreenDestination.PARENT_DASHBOARD
                } else {
                    _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
                }
            }
            NavTab.FAMILY -> _currentScreen.value = ScreenDestination.FAMILY_MEMBERS
            NavTab.MAP -> _currentScreen.value = ScreenDestination.FAMILY_MAP
            NavTab.MORE -> _currentScreen.value = ScreenDestination.SETTINGS
        }
    }

    fun switchDashboardRole(role: UserRole) {
        _activeDashboardRole.value = role
        if (role == UserRole.PARENT) {
            _currentScreen.value = ScreenDestination.PARENT_DASHBOARD
        } else {
            _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
        }
    }

    fun setAppThemeColor(colorName: String) {
        _themeAccent.value = when (colorName.lowercase()) {
            "violet" -> AccentViolet
            "blue" -> AccentBlue
            "green" -> AccentGreen
            "teal" -> AccentTeal
            "orange" -> AccentOrange
            "rose" -> AccentRose
            "indigo" -> AccentIndigo
            else -> AccentBlue
        }
    }

    fun submitJourneyRequest() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Broadcasting Route Request..."
            val record = JourneyRecord(
                studentName = "Alex",
                origin = journeyFrom.value,
                destination = journeyTo.value,
                travelMode = journeyMode.value,
                expectedArrival = journeyArrivalTime.value,
                status = JourneyStatus.PENDING_APPROVAL,
                riskState = RiskState.NORMAL
            )
            repository.updateJourney(record)
            delay(500)
            _isDataLoading.value = false
            _loadingStatus.value = "Awaiting Parent Approval"
            _currentScreen.value = ScreenDestination.PARENT_APPROVAL
        }
    }

    fun approveJourney() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Establishing Secure Corridor..."
            val updated = latestJourney.value?.copy(
                status = JourneyStatus.ACTIVE,
                currentEta = "12 min",
                remainingDistance = "2.4 km",
                progressPercent = 64
            ) ?: JourneyRecord()
            repository.updateJourney(updated)
            repository.addTimelineEvent(
                TimelineEvent(
                    title = "Parent Approved",
                    timeFormatted = "4:14 PM",
                    locationOrStatus = "Consent Confirmed",
                    iconType = "check"
                )
            )
            delay(600)
            _isDataLoading.value = false
            _loadingStatus.value = "Active Telemetry Stream"
            _currentScreen.value = ScreenDestination.FAMILY_MAP
        }
    }

    fun denyJourney() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Updating Request Status..."
            val updated = latestJourney.value?.copy(
                status = JourneyStatus.DECLINED
            ) ?: JourneyRecord()
            repository.updateJourney(updated)
            delay(400)
            _isDataLoading.value = false
            _loadingStatus.value = "All Protections Armed"
            _currentScreen.value = ScreenDestination.PARENT_DASHBOARD
        }
    }

    fun triggerImOk() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Broadcasting 'I'm OK'..."
            repository.addTimelineEvent(
                TimelineEvent(
                    title = "Student Checked In (I'm OK)",
                    timeFormatted = "Just now",
                    locationOrStatus = "Safe",
                    iconType = "check"
                )
            )
            delay(600)
            _isDataLoading.value = false
            _loadingStatus.value = "Check-in Confirmed ✓"
            _demoStatusMessage.value = "✓ 'I'm OK' notification sent to linked parents!"
            delay(2500)
            _demoStatusMessage.value = null
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun submitPin(enteredPin: String, onSuccess: () -> Unit, onDuress: () -> Unit) {
        val settings = emergencySettings.value ?: EmergencySettings()
        if (enteredPin == settings.duressPin) {
            // Duress pin triggered: silently initiate emergency
            viewModelScope.launch {
                _isDataLoading.value = true
                _loadingStatus.value = "Updating Status..."
                val updated = latestJourney.value?.copy(
                    riskState = RiskState.EMERGENCY,
                    status = JourneyStatus.EMERGENCY
                ) ?: JourneyRecord()
                repository.updateJourney(updated)
                repository.addTimelineEvent(
                    TimelineEvent(
                        title = "Emergency Triggered (Duress PIN)",
                        timeFormatted = "Just now",
                        locationOrStatus = "High Alert Broadcast",
                        iconType = "flag"
                    )
                )
                delay(500)
                _isDataLoading.value = false
                _loadingStatus.value = "Status Updated ✓"
            }
            onDuress()
        } else if (enteredPin == settings.normalPin) {
            onSuccess()
        }
    }

    fun runDemoSimulation() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Simulating Travel Corridor..."
            _demoStatusMessage.value = "🚀 Running SafeSphere Demo Simulation..."
            delay(1000)
            _loadingStatus.value = "Telemetry: School → Home"
            _demoStatusMessage.value = "📍 Step 1: Student requested travel from School -> Home"
            delay(1200)
            _loadingStatus.value = "Parent Consent Verified"
            _demoStatusMessage.value = "✓ Step 2: Parent approved. Telemetry active."
            delay(1200)
            _loadingStatus.value = "Within Safe Corridor"
            _demoStatusMessage.value = "🗺️ Step 3: Moving along safe corridor (Highland Way)"
            delay(1200)
            _isDataLoading.value = false
            _loadingStatus.value = "Simulation Complete ✓"
            _demoStatusMessage.value = "✨ Simulation Complete: All systems operational!"
            delay(2500)
            _demoStatusMessage.value = null
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun addSafeZone(name: String, type: String, address: String, radius: Int) {
        viewModelScope.launch {
            repository.addSafeZone(
                SafeZone(
                    name = name,
                    type = type,
                    address = address,
                    radiusMeters = radius,
                    expectedSchedule = "Active Schedule"
                )
            )
        }
    }

    fun removeSafeZone(id: Long) {
        viewModelScope.launch {
            repository.removeSafeZone(id)
        }
    }
}
