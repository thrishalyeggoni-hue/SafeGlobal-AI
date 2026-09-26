package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.SafeSphereBottomNavigation
import com.example.ui.components.WithSafeSphereWatermark
import com.example.ui.components.TopCenterBrandedLoadingIndicator
import com.example.ui.screens.CameraShareScreen
import com.example.ui.screens.CameraViewScreen
import com.example.ui.screens.CompleteProfileScreen
import com.example.ui.screens.ConsentScreen
import com.example.ui.screens.CreateIdScreen
import com.example.ui.screens.CreatePasswordScreen
import com.example.ui.screens.DemoSimulatorScreen
import com.example.ui.screens.EmergencyScreen
import com.example.ui.screens.EnterLinkCodeScreen
import com.example.ui.screens.FamilyMapScreen
import com.example.ui.screens.FamilyMembersScreen
import com.example.ui.screens.LinkCodeGeneratorScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.OtpVerificationScreen
import com.example.ui.screens.ParentApprovalScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.screens.PhoneVerificationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RequestJourneyScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SafeZoneCreatorScreen
import com.example.ui.screens.SafeZonesScreen
import com.example.ui.screens.SafetyTimelineScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentControlCenterScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.theme.SafeSphereTheme
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {

    private val viewModel: SafeSphereViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeAccent by viewModel.themeAccent.collectAsState()

            SafeSphereTheme(primaryAccent = themeAccent) {
                SafeSphereApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SafeSphereApp(viewModel: SafeSphereViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentNavTab by viewModel.currentNavTab.collectAsState()
    val activeRole by viewModel.activeDashboardRole.collectAsState()

    // Back handling for sub-screens
    BackHandler(
        enabled = currentScreen != ScreenDestination.PARENT_DASHBOARD &&
            currentScreen != ScreenDestination.STUDENT_DASHBOARD &&
            currentScreen != ScreenDestination.PHONE_VERIFY &&
            currentScreen != ScreenDestination.SPLASH
    ) {
        when (currentScreen) {
            ScreenDestination.LOADING -> viewModel.navigateTo(ScreenDestination.PHONE_VERIFY)
            ScreenDestination.CONSENT -> viewModel.navigateTo(ScreenDestination.PHONE_VERIFY)
            ScreenDestination.OTP_VERIFY -> viewModel.navigateTo(ScreenDestination.PHONE_VERIFY)
            ScreenDestination.CHOOSE_ROLE -> viewModel.navigateTo(ScreenDestination.OTP_VERIFY)
            ScreenDestination.CREATE_ID -> viewModel.navigateTo(ScreenDestination.CHOOSE_ROLE)
            ScreenDestination.CREATE_PASSWORD -> viewModel.navigateTo(ScreenDestination.CREATE_ID)
            ScreenDestination.COMPLETE_PROFILE -> viewModel.navigateTo(ScreenDestination.CREATE_PASSWORD)
            ScreenDestination.LINK_CODE_GENERATOR -> viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
            ScreenDestination.ENTER_LINK_CODE -> viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
            ScreenDestination.STUDENT_CONTROL_CENTER -> viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
            ScreenDestination.SAFE_ZONE_CREATOR -> viewModel.navigateTo(ScreenDestination.STUDENT_CONTROL_CENTER)
            ScreenDestination.CAMERA_REQUEST,
            ScreenDestination.CAMERA_VIEW -> viewModel.navigateTo(ScreenDestination.STUDENT_CONTROL_CENTER)
            ScreenDestination.CAMERA_SHARE -> viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
            ScreenDestination.REQUEST_JOURNEY,
            ScreenDestination.PARENT_APPROVAL,
            ScreenDestination.SAFETY_TIMELINE,
            ScreenDestination.FAMILY_MAP,
            ScreenDestination.EMERGENCY,
            ScreenDestination.SETTINGS,
            ScreenDestination.DEMO_SIMULATOR,
            ScreenDestination.SAFE_ZONES,
            ScreenDestination.FAMILY_MEMBERS,
            ScreenDestination.PROFILE -> {
                if (activeRole == UserRole.PARENT) {
                    viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                } else {
                    viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                }
            }
            else -> {}
        }
    }

    val isDataLoading by viewModel.isDataLoading.collectAsState()
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val accessDeniedMessage by viewModel.accessDeniedMessage.collectAsState()

    // Access Denied Security Dialog
    if (accessDeniedMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAccessDenied() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security Alert",
                    tint = Color(0xFFBA1A1A),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Security Access Restricted",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF121C2A)
                )
            },
            text = {
                Text(
                    text = accessDeniedMessage ?: "",
                    fontSize = 13.5.sp,
                    color = Color(0xFF474552),
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissAccessDenied() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    val showBottomNav = currentScreen in listOf(
        ScreenDestination.PARENT_DASHBOARD,
        ScreenDestination.STUDENT_DASHBOARD,
        ScreenDestination.FAMILY_MAP,
        ScreenDestination.FAMILY_MEMBERS,
        ScreenDestination.SETTINGS,
        ScreenDestination.PROFILE
    )

    val watermarkMode by viewModel.watermarkMode.collectAsState()
    val gpsRippleTrigger by viewModel.gpsRippleTimestamp.collectAsState()
    val geofenceAlertTrigger by viewModel.geofenceAlertTimestamp.collectAsState()
    val watermarkStatusOverride by viewModel.watermarkStatusOverride.collectAsState()

    WithSafeSphereWatermark(
        mode = watermarkMode,
        gpsUpdateTrigger = gpsRippleTrigger,
        geofenceAlertTrigger = geofenceAlertTrigger,
        statusOverride = watermarkStatusOverride
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 640.dp)
                .testTag("safesphere_app_scaffold"),
            containerColor = Color.Transparent,  // transparent so watermark shows through
            bottomBar = {
                if (showBottomNav) {
                    SafeSphereBottomNavigation(
                        selectedTab = currentNavTab,
                        onTabSelected = { viewModel.selectNavTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Global Top-Center Branded Loading Animation during any data operation or API call
                if (isDataLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TopCenterBrandedLoadingIndicator(
                            isLoading = true,
                            label = loadingStatus,
                            onSyncClick = { viewModel.triggerDataSync() }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                when (currentScreen) {
                    // ── Auth Flow: Phone → OTP → Role → Profile → Dashboard ──────────
                    ScreenDestination.SPLASH -> SplashScreen(
                        onTimeoutOrNext = { viewModel.navigateTo(ScreenDestination.PHONE_VERIFY) }
                    )
                    ScreenDestination.LOADING -> LoadingScreen(
                        onLoaded = { viewModel.navigateTo(ScreenDestination.PHONE_VERIFY) }
                    )
                    ScreenDestination.CONSENT -> ConsentScreen(
                        onContinue = { viewModel.navigateTo(ScreenDestination.PHONE_VERIFY) },
                        onBack = { viewModel.navigateTo(ScreenDestination.PHONE_VERIFY) }
                    )
                    ScreenDestination.PHONE_VERIFY -> PhoneVerificationScreen(
                        viewModel = viewModel,
                        onSendOtp = { viewModel.navigateTo(ScreenDestination.OTP_VERIFY) },
                        onBack = { viewModel.navigateTo(ScreenDestination.SPLASH) }
                    )
                    ScreenDestination.OTP_VERIFY -> OtpVerificationScreen(
                        viewModel = viewModel,
                        // After OTP verified, ask who they are (Student or Parent)
                        onVerifyOtp = { viewModel.navigateTo(ScreenDestination.CHOOSE_ROLE) },
                        onBack = { viewModel.navigateTo(ScreenDestination.PHONE_VERIFY) }
                    )
                    ScreenDestination.CHOOSE_ROLE -> RoleSelectionScreen(
                        viewModel = viewModel,
                        onContinue = { viewModel.navigateTo(ScreenDestination.CREATE_ID) },
                        onBack = { viewModel.navigateTo(ScreenDestination.OTP_VERIFY) }
                    )
                    ScreenDestination.CREATE_ID -> CreateIdScreen(
                        viewModel = viewModel,
                        onContinue = { viewModel.navigateTo(ScreenDestination.CREATE_PASSWORD) },
                        onBack = { viewModel.navigateTo(ScreenDestination.CHOOSE_ROLE) }
                    )
                    ScreenDestination.CREATE_PASSWORD -> CreatePasswordScreen(
                        viewModel = viewModel,
                        onContinue = { viewModel.navigateTo(ScreenDestination.COMPLETE_PROFILE) },
                        onBack = { viewModel.navigateTo(ScreenDestination.CREATE_ID) }
                    )
                    ScreenDestination.COMPLETE_PROFILE -> CompleteProfileScreen(
                        viewModel = viewModel,
                        onCreateAccount = {
                            if (viewModel.selectedRole.value == UserRole.PARENT) {
                                viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                            } else {
                                viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                            }
                        },
                        onBack = { viewModel.navigateTo(ScreenDestination.CREATE_PASSWORD) }
                    )
                    ScreenDestination.PARENT_DASHBOARD -> ParentDashboardScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.STUDENT_DASHBOARD -> StudentDashboardScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.FAMILY_MAP -> FamilyMapScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.REQUEST_JOURNEY -> RequestJourneyScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.PARENT_APPROVAL -> ParentApprovalScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.SAFETY_TIMELINE -> SafetyTimelineScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.EMERGENCY -> EmergencyScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.DEMO_SIMULATOR -> DemoSimulatorScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.SAFE_ZONES -> SafeZonesScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.FAMILY_MEMBERS -> FamilyMembersScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.PROFILE -> ProfileScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.LINK_CODE_GENERATOR -> LinkCodeGeneratorScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.ENTER_LINK_CODE -> EnterLinkCodeScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.STUDENT_CONTROL_CENTER -> StudentControlCenterScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.SAFE_ZONE_CREATOR -> SafeZoneCreatorScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.CAMERA_REQUEST,
                    ScreenDestination.CAMERA_VIEW -> CameraViewScreen(
                        viewModel = viewModel
                    )
                    ScreenDestination.CAMERA_SHARE -> CameraShareScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
    }
}
}
