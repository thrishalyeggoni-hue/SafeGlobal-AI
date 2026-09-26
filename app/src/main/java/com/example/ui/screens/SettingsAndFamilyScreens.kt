package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun SettingsScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    var isLocationSharingOn by remember { mutableStateOf(true) }
    var isCameraVerificationOn by remember { mutableStateOf(true) }
    var isGeofenceAlertsOn by remember { mutableStateOf(true) }
    var showColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
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
            Text(text = "Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
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
                    title = "Family Members",
                    icon = Icons.Default.FamilyRestroom,
                    onClick = { viewModel.navigateTo(ScreenDestination.FAMILY_MEMBERS) }
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
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
                        Icon(imageVector = Icons.Default.Badge, contentDescription = "SafeSphere ID", tint = MaterialTheme.colorScheme.primary)
                        Text(text = "SafeSphere ID", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                    Text(text = "#SF-9042", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006398))
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

        // Demo Simulator Shortcut
        Button(
            onClick = { viewModel.navigateTo(ScreenDestination.DEMO_SIMULATOR) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006398)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_demo_simulator_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Demo", tint = Color.White)
                Text(text = "Open Demo Simulator", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
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
            .background(Color(0xFFF8F9FF))
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
            .background(Color(0xFFF8F9FF))
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCCE5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Fence, contentDescription = zone.name, tint = Color(0xFF006398), modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(text = zone.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                                Text(text = "Within ${zone.radiusMeters}m • ${zone.expectedSchedule}", fontSize = 11.5.sp, color = Color(0xFF474552))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF6FFBBE))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = "Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005236))
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_safe_zone_btn")
        ) {
            Text(text = "ADD SAFE ZONE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
    var showInviteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
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
                // Sarah
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(SARAH_AVATAR_URL).crossfade(true).build(),
                            contentDescription = "Sarah",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                        )
                        Column {
                            Text(text = "Sarah (Parent)", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                            Text(text = "Primary Guardian", fontSize = 11.5.sp, color = Color(0xFF474552))
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Details", tint = Color(0xFF787584))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))

                // Alex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(ALEX_AVATAR_URL).crossfade(true).build(),
                            contentDescription = "Alex",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                        )
                        Column {
                            Text(text = "Alex (Student)", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                            Text(text = "10th Grade", fontSize = 11.5.sp, color = Color(0xFF474552))
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Details", tint = Color(0xFF787584))
                }
            }
        }

        Button(
            onClick = { showInviteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("invite_member_btn")
        ) {
            Text(text = "INVITE MEMBER", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(70.dp))
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite to Family Circle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Share this pairing code with your family member:")
                    Text("Family ID: SF-8X21P", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF006398))
                    Text("Pairing Code: 748291", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF006B49))
                    Text("Codes expire in 24 hours.", fontSize = 11.5.sp, color = Color(0xFF474552))
                }
            },
            confirmButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("Done", fontWeight = FontWeight.Bold) }
            }
        )
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
            .background(Color(0xFFF8F9FF))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WaterWaveLoadingIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = "Profile Protected"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD) }) {
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
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(ALEX_AVATAR_URL).crossfade(true).build(),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(72.dp).clip(CircleShape)
                )

                Text(text = "Alex", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                Text(text = "10th Grade • Student", fontSize = 12.sp, color = Color(0xFF474552))
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingRowItem(title = "Account Details", icon = Icons.Default.Person, onClick = {})
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                SettingRowItem(title = "Journey History", icon = Icons.Default.ShareLocation, onClick = { viewModel.navigateTo(ScreenDestination.SAFETY_TIMELINE) })
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF4FF)))
                SettingRowItem(title = "Safety Settings", icon = Icons.Default.Security, onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) })
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
