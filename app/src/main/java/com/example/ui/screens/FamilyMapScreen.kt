package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.WaterWaveLoadingIndicator
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
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var showLayersToast by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Water loading animation near top center
        WaterWaveLoadingIndicator(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp),
            label = "Live GPS Mutual Telemetry Active"
        )

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
                    onClick = { viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD) },
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
                        text = "Live mutual sharing active",
                        fontSize = 11.5.sp,
                        color = Color(0xFF474552)
                    )
                }
            }

            // Quick Tool Action Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showLayersToast = !showLayersToast },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Layers",
                        tint = Color(0xFF121C2A),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.SAFE_ZONES) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = Color(0xFF121C2A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Map Canvas Area with Overlays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("interactive_map_canvas")
        ) {
            // Static Seattle Map Background
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(MAP_BACKGROUND_SEATTLE)
                    .crossfade(true)
                    .build(),
                contentDescription = "Map Canvas",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(zoomLevel)
            )

            // Vector Overlay Drawing (Route, Geofence circle, Waypoints)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Safe Zone Geofence Circle around School (cx: w * 0.58, cy: h * 0.28)
                val schoolCenter = Offset(w * 0.58f, h * 0.28f)
                drawCircle(
                    color = Color(0x336FFBBE),
                    radius = w * 0.22f,
                    center = schoolCenter
                )
                drawCircle(
                    color = Color(0xFF006B49),
                    radius = w * 0.22f,
                    center = schoolCenter,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )

                // Route path from School to Home
                val alexPos = Offset(w * 0.32f, h * 0.62f)
                val homePos = Offset(w * 0.45f, h * 0.88f)

                val routePath = Path().apply {
                    moveTo(schoolCenter.x, schoolCenter.y)
                    lineTo(schoolCenter.x, h * 0.42f)
                    quadraticTo(schoolCenter.x, h * 0.48f, w * 0.45f, h * 0.52f)
                    lineTo(alexPos.x, alexPos.y)
                    quadraticTo(w * 0.28f, h * 0.72f, homePos.x, homePos.y)
                }

                // Route glow shadow
                drawPath(
                    path = routePath,
                    color = Color(0x665BB8FE),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                // Active route dash line
                drawPath(
                    path = routePath,
                    color = Color(0xFF006398),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                    )
                )
            }

            // Safe Zone Badge Top Center
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF006B49))
                    )
                    Text(
                        text = "Safe Zone (Lincoln School)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006B49)
                    )
                }
            }

            // School Marker Pin
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 120.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF005036)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "School",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Alex Real-time Pin with Pulsating Halo
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 90.dp, top = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulsating aura
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .scale(haloPulse)
                        .clip(CircleShape)
                        .background(Color(0x400284C7))
                )

                // Alex Pin
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF006398), CircleShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ALEX_AVATAR_URL)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Alex on map",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Walker indicator badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF006398)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Walk",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            // Destination Home Pin
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp, start = 30.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Map Controls Right Dock
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                ) {
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel + 0.15f).coerceAtMost(1.6f) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom in", tint = Color(0xFF121C2A), modifier = Modifier.size(16.dp))
                    }
                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(Color(0xFFE2E8F0)).align(Alignment.CenterHorizontally))
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel - 0.15f).coerceAtLeast(0.85f) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom out", tint = Color(0xFF121C2A), modifier = Modifier.size(16.dp))
                    }
                }

                IconButton(
                    onClick = { zoomLevel = 1.0f },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f))
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Recenter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f))
                ) {
                    Icon(imageVector = Icons.Default.Traffic, contentDescription = "Traffic", tint = Color(0xFF474552), modifier = Modifier.size(16.dp))
                }
            }

            // Compass Indicator Top Left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.North,
                    contentDescription = "Compass",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(16.dp)
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
                    Text(
                        text = "Location mutual session active until 5:00 PM",
                        fontSize = 11.5.sp,
                        color = Color(0xFF474552)
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

        // Bottom Floating Telemetry & Family Member Card (Alex)
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.size(50.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(ALEX_AVATAR_URL)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Alex",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF006B49))
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Alex",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFCCE5FF))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "In Transit",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF004B73)
                                    )
                                }
                            }
                            Text(
                                text = "On the way to Home",
                                fontSize = 12.sp,
                                color = Color(0xFF474552)
                            )
                        }
                    }

                    // Direct Call Action
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE4DFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Alex",
                            tint = Color(0xFF43359F),
                            modifier = Modifier.size(20.dp)
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
                        Text(text = "ETA", fontSize = 11.sp, color = Color(0xFF474552))
                        Text(text = "12 min", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Remaining", fontSize = 11.sp, color = Color(0xFF474552))
                        Text(text = "2.4 km", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Battery", fontSize = 11.sp, color = Color(0xFF474552))
                        Text(text = "84%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006B49))
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
                            .height(46.dp)
                            .testTag("notify_on_arrival_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isArrivalNotified) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                                contentDescription = "Notify",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isArrivalNotified) "Reminder Configured ✓" else "Notify on Arrival",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.triggerImOk() },
                        modifier = Modifier
                            .size(46.dp)
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
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sibling Quick Selector Bar
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
                    text = "Family Circle (3)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF474552)
                )
                Text(
                    text = "All Safe",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006B49)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Alex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE4DFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "Alex • 12m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43359F))
                }

                // Maya
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "Maya • Library", fontSize = 11.sp, color = Color(0xFF121C2A))
                }

                // David
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "David • Office", fontSize = 11.sp, color = Color(0xFF121C2A))
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
