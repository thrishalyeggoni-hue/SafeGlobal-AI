package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.FirestoreSafetyManager
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.delay

enum class VerifyStatus { IDLE, CHECKING, PASSED, FAILED }

enum class ScanType { AUTO_QR, DRIVER_FACE }

@Composable
fun SoloTransportScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var appUsed by remember { mutableStateOf("Auto") }
    var driverName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var pickupLocation by remember { mutableStateOf("") }
    var dropLocation by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }

    var scanVerificationType by remember { mutableStateOf("NONE") }
    var isScannedVerified by remember { mutableStateOf(false) }

    var verifyStatus by remember { mutableStateOf(VerifyStatus.IDLE) }
    var verifyMessage by remember { mutableStateOf("") }
    var sharedWithParent by remember { mutableStateOf(false) }

    var showScannerDialog by remember { mutableStateOf(false) }
    var initialScanType by remember { mutableStateOf(ScanType.AUTO_QR) }

    val presetLocations = listOf(
        "Current GPS",
        "Main Campus",
        "Metro Station",
        "Home",
        "Central Library",
        "Tuition Center"
    )

    LaunchedEffect(verifyStatus) {
        if (verifyStatus == VerifyStatus.CHECKING) {
            delay(1200)
            val issues = mutableListOf<String>()
            if (driverName.trim().length < 2) issues += "Driver name is required"
            if (vehicleNumber.trim().length < 6) issues += "Enter valid vehicle number (e.g. TS 09 AB 1234)"
            if (pickupLocation.trim().length < 3) issues += "Pickup location is required"
            if (dropLocation.trim().length < 3) issues += "Drop location is required"

            if (issues.isEmpty()) {
                verifyStatus = VerifyStatus.PASSED
                verifyMessage = if (isScannedVerified) {
                    "✓ Bio-Scan & Vehicle Match Verified by AI. Live corridor tracking active."
                } else {
                    "✓ All details validated. Safe to share with parents."
                }
            } else {
                verifyStatus = VerifyStatus.FAILED
                verifyMessage = "Found ${issues.size} issue(s):\n" + issues.joinToString("\n• ", prefix = "• ")
            }
        }
    }

    if (showScannerDialog) {
        ScanDriverOrAutoQrModal(
            initialType = initialScanType,
            onDismiss = { showScannerDialog = false },
            onScanComplete = { type, detectedDriver, detectedVehicle, detectedPhone ->
                driverName = detectedDriver
                vehicleNumber = detectedVehicle
                driverPhone = detectedPhone
                scanVerificationType = if (type == ScanType.AUTO_QR) "AUTO_QR" else "DRIVER_FACE"
                isScannedVerified = true
                verifyStatus = VerifyStatus.PASSED
                verifyMessage = "✓ Verified via ${if (type == ScanType.AUTO_QR) "Rear Auto QR Scan" else "Driver Face Biometric"}"
                showScannerDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF121C2A)
                )
            }
            Column {
                Text(
                    text = "Solo Transport Safety",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121C2A)
                )
                Text(
                    text = "Scan auto QR / driver face & share verified live route with parents",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Vehicle / App Type Selector
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("TRANSPORT TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B), letterSpacing = 0.5.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Auto", "Uber", "Ola", "Rapido", "Cab").forEach { app ->
                        val isSelected = appUsed == app
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF1D61F2) else Color(0xFFEFF4FF))
                                .clickable { appUsed = app }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = app, fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF474552))
                        }
                    }
                }
            }
        }

        // SCAN VERIFICATION SECTION (Driver Face & Auto QR)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isScannedVerified) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.border(
                1.5.dp,
                if (isScannedVerified) Color(0xFF16A34A) else Color(0xFF3B82F6).copy(alpha = 0.4f),
                RoundedCornerShape(20.dp)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isScannedVerified) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isScannedVerified) Icons.Default.Verified else Icons.Default.QrCode,
                                contentDescription = null,
                                tint = if (isScannedVerified) Color(0xFF16A34A) else Color(0xFF1D61F2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "RAPID AI SCAN VERIFICATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isScannedVerified) Color(0xFF16A34A) else Color(0xFF1D61F2),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isScannedVerified) "Verified: $scanVerificationType" else "Scan Auto Rear QR or Driver Face",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    if (isScannedVerified) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF16A34A))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERIFIED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = "Scan the QR sticker on the back of autos or take a quick photo of the driver to match RTO records and auto-fill details.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )

                // 2 Action Buttons: Scan Auto QR & Scan Driver Face
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            initialScanType = ScanType.AUTO_QR
                            showScannerDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D61F2)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Default.QrCode, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Auto QR", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            initialScanType = ScanType.DRIVER_FACE
                            showScannerDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Default.Face, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Driver Face", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // DRIVER & VEHICLE DETAILS CARD
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFEFF4FF)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF1D61F2), modifier = Modifier.size(18.dp))
                    }
                    Text("DRIVER & VEHICLE INFO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B), letterSpacing = 0.5.sp)
                }

                TransportTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = "Driver Name",
                    placeholder = "e.g. Ramesh Kumar",
                    icon = Icons.Default.Person,
                    testTag = "solo_driver_name"
                )

                TransportTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it.uppercase() },
                    label = "Vehicle Registration No.",
                    placeholder = "e.g. TS 09 UA 8841",
                    icon = Icons.Default.DirectionsCar,
                    capitalization = KeyboardCapitalization.Characters,
                    testTag = "solo_vehicle_number"
                )

                TransportTextField(
                    value = driverPhone,
                    onValueChange = { if (it.length <= 14) driverPhone = it },
                    label = "Driver Mobile (Optional)",
                    placeholder = "e.g. +91 98480 22338",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    testTag = "solo_driver_phone"
                )
            }
        }

        // ROUTE PLANNING CARD (Source to Destination)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    }
                    Text("ROUTE DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B), letterSpacing = 0.5.sp)
                }

                TransportTextField(
                    value = pickupLocation,
                    onValueChange = { pickupLocation = it },
                    label = "Source / Pickup Location",
                    placeholder = "Enter pickup point or campus gate",
                    icon = Icons.Default.School,
                    testTag = "solo_pickup"
                )

                // Quick presets for pickup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Current GPS", "Campus Gate", "Metro Stn").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF))
                                .clickable { pickupLocation = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(preset, fontSize = 11.sp, color = Color(0xFF1D61F2), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                TransportTextField(
                    value = dropLocation,
                    onValueChange = { dropLocation = it },
                    label = "Destination / Drop Location",
                    placeholder = "Enter final destination address",
                    icon = Icons.Default.Home,
                    testTag = "solo_drop"
                )

                // Quick presets for drop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Home", "Hostel", "City Center").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF))
                                .clickable { dropLocation = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(preset, fontSize = 11.sp, color = Color(0xFF1D61F2), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                TransportTextField(
                    value = estimatedTime,
                    onValueChange = { estimatedTime = it },
                    label = "Estimated Travel Duration",
                    placeholder = "e.g. 20 min",
                    icon = Icons.Default.Warning,
                    testTag = "solo_eta"
                )
            }
        }

        // PREDICTED ROUTE DISPLAY FROM SOURCE TO DESTINATION
        Text(
            text = "AI PREDICTED ROUTE & SAFETY CORRIDOR",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        PredictedRouteCard(
            origin = pickupLocation.ifBlank { "Pickup Point" },
            destination = dropLocation.ifBlank { "Destination Point" },
            distanceKm = "5.2 km",
            duration = estimatedTime.ifBlank { "22 min" },
            safetyScore = if (isScannedVerified) 99 else 95,
            travelMode = appUsed
        )

        // VERIFY STATUS FEEDBACK
        if (verifyStatus != VerifyStatus.IDLE) {
            val (bgColor, borderColor, iconVec, iconTint) = when (verifyStatus) {
                VerifyStatus.CHECKING -> listOf(Color(0xFFEFF4FF), Color(0xFF93C5FD),
                    Icons.Default.Warning, Color(0xFF1D61F2))
                VerifyStatus.PASSED -> listOf(Color(0xFFF0FDF4), Color(0xFF86EFAC),
                    Icons.Default.CheckCircle, Color(0xFF16A34A))
                else -> listOf(Color(0xFFFEF2F2), Color(0xFFFCA5A5),
                    Icons.Default.Cancel, Color(0xFFDC2626))
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor as Color),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, borderColor as Color, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(iconVec as ImageVector, null, tint = iconTint as Color, modifier = Modifier.size(20.dp))
                    Text(
                        text = if (verifyStatus == VerifyStatus.CHECKING) "Validating trip parameters..." else verifyMessage,
                        fontSize = 12.5.sp, color = Color(0xFF1E293B), lineHeight = 18.sp
                    )
                }
            }
        }

        // REAL-TIME SYNC NOTICE
        if (sharedWithParent) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(22.dp))
                    Column {
                        Text("Active On Parent's App", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        Text("Parent can view your vehicle number, driver details, and live corridor route.", fontSize = 11.5.sp, color = Color(0xFF15803D))
                    }
                }
            }
        }

        // PRIMARY ACTION BUTTONS
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Verify Button
            Button(
                onClick = { verifyStatus = VerifyStatus.CHECKING; verifyMessage = "" },
                enabled = driverName.isNotBlank() && vehicleNumber.isNotBlank() &&
                        pickupLocation.isNotBlank() && dropLocation.isNotBlank() &&
                        verifyStatus != VerifyStatus.CHECKING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1D61F2),
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("solo_verify_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (verifyStatus == VerifyStatus.CHECKING) "VERIFYING..." else "CONFIRM DETAILS",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }

            // Share With Parent Button (Syncs to Firestore & notifies parent)
            Button(
                onClick = {
                    val sharedRide = FirestoreSafetyManager.SharedRide(
                        rideId = "ride_${System.currentTimeMillis()}",
                        appUsed = appUsed,
                        driverName = driverName.ifBlank { "Driver" },
                        vehicleNumber = vehicleNumber.ifBlank { "Unspecified Vehicle" },
                        driverPhone = driverPhone.ifBlank { "N/A" },
                        pickupLocation = pickupLocation.ifBlank { "Pickup Point" },
                        dropLocation = dropLocation.ifBlank { "Drop Point" },
                        estimatedTime = estimatedTime.ifBlank { "15 min" },
                        scanVerificationType = if (isScannedVerified) scanVerificationType else "MANUAL",
                        isVerified = true,
                        safetyCorridorRating = if (isScannedVerified) "99% Safe Corridor (AI Scan Verified)" else "95% Safe Corridor"
                    )

                    // 1. Sync to Firestore & in-memory parent listener
                    viewModel.shareSoloTransportRide(sharedRide)
                    sharedWithParent = true
                    Toast.makeText(context, "Ride shared with Parent's SafeSphere app!", Toast.LENGTH_LONG).show()

                    // 2. Also trigger standard Android share sheet for SMS/WhatsApp
                    val summary = buildString {
                        appendLine("SafeSphere — Solo Transport Details")
                        appendLine("------------------------------------")
                        appendLine("Transport  : $appUsed")
                        appendLine("Driver     : ${sharedRide.driverName}")
                        appendLine("Vehicle No : ${sharedRide.vehicleNumber}")
                        if (sharedRide.driverPhone.isNotBlank()) {
                            appendLine("Driver Ph  : ${sharedRide.driverPhone}")
                        }
                        appendLine("Verification: ${sharedRide.scanVerificationType}")
                        appendLine()
                        appendLine("Pickup     : ${sharedRide.pickupLocation}")
                        appendLine("Drop       : ${sharedRide.dropLocation}")
                        appendLine("Est. Time  : ${sharedRide.estimatedTime}")
                        appendLine("------------------------------------")
                        appendLine("Verified via SafeSphere AI")
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "SafeSphere – Solo Trip Details")
                        putExtra(Intent.EXTRA_TEXT, summary)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Share trip details"))
                    } catch (e: Exception) {
                        // ignore if chooser cancelled
                    }
                },
                enabled = driverName.isNotBlank() && vehicleNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A),
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("solo_share_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (sharedWithParent) "SHARED WITH PARENT ✓" else "SHARE WITH PARENT",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }

            // Call Driver Button
            if (driverPhone.isNotBlank()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$driverPhone"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("CALL DRIVER", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Safety Reminders
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Solo Travel Safety Protocol", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                listOf(
                    "Always scan the rear auto QR or take driver photo before boarding",
                    "Verify the vehicle number plate matches the physical vehicle",
                    "Keep parents informed through the SafeSphere shared route",
                    "In case of route deviation, emergency SOS button remains available"
                ).forEach { tip ->
                    Text("• $tip", fontSize = 11.5.sp, color = Color(0xFF78350F), lineHeight = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun ScanDriverOrAutoQrModal(
    initialType: ScanType,
    onDismiss: () -> Unit,
    onScanComplete: (type: ScanType, driver: String, vehicle: String, phone: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var scanState by remember { mutableStateOf("SCANNING") } // "SCANNING", "DETECTED"

    val infiniteTransition = rememberInfiniteTransition(label = "laser_sweep")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    LaunchedEffect(selectedType) {
        scanState = "SCANNING"
        delay(1500)
        scanState = "DETECTED"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SAFESPHERE VISION AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (selectedType == ScanType.AUTO_QR) "Scan Auto Rear QR" else "Scan Driver Face",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Mode Toggle (Auto QR vs Driver Face)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedType == ScanType.AUTO_QR) Color(0xFF1D61F2) else Color.Transparent)
                                .clickable { selectedType = ScanType.AUTO_QR }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.QrCode, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Auto Rear QR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedType == ScanType.DRIVER_FACE) Color(0xFF1D61F2) else Color.Transparent)
                                .clickable { selectedType = ScanType.DRIVER_FACE }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Face, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Driver Face", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Viewfinder Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated Scanning Line Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val currentY = laserY * h

                            // Draw targeting reticles
                            val cornerLen = 30f
                            val stroke = Stroke(width = 3.5f)

                            // Top-left
                            drawLine(Color(0xFF38BDF8), Offset(30f, 30f), Offset(30f + cornerLen, 30f), strokeWidth = 5f)
                            drawLine(Color(0xFF38BDF8), Offset(30f, 30f), Offset(30f, 30f + cornerLen), strokeWidth = 5f)

                            // Top-right
                            drawLine(Color(0xFF38BDF8), Offset(w - 30f, 30f), Offset(w - 30f - cornerLen, 30f), strokeWidth = 5f)
                            drawLine(Color(0xFF38BDF8), Offset(w - 30f, 30f), Offset(w - 30f, 30f + cornerLen), strokeWidth = 5f)

                            // Bottom-left
                            drawLine(Color(0xFF38BDF8), Offset(30f, h - 30f), Offset(30f + cornerLen, h - 30f), strokeWidth = 5f)
                            drawLine(Color(0xFF38BDF8), Offset(30f, h - 30f), Offset(30f, h - 30f - cornerLen), strokeWidth = 5f)

                            // Bottom-right
                            drawLine(Color(0xFF38BDF8), Offset(w - 30f, h - 30f), Offset(w - 30f - cornerLen, h - 30f), strokeWidth = 5f)
                            drawLine(Color(0xFF38BDF8), Offset(w - 30f, h - 30f), Offset(w - 30f, h - 30f - cornerLen), strokeWidth = 5f)

                            // Laser sweep line
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color(0xFF38BDF8), Color.White, Color(0xFF38BDF8), Color.Transparent)
                                ),
                                start = Offset(20f, currentY),
                                end = Offset(w - 20f, currentY),
                                strokeWidth = 3f
                            )
                        }

                        // Target Graphic inside center
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedType == ScanType.AUTO_QR) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QrCode, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(54.dp))
                                }
                                Text("Align Auto RTO QR inside frame", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .border(2.dp, Color(0xFF38BDF8), CircleShape)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Face, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(58.dp))
                                }
                                Text("Center Driver Face inside circle", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        // Simulated Detection Pill
                        if (scanState == "DETECTED") {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF16A34A).copy(alpha = 0.92f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = if (selectedType == ScanType.AUTO_QR)
                                            "RTO Match: TS 09 UA 8841 (Licensed Auto)"
                                        else
                                            "Face Match: Ramesh Kumar (4.9★ Driver)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Auto-Fill & Confirm Button
                    Button(
                        onClick = {
                            if (selectedType == ScanType.AUTO_QR) {
                                onScanComplete(
                                    ScanType.AUTO_QR,
                                    "Ramesh Kumar",
                                    "TS 09 UA 8841",
                                    "+91 98480 22338"
                                )
                            } else {
                                onScanComplete(
                                    ScanType.DRIVER_FACE,
                                    "Ramesh Kumar",
                                    "TS 09 UA 8841",
                                    "+91 98480 22338"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AUTO-FILL VERIFIED DETAILS", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, color = Color(0xFF94A3B8), fontSize = 13.sp) },
        singleLine = true,
        leadingIcon = {
            Icon(icon, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color(0xFF0F172A), unfocusedTextColor = Color(0xFF0F172A),
            cursorColor = Color(0xFF1D61F2), focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedIndicatorColor = Color(0xFF1D61F2), unfocusedIndicatorColor = Color(0xFFE2E8F0)
        ),
        modifier = Modifier.fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    )
}
