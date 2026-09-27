package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCameraFront
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.darkTextFieldColors
import com.example.ui.components.WaterWaveLoadingIndicator
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.data.model.UserRole

@Composable
fun SettingsScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    var isLocationSharingOn by remember { mutableStateOf(true) }
    var isCameraVerificationOn by remember { mutableStateOf(true) }
    var isGeofenceAlertsOn by remember { mutableStateOf(true) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showAccountDetailsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val isParent = viewModel.activeDashboardRole.collectAsState().value == com.example.data.model.UserRole.PARENT

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = {
                if (isParent) {
                    viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                } else {
                    viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        val profile by viewModel.firestoreProfile.collectAsState()

        if (showAccountDetailsDialog) {
            AccountDetailsDialog(
                profile = profile,
                isParent = isParent,
                onDismiss = { showAccountDetailsDialog = false },
                onEditProfile = {
                    showAccountDetailsDialog = false
                    viewModel.navigateTo(ScreenDestination.COMPLETE_PROFILE)
                }
            )
        }

        // Account Section
        Text(text = "ACCOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingRowItem(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    onClick = { viewModel.navigateTo(ScreenDestination.PROFILE) }
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                SettingRowItem(
                    title = "Account Details",
                    icon = Icons.Default.Badge,
                    onClick = { showAccountDetailsDialog = true }
                )
                if (isParent) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                    SettingRowItem(
                        title = "Family Members",
                        icon = Icons.Default.FamilyRestroom,
                        onClick = { viewModel.navigateTo(ScreenDestination.FAMILY_MEMBERS) }
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountDetailsDialog = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = "SafeSphere ID", tint = MaterialTheme.colorScheme.primary)
                        Text(text = "SafeSphere ID", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                    Text(
                        text = profile?.safeSphereId?.ifBlank { "#SF-9042" } ?: "#SF-9042",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006398)
                    )
                }
            }
        }

        // Safety & Privacy Section
        Text(text = "SAFETY & PRIVACY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ToggleSettingRow(
                    title = "Location Sharing",
                    icon = Icons.Default.ShareLocation,
                    isOn = isLocationSharingOn,
                    onToggle = { isLocationSharingOn = it }
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                ToggleSettingRow(
                    title = "Camera Verification",
                    icon = Icons.Default.PhotoCameraFront,
                    isOn = isCameraVerificationOn,
                    onToggle = { isCameraVerificationOn = it }
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                ToggleSettingRow(
                    title = "Geofence Alerts",
                    icon = Icons.Default.Fence,
                    isOn = isGeofenceAlertsOn,
                    onToggle = { isGeofenceAlertsOn = it }
                )
            }
        }

        // Appearance Section
        Text(text = "APPEARANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showColorPicker = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary)
                    Text(text = "Accent Color & Theme", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Select", tint = Color(0xFF787584))
                }
            }
        }

        // Device Link Shortcut
        Button(
            onClick = {
                if (isParent) viewModel.navigateTo(ScreenDestination.FAMILY_MEMBERS)
                else viewModel.navigateTo(ScreenDestination.LINK_CODE_GENERATOR)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006398)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("manage_family_link_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.AddLink, contentDescription = "Link Devices", tint = Color.White)
                Text(
                    text = if (isParent) "Manage Linked Devices" else "Pair With Guardian",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Log Out Button
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A)),
            border = BorderStroke(1.dp, Color(0xFFFFDAD6)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("settings_logout_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log Out", tint = Color(0xFFBA1A1A))
                Text(
                    text = "Log Out",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBA1A1A)
                )
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFBA1A1A),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Log out of SafeSphere?", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            },
            text = {
                Text(
                    "Are you sure you want to log out? Your family connections, safe zones, and safety history will remain securely saved in your account.",
                    color = Color(0xFF474552),
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF474552))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Choose SafeSphere Accent Color", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Pair("Blue", AccentBlue),
                        Pair("Violet", AccentViolet),
                        Pair("Green", AccentGreen),
                        Pair("Teal", AccentTeal),
                        Pair("Orange", AccentOrange),
                        Pair("Rose", AccentRose),
                        Pair("Indigo", AccentIndigo)
                    ).forEach { (name, color) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppThemeColor(name)
                                    showColorPicker = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingRowItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = Color(0xFF787584))
    }
}

@Composable
fun ToggleSettingRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF006B49))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
        }
        Switch(
            checked = isOn,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun DemoSimulatorScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val demoStatus by viewModel.demoStatusMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Demo Simulator", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        // Hero Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCCE5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Robot", tint = Color(0xFF006398), modifier = Modifier.size(36.dp))
                }

                Text(text = "Try SafeSphere", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                Text(text = "(Interactive Demo)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Explore key features with simulated data in a safe, risk-free sandbox environment.",
                    fontSize = 12.sp,
                    color = Color(0xFF474552),
                    lineHeight = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Features Included Checklist
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "FEATURES INCLUDED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
                listOf(
                    "Journey requests",
                    "Location tracking",
                    "Emergency alerts",
                    "Safety timeline",
                    "Duress PIN detection"
                ).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6FFBBE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Included", tint = Color(0xFF005236), modifier = Modifier.size(13.dp))
                        }
                        Text(text = feature, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF121C2A))
                    }
                }
            }
        }

        demoStatus?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFCCE5FF))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = msg, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004B73))
            }
        }

        // Start Demo CTA
        Button(
            onClick = { viewModel.runDemoSimulation() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006398)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_demo_simulation_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                Text(text = "START DEMO", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun SafeZonesScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val safeZones by viewModel.safeZones.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var zoneName by remember { mutableStateOf("") }
    var zoneAddress by remember { mutableStateOf("") }
    var zoneRadius by remember { mutableStateOf("500") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Safe Zones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (safeZones.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF4FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "No Safe Zones Added",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                        Text(
                            text = "Set up Home, School, or other trusted zones to receive geofence entry and exit alerts.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    safeZones.forEach { zone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEFF4FF))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFCCE5FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fence,
                                        contentDescription = zone.name,
                                        tint = Color(0xFF006398),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = zone.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF121C2A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Within ${zone.radiusMeters}m • ${zone.address}",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF474552),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF6FFBBE))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF005236),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B49)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.PinDrop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "MARK ON MAP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("add_safe_zone_btn")
            ) {
                Text(text = "ADD BY TEXT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Safe Zone", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        label = { Text("Zone Name (e.g. Tuition)", color = Color(0xFF475569)) },
                        singleLine = true,
                        colors = darkTextFieldColors()
                    )
                    OutlinedTextField(
                        value = zoneAddress,
                        onValueChange = { zoneAddress = it },
                        label = { Text("Address / Area", color = Color(0xFF475569)) },
                        singleLine = true,
                        colors = darkTextFieldColors()
                    )
                    OutlinedTextField(
                        value = zoneRadius,
                        onValueChange = { zoneRadius = it },
                        label = { Text("Radius (meters)", color = Color(0xFF475569)) },
                        singleLine = true,
                        colors = darkTextFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val radiusInt = zoneRadius.toIntOrNull() ?: 500
                        viewModel.addSafeZone(zoneName.ifEmpty { "New Zone" }, "Custom", zoneAddress.ifEmpty { "City Center" }, radiusInt)
                        showAddDialog = false
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FamilyMembersScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val activeRole by viewModel.activeDashboardRole.collectAsState()
    val linkedStudents by viewModel.linkedStudents.collectAsState()
    val linkedParents by viewModel.linkedParents.collectAsState()
    val userProfile by viewModel.firestoreProfile.collectAsState()
    val currentUserName = userProfile?.displayName?.ifBlank { null } ?: if (activeRole == UserRole.PARENT) "Parent" else "Student"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = {
                if (activeRole == UserRole.PARENT) viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                else viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Family Members", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Current User
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (activeRole == UserRole.PARENT) Color(0xFF43359F) else Color(0xFF006398)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (activeRole == UserRole.PARENT) Icons.Default.Person else Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(text = "$currentUserName (You)", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                            Text(
                                text = if (activeRole == UserRole.PARENT) "Primary Guardian" else "Student Account",
                                fontSize = 11.5.sp,
                                color = Color(0xFF474552)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE4DFFF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (activeRole == UserRole.PARENT) "Parent" else "Student",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43359F)
                        )
                    }
                }

                // If Parent: show linked students
                if (activeRole == UserRole.PARENT) {
                    if (linkedStudents.isNotEmpty()) {
                        linkedStudents.forEach { student ->
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF006398)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                                    }
                                    Column {
                                        Text(text = "${student.studentName} (Student)", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                                        Text(text = "Device Paired • Encrypted Sync", fontSize = 11.5.sp, color = Color(0xFF474552))
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFCCE5FF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Connected", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004B73))
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                        Text(
                            text = "No linked children yet. Tap LINK CHILD below to connect via 6-digit code or QR code.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                } else {
                    // If Student: show linked parents
                    if (linkedParents.isNotEmpty()) {
                        linkedParents.forEach { parent ->
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF43359F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                    }
                                    Column {
                                        Text(text = "${parent.parentName} (Parent)", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                                        Text(text = "Emergency Contact • Linked", fontSize = 11.5.sp, color = Color(0xFF474552))
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE4DFFF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Guardian", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43359F))
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                        Text(
                            text = "No guardian linked yet. Tap LINK GUARDIAN below to generate your 6-digit pairing code.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (activeRole == UserRole.PARENT) {
                    viewModel.navigateTo(ScreenDestination.ENTER_LINK_CODE)
                } else {
                    viewModel.navigateTo(ScreenDestination.LINK_CODE_GENERATOR)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("invite_member_btn")
        ) {
            Icon(Icons.Default.AddLink, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (activeRole == UserRole.PARENT) "LINK CHILD" else "LINK GUARDIAN",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun ProfileScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WaterWaveLoadingIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = "Profile Protected"
        )

        val isParent = viewModel.activeDashboardRole.collectAsState().value == com.example.data.model.UserRole.PARENT
        val profile by viewModel.firestoreProfile.collectAsState()
        var showLogoutDialog by remember { mutableStateOf(false) }
        var showAccountDetailsDialog by remember { mutableStateOf(false) }
        var showAvatarDialog by remember { mutableStateOf(false) }
        val userAvatarIndex = profile?.avatarIndex ?: if (isParent) 1 else 7

        if (showAvatarDialog) {
            com.example.ui.components.AvatarSelectionDialog(
                currentAvatarIndex = userAvatarIndex,
                onAvatarSelected = { viewModel.updateUserAvatar(it) },
                onDismissRequest = { showAvatarDialog = false }
            )
        }

        if (showAccountDetailsDialog) {
            AccountDetailsDialog(
                profile = profile,
                isParent = isParent,
                onDismiss = { showAccountDetailsDialog = false },
                onEditProfile = {
                    showAccountDetailsDialog = false
                    viewModel.navigateTo(ScreenDestination.COMPLETE_PROFILE)
                }
            )
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text("Log out of SafeSphere?", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                },
                text = {
                    Text(
                        "Are you sure you want to log out? Your family connections, safe zones, and safety history will remain securely saved in your account.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = {
                if (isParent) {
                    viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                } else {
                    viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(3.dp, if (isParent) Color(0xFF2563EB) else Color(0xFF16A34A), CircleShape)
                        .clickable { showAvatarDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.ui.components.SafeSphereAvatarHelper.getAvatarDrawable(userAvatarIndex)),
                        contentDescription = "Profile Avatar - Tap to change",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                TextButton(
                    onClick = { showAvatarDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (isParent) Color(0xFF2563EB) else Color(0xFF16A34A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Change Avatar",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isParent) Color(0xFF2563EB) else Color(0xFF16A34A)
                    )
                }

                Text(
                    text = profile?.displayName?.ifBlank { null } ?: if (isParent) "Parent Account" else "Student Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121C2A)
                )
                Text(
                    text = if (isParent) (profile?.phone?.ifBlank { null } ?: "Family Primary Guardian") else ("${profile?.gradeClass ?: "Student"} • ${profile?.phone ?: ""}"),
                    fontSize = 12.sp,
                    color = Color(0xFF474552)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingRowItem(title = "Account Details", icon = Icons.Default.Person, onClick = { showAccountDetailsDialog = true })
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                SettingRowItem(title = "Journey History", icon = Icons.Default.ShareLocation, onClick = { viewModel.navigateTo(ScreenDestination.SAFETY_TIMELINE) })
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                SettingRowItem(title = "Safety Settings", icon = Icons.Default.Security, onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) })
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                SettingRowItem(title = "Log Out", icon = Icons.AutoMirrored.Filled.ExitToApp, onClick = { showLogoutDialog = true })
            }
        }

        Button(
            onClick = { viewModel.navigateTo(ScreenDestination.COMPLETE_PROFILE) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("edit_profile_btn")
        ) {
            Text(text = "EDIT PROFILE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun AccountDetailsDialog(
    profile: com.example.data.auth.FirestoreUserManager.UserProfile?,
    isParent: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val safeSphereId = profile?.safeSphereId?.ifBlank { "Not set" } ?: "Not set"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isParent) Color(0xFFDBEAFE) else Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = if (isParent) Color(0xFF1D4ED8) else Color(0xFF15803D),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Account Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "SafeSphere Credentials & Security",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SafeSphere ID highlight card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SafeSphere ID", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = safeSphereId,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(safeSphereId))
                                android.widget.Toast.makeText(context, "SafeSphere ID copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = Color(0xFF1D61F2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Account items
                AccountDetailRow(label = "Full Name", value = profile?.displayName?.ifBlank { "SafeSphere User" } ?: "SafeSphere User")
                AccountDetailRow(label = "Account Role", value = if (isParent) "Primary Parent / Guardian" else "Student / Child")
                if (!isParent && !profile?.gradeClass.isNullOrBlank()) {
                    AccountDetailRow(label = "Grade / Class", value = profile?.gradeClass ?: "")
                }
                if (!profile?.phone.isNullOrBlank()) {
                    AccountDetailRow(label = "Phone Number", value = profile?.phone ?: "")
                }
                AccountDetailRow(label = "Family ID", value = profile?.familyId?.ifBlank { "SF-FAMILY-01" } ?: "SF-FAMILY-01")

                // Status badges
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Verified, null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                            Text("Cloud Synced", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Security, null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(14.dp))
                            Text("SHA-256 Auth", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onEditProfile) {
                Text("Edit Profile", color = Color(0xFF1D61F2), fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White
    )
}

@Composable
private fun AccountDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.5.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
    }
}
