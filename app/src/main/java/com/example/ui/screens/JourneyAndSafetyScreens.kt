package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.TravelMode
import com.example.ui.components.WaterWaveLoadingIndicator
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun RequestJourneyScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val from by viewModel.journeyFrom.collectAsState()
    val to by viewModel.journeyTo.collectAsState()
    val travelMode by viewModel.journeyMode.collectAsState()
    val expectedArrival by viewModel.journeyArrivalTime.collectAsState()

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
            label = "Solo Transport Protocol Active"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Request Journey", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        // Departure / Destination Card
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
                Text(text = "FROM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "From", tint = MaterialTheme.colorScheme.primary)
                        Text(text = from, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDEE9FC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Swap", tint = Color(0xFF474552), modifier = Modifier.size(18.dp))
                    }
                }

                Text(text = "TO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "To", tint = Color(0xFF006398))
                        Text(text = to, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                }
            }
        }

        // Travel Mode Selection
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
                Text(text = "TRAVEL MODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Triple("Car", Icons.Default.DirectionsCar, TravelMode.CAR),
                        Triple("Bus", Icons.Default.DirectionsBus, TravelMode.BUS),
                        Triple("Walk", Icons.Default.DirectionsWalk, TravelMode.WALK)
                    ).forEach { (label, icon, mode) ->
                        val isSelected = travelMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFFCCE5FF) else Color(0xFFEFF4FF))
                                .clickable { viewModel.journeyMode.value = mode }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color(0xFF004B73) else Color(0xFF474552),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF004B73) else Color(0xFF474552),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expected Arrival
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "EXPECTED ARRIVAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = "Time", tint = MaterialTheme.colorScheme.primary)
                            Text(text = expectedArrival, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                        }
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar", tint = Color(0xFF787584), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = { viewModel.submitJourneyRequest() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("send_journey_request_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "SEND REQUEST", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun ParentApprovalScreen(
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
            label = "Parent Verification Gate"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Journey Request", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        // Verification Ready Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEFF4FF))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Live telemetry ready. Both parties must confirm before transit.",
                    fontSize = 12.sp,
                    color = Color(0xFF474552)
                )
            }
        }

        // Student Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(54.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(ALEX_AVATAR_URL)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Alex",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4EDEA3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.School, contentDescription = "Student", tint = Color(0xFF002113), modifier = Modifier.size(11.dp))
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "Alex", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE4DFFF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Student", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43359F))
                            }
                        }
                        Text(text = "School → Home", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                        Text(text = "Expected arrival: 5:00 PM", fontSize = 11.5.sp, color = Color(0xFF006398))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Alex has requested to start their journey. Please approve or deny this route request.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF474552),
                        lineHeight = 17.sp
                    )
                }

                // Approve & Deny Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.approveJourney() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B49)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("parent_approve_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Approve", tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(text = "APPROVE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.denyJourney() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("parent_deny_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Deny", tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(text = "DENY", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun SafetyTimelineScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val timelineEvents by viewModel.timelineEvents.collectAsState()

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
            label = "Journey Audit Active"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
                }
                Text(text = "Safety Timeline", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6FFBBE))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "Completed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005236))
            }
        }

        // Timeline Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                timelineEvents.forEachIndexed { index, event ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val icon = when (event.iconType) {
                            "car" -> Icons.Default.DirectionsCar
                            "location" -> Icons.Default.LocationOn
                            "safe_zone" -> Icons.Default.VerifiedUser
                            "camera" -> Icons.Default.PhotoCamera
                            else -> Icons.Default.Flag
                        }

                        val bgColor = when (event.iconType) {
                            "car" -> Color(0xFF6FFBBE)
                            "location" -> Color(0xFF4EDEA3)
                            "safe_zone" -> Color(0xFF006B49)
                            "camera" -> Color(0xFF5BB8FE)
                            else -> Color(0xFF005036)
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = event.title,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(text = event.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                            Text(text = "${event.timeFormatted} • ${event.locationOrStatus}", fontSize = 12.sp, color = Color(0xFF474552))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun EmergencyScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_pulse_size"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WaterWaveLoadingIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = "Emergency Broadcast Armed"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Text(text = "Emergency", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Big Glowing Red SOS Button
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple halo
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseSize)
                    .clip(CircleShape)
                    .background(Color(0xFFFFDAD6))
            )

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBA1A1A))
                    .clickable { viewModel.runDemoSimulation() }
                    .testTag("sos_emergency_btn"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SOS", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(text = "HOLD 3 SEC", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        Text(
            text = "Press and hold for emergency services & family broadcast",
            fontSize = 11.5.sp,
            color = Color(0xFF474552)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Share Location
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.clickable { viewModel.triggerImOk() }
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE4DFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = Color(0xFF42349F), modifier = Modifier.size(18.dp))
                        }
                        Text(text = "Share Location", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = Color(0xFF787584))
                }
            }

            // Call Trusted Contact
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543211"))
                    context.startActivity(intent)
                }
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCCE5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF006398), modifier = Modifier.size(18.dp))
                        }
                        Text(text = "Call Trusted Contact", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = Color(0xFF787584))
                }
            }

            // Notify Parents
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.clickable { viewModel.triggerImOk() }
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6FFBBE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = "Group", tint = Color(0xFF005036), modifier = Modifier.size(18.dp))
                        }
                        Text(text = "Notify Parents", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = Color(0xFF787584))
                }
            }
        }

        // Emergency PIN Card (Tap to enter duress / normal code)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPinDialog = true }
                .testTag("emergency_pin_card")
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBA1A1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(text = "Emergency PIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF410002))
                        Text(text = "Tap to enter duress or safety code", fontSize = 11.5.sp, color = Color(0xFF410002).copy(alpha = 0.8f))
                    }
                }
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Enter", tint = Color(0xFF410002))
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Emergency / Duress PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your 4-digit code. Duress PIN (4822) activates covert emergency broadcast.", fontSize = 12.sp)
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { if (it.length <= 4) enteredPin = it },
                        placeholder = { Text("••••") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    pinMessage?.let {
                        Text(it, color = Color(0xFFBA1A1A), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.submitPin(
                            enteredPin = enteredPin,
                            onSuccess = {
                                pinMessage = "✓ Normal PIN verified. Status OK."
                            },
                            onDuress = {
                                pinMessage = "✓ Status updated. (Emergency Triggered)"
                            }
                        )
                    }
                ) {
                    Text("SUBMIT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
