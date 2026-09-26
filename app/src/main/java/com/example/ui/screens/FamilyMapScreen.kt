package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserRole
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination

const val MAP_BACKGROUND_SEATTLE =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBQD83Ecc27kbK3Kr4hzwzSM84-goC0l95Y24Wm4lUTe6wBAIZZ-8JROIhBaGfJZGvCJoTmtTO2kgFtFgIn6W1k2OAnE6fY-fdgGmFfCdPoWNduUmJj_TBnytELb0SujIgXEikoBuUxvQYIXkafT0uDuhIDRVf4JMZgytKefncEd1MO4V1-xmGte1Vktf0kouwbJHKDDhAFoLeYpy4Yb1ItWYT1Ze08YzdTMp9WzbvKAUsXoktLFnDfzA"

@Composable
fun FamilyMapScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isArrivalNotified by remember { mutableStateOf(false) }
    var showLayersToast by remember { mutableStateOf(false) }
    var isFullscreenMap by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val activeRole by viewModel.activeDashboardRole.collectAsState()
    val linkedStudents by viewModel.linkedStudents.collectAsState()
    val linkedParents by viewModel.linkedParents.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val liveLoc by viewModel.selectedStudentLocation.collectAsState()

    // Safe Zone Direct Map Marking State
    var isMarkingSafeZone by remember { mutableStateOf(false) }
    var markedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var newZoneName by remember { mutableStateOf("") }
    var newZoneRadius by remember { mutableFloatStateOf(200f) }
    var newZoneAlertOnExit by remember { mutableStateOf(true) }
    var showCameraSelectDialog by remember { mutableStateOf(false) }
    val activeGeofenceAlert by viewModel.activeGeofenceAlert.collectAsState()

    if (isFullscreenMap) {
        // Full Field View (100vh) for Leaflet Map
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .statusBarsPadding()
        ) {
            LeafletMapView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
                isMarkingMode = isMarkingSafeZone,
                markingRadius = newZoneRadius,
                onLocationMarked = { lat, lng ->
                    markedLocation = Pair(lat, lng)
                },
                onWebViewReady = { webViewRef = it }
            )

            // Floating Header Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { isFullscreenMap = false },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Fullscreen",
                        tint = Color(0xFF121C2A),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Button(
                    onClick = {
                        isMarkingSafeZone = !isMarkingSafeZone
                        if (!isMarkingSafeZone) {
                            markedLocation = null
                            webViewRef?.evaluateJavascript("if (window.clearTempMark) window.clearTempMark();", null)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMarkingSafeZone) Color(0xFFDC2626) else Color(0xFF006B49)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = if (isMarkingSafeZone) Icons.Default.Close else Icons.Default.PinDrop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMarkingSafeZone) "Cancel Mark" else "📍 Mark Safe Zone",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { webViewRef?.evaluateJavascript("window.recenterChild();", null) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D61F2))
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isMarkingSafeZone) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF006B49).copy(alpha = 0.94f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Text(
                        text = "📍 TAP ANYWHERE ON MAP to mark center pin for new Safe Zone",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Subheader navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (viewModel.activeDashboardRole.value == com.example.data.model.UserRole.PARENT) {
                            viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                        } else {
                            viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF121C2A),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Family Map",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                    Text(
                        text = "Live GPS • Leaflet & OpenStreetMap",
                        fontSize = 11.5.sp,
                        color = Color(0xFF474552)
                    )
                }
            }

            // Quick Tool Action Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.SAFE_ZONES) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Safe Zones",
                        tint = Color(0xFF121C2A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Active Geofence Breach Alert Banner (Triggered when student exits safe zone)
        if (activeGeofenceAlert != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "SECURITY TRIGGER: GEOFENCE EXIT",
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = activeGeofenceAlert ?: "",
                        color = Color(0xFF7F1D1D),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { webViewRef?.evaluateJavascript("window.recenterChild();", null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Track on Map", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.dismissGeofenceAlert() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Acknowledge", fontSize = 11.5.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Safe Zone Tap Instructions Banner
        if (isMarkingSafeZone) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF006B49))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📍 TAP ON MAP to drop center pin (Radius: ${newZoneRadius.toInt()}m)",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cancel",
                        color = Color(0xFF6FFBBE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            isMarkingSafeZone = false
                            markedLocation = null
                            webViewRef?.evaluateJavascript("if (window.clearTempMark) window.clearTempMark();", null)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Map Canvas Area — Real Leaflet/OpenStreetMap via WebView
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("interactive_map_canvas")
        ) {
            // Real interactive OSM/Leaflet map via WebView
            LeafletMapView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
                isMarkingMode = isMarkingSafeZone,
                markingRadius = newZoneRadius,
                onLocationMarked = { lat, lng ->
                    markedLocation = Pair(lat, lng)
                },
                onWebViewReady = { webViewRef = it }
            )

            // Top Overlay Bar: Live Telemetry + Fullscreen & Mark Zone Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val liveLoc by viewModel.selectedStudentLocation.collectAsState()
                val lat = liveLoc?.latitude ?: 17.3850
                val lng = liveLoc?.longitude ?: 78.4867
                val accuracy = liveLoc?.accuracy?.toInt() ?: 15
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.94f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            isMarkingSafeZone = !isMarkingSafeZone
                            if (!isMarkingSafeZone) {
                                markedLocation = null
                                webViewRef?.evaluateJavascript("if (window.clearTempMark) window.clearTempMark();", null)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMarkingSafeZone) Color(0xFFDC2626) else Color(0xFF006B49)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMarkingSafeZone) Icons.Default.Close else Icons.Default.PinDrop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMarkingSafeZone) "Cancel" else "📍 Mark Zone",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { isFullscreenMap = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.95f),
                            contentColor = Color(0xFF1D61F2)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("⛶ Full", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom Right Controls: Recenter FAB
            IconButton(
                onClick = { webViewRef?.evaluateJavascript("window.recenterChild();", null) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter",
                    tint = Color(0xFF1D61F2),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Shared Autonomy Transparency Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFDEE9FC).copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockClock,
                        contentDescription = "Session",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    val sessionActive = if (activeRole == UserRole.PARENT) linkedStudents.isNotEmpty() else linkedParents.isNotEmpty()
                    Text(
                        text = if (sessionActive) "Location mutual session active • End-to-end encrypted" else "Device unlinked • Connect parent & child to enable telemetry",
                        fontSize = 11.5.sp,
                        color = Color(0xFF474552),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Text(
                    text = "Manage",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Family Card based on Role & Linkage
        if (activeRole == UserRole.PARENT) {
            if (linkedStudents.isEmpty()) {
                // Unlinked Empty State Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF4FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShareLocation,
                                contentDescription = "Unlinked",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "No Child Device Linked",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                        Text(
                            text = "Link your child's phone using a secure 6-digit sync code to track real-time location, safe zones, and battery health.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Button(
                            onClick = { viewModel.navigateTo(ScreenDestination.ENTER_LINK_CODE) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Link Student Phone", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                val currentStudent = selectedStudent ?: linkedStudents.first()
                val stuName = currentStudent.studentName.ifBlank { "Student" }
                val isMoving = liveLoc?.isMoving == true
                val speedKmh = ((liveLoc?.speed ?: 0f) * 3.6f)
                val accuracyM = liveLoc?.accuracy?.toInt() ?: 15
                val batteryPct = liveLoc?.batteryLevel ?: 95

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Identity Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE4DFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stuName.take(1).uppercase(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF43359F)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(if (isMoving) Color(0xFF0284C7) else Color(0xFF006B49))
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stuName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF121C2A),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Box(
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isMoving) Color(0xFFCCE5FF) else Color(0xFFE8F5E9))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isMoving) "In Motion" else "Stationary",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMoving) Color(0xFF004B73) else Color(0xFF1B5E20),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isMoving) "Speed: ${String.format("%.1f", speedKmh)} km/h" else "Safe at verified location",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF474552)
                                    )
                                }
                            }

                            // Direct Call Action
                            IconButton(
                                onClick = {
                                    val phoneToCall = currentStudent.studentPhone.ifBlank { "911" }
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE4DFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call $stuName",
                                    tint = Color(0xFF43359F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Route Status Metrics Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEFF4FF))
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Status", fontSize = 11.sp, color = Color(0xFF474552))
                                Text(
                                    text = if (isMoving) "Moving" else "Stationary",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Accuracy", fontSize = 11.sp, color = Color(0xFF474552))
                                Text(
                                    text = "±${accuracyM}m",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Battery", fontSize = 11.sp, color = Color(0xFF474552))
                                Text(
                                    text = "$batteryPct%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006B49)
                                )
                            }
                        }

                        // Action Pill Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isArrivalNotified = !isArrivalNotified },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isArrivalNotified) Color(0xFF006B49) else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("notify_on_arrival_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isArrivalNotified) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                                        contentDescription = "Arrival",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isArrivalNotified) "Reminder Configured ✓" else "Notify on Arrival",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.triggerImOk() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF4FF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShareLocation,
                                    contentDescription = "Share",
                                    tint = Color(0xFF006398)
                                )
                            }
                        }

                        // Consensual Live Camera Telemetry Button for Parent
                        Button(
                            onClick = { showCameraSelectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Request Live Camera Access",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // Student View
            if (linkedParents.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF4FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShareLocation,
                                contentDescription = "Unlinked",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "No Parent Linked",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                        Text(
                            text = "Generate a link code to connect your parent's phone so they can receive your real-time safety updates.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Button(
                            onClick = { viewModel.navigateTo(ScreenDestination.LINK_CODE_GENERATOR) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ShareLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Link Code", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sharing Active",
                                    tint = Color(0xFF006B49),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Location Sharing Active",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                                Text(
                                    text = "Connected with ${linkedParents.joinToString(", ") { it.parentName }}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF474552)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.triggerImOk() },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B49)),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send \"I'm OK\" to Parents", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Sibling Quick Selector Bar
        if (activeRole == UserRole.PARENT && linkedStudents.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Family Circle (${linkedStudents.size})",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF474552)
                    )
                    Text(
                        text = "Linked",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006B49)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    linkedStudents.forEach { student ->
                        val isSelected = (selectedStudent?.studentUid == student.studentUid) || (selectedStudent == null && student == linkedStudents.first())
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFFE4DFFF) else Color(0xFFEFF4FF))
                                .clickable { viewModel.selectStudent(student) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = student.studentName.ifBlank { "Student" },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF43359F) else Color(0xFF121C2A),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // ── Dialog 1: Direct Map Pin Safe Zone Creation ──────────────────
    if (markedLocation != null) {
        val lat = markedLocation!!.first
        val lng = markedLocation!!.second
        AlertDialog(
            onDismissRequest = {
                markedLocation = null
                webViewRef?.evaluateJavascript("if (window.clearTempMark) window.clearTempMark();", null)
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛡️", fontSize = 24.sp)
                }
            },
            title = {
                Text(
                    text = "Create Safe Zone on Map",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF121C2A)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Center: ${String.format("%.5f", lat)}, ${String.format("%.5f", lng)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF006B49)
                    )

                    OutlinedTextField(
                        value = newZoneName,
                        onValueChange = { newZoneName = it },
                        label = { Text("Safe Zone Name (e.g. Campus, Home)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick name suggestion chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Home", "School", "Hostel", "Library").forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (newZoneName == suggestion) Color(0xFF006B49) else Color(0xFFEFF4FF))
                                    .clickable { newZoneName = suggestion }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (newZoneName == suggestion) Color.White else Color(0xFF1D61F2)
                                )
                            }
                        }
                    }

                    // Radius slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Safe Zone Radius:", fontSize = 12.sp, color = Color(0xFF474552))
                            Text(
                                text = "${newZoneRadius.toInt()} meters",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006B49)
                            )
                        }
                        Slider(
                            value = newZoneRadius,
                            onValueChange = {
                                newZoneRadius = it
                                webViewRef?.evaluateJavascript("if (window.updateTempRadius) window.updateTempRadius(${it.toInt()});", null)
                            },
                            valueRange = 50f..1500f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF006B49),
                                activeTrackColor = Color(0xFF006B49)
                            )
                        )
                    }

                    // Trigger alert on exit switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alert Parent on Exit",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Trigger instant notification if student leaves zone",
                                fontSize = 10.5.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                        Switch(
                            checked = newZoneAlertOnExit,
                            onCheckedChange = { newZoneAlertOnExit = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFDC2626)
                            )
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        markedLocation = null
                        webViewRef?.evaluateJavascript("if (window.clearTempMark) window.clearTempMark();", null)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newZoneName.ifBlank { "Safe Zone" }
                        viewModel.createFirestoreSafeZone(
                            name = name,
                            lat = lat,
                            lng = lng,
                            radius = newZoneRadius.toDouble(),
                            alertOnExit = newZoneAlertOnExit
                        )
                        markedLocation = null
                        isMarkingSafeZone = false
                        newZoneName = ""
                        webViewRef?.evaluateJavascript("if (window.clearTempMark) window.clearTempMark();", null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B49)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Safe Zone", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // ── Dialog 2: Request Live Camera Access ─────────────────────────
    if (showCameraSelectDialog) {
        val student = selectedStudent
        val stuName = student?.studentName ?: "Student"
        AlertDialog(
            onDismissRequest = { showCameraSelectDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Request Live Camera Feed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF121C2A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A consent request will be sent to $stuName's phone. Once accepted, live camera video will stream to your app in real-time.",
                        fontSize = 13.sp,
                        color = Color(0xFF474552),
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "Select starting camera lens:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF121C2A)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showCameraSelectDialog = false
                                viewModel.requestLiveCamera(initialFacing = "BACK")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Back Lens\n(Surroundings)", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showCameraSelectDialog = false
                                viewModel.requestLiveCamera(initialFacing = "FRONT")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43359F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Front Lens\n(Selfie/Face)", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCameraSelectDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

/**
 * Interactive Leaflet Map using Google tile layers and OpenStreetMap fallback,
 * rendered within an Android WebView.
 * Implements:
 * - Live Google Maps tiles via Leaflet tileLayer
 * - Interactive Markers for Home (Safe Zone), Destination School, and In-Transit Child (Alex)
 * - Safe zone geofence radius boundary
 * - Transit path visualization
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier,
    isMarkingMode: Boolean = false,
    markingRadius: Float = 200f,
    onLocationMarked: ((Double, Double) -> Unit)? = null,
    onWebViewReady: ((WebView) -> Unit)? = null
) {
    val liveLocation by viewModel.selectedStudentLocation.collectAsState()
    val safeZones by viewModel.selectedStudentSafeZones.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()

    // Default to Hyderabad coordinates 17.3850, 78.4867 if live location is not yet received
    val studentLat = liveLocation?.latitude ?: 17.3850
    val studentLng = liveLocation?.longitude ?: 78.4867
    val studentName = selectedStudent?.studentName ?: "Student"
    val accuracy = liveLocation?.accuracy ?: 15f
    val speedKmh = ((liveLocation?.speed ?: 0f) * 3.6f)
    val statusText = if (liveLocation?.isMoving == true) "Moving • ${String.format("%.1f", speedKmh)} km/h" else "Stationary"
    val batteryLevel = liveLocation?.batteryLevel ?: 92

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Real-time reactive update: when student phone publishes new GPS coords to Firebase,
    // update the Leaflet childMarker and pan the map smoothly WITHOUT reloading the WebView.
    LaunchedEffect(liveLocation?.latitude, liveLocation?.longitude, liveLocation?.accuracy, liveLocation?.isMoving, liveLocation?.batteryLevel) {
        liveLocation?.let { loc ->
            val movingDesc = if (loc.isMoving) "Moving • ${String.format("%.1f", (loc.speed * 3.6f))} km/h" else "Stationary"
            val js = "if (window.updateChildLocation) { window.updateChildLocation(${loc.latitude}, ${loc.longitude}, ${loc.accuracy}, '$movingDesc', ${loc.batteryLevel}); }"
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    // Reactive update for safe-zone marking mode & live radius adjustment
    LaunchedEffect(isMarkingMode, markingRadius) {
        val js = "if (window.setMarkingMode) { window.setMarkingMode($isMarkingMode, $markingRadius); }"
        webViewRef?.evaluateJavascript(js, null)
    }

    val safeZonesJs = remember(safeZones) {
        if (safeZones.isEmpty()) {
            "// No safe zones configured yet"
        } else {
            safeZones.joinToString("\n") { zone ->
                """
                const zone_${zone.zoneId.replace("-", "_")} = L.circle([${zone.latitude}, ${zone.longitude}], {
                    radius: ${zone.radiusMeters},
                    color: '#059669',
                    fillColor: '#10B981',
                    fillOpacity: 0.22,
                    weight: 2
                }).addTo(map);
                zone_${zone.zoneId.replace("-", "_")}.bindPopup("<b>🛡️ ${zone.name}</b><br>Radius: ${zone.radiusMeters}m");
                """.trimIndent()
            }
        }
    }

    val htmlContent = remember(safeZonesJs) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="file:///android_asset/leaflet/leaflet.css" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="file:///android_asset/leaflet/leaflet.js"></script>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                * { box-sizing: border-box; }
                html, body {
                    height: 100%;
                    min-height: 100vh;
                    width: 100%;
                    margin: 0;
                    padding: 0;
                    background: #F8FAFC;
                    overflow: hidden;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                }
                #map {
                    position: fixed;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    height: 100vh;
                    width: 100vw;
                    min-height: 250px;
                    background: #E2E8F0;
                }
                .pulse-avatar {
                    background: #1D61F2;
                    border: 2.5px solid #FFFFFF;
                    border-radius: 50%;
                    box-shadow: 0 0 16px rgba(29, 97, 242, 0.95);
                    animation: pulse 1.6s infinite;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-size: 14px;
                }
                @keyframes pulse {
                    0% { transform: scale(0.92); box-shadow: 0 0 0 0 rgba(29, 97, 242, 0.7); }
                    70% { transform: scale(1.18); box-shadow: 0 0 0 12px rgba(29, 97, 242, 0); }
                    100% { transform: scale(0.92); box-shadow: 0 0 0 0 rgba(29, 97, 242, 0); }
                }
                .leaflet-popup-content-wrapper {
                    border-radius: 12px;
                    box-shadow: 0 4px 14px rgba(0,0,0,0.15);
                }
                .leaflet-popup-content {
                    margin: 10px 14px;
                    line-height: 1.4;
                }
            </style>
        </head>
        <body>
            <div id="map" style="height: 100vh; width: 100%;"></div>
            <script>
                let map;
                let childMarker;
                let accuracyCircle;
                let tempMarkMarker = null;
                let tempMarkCircle = null;
                window.isMarkingMode = false;
                window.currentRadius = 200;

                function resizeMapToFit() {
                    var h = window.innerHeight || document.documentElement.clientHeight || 400;
                    var w = window.innerWidth || document.documentElement.clientWidth || 360;
                    var mapEl = document.getElementById('map');
                    if (mapEl && h > 0) {
                        mapEl.style.height = h + 'px';
                        mapEl.style.width = w + 'px';
                    }
                    if (typeof map !== 'undefined' && map) {
                        map.invalidateSize();
                    }
                }
                window.addEventListener('resize', resizeMapToFit);
                setInterval(resizeMapToFit, 500);

                window.setMarkingMode = function(enabled, radius) {
                    window.isMarkingMode = enabled;
                    if (radius) window.currentRadius = radius;
                    if (!enabled && tempMarkMarker) {
                        if (map) {
                            if (tempMarkMarker) map.removeLayer(tempMarkMarker);
                            if (tempMarkCircle) map.removeLayer(tempMarkCircle);
                        }
                        tempMarkMarker = null;
                        tempMarkCircle = null;
                    }
                };

                window.updateTempRadius = function(radius) {
                    window.currentRadius = radius;
                    if (tempMarkCircle) {
                        tempMarkCircle.setRadius(radius);
                    }
                };

                window.clearTempMark = function() {
                    if (tempMarkMarker && map) map.removeLayer(tempMarkMarker);
                    if (tempMarkCircle && map) map.removeLayer(tempMarkCircle);
                    tempMarkMarker = null;
                    tempMarkCircle = null;
                };

                function createPopupHtml(lat, lng, status, battery) {
                    return '<div style="font-size: 12px; font-family: sans-serif;">' +
                           '  <strong style="color: #1D61F2; font-size: 13px;">📍 $studentName</strong><br>' +
                           '  <span><b>Status:</b> ' + (status || '$statusText') + '</span><br>' +
                           '  <span><b>Battery:</b> ' + (battery || $batteryLevel) + '%</span><br>' +
                           '  <span style="color: #64748B; font-size: 11px;">' + Number(lat).toFixed(4) + ', ' + Number(lng).toFixed(4) + '</span>' +
                           '</div>';
                }

                function initMap() {
                    if (map) return;
                    try {
                        console.log("SafeSphere: Starting Leaflet map initialization...");
                        // 1. Initialize Leaflet Map
                        map = L.map("map", {
                            zoomControl: true,
                            attributionControl: true
                        }).setView([$studentLat, $studentLng], 15);

                        // 2. OpenStreetMap Tile Layer
                        const osmLayer = L.tileLayer(
                            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
                            {
                                maxZoom: 19,
                                attribution: "&copy; OpenStreetMap contributors"
                            }
                        ).addTo(map);

                        // Satellite layer option
                        const satLayer = L.tileLayer("https://mt1.google.com/vt/lyrs=s&x={x}&y={y}&z={z}", {
                            maxZoom: 20,
                            attribution: "&copy; Google Satellite"
                        });

                        L.control.layers({
                            "OpenStreetMap": osmLayer,
                            "Satellite": satLayer
                        }, null, { position: 'topright' }).addTo(map);

                        L.control.scale({ imperial: false, metric: true, position: 'bottomleft' }).addTo(map);

                        // 3. Child Marker
                        const childIcon = L.divIcon({
                            className: 'pulse-avatar',
                            html: '📍',
                            iconSize: [28, 28],
                            iconAnchor: [14, 14]
                        });

                        childMarker = L.marker([$studentLat, $studentLng], { icon: childIcon }).addTo(map);
                        childMarker.bindPopup(createPopupHtml($studentLat, $studentLng, '$statusText', $batteryLevel)).openPopup();

                        // GPS Accuracy circle
                        accuracyCircle = L.circle([$studentLat, $studentLng], {
                            radius: $accuracy,
                            color: '#1D61F2',
                            fillColor: '#93C5FD',
                            fillOpacity: 0.16,
                            weight: 1
                        }).addTo(map);

                        // 4. Safe-zone circles
                        $safeZonesJs

                        // 5. Interactive Click Listener for Safe Zone Marking on Map
                        map.on('click', function(e) {
                            if (window.isMarkingMode) {
                                var lat = e.latlng.lat;
                                var lng = e.latlng.lng;
                                if (tempMarkMarker && map) map.removeLayer(tempMarkMarker);
                                if (tempMarkCircle && map) map.removeLayer(tempMarkCircle);

                                var pinIcon = L.divIcon({
                                    className: 'mark-pin',
                                    html: '<div style="background:#006B49;color:white;border-radius:50%;width:34px;height:34px;display:flex;align-items:center;justify-content:center;font-size:18px;border:3px solid white;box-shadow:0 3px 12px rgba(0,0,0,0.35);">🛡️</div>',
                                    iconSize: [34, 34],
                                    iconAnchor: [17, 17]
                                });

                                tempMarkMarker = L.marker([lat, lng], { icon: pinIcon }).addTo(map);
                                tempMarkCircle = L.circle([lat, lng], {
                                    radius: window.currentRadius || 200,
                                    color: '#006B49',
                                    fillColor: '#10B981',
                                    fillOpacity: 0.28,
                                    dashArray: '6, 6',
                                    weight: 2.5
                                }).addTo(map);

                                if (window.SafeSphereBridge && window.SafeSphereBridge.onLocationMarked) {
                                    window.SafeSphereBridge.onLocationMarked(lat, lng);
                                }
                            }
                        });

                        console.log("SafeSphere: Leaflet map initialized successfully!");
                        setTimeout(function() {
                            if (map) map.invalidateSize();
                        }, 250);
                    } catch(e) {
                        console.error("SafeSphere: Leaflet init error: " + e.message);
                    }
                }

                // 6. Reactive function to update child location and pan
                function updateChildLocation(latitude, longitude, accuracy, status, battery) {
                    if (!childMarker || !map) return;
                    childMarker.setLatLng([latitude, longitude]);
                    if (accuracyCircle) {
                        accuracyCircle.setLatLng([latitude, longitude]);
                        if (accuracy) accuracyCircle.setRadius(accuracy);
                    }
                    childMarker.setPopupContent(createPopupHtml(latitude, longitude, status, battery));
                    map.panTo([latitude, longitude], { animate: true, duration: 0.8 });
                }

                window.updateChildLocation = updateChildLocation;

                window.recenterChild = function() {
                    if (childMarker && map) {
                        map.setView(childMarker.getLatLng(), 16, { animate: true });
                        childMarker.openPopup();
                    }
                };

                function pollForLeaflet(attempts) {
                    if (typeof L !== 'undefined' && typeof L.map === 'function') {
                        initMap();
                    } else if (attempts < 60) {
                        setTimeout(function() { pollForLeaflet(attempts + 1); }, 100);
                    } else {
                        console.error("SafeSphere: Timeout waiting for Leaflet library to load from CDN.");
                    }
                }

                if (document.readyState === 'complete' || document.readyState === 'interactive') {
                    pollForLeaflet(0);
                } else {
                    window.addEventListener('DOMContentLoaded', function() { pollForLeaflet(0); });
                    window.addEventListener('load', function() { pollForLeaflet(0); });
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                webViewRef = this
                onWebViewReady?.invoke(this)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Fix: use android.graphics.Color to avoid hex overflow on Int
                setBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"))

                // JavaScript interface bridge for receiving marked coordinates on map click
                addJavascriptInterface(
                    object {
                        @android.webkit.JavascriptInterface
                        fun onLocationMarked(lat: Double, lng: Double) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onLocationMarked?.invoke(lat, lng)
                            }
                        }
                    },
                    "SafeSphereBridge"
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    // CRITICAL FIX: useWideViewPort must be TRUE for Leaflet map to render
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    // Required for OSM tiles and Leaflet CDN fallback to load
                    mediaPlaybackRequiresUserGesture = false
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 SafeSphereFamily/1.0"
                    // Use LOAD_NO_CACHE to avoid serving stale blank pages
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    allowFileAccess = true
                    allowContentAccess = true
                    @Suppress("DEPRECATION")
                    allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    allowUniversalAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                WebView.setWebContentsDebuggingEnabled(true)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("SafeSphereMap", "WebView onPageFinished: $url")
                        // Force map to recalculate its size after page finishes loading
                        view?.evaluateJavascript("""
                            (function() {
                                var mapEl = document.getElementById('map');
                                if (mapEl) {
                                    mapEl.style.height = window.innerHeight + 'px';
                                    mapEl.style.width = window.innerWidth + 'px';
                                }
                                if (typeof pollForLeaflet === 'function') { pollForLeaflet(0); }
                                if (typeof map !== 'undefined' && map) { map.invalidateSize(); }
                                var rect = mapEl ? mapEl.getBoundingClientRect() : null;
                                var tileCount = document.querySelectorAll('.leaflet-tile').length;
                                return JSON.stringify({
                                    hasL: typeof L !== 'undefined',
                                    hasMap: typeof map !== 'undefined',
                                    mapRect: rect ? {w: rect.width, h: rect.height} : 'none',
                                    tileCount: tileCount,
                                    bodyH: document.body.offsetHeight,
                                    bodyW: document.body.offsetWidth
                                });
                            })()
                        """) { res ->
                            Log.e("SafeSphereDiagnostic", "MAP_INSPECT: $res")
                        }
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        Log.e("SafeSphereMap", "WebView resource error: ${error?.description} on ${request?.url}")
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("SafeSphereJS", "[${consoleMessage?.messageLevel()}]: ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                        return true
                    }
                }
                // Load with file:///android_asset/leaflet/ as base URL so local
                // leaflet.js and leaflet.css load via relative paths, then OSM
                // tiles load over the internet via absolute URLs.
                loadDataWithBaseURL(
                    "file:///android_asset/leaflet/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            webViewRef = webView
            onWebViewReady?.invoke(webView)
        }
    )
}

