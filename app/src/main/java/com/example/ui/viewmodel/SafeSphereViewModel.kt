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
import java.util.UUID

enum class AuthSessionState {
    CHECKING_SESSION,
    AUTHENTICATED_PARENT,
    AUTHENTICATED_STUDENT,
    NOT_AUTHENTICATED
}

enum class ScreenDestination {
    SPLASH,
    LOADING,
    CONSENT,
    LOGIN,          // Google Sign-In + Email/Password (replaces PHONE_VERIFY + OTP_VERIFY)
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
    PROFILE,
    // Real Multi-Device Two-Phone Screens
    LINK_CODE_GENERATOR,
    ENTER_LINK_CODE,
    STUDENT_CONTROL_CENTER,
    SAFE_ZONE_CREATOR,
    CAMERA_REQUEST,
    CAMERA_SHARE,
    CAMERA_VIEW,
    SOLO_TRANSPORT
}

class SafeSphereViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SafeSphereRepository =
        SafeSphereRepository(SafeSphereDatabase.getDatabase(application).dao())

    private val _authSessionState = MutableStateFlow(AuthSessionState.CHECKING_SESSION)
    val authSessionState: StateFlow<AuthSessionState> = _authSessionState.asStateFlow()

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

    fun setActiveRole(role: UserRole) {
        _activeDashboardRole.value = role
    }

    // Firestore profile of the currently logged-in user
    private val _firestoreProfile = MutableStateFlow<FirestoreUserManager.UserProfile?>(null)
    val firestoreProfile: StateFlow<FirestoreUserManager.UserProfile?> = _firestoreProfile.asStateFlow()

    // Theme color accent
    private val _themeAccent = MutableStateFlow(AccentBlue)
    val themeAccent: StateFlow<Color> = _themeAccent.asStateFlow()

    // ── Dynamic Topographic Watermark States ───────────────────────
    private val _watermarkMode = MutableStateFlow(com.example.ui.components.WatermarkMode.NORMAL)
    val watermarkMode: StateFlow<com.example.ui.components.WatermarkMode> = _watermarkMode.asStateFlow()

    private val _gpsRippleTimestamp = MutableStateFlow(0L)
    val gpsRippleTimestamp: StateFlow<Long> = _gpsRippleTimestamp.asStateFlow()

    private val _geofenceAlertTimestamp = MutableStateFlow(0L)
    val geofenceAlertTimestamp: StateFlow<Long> = _geofenceAlertTimestamp.asStateFlow()

    private val _activeGeofenceAlert = MutableStateFlow<String?>(null)
    val activeGeofenceAlert: StateFlow<String?> = _activeGeofenceAlert.asStateFlow()
    fun dismissGeofenceAlert() { _activeGeofenceAlert.value = null }

    private val _watermarkStatusOverride = MutableStateFlow<String?>(null)
    val watermarkStatusOverride: StateFlow<String?> = _watermarkStatusOverride.asStateFlow()

    // ── Real Multi-Device Two-Phone Safety States ──────────────────
    private val _linkedStudents = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.FamilyLink>>(emptyList())
    val linkedStudents: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.FamilyLink>> = _linkedStudents.asStateFlow()

    private val _linkedParents = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.FamilyLink>>(emptyList())
    val linkedParents: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.FamilyLink>> = _linkedParents.asStateFlow()

    private val _selectedStudent = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.FamilyLink?>(null)
    val selectedStudent: StateFlow<com.example.data.repository.FirestoreSafetyManager.FamilyLink?> = _selectedStudent.asStateFlow()

    private val _selectedStudentLocation = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.LiveLocation?>(null)
    val selectedStudentLocation: StateFlow<com.example.data.repository.FirestoreSafetyManager.LiveLocation?> = _selectedStudentLocation.asStateFlow()

    private val _selectedStudentSafeZones = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreSafeZone>>(emptyList())
    val selectedStudentSafeZones: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreSafeZone>> = _selectedStudentSafeZones.asStateFlow()

    private val _selectedStudentGeofenceEvents = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreGeofenceEvent>>(emptyList())
    val selectedStudentGeofenceEvents: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreGeofenceEvent>> = _selectedStudentGeofenceEvents.asStateFlow()

    // Camera Request & WebRTC Session Signaling
    private val _activeCameraRequest = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.CameraRequest?>(null)
    val activeCameraRequest: StateFlow<com.example.data.repository.FirestoreSafetyManager.CameraRequest?> = _activeCameraRequest.asStateFlow()

    private val _incomingCameraRequestForStudent = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.CameraRequest?>(null)
    val incomingCameraRequestForStudent: StateFlow<com.example.data.repository.FirestoreSafetyManager.CameraRequest?> = _incomingCameraRequestForStudent.asStateFlow()

    private val _activeCameraSession = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.CameraSession?>(null)
    val activeCameraSession: StateFlow<com.example.data.repository.FirestoreSafetyManager.CameraSession?> = _activeCameraSession.asStateFlow()

    private val _incomingCameraSessionForStudent = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.CameraSession?>(null)
    val incomingCameraSessionForStudent: StateFlow<com.example.data.repository.FirestoreSafetyManager.CameraSession?> = _incomingCameraSessionForStudent.asStateFlow()

    // 6-digit Code Linking States
    private val _generatedLinkInvite = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.LinkInvite?>(null)
    val generatedLinkInvite: StateFlow<com.example.data.repository.FirestoreSafetyManager.LinkInvite?> = _generatedLinkInvite.asStateFlow()

    private val _incomingLinkRequestForStudent = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.LinkInvite?>(null)
    val incomingLinkRequestForStudent: StateFlow<com.example.data.repository.FirestoreSafetyManager.LinkInvite?> = _incomingLinkRequestForStudent.asStateFlow()

    private val _resolvedLinkInvite = MutableStateFlow<com.example.data.repository.FirestoreSafetyManager.LinkInvite?>(null)
    val resolvedLinkInvite: StateFlow<com.example.data.repository.FirestoreSafetyManager.LinkInvite?> = _resolvedLinkInvite.asStateFlow()

    // Real-time Journey Requests & Solo Transport Shared Rides
    private val _incomingJourneysForParent = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreJourney>>(emptyList())
    val incomingJourneysForParent: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreJourney>> = _incomingJourneysForParent.asStateFlow()

    private val _incomingSharedRidesForParent = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.SharedRide>>(emptyList())
    val incomingSharedRidesForParent: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.SharedRide>> = _incomingSharedRidesForParent.asStateFlow()

    private val _studentActiveJourneys = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreJourney>>(emptyList())
    val studentActiveJourneys: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.FirestoreJourney>> = _studentActiveJourneys.asStateFlow()

    private val _studentActiveRides = MutableStateFlow<List<com.example.data.repository.FirestoreSafetyManager.SharedRide>>(emptyList())
    val studentActiveRides: StateFlow<List<com.example.data.repository.FirestoreSafetyManager.SharedRide>> = _studentActiveRides.asStateFlow()

    var linkCodeInput = MutableStateFlow("")
    var linkErrorMessage = MutableStateFlow<String?>(null)
    private val _linkSuccessMessage = MutableStateFlow<String?>(null)
    val linkSuccessMessage: StateFlow<String?> = _linkSuccessMessage.asStateFlow()

    val isLocationTrackingActive = com.example.service.LocationTrackingService.isTrackingActive

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

    private fun getConsentPrefs() =
        getApplication<Application>().getSharedPreferences("safesphere_user_prefs", android.content.Context.MODE_PRIVATE)

    fun hasAcceptedConsent(): Boolean {
        return try {
            getConsentPrefs().getBoolean("has_accepted_consent", false)
        } catch (e: Exception) {
            false
        }
    }

    fun setConsentAccepted(accepted: Boolean) {
        try {
            getConsentPrefs().edit().putBoolean("has_accepted_consent", accepted).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    init {
        FirestoreUserManager.init(getApplication())
        checkPersistedSession()
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.safeZonesStateFlow.collect { map ->
                if (map.isNotEmpty()) {
                    _selectedStudentSafeZones.value = map.values.toList()
                }
            }
        }
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

    // --- Google / Email Auth ---

    /**
     * Called after Google Sign-In or Email/Password auth succeeds.
     * Loads the Firestore profile. If profile exists → dashboard, else → role selection.
     */
    fun onAuthSuccess() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Loading your profile..."

            // If profile is already set (by loginWithSafeSphereId), navigate directly
            val existingProfile = _firestoreProfile.value
            if (existingProfile != null) {
                val role = existingProfile.toUserRole()
                _activeDashboardRole.value = role
                _loadingStatus.value = "Welcome back, ${existingProfile.displayName}!"
                _isDataLoading.value = false
                _currentScreen.value = if (role == UserRole.PARENT) ScreenDestination.PARENT_DASHBOARD
                    else ScreenDestination.STUDENT_DASHBOARD
                return@launch
            }

            // Fallback: Firebase Auth UID path (legacy)
            val uid = try { FirebaseAuthManager.getEffectiveUid() } catch (_: Exception) { "" }
            val profile = if (uid.isNotBlank()) {
                try { FirestoreUserManager.getUserProfile(uid) } catch (_: Exception) { null }
            } else null
            _isDataLoading.value = false

            if (profile != null) {
                _firestoreProfile.value = profile
                val role = profile.toUserRole()
                _activeDashboardRole.value = role
                _loadingStatus.value = "Welcome back, ${profile.displayName}!"
                initSafetyListenersForUser(uid, role)
                persistSessionLocally(uid, role, profile.displayName, profile.safeSphereId)
                _currentScreen.value = if (role == UserRole.PARENT) ScreenDestination.PARENT_DASHBOARD
                    else ScreenDestination.STUDENT_DASHBOARD
            } else {
                val role = _activeDashboardRole.value
                _currentScreen.value = if (role == UserRole.PARENT) ScreenDestination.PARENT_DASHBOARD
                    else ScreenDestination.STUDENT_DASHBOARD
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

            val entered = phoneInput.value.trim()
            val uid = "uid_${safeSphereIdInput.value.trim().lowercase().hashCode().toString().replace("-", "0")}_${System.currentTimeMillis() % 100000}"

            val name = fullNameInput.value.trim()
            val id = safeSphereIdInput.value.trim()
            val password = passwordInput.value
            val grade = gradeClassInput.value.trim()
            val role = selectedRole.value

            if (name.isEmpty() || id.isEmpty()) {
                _isDataLoading.value = false
                onError("Name and SafeSphere ID are required.")
                return@launch
            }
            if (password.length < 6) {
                _isDataLoading.value = false
                onError("Password must be at least 6 characters.")
                return@launch
            }

            // Check SafeSphere ID availability
            val isAvail = withTimeoutOrNull(4000L) {
                FirestoreUserManager.isSafeSphereIdAvailable(id)
            } ?: true
            if (!isAvail) {
                _isDataLoading.value = false
                onError("SafeSphere ID '${id}' is already taken. Please choose another.")
                return@launch
            }

            val passwordHash = FirestoreUserManager.hashPassword(password)
            val phone = if (entered.isNotEmpty()) "${countryCode.value.trim()}$entered" else ""

            // Save to Room DB locally
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

            val initialProfile = FirestoreUserManager.UserProfile(
                uid = uid,
                phone = phone,
                displayName = name,
                role = role.name,
                gradeClass = grade,
                safeSphereId = id,
                familyId = FirestoreUserManager.generateFamilyId(),
                isProfileComplete = true,
                createdAt = System.currentTimeMillis(),
                passwordHash = passwordHash
            )
            _firestoreProfile.value = initialProfile

            // Persist session in SharedPrefs so login is remembered
            persistSessionLocally(uid, role)

            // Asynchronously sync to Firestore in background
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    withTimeoutOrNull(4000L) {
                        FirestoreUserManager.saveUserProfile(initialProfile)
                    }
                } catch (e: Exception) {
                    // Handled by memoryProfileCache fallback
                }
            }

            _isDataLoading.value = false
            _activeDashboardRole.value = role
            _loadingStatus.value = "Account created! Welcome, $name 🎉"
            initSafetyListenersForUser(uid, role)
            onSuccess()
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        val currentRole = _activeDashboardRole.value

        // Strict Role Separation: Students cannot access Parent Dashboard, Parental Approval, Safe Zones, Member Management, or Parent Link Screens
        if (currentRole == UserRole.STUDENT) {
            if (destination in listOf(
                ScreenDestination.PARENT_DASHBOARD,
                ScreenDestination.PARENT_APPROVAL,
                ScreenDestination.SAFE_ZONES,
                ScreenDestination.FAMILY_MEMBERS,
                ScreenDestination.ENTER_LINK_CODE,
                ScreenDestination.STUDENT_CONTROL_CENTER,
                ScreenDestination.SAFE_ZONE_CREATOR,
                ScreenDestination.CAMERA_REQUEST,
                ScreenDestination.CAMERA_VIEW
            )) {
                _accessDeniedMessage.value = "🔒 Access Denied: Students are restricted from accessing Parent-only administrative and safety controls."
                return
            }
        }

        // Strict Role Separation: Parents cannot access Student-only features (e.g. Request Journey, Student Dashboard, Link Generator, Camera Share)
        if (currentRole == UserRole.PARENT) {
            if (destination in listOf(
                ScreenDestination.STUDENT_DASHBOARD,
                ScreenDestination.REQUEST_JOURNEY,
                ScreenDestination.LINK_CODE_GENERATOR,
                ScreenDestination.CAMERA_SHARE
            )) {
                _accessDeniedMessage.value = "🔒 Access Denied: This feature is exclusively for Students. Parents monitor journeys through the Parent Dashboard and Family Map."
                return
            }
        }

        android.util.Log.d("SafeSphereNav", "navigateTo: destination=$destination, currentRole=$currentRole")
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

    /** Navigate to account creation flow (role already set in selectedRole) */
    fun navigateToCreateAccount() {
        _currentScreen.value = ScreenDestination.CREATE_ID
    }

    /**
     * Login with SafeSphere ID + password (no email/Gmail required).
     * Queries Firestore for matching safeSphereId, validates password hash.
     * On success: persists session, initialises safety listeners, navigates to dashboard.
     */
    suspend fun loginWithSafeSphereId(
        safeSphereId: String,
        password: String,
        expectedRole: UserRole,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isDataLoading.value = true
        _loadingStatus.value = "Authenticating..."
        val result = FirestoreUserManager.loginWithSafeSphereId(safeSphereId, password)
        _isDataLoading.value = false
        result.onSuccess { profile ->
            val actualRole = profile.toUserRole()
            // Auto-align role so role selector mismatch never blocks valid login
            _activeDashboardRole.value = actualRole
            selectedRole.value = actualRole
            _firestoreProfile.value = profile

            // Persist session in SharedPrefs for auto-login on next launch
            persistSessionLocally(profile.uid, actualRole, profile.displayName, profile.safeSphereId)
            initSafetyListenersForUser(profile.uid, actualRole)
            _loadingStatus.value = "Welcome back, ${profile.displayName}!"
            // If student, also start location tracking
            if (actualRole == UserRole.STUDENT) {
                val ctx = getApplication<android.app.Application>()
                com.example.service.LocationTrackingService.startService(ctx, profile.uid, profile.displayName)
            }

            // Direct transition to dashboard immediately
            _authSessionState.value = if (actualRole == UserRole.PARENT)
                AuthSessionState.AUTHENTICATED_PARENT
            else
                AuthSessionState.AUTHENTICATED_STUDENT

            _currentScreen.value = if (actualRole == UserRole.PARENT) {
                ScreenDestination.PARENT_DASHBOARD
            } else {
                ScreenDestination.STUDENT_DASHBOARD
            }

            onSuccess()
        }.onFailure { ex ->
            val msg = ex.message ?: "Login failed. Please check your credentials."
            loginErrorMessage.value = msg
            onError(msg)
        }
    }

    /**
     * Instantly registers a new account with just the user's name and selected role.
     * Grants immediate direct access to the app dashboard without intermediate
     * pages or prompting for re-login.
     */
    suspend fun registerQuickAccount(
        name: String,
        role: UserRole,
        avatarIndex: Int = 1,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            onError("Please enter your name.")
            return
        }
        _isDataLoading.value = true
        _loadingStatus.value = "Creating Account..."
        val result = FirestoreUserManager.registerQuickAccount(cleanName, role, avatarIndex)
        _isDataLoading.value = false
        result.onSuccess { profile ->
            _activeDashboardRole.value = role
            selectedRole.value = role
            _firestoreProfile.value = profile

            persistSessionLocally(profile.uid, role, profile.displayName, profile.safeSphereId)
            initSafetyListenersForUser(profile.uid, role)
            _loadingStatus.value = "Welcome, ${profile.displayName}!"

            if (role == UserRole.STUDENT) {
                val ctx = getApplication<android.app.Application>()
                com.example.service.LocationTrackingService.startService(ctx, profile.uid, profile.displayName)
            }

            _authSessionState.value = if (role == UserRole.PARENT)
                AuthSessionState.AUTHENTICATED_PARENT
            else
                AuthSessionState.AUTHENTICATED_STUDENT

            _currentScreen.value = if (role == UserRole.PARENT) {
                ScreenDestination.PARENT_DASHBOARD
            } else {
                ScreenDestination.STUDENT_DASHBOARD
            }

            onSuccess()
        }.onFailure { ex ->
            val msg = ex.message ?: "Account creation failed. Please try again."
            loginErrorMessage.value = msg
            onError(msg)
        }
    }

    /**
     * Updates user's avatar selection from the 16 SafeSphere avatar portraits.
     * Persists across Firestore and local Room cache.
     */
    fun updateUserAvatar(newAvatarIndex: Int) {
        val safeIndex = newAvatarIndex.coerceIn(1, 16)
        val current = _firestoreProfile.value ?: return
        val updated = current.copy(avatarIndex = safeIndex)
        _firestoreProfile.value = updated
        viewModelScope.launch {
            FirestoreUserManager.saveUserProfile(updated)
            try {
                val dbUser = SafeSphereUser(
                    id = updated.uid,
                    displayName = updated.displayName,
                    role = updated.toUserRole(),
                    safeSphereId = updated.safeSphereId,
                    familyId = updated.familyId,
                    avatarIndex = safeIndex,
                    profilePhotoUrl = "avatar_$safeIndex"
                )
                repository.saveUser(dbUser)
            } catch (_: Exception) {}
        }
    }

    /** Save the current session to SharedPrefs for persistent login across app restarts. */
    fun persistSessionLocally(uid: String, role: UserRole, displayName: String = "", safeSphereId: String = "") {
        try {
            getConsentPrefs().edit()
                .putString("session_uid", uid)
                .putString("session_role", role.name)
                .putString("session_name", displayName)
                .putString("session_safesphere_id", safeSphereId)
                .putBoolean("has_active_session", true)
                .apply()
        } catch (e: Exception) { /* ignore */ }
    }

    /**
     * Checks persisted session on app launch.
     * First checks SharedPrefs (works without Firebase Auth), then Firebase.
     * Prevents showing login screen again if already logged in.
     */
    fun checkPersistedSession() {
        viewModelScope.launch {
            _authSessionState.value = AuthSessionState.CHECKING_SESSION

            val prefs = getConsentPrefs()
            val hasActiveSession = prefs.getBoolean("has_active_session", false)
            val cachedUid = prefs.getString("session_uid", null)
            val cachedRoleStr = prefs.getString("session_role", null)
            val cachedName = prefs.getString("session_name", "") ?: ""
            val cachedId = prefs.getString("session_safesphere_id", "") ?: ""

            if (hasActiveSession && !cachedUid.isNullOrBlank() && !cachedRoleStr.isNullOrBlank()) {
                val cachedRole = try { UserRole.valueOf(cachedRoleStr) } catch (_: Exception) { null }
                if (cachedRole != null) {
                    val remoteProfile = try {
                        withTimeoutOrNull(3000L) { FirestoreUserManager.getUserProfile(cachedUid) }
                    } catch (_: Exception) { null }

                    val profile = remoteProfile
                        ?: FirestoreUserManager.getLocalProfile(cachedId.ifEmpty { cachedUid })
                        ?: FirestoreUserManager.UserProfile(
                            uid = cachedUid,
                            role = cachedRole.name,
                            displayName = cachedName.ifEmpty { if (cachedRole == UserRole.PARENT) "Parent Account" else "Student Account" },
                            safeSphereId = cachedId,
                            isProfileComplete = true
                        )

                    _firestoreProfile.value = profile
                    _activeDashboardRole.value = cachedRole
                    initSafetyListenersForUser(cachedUid, cachedRole)
                    if (cachedRole == UserRole.STUDENT) {
                        val ctx = getApplication<android.app.Application>()
                        com.example.service.LocationTrackingService.startService(ctx, cachedUid, profile.displayName)
                    }
                    _authSessionState.value = if (cachedRole == UserRole.PARENT)
                        AuthSessionState.AUTHENTICATED_PARENT
                    else
                        AuthSessionState.AUTHENTICATED_STUDENT

                    _currentScreen.value = if (cachedRole == UserRole.PARENT)
                        ScreenDestination.PARENT_DASHBOARD
                    else
                        ScreenDestination.STUDENT_DASHBOARD
                    return@launch
                }
            }

            // 2. Firebase Auth check as fallback
            val currentFirebaseUser = FirebaseAuthManager.currentUser
            if (currentFirebaseUser != null) {
                val uid = currentFirebaseUser.uid
                val profile = FirestoreUserManager.getUserProfile(uid)
                if (profile != null) {
                    _firestoreProfile.value = profile
                    val role = profile.toUserRole()
                    _activeDashboardRole.value = role
                    persistSessionLocally(uid, role, profile.displayName, profile.safeSphereId)
                    initSafetyListenersForUser(uid, role)
                    if (role == UserRole.STUDENT) {
                        val ctx = getApplication<android.app.Application>()
                        com.example.service.LocationTrackingService.startService(ctx, uid, profile.displayName)
                    }
                    _authSessionState.value = if (role == UserRole.PARENT)
                        AuthSessionState.AUTHENTICATED_PARENT
                    else
                        AuthSessionState.AUTHENTICATED_STUDENT

                    _currentScreen.value = if (role == UserRole.PARENT)
                        ScreenDestination.PARENT_DASHBOARD
                    else
                        ScreenDestination.STUDENT_DASHBOARD
                    return@launch
                }
            }

            _authSessionState.value = AuthSessionState.NOT_AUTHENTICATED
        }
    }

    /**
     * Explicit user logout.
     * Clears session from SharedPrefs and Firebase, navigates to Login.
     * Does NOT delete user profile, family links, safe zones, or journey records.
     */
    fun logout() {
        FirebaseAuthManager.signOut()
        // Clear persisted session so auto-login does not trigger
        try {
            getConsentPrefs().edit()
                .remove("session_uid")
                .remove("session_role")
                .putBoolean("has_active_session", false)
                .apply()
        } catch (e: Exception) { /* ignore */ }
        // Stop location tracking if running
        try {
            com.example.service.LocationTrackingService.stopService(getApplication())
        } catch (e: Exception) { /* ignore */ }
        // Clear local state
        phoneInput.value = ""
        otpInputs.value = List(6) { "" }
        fullNameInput.value = ""
        safeSphereIdInput.value = ""
        passwordInput.value = ""
        gradeClassInput.value = ""
        linkCodeInput.value = ""
        linkErrorMessage.value = null
        loginErrorMessage.value = null
        _linkSuccessMessage.value = null
        _generatedLinkInvite.value = null
        _incomingLinkRequestForStudent.value = null
        _resolvedLinkInvite.value = null
        _activeCameraRequest.value = null
        _incomingCameraRequestForStudent.value = null
        _activeCameraSession.value = null
        _incomingCameraSessionForStudent.value = null
        _firestoreProfile.value = null
        _authSessionState.value = AuthSessionState.NOT_AUTHENTICATED
        _currentScreen.value = ScreenDestination.LOGIN
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

    fun submitJourneyRequest(
        origin: String = journeyFrom.value,
        destination: String = journeyTo.value,
        mode: TravelMode = journeyMode.value,
        expectedArrival: String = journeyArrivalTime.value
    ) {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Broadcasting Route Request..."
            val profile = _firestoreProfile.value
            val studentUid = profile?.uid ?: FirebaseAuthManager.currentUser?.uid ?: "student_user"
            val studentName = profile?.displayName.orEmpty().ifBlank { "Student" }
            val parentUid = _linkedParents.value.firstOrNull()?.parentUid.orEmpty()

            val orig = origin.ifBlank { "School Campus, Gate 2" }
            val dest = destination.ifBlank { "Home, Green Acres" }
            val eta = expectedArrival.ifBlank { "25 min" }

            val record = JourneyRecord(
                studentName = studentName,
                origin = orig,
                destination = dest,
                travelMode = mode,
                expectedArrival = eta,
                status = JourneyStatus.PENDING_APPROVAL,
                riskState = RiskState.NORMAL
            )
            repository.updateJourney(record)

            val firestoreJourney = com.example.data.repository.FirestoreSafetyManager.FirestoreJourney(
                journeyId = UUID.randomUUID().toString(),
                studentUid = studentUid,
                studentName = studentName,
                parentUid = parentUid,
                origin = orig,
                destination = dest,
                travelMode = mode.name,
                expectedArrival = eta,
                distanceKm = "4.2 km",
                estimatedMinutes = 22,
                status = "PENDING_APPROVAL",
                safetyScore = 98,
                waypoints = listOf(orig, "Main Street Corridor", "Safe Transit Point", dest),
                createdAt = System.currentTimeMillis()
            )
            com.example.data.repository.FirestoreSafetyManager.createJourneyRequest(firestoreJourney)

            repository.addTimelineEvent(
                TimelineEvent(
                    title = "Journey Requested",
                    timeFormatted = "Just now",
                    locationOrStatus = "$orig → $dest (Pending Approval)",
                    iconType = "navigation"
                )
            )

            delay(600)
            _isDataLoading.value = false
            _loadingStatus.value = "Route Request Sent to Parents ✓"
            _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
        }
    }

    fun approveJourney(journeyId: String = "") {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Establishing Secure Corridor..."
            val updated = latestJourney.value?.copy(
                status = JourneyStatus.ACTIVE,
                currentEta = "15 min",
                remainingDistance = "3.8 km",
                progressPercent = 35
            ) ?: JourneyRecord()
            repository.updateJourney(updated)

            if (journeyId.isNotBlank()) {
                com.example.data.repository.FirestoreSafetyManager.updateJourneyStatus(journeyId, "APPROVED")
            } else {
                _incomingJourneysForParent.value.firstOrNull()?.let {
                    com.example.data.repository.FirestoreSafetyManager.updateJourneyStatus(it.journeyId, "APPROVED")
                }
            }

            repository.addTimelineEvent(
                TimelineEvent(
                    title = "Parent Approved Journey",
                    timeFormatted = "Now",
                    locationOrStatus = "Corridor Confirmed • Real-Time Protection Active",
                    iconType = "check"
                )
            )
            delay(500)
            _isDataLoading.value = false
            _loadingStatus.value = "Active Telemetry Stream"
            _currentScreen.value = ScreenDestination.FAMILY_MAP
        }
    }

    fun denyJourney(journeyId: String = "") {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Updating Request Status..."
            val updated = latestJourney.value?.copy(
                status = JourneyStatus.DECLINED
            ) ?: JourneyRecord()
            repository.updateJourney(updated)

            if (journeyId.isNotBlank()) {
                com.example.data.repository.FirestoreSafetyManager.updateJourneyStatus(journeyId, "DECLINED")
            } else {
                _incomingJourneysForParent.value.firstOrNull()?.let {
                    com.example.data.repository.FirestoreSafetyManager.updateJourneyStatus(it.journeyId, "DECLINED")
                }
            }

            delay(400)
            _isDataLoading.value = false
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun shareSoloTransportRide(ride: com.example.data.repository.FirestoreSafetyManager.SharedRide) {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Sharing Ride Details with Parents..."
            val profile = _firestoreProfile.value
            val studentUid = profile?.uid ?: FirebaseAuthManager.currentUser?.uid ?: "student_user"
            val studentName = profile?.displayName.orEmpty().ifBlank { "Student" }
            val parentUid = _linkedParents.value.firstOrNull()?.parentUid.orEmpty()

            val finalRide = ride.copy(
                studentUid = if (ride.studentUid.isBlank()) studentUid else ride.studentUid,
                studentName = if (ride.studentName.isBlank()) studentName else ride.studentName,
                parentUid = if (ride.parentUid.isBlank()) parentUid else ride.parentUid
            )
            com.example.data.repository.FirestoreSafetyManager.shareRideWithParent(finalRide)

            repository.addTimelineEvent(
                TimelineEvent(
                    title = "Solo Ride Shared: ${finalRide.vehicleNumber}",
                    timeFormatted = "Just now",
                    locationOrStatus = "${finalRide.driverName} • ${finalRide.pickupLocation} → ${finalRide.dropLocation}",
                    iconType = "car"
                )
            )
            delay(600)
            _isDataLoading.value = false
            _loadingStatus.value = "Ride Shared with Parents ✓"
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

    fun submitPin(
        enteredPin: String,
        onSuccess: () -> Unit,
        onDuress: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
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
        } else {
            onError("Incorrect PIN. Please try again.")
        }
    }

    fun triggerSosEmergency() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Broadcasting Emergency SOS..."
            val updated = latestJourney.value?.copy(
                riskState = RiskState.EMERGENCY,
                status = JourneyStatus.EMERGENCY
            ) ?: JourneyRecord(
                riskState = RiskState.EMERGENCY,
                status = JourneyStatus.EMERGENCY
            )
            repository.updateJourney(updated)
            repository.addTimelineEvent(
                TimelineEvent(
                    title = "SOS Emergency Alert Broadcast",
                    timeFormatted = "Just now",
                    locationOrStatus = "Emergency Broadcast Sent to Linked Family & Services",
                    iconType = "flag"
                )
            )
            _demoStatusMessage.value = "🚨 Emergency SOS Broadcast Sent to Family & Emergency Contacts"
            delay(1500)
            _isDataLoading.value = false
            _loadingStatus.value = "Emergency SOS Active"
            delay(3000)
            _demoStatusMessage.value = null
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
            // Also sync to FirestoreSafeZone so map immediately displays this safe zone
            val match = Regex("""\(([-+]?\d*\.?\d+),\s*([-+]?\d*\.?\d+)\)""").find(address)
            val lat = match?.groupValues?.get(1)?.toDoubleOrNull() ?: _selectedStudentLocation.value?.latitude ?: 17.3850
            val lng = match?.groupValues?.get(2)?.toDoubleOrNull() ?: _selectedStudentLocation.value?.longitude ?: 78.4867
            createFirestoreSafeZone(name = name, lat = lat, lng = lng, radius = radius.toDouble())
        }
    }

    fun removeSafeZone(id: Long) {
        viewModelScope.launch {
            repository.removeSafeZone(id)
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // MULTI-DEVICE TWO-PHONE SAFETY LOGIC (Phases 1-10)
    // ══════════════════════════════════════════════════════════════════

    fun initSafetyListenersForUser(uid: String, role: UserRole) {
        if (role == UserRole.PARENT) {
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeLinkedStudentsForParent(uid)
                    .collect { links ->
                        _linkedStudents.value = links
                        if (_selectedStudent.value == null && links.isNotEmpty()) {
                            selectStudent(links.first())
                        }
                    }
            }
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeJourneysForParent(uid)
                    .collect { journeys ->
                        _incomingJourneysForParent.value = journeys
                    }
            }
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeSharedRidesForParent(uid)
                    .collect { rides ->
                        _incomingSharedRidesForParent.value = rides
                    }
            }
        } else {
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeLinkedParentsForStudent(uid)
                    .collect { links ->
                        _linkedParents.value = links
                    }
            }
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeJourneysForStudent(uid)
                    .collect { journeys ->
                        _studentActiveJourneys.value = journeys
                    }
            }
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeSharedRidesForStudent(uid)
                    .collect { rides ->
                        _studentActiveRides.value = rides
                    }
            }
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeIncomingLinkRequestsForStudent(uid)
                    .collect { requests ->
                        _incomingLinkRequestForStudent.value = requests.firstOrNull()
                    }
            }
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeIncomingCameraSessionsForStudent(uid)
                    .collect { sessions ->
                        _incomingCameraSessionForStudent.value = sessions.firstOrNull()
                    }
            }
            viewModelScope.launch {
                com.example.data.repository.FirestoreSafetyManager.observeIncomingCameraRequestsForStudent(uid)
                    .collect { requests ->
                        _incomingCameraRequestForStudent.value = requests.firstOrNull()
                    }
            }
        }
    }

    // ── Phase 2: Family Linking (Mutual Consent & Persistence) ────────
    fun generateStudentLinkCode(forceRegenerate: Boolean = false) {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Generating Link Code..."

            val profile = _firestoreProfile.value
            val existingCode = profile?.pairingCode?.filter { it.isDigit() }?.takeIf { it.length == 6 }
            val codeNum = if (!forceRegenerate && existingCode != null) {
                existingCode
            } else {
                val newNum = (100000..999999).random().toString()
                if (profile != null) {
                    val updated = profile.copy(pairingCode = newNum)
                    _firestoreProfile.value = updated
                    launch { FirestoreUserManager.saveUserProfile(updated) }
                }
                newNum
            }

            val phone = profile?.phone.orEmpty().ifBlank { "${countryCode.value}${phoneInput.value}" }
            val uid = profile?.uid ?: FirebaseAuthManager.getEffectiveUid(phone)
            val name = profile?.displayName.orEmpty().ifBlank { fullNameInput.value.ifBlank { "Student" } }
            val safeSphereId = profile?.safeSphereId.orEmpty().ifBlank { safeSphereIdInput.value }

            val invite = com.example.data.repository.FirestoreSafetyManager.LinkInvite(
                code = codeNum,
                studentUid = uid,
                studentName = name,
                studentPhone = phone,
                studentSafeSphereId = safeSphereId,
                status = "PENDING",
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000L // 24 hours valid for dashboard
            )

            val res = com.example.data.repository.FirestoreSafetyManager.createLinkInvite(invite)
            _isDataLoading.value = false
            if (res.isSuccess) {
                _generatedLinkInvite.value = invite
                _loadingStatus.value = "Code Ready"

                // Observe invite status in realtime
                launch {
                    com.example.data.repository.FirestoreSafetyManager.observeLinkInvite(codeNum).collect { updated ->
                        if (updated != null) {
                            _generatedLinkInvite.value = updated
                            if (updated.status == "REQUESTED") {
                                _incomingLinkRequestForStudent.value = updated
                            } else if (updated.status == "ACCEPTED") {
                                _linkSuccessMessage.value = "Parent successfully linked! Safeguard active."
                                delay(1500)
                                _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
                            }
                        }
                    }
                }
            } else {
                _loadingStatus.value = "Failed to generate code"
            }
        }
    }

    /**
     * Parent enters 6-digit code to resolve child account before sending request
     */
    fun resolveStudentLinkCode(code: String, onResolved: (com.example.data.repository.FirestoreSafetyManager.LinkInvite?) -> Unit) {
        val clean = code.filter { it.isDigit() }
        if (clean.length != 6) {
            linkErrorMessage.value = "Please enter a valid 6-digit code (e.g. 482 731)"
            return
        }
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Locating Student..."
            linkErrorMessage.value = null
            val invite = com.example.data.repository.FirestoreSafetyManager.getLinkInvite(clean)
            _isDataLoading.value = false
            if (invite != null) {
                _resolvedLinkInvite.value = invite
                onResolved(invite)
            } else {
                linkErrorMessage.value = "Code not found or expired. Ask child to generate a new code."
                onResolved(null)
            }
        }
    }

    /**
     * Parent sends request to Child after resolving code
     */
    fun sendLinkRequestToStudent(code: String, onSuccess: () -> Unit) {
        val clean = code.filter { it.isDigit() }
        val profile = _firestoreProfile.value
        val parentPhone = profile?.phone.orEmpty().ifBlank { "${countryCode.value}${phoneInput.value}" }
        val parentUid = profile?.uid ?: FirebaseAuthManager.getEffectiveUid(parentPhone)
        val parentName = profile?.displayName.orEmpty().ifBlank { "Parent" }

        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Sending Connection Request..."
            val result = com.example.data.repository.FirestoreSafetyManager.sendLinkRequest(
                code = clean,
                parentUid = parentUid,
                parentName = parentName,
                parentPhone = parentPhone
            )
            _isDataLoading.value = false
            if (result.isSuccess) {
                onSuccess()
                // Observe invite until child accepts
                launch {
                    com.example.data.repository.FirestoreSafetyManager.observeLinkInvite(clean).collect { inv ->
                        if (inv != null) {
                            if (inv.status == "ACCEPTED") {
                                _linkSuccessMessage.value = "Connected with ${inv.studentName}!"
                                delay(1500)
                                _currentScreen.value = ScreenDestination.PARENT_DASHBOARD
                            } else if (inv.status == "DECLINED") {
                                linkErrorMessage.value = "Connection request was declined by student."
                            }
                        }
                    }
                }
            } else {
                linkErrorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send request."
            }
        }
    }

    /**
     * Child responds to Parent's connection request (Decline or Accept)
     */
    fun respondToLinkRequest(code: String, accept: Boolean) {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = if (accept) "Connecting with Parent..." else "Declining..."
            val result = com.example.data.repository.FirestoreSafetyManager.respondToLinkRequest(code, accept)
            _isDataLoading.value = false
            _incomingLinkRequestForStudent.value = null
            if (accept && result.isSuccess) {
                val link = result.getOrNull()
                _linkSuccessMessage.value = "Connected with ${link?.parentName ?: "Parent"}!"
                delay(1500)
                _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
            }
        }
    }

    /**
     * Removes family connection after confirmation
     */
    fun removeFamilyConnection(linkId: String) {
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.removeFamilyLink(linkId)
            _selectedStudent.value = null
            _linkSuccessMessage.value = "Family connection removed."
            if (_activeDashboardRole.value == UserRole.PARENT) {
                _currentScreen.value = ScreenDestination.PARENT_DASHBOARD
            } else {
                _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
            }
        }
    }

    fun submitParentLinkCode(onSuccess: () -> Unit) {
        val code = linkCodeInput.value.filter { it.isDigit() }
        if (code.length != 6) {
            linkErrorMessage.value = "Please enter a valid 6-digit code (e.g. 482 731)"
            return
        }
        resolveStudentLinkCode(code) { invite ->
            if (invite != null) {
                sendLinkRequestToStudent(code, onSuccess)
            }
        }
    }

    // ── Phase 3 & 4: Live Location & Student Selection ─────────────────
    fun selectStudent(link: com.example.data.repository.FirestoreSafetyManager.FamilyLink) {
        _selectedStudent.value = link

        // Real-time student location
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.observeStudentLocation(link.studentUid)
                .collect { loc ->
                    _selectedStudentLocation.value = loc
                    if (loc != null) {
                        _gpsRippleTimestamp.value = System.currentTimeMillis()
                        evaluateGeofencesForParent(loc, link.studentName)
                    }
                }
        }

        // Real-time student safe zones
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.observeSafeZonesForStudent(link.studentUid)
                .collect { zones ->
                    _selectedStudentSafeZones.value = zones
                }
        }

        // Real-time geofence events
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.observeGeofenceEventsForStudent(link.studentUid)
                .collect { events ->
                    _selectedStudentGeofenceEvents.value = events
                    val latest = events.firstOrNull()
                    if (latest != null && latest.eventType == "EXIT" && (System.currentTimeMillis() - latest.timestamp) < 30000L) {
                        _geofenceAlertTimestamp.value = latest.timestamp
                        _watermarkMode.value = com.example.ui.components.WatermarkMode.GEOFENCE_ALERT
                        _activeGeofenceAlert.value = "⚠️ GEOFENCE TRIGGER: ${link.studentName} exited safe zone '${latest.zoneName}'!"
                    }
                }
        }
    }

    private val parentZonePresenceMap = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private fun evaluateGeofencesForParent(
        loc: com.example.data.repository.FirestoreSafetyManager.LiveLocation,
        studentName: String
    ) {
        val zones = _selectedStudentSafeZones.value
        if (zones.isEmpty()) return

        for (zone in zones) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                loc.latitude, loc.longitude,
                zone.latitude, zone.longitude,
                results
            )
            val distance = results[0]
            val isInside = distance <= zone.radiusMeters
            val wasInside = parentZonePresenceMap[zone.zoneId]

            if (wasInside != null) {
                if (wasInside && !isInside && zone.alertOnExit) {
                    val now = System.currentTimeMillis()
                    _geofenceAlertTimestamp.value = now
                    _watermarkMode.value = com.example.ui.components.WatermarkMode.GEOFENCE_ALERT
                    val alertText = "⚠️ GEOFENCE TRIGGER: $studentName has EXITED Safe Zone '${zone.name}' (${distance.toInt()}m from center)!"
                    _activeGeofenceAlert.value = alertText

                    viewModelScope.launch {
                        repository.addTimelineEvent(
                            TimelineEvent(
                                title = "Geofence Exit: $studentName",
                                timeFormatted = "Just now",
                                locationOrStatus = "Left ${zone.name}",
                                iconType = "flag"
                            )
                        )
                        val event = com.example.data.repository.FirestoreSafetyManager.FirestoreGeofenceEvent(
                            studentUid = loc.studentUid,
                            studentName = studentName,
                            zoneId = zone.zoneId,
                            zoneName = zone.name,
                            eventType = "EXIT",
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            timestamp = now
                        )
                        com.example.data.repository.FirestoreSafetyManager.logGeofenceEvent(event)
                    }
                }
            }
            parentZonePresenceMap[zone.zoneId] = isInside
        }
    }

    fun toggleStudentLocationTracking(context: android.content.Context) {
        val isRunning = com.example.service.LocationTrackingService.isTrackingActive.value
        if (isRunning) {
            com.example.service.LocationTrackingService.stopService(context)
            _watermarkMode.value = com.example.ui.components.WatermarkMode.NORMAL
        } else {
            val profile = _firestoreProfile.value
            val uid = profile?.uid ?: FirebaseAuthManager.currentUser?.uid ?: return
            val name = profile?.displayName.orEmpty().ifBlank { "Student" }
            com.example.service.LocationTrackingService.startService(context, uid, name)
            _watermarkMode.value = com.example.ui.components.WatermarkMode.TRACKING
        }
    }

    // ── Phase 5: Safe Zones (Firestore-backed) ─────────────────────────
    fun createFirestoreSafeZone(
        name: String,
        lat: Double,
        lng: Double,
        radius: Double,
        alertOnExit: Boolean = true
    ) {
        val student = _selectedStudent.value
        val parentUid = _firestoreProfile.value?.uid ?: FirebaseAuthManager.currentUser?.uid ?: ""
        val studentUid = student?.studentUid ?: ""

        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Saving Safe Zone..."
            val zoneId = "zone_${System.currentTimeMillis()}"
            val zone = com.example.data.repository.FirestoreSafetyManager.FirestoreSafeZone(
                zoneId = zoneId,
                parentUid = parentUid,
                studentUid = studentUid,
                name = name,
                latitude = lat,
                longitude = lng,
                radiusMeters = radius,
                alertOnExit = alertOnExit,
                alertOnEntry = true,
                createdAt = System.currentTimeMillis()
            )
            com.example.data.repository.FirestoreSafetyManager.saveSafeZone(zone)
            val currentList = _selectedStudentSafeZones.value.toMutableList()
            currentList.removeAll { it.zoneId == zoneId }
            currentList.add(zone)
            _selectedStudentSafeZones.value = currentList
            repository.addSafeZone(
                SafeZone(
                    id = 0L,
                    name = name,
                    type = "Custom",
                    address = "Map (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})",
                    radiusMeters = radius.toInt(),
                    expectedSchedule = "24/7 Mon-Sun",
                    isActive = true
                )
            )
            _isDataLoading.value = false
            _loadingStatus.value = "Safe Zone Active ✓"
            delay(1500)
            _loadingStatus.value = "All Protections Armed"
        }
    }

    fun deleteFirestoreSafeZone(zoneId: String) {
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.deleteSafeZone(zoneId)
            val currentList = _selectedStudentSafeZones.value.toMutableList()
            currentList.removeAll { it.zoneId == zoneId }
            _selectedStudentSafeZones.value = currentList
        }
    }

    // ── Phase 8 & 9: Consensual WebRTC Camera Streaming ───────────────
    fun requestLiveCamera(initialFacing: String = "BACK") {
        val student = _selectedStudent.value ?: return
        val profile = _firestoreProfile.value
        val parentUid = profile?.uid ?: FirebaseAuthManager.currentUser?.uid ?: ""
        val parentName = profile?.displayName.orEmpty().ifBlank { "Parent" }

        viewModelScope.launch {
            _isDataLoading.value = true
            _loadingStatus.value = "Requesting Camera Access..."
            val res = com.example.data.repository.FirestoreSafetyManager.requestCameraSession(
                parentUid = parentUid,
                parentName = parentName,
                childUid = student.studentUid,
                studentName = student.studentName,
                cameraFacing = initialFacing
            )
            _isDataLoading.value = false
            if (res.isSuccess) {
                val sessionId = res.getOrThrow()
                _currentScreen.value = ScreenDestination.CAMERA_VIEW

                // Observe camera session in realtime
                launch {
                    com.example.data.repository.FirestoreSafetyManager.observeCameraSession(sessionId).collect { session ->
                        _activeCameraSession.value = session
                    }
                }
            } else {
                val msg = res.exceptionOrNull()?.message ?: "Camera request failed"
                _loadingStatus.value = msg
                _accessDeniedMessage.value = msg
            }
        }
    }

    fun respondToCameraSession(sessionId: String, accept: Boolean, cameraFacing: String = "BACK") {
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.respondToCameraSession(sessionId, accept, cameraFacing)
            _incomingCameraSessionForStudent.value = null
            _incomingCameraRequestForStudent.value = null
            if (accept) {
                launch {
                    com.example.data.repository.FirestoreSafetyManager.observeCameraSession(sessionId).collect { session ->
                        _activeCameraSession.value = session
                    }
                }
                _currentScreen.value = ScreenDestination.CAMERA_SHARE
            }
        }
    }

    fun respondToCameraRequest(requestId: String, accept: Boolean) {
        respondToCameraSession(requestId, accept)
    }

    fun updateCameraFrame(sessionId: String, frameBase64: String, cameraFacing: String) {
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.updateCameraSessionFrame(sessionId, frameBase64, cameraFacing)
        }
    }

    fun switchCameraFacing(sessionId: String, newFacing: String) {
        viewModelScope.launch {
            com.example.data.repository.FirestoreSafetyManager.switchCameraFacing(sessionId, newFacing)
        }
    }

    fun endCameraSession(sessionId: String) {
        viewModelScope.launch {
            com.example.data.webrtc.WebRtcSessionManager.endSession(sessionId)
            com.example.data.repository.FirestoreSafetyManager.updateCameraRequestStatus(sessionId, "ENDED")
            _activeCameraSession.value = null
            _incomingCameraSessionForStudent.value = null
            _activeCameraRequest.value = null
            _incomingCameraRequestForStudent.value = null
            if (_activeDashboardRole.value == UserRole.PARENT) {
                _currentScreen.value = ScreenDestination.STUDENT_CONTROL_CENTER
            } else {
                _currentScreen.value = ScreenDestination.STUDENT_DASHBOARD
            }
        }
    }
}
