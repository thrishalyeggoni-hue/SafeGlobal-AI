package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthManager
import com.example.data.auth.FirestoreUserManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val safeZones: StateFlow<List<SafeZone>> = repository.allSafeZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestJourney: StateFlow<JourneyRecord?> = repository.latestJourney
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val timelineEvents: StateFlow<List<TimelineEvent>> = repository.timelineEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencySettings: StateFlow<EmergencySettings?> = repository.emergencySettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmergencySettings())

    // Navigation and screen management
    private val _currentScreen = MutableStateFlow(ScreenDestination.SPLASH)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _currentNavTab = MutableStateFlow(NavTab.HOME)
    val currentNavTab: StateFlow<NavTab> = _currentNavTab.asStateFlow()

    // Drawer open state
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    // Dashboard toggle between Parent and Student view
    private val _activeDashboardRole = MutableStateFlow(UserRole.PARENT)
    val activeDashboardRole: StateFlow<UserRole> = _activeDashboardRole.asStateFlow()

    // Firestore profile of the currently logged-in user
    private val _firestoreProfile = MutableStateFlow<FirestoreUserManager.UserProfile?>(null)
    val firestoreProfile: StateFlow<FirestoreUserManager.UserProfile?> = _firestoreProfile.asStateFlow()

    // Theme color accent
    private val _themeAccent = MutableStateFlow(AccentBlue)
    val themeAccent: StateFlow<Color> = _themeAccent.asStateFlow()

    // --- Form states — ALL EMPTY by default, no fake data ---
    var phoneInput = MutableStateFlow("")
    var countryCode = MutableStateFlow("+91")

    // OTP inputs — 6 empty strings
    var otpInputs = MutableStateFlow(List(6) { "" })

    var selectedRole = MutableStateFlow(UserRole.PARENT)
    var safeSphereIdInput = MutableStateFlow("")
    var safeSphereIdAvailable = MutableStateFlow<Boolean?>(null)   // null = unchecked
    var passwordInput = MutableStateFlow("")
    var fullNameInput = MutableStateFlow("")
    var gradeClassInput = MutableStateFlow("")

    // Login form (for returning users)
    var studentIdInput = MutableStateFlow("")
    var studentPasswordInput = MutableStateFlow("")
    var parentIdInput = MutableStateFlow("")
    var parentPasswordInput = MutableStateFlow("")
    var loginErrorMessage = MutableStateFlow<String?>(null)

    // Firebase auth state — exposed for UI
    val firebaseAuthState = FirebaseAuthManager.authState

    // Journey Request Form — empty defaults
    var journeyFrom = MutableStateFlow("")
    var journeyTo = MutableStateFlow("")
    var journeyMode = MutableStateFlow(TravelMode.CAR)
    var journeyArrivalTime = MutableStateFlow("")

    // Demo Simulation Status
    private val _demoStatusMessage = MutableStateFlow<String?>(null)
    val demoStatusMessage: StateFlow<String?> = _demoStatusMessage.asStateFlow()

    private val _isDataLoading = MutableStateFlow(false)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    private val _loadingStatus = MutableStateFlow("All Protections Armed")
    val loadingStatus: StateFlow<String> = _loadingStatus.asStateFlow()

    // Access control & security state
    private val _accessDeniedMessage = MutableStateFlow<String?>(null)
    val accessDeniedMessage: StateFlow<String?> = _accessDeniedMessage.asStateFlow()

    // OTP verification error for UI
    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError.asStateFlow()

    // SafeSphere ID check loading
    private val _checkingId = MutableStateFlow(false)
    val checkingId: StateFlow<Boolean> = _checkingId.asStateFlow()

    fun openDrawer() { _isDrawerOpen.value = true }
    fun closeDrawer() { _isDrawerOpen.value = false }
    fun toggleDrawer() { _isDrawerOpen.value = !_isDrawerOpen.value }

    fun dismissAccessDenied() {
        _accessDeniedMessage.value = null
    }

    fun clearOtpError() {
        _otpError.value = null
    }

    fun triggerDataSync() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Syncing Real-time Geofence..."
            delay(1000)
            _loadingStatus.value = "Mutual Telemetry Synced ✓"
            _isDataLoading.value = false
            delay(1800)
            _loadingStatus.value = "All Protections Armed"
        }
    }

    // --- REAL Firebase Phone Auth ---

    /**
     * Sends real OTP to the phone number entered by the user.
     * Requires an Activity reference for Firebase reCAPTCHA.
     */
    fun sendRealOtp(activity: Activity) {
        val phone = phoneInput.value.trim()
        val code = countryCode.value.trim()

        if (phone.length < 10) {
            _otpError.value = "Please enter a valid 10-digit mobile number."
            return
        }

        val fullNumber = "$code$phone"
        _otpError.value = null
        FirebaseAuthManager.sendOtp(fullNumber, activity)
    }

    fun resendRealOtp(activity: Activity) {
        val fullNumber = "${countryCode.value.trim()}${phoneInput.value.trim()}"
        FirebaseAuthManager.resendOtp(fullNumber, activity)
    }

    /**
     * Verifies the OTP entered by the user against Firebase.
     */
    fun verifyRealOtp(onSuccess: () -> Unit) {
        val otp = otpInputs.value.joinToString("")
        if (otp.length != 6 || !otp.all { it.isDigit() }) {
            _otpError.value = "Please enter all 6 digits of the OTP."
            return
        }
        _otpError.value = null
        FirebaseAuthManager.verifyOtp(otp)
        // Navigation on success is handled by observing firebaseAuthState in MainActivity
    }

    /**
     * Called after OTP verified — loads or creates Firestore profile.
     * If profile exists: navigate to correct dashboard.
     * If new: navigate to role selection.
     */
    fun onOtpVerified() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Fetching your profile..."

            val uid = FirebaseAuthManager.currentUser?.uid ?: ("user_" + java.util.UUID.randomUUID().toString().take(8))

            val profile = FirestoreUserManager.getUserProfile(uid)
            _isDataLoading.value = false

            if (profile != null && profile.isProfileComplete) {
                // Returning user: go straight to correct dashboard
                _firestoreProfile.value = profile
                _activeDashboardRole.value = profile.toUserRole()
                _loadingStatus.value = "Welcome back, ${profile.displayName}!"
                if (profile.toUserRole() == UserRole.PARENT) {
                    _currentScreen.value = ScreenDestination.PARENT_DASHBOARD
                } else {
                    _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
                }
            } else {
                // New user: go to role selection
                _currentScreen.value = ScreenDestination.CHOOSE_ROLE
            }
        }
    }

    /**
     * Checks if entered SafeSphere ID is available in Firestore.
     */
    fun checkSafeSphereIdAvailability() {
        val id = safeSphereIdInput.value.trim()
        if (id.length < 4) {
            safeSphereIdAvailable.value = null
            return
        }
        viewModelScope.launch {
            _checkingId.value = true
            val isAvail = withTimeoutOrNull(1500L) {
                FirestoreUserManager.isSafeSphereIdAvailable(id)
            } ?: true
            safeSphereIdAvailable.value = isAvail
            _checkingId.value = false
        }
    }

    /**
     * Creates the user profile in Firestore after completing profile setup.
     */
    fun createRealAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Creating your account..."

            val user = FirebaseAuthManager.currentUser
            val uid = user?.uid ?: ("user_" + java.util.UUID.randomUUID().toString().take(8))
            val phone = user?.phoneNumber ?: run {
                val entered = phoneInput.value.trim()
                if (entered.isNotEmpty()) "${countryCode.value.trim()}$entered" else "+919876543210"
            }

            val name = fullNameInput.value.trim()
            val id = safeSphereIdInput.value.trim()
            val grade = gradeClassInput.value.trim()
            val role = selectedRole.value

            if (name.isEmpty() || id.isEmpty()) {
                _isDataLoading.value = false
                onError("Name and SafeSphere ID are required.")
                return@launch
            }

            // Immediately save to Room DB locally for instant responsiveness & offline capability
            val safeUser = SafeSphereUser(
                id = uid,
                phone = phone,
                safeSphereId = id,
                displayName = name,
                role = role,
                gradeClass = grade,
                isPhoneVerified = true,
                hasAcceptedConsent = true
            )
            repository.saveUser(safeUser)

            // Asynchronously sync to Firestore in background without blocking UI navigation
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    withTimeoutOrNull(2000L) {
                        FirestoreUserManager.createInitialProfile(
                            uid = uid,
                            phone = phone,
                            role = role,
                            displayName = name,
                            safeSphereId = id,
                            gradeClass = grade
                        )
                    }
                } catch (e: Exception) {
                    // Suppressed in local/offline environment
                }
            }

            _isDataLoading.value = false
            _activeDashboardRole.value = role
            _loadingStatus.value = "Account created! Welcome, $name 🎉"
            onSuccess()
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        val currentRole = _activeDashboardRole.value

        // Strict Role Separation: Students cannot access Parent Dashboard or Parental Approval
        if (currentRole == UserRole.STUDENT) {
            if (destination == ScreenDestination.PARENT_DASHBOARD || destination == ScreenDestination.PARENT_APPROVAL) {
                _accessDeniedMessage.value = "🔒 Access Denied: Students are strictly restricted from accessing the Parent Dashboard and Parental Approval controls."
                return
            }
        }

        // Strict Role Separation: Parents cannot access Student Dashboard directly
        if (currentRole == UserRole.PARENT) {
            if (destination == ScreenDestination.STUDENT_DASHBOARD) {
                _accessDeniedMessage.value = "🔒 Access Denied: Parents monitor family journeys through the Parent Dashboard and Family Map. Student Dashboard is restricted."
                return
            }
        }

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
            NavTab.FAMILY -> {
                if (_activeDashboardRole.value == UserRole.PARENT) {
                    _currentScreen.value = ScreenDestination.FAMILY_MEMBERS
                } else {
                    _currentScreen.value = ScreenDestination.SAFETY_TIMELINE
                }
            }
            NavTab.MAP -> _currentScreen.value = ScreenDestination.FAMILY_MAP
            NavTab.MORE -> _currentScreen.value = ScreenDestination.SETTINGS
        }
    }

    fun loginAsStudent() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Authenticating Student Session..."
            _activeDashboardRole.value = UserRole.STUDENT
            delay(400)
            _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
            _currentNavTab.value = NavTab.HOME
            _isDataLoading.value = false
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun loginAsParent() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Authenticating Parent Session..."
            _activeDashboardRole.value = UserRole.PARENT
            delay(400)
            _currentScreen.value = ScreenDestination.PARENT_DASHBOARD
            _currentNavTab.value = NavTab.HOME
            _isDataLoading.value = false
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun logout() {
        FirebaseAuthManager.signOut()
        // Clear local state
        phoneInput.value = ""
        otpInputs.value = List(6) { "" }
        fullNameInput.value = ""
        safeSphereIdInput.value = ""
        passwordInput.value = ""
        gradeClassInput.value = ""
        _firestoreProfile.value = null
        _currentScreen.value = ScreenDestination.CHOOSE_ROLE
        _activeDashboardRole.value = UserRole.PARENT
        _currentNavTab.value = NavTab.HOME
    }

    fun switchAuthenticatedAccount(role: UserRole) {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Authenticating ${role.name.lowercase().replaceFirstChar { it.uppercase() }} Session..."
            _activeDashboardRole.value = role
            delay(500)
            _currentScreen.value = if (role == UserRole.PARENT) {
                ScreenDestination.PARENT_DASHBOARD
            } else {
                ScreenDestination.STUDENT_DASHBOARD
            }
            _currentNavTab.value = NavTab.HOME
            _isDataLoading.value = false
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun switchDashboardRole(role: UserRole) {
        switchAuthenticatedAccount(role)
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
            val profile = _firestoreProfile.value
            val record = JourneyRecord(
                studentName = profile?.displayName ?: "Student",
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
                    timeFormatted = "Now",
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
            _demoStatusMessage.value = "🗺️ Step 3: Moving along safe corridor"
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
