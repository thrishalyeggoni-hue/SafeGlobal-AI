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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.darkTextFieldColors
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun PredictedRouteCard(
    origin: String,
    destination: String,
    distanceKm: String = "4.2 km",
    duration: String = "20 min",
    safetyScore: Int = 98,
    travelMode: String = "Car",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "route_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tracer_dot"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("PREDICTED ROUTE DISPLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), letterSpacing = 0.5.sp)
                        Text("AI Safe Corridor Navigation", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("$safetyScore% Safe Corridor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }

            // Visual route map canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val p0 = Offset(w * 0.12f, h * 0.65f)
                    val p1 = Offset(w * 0.45f, h * 0.25f)
                    val p2 = Offset(w * 0.88f, h * 0.55f)

                    val routePath = Path().apply {
                        moveTo(p0.x, p0.y)
                        quadraticTo(p1.x, p1.y, p2.x, p2.y)
                    }

                    // Glow background line
                    drawPath(
                        path = routePath,
                        color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                        style = Stroke(width = 10f, cap = StrokeCap.Round)
                    )
                    // Core route line
                    drawPath(
                        path = routePath,
                        color = Color(0xFF38BDF8),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Origin Dot
                    drawCircle(color = Color(0xFF22C55E), radius = 9f, center = p0)
                    drawCircle(color = Color.White, radius = 4f, center = p0)

                    // Destination Dot
                    drawCircle(color = Color(0xFFEF4444), radius = 9f, center = p2)
                    drawCircle(color = Color.White, radius = 4f, center = p2)

                    // Animated Pulse vehicle indicator
                    val t = pulseProgress
                    val invT = 1f - t
                    val curX = invT * invT * p0.x + 2f * invT * t * p1.x + t * t * p2.x
                    val curY = invT * invT * p0.y + 2f * invT * t * p1.y + t * t * p2.y
                    val pulseOffset = Offset(curX, curY)

                    drawCircle(color = Color(0xFFFBBF24).copy(alpha = 0.4f), radius = 16f, center = pulseOffset)
                    drawCircle(color = Color(0xFFF59E0B), radius = 7f, center = pulseOffset)
                    drawCircle(color = Color.White, radius = 3f, center = pulseOffset)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(origin.ifBlank { "Origin" }.take(18), fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Text(destination.ifBlank { "Destination" }.take(18), fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                }
            }

            // Route Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DISTANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(distanceKm, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EST. TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(duration, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFCBD5E1)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(travelMode, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                }
            }

            // Safe Corridor Waypoint Checkpoints
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("SECURE WAYPOINT CHECKPOINTS", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.5.sp)
                listOf(
                    "📍 Origin: ${origin.ifBlank { "School Campus" }}",
                    "🛡️ Safe Corridor: Nehru Outer Ring Road (CCTV Armed)",
                    "🚦 Transit Crossing: Safe Zone Sector 4",
                    "🏁 Destination: ${destination.ifBlank { "Home, Green Acres" }}"
                ).forEach { step ->
                    Text(step, fontSize = 11.sp, color = Color(0xFF334155))
                }
            }
        }
    }
}

@Composable
fun RequestJourneyScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val fromState by viewModel.journeyFrom.collectAsState()
    val toState by viewModel.journeyTo.collectAsState()
    val travelMode by viewModel.journeyMode.collectAsState()
    val expectedArrival by viewModel.journeyArrivalTime.collectAsState()

    var fromText by remember { mutableStateOf(fromState.ifBlank { "School Campus, Gate 2" }) }
    var toText by remember { mutableStateOf(toState.ifBlank { "Home, Green Acres" }) }
    var arrivalText by remember { mutableStateOf(expectedArrival.ifBlank { "25 min" }) }

    LaunchedEffect(fromText, toText, arrivalText) {
        viewModel.journeyFrom.value = fromText
        viewModel.journeyTo.value = toText
        viewModel.journeyArrivalTime.value = arrivalText
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
            }
            Column {
                Text(text = "Request Journey", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                Text(text = "Plan your route & request guardian approval with live tracking", fontSize = 11.5.sp, color = Color(0xFF64748B))
            }
        }

        // Editable Departure & Destination Card
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
                Text(text = "DEPARTURE & DESTINATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)

                OutlinedTextField(
                    value = fromText,
                    onValueChange = { fromText = it },
                    label = { Text("FROM (Origin)") },
                    placeholder = { Text("e.g. School Campus, Gate 2") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF16A34A)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC), unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedIndicatorColor = Color(0xFF16A34A), unfocusedIndicatorColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("journey_from_input")
                )

                // Quick presets for origin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("School", "Library", "Metro Stn", "Current GPS").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF4FF))
                                .clickable {
                                    fromText = when (preset) {
                                        "School" -> "School Campus, Gate 2"
                                        "Library" -> "Central City Library"
                                        "Metro Stn" -> "Metro Station Sector 10"
                                        else -> "Current GPS Location"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(preset, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                        }
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDEE9FC))
                            .clickable {
                                val temp = fromText
                                fromText = toText
                                toText = temp
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Swap Locations", tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    }
                }

                OutlinedTextField(
                    value = toText,
                    onValueChange = { toText = it },
                    label = { Text("TO (Destination)") },
                    placeholder = { Text("e.g. Home, Green Acres") },
                    leadingIcon = { Icon(Icons.Default.Home, null, tint = Color(0xFF2563EB)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC), unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedIndicatorColor = Color(0xFF2563EB), unfocusedIndicatorColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("journey_to_input")
                )

                // Quick presets for destination
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Home", "Tuition", "Sports Ground", "Friend's Place").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF4FF))
                                .clickable {
                                    toText = when (preset) {
                                        "Home" -> "Home, Green Acres"
                                        "Tuition" -> "Alpha Coaching Academy"
                                        "Sports Ground" -> "Sports Complex Ground"
                                        else -> "Friend's House, Sector 7"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(preset, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                        }
                    }
                }
            }
        }

        // Predicted Route Display
        PredictedRouteCard(
            origin = fromText,
            destination = toText,
            distanceKm = "4.2 km",
            duration = arrivalText,
            safetyScore = 98,
            travelMode = when (travelMode) {
                TravelMode.CAR -> "Car / Taxi"
                TravelMode.BUS -> "Public Transit"
                TravelMode.WALK -> "Walking"
            }
        )

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
                        Triple("Car / Cab", Icons.Default.DirectionsCar, TravelMode.CAR),
                        Triple("Bus", Icons.Default.DirectionsBus, TravelMode.BUS),
                        Triple("Walk", Icons.Default.DirectionsWalk, TravelMode.WALK)
                    ).forEach { (label, icon, mode) ->
                        val isSelected = travelMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF1D61F2) else Color(0xFFEFF4FF))
                                .clickable { viewModel.journeyMode.value = mode }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color.White else Color(0xFF474552),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF474552),
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
                Text(text = "EXPECTED TRAVEL TIME (ETA)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF474552), letterSpacing = 0.5.sp)
                OutlinedTextField(
                    value = arrivalText,
                    onValueChange = { arrivalText = it },
                    label = { Text("Travel Time / ETA") },
                    leadingIcon = { Icon(Icons.Default.Schedule, null, tint = Color(0xFF2563EB)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC), unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedIndicatorColor = Color(0xFF2563EB), unfocusedIndicatorColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("journey_eta_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("15 min", "25 min", "40 min", "1 hour").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (arrivalText == preset) Color(0xFF1D61F2) else Color(0xFFEFF4FF))
                                .clickable { arrivalText = preset }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(preset, fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                                color = if (arrivalText == preset) Color.White else Color(0xFF2563EB))
                        }
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                viewModel.submitJourneyRequest(
                    origin = fromText,
                    destination = toText,
                    mode = travelMode,
                    expectedArrival = arrivalText
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
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
                Text(text = "SEND ROUTE REQUEST TO PARENTS", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    val incomingJourneys by viewModel.incomingJourneysForParent.collectAsState()

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

        if (incomingJourneys.isEmpty()) {
            // Empty State Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No Pending",
                            tint = Color(0xFF006B49),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "No Pending Journey Requests",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF121C2A)
                    )
                    Text(
                        text = "All trips are currently clear. When your linked child requests permission for a trip or transit, it will appear here in real time.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        } else {
            incomingJourneys.forEach { journey ->
                val stuName = journey.studentName.ifBlank { "Student" }
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE4DFFF))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = journey.travelMode,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF43359F),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Text(
                                    text = "${journey.origin} → ${journey.destination}",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF121C2A)
                                )
                                Text(
                                    text = "Expected arrival: ${journey.expectedArrival.ifBlank { "${journey.estimatedMinutes} min" }}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF006398)
                                )
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
                                text = "$stuName has requested approval to begin transit (${journey.distanceKm}, ~${journey.estimatedMinutes} min). AI Route Safety Score: ${journey.safetyScore}/100.",
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
                                onClick = { viewModel.approveJourney(journey.journeyId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B49)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("parent_approve_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Approve", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(text = "APPROVE", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }

                            Button(
                                onClick = { viewModel.denyJourney(journey.journeyId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("parent_deny_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Deny", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(text = "DENY", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }
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
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                IconButton(onClick = {
                    if (viewModel.activeDashboardRole.value == com.example.data.model.UserRole.PARENT) {
                        viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                    } else {
                        viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                    }
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF121C2A))
                }
                Text(text = "Safety Timeline", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
            }

            if (timelineEvents.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF6FFBBE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005236), maxLines = 1, softWrap = false)
                }
            }
        }

        // Timeline Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            if (timelineEvents.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
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
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = "No Timeline Events Yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF121C2A)
                    )
                    Text(
                        text = "Real-time safety events, trip approvals, check-ins, and corridor alerts will appear here as they occur.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            } else {
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = event.title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121C2A))
                                Text(text = "${event.timeFormatted} • ${event.locationOrStatus}", fontSize = 12.sp, color = Color(0xFF474552))
                            }
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

    var showShareLocationDialog by remember { mutableStateOf(false) }
    var showNotifyParentsDialog by remember { mutableStateOf(false) }

    val liveLoc by com.example.service.LocationTrackingService.currentLocation.collectAsState()
    val studentLoc by viewModel.selectedStudentLocation.collectAsState()
    val lat = liveLoc?.latitude ?: studentLoc?.latitude ?: 17.3850
    val lng = liveLoc?.longitude ?: studentLoc?.longitude ?: 78.4867
    val mapLink = "https://maps.google.com/?q=$lat,$lng"

    val linkedParents by viewModel.linkedParents.collectAsState()
    val emergencySettings by viewModel.emergencySettings.collectAsState()
    val parentPhone = linkedParents.firstOrNull()?.parentPhone?.ifBlank { null }
        ?: emergencySettings?.trustedContactPhone?.ifBlank { null }
    val parentName = linkedParents.firstOrNull()?.parentName?.ifBlank { null }
        ?: emergencySettings?.trustedContactName?.ifBlank { null } ?: "Mom"

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
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (viewModel.activeDashboardRole.value == com.example.data.model.UserRole.PARENT) {
                    viewModel.navigateTo(ScreenDestination.PARENT_DASHBOARD)
                } else {
                    viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                }
            }) {
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
                    .clickable {
                        viewModel.triggerSosEmergency()
                        showShareLocationDialog = true
                    }
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
                modifier = Modifier.clickable { showShareLocationDialog = true }
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
            val contactPhone = emergencySettings?.trustedContactPhone?.ifBlank { null }
            val contactName = emergencySettings?.trustedContactName?.ifBlank { null }
            val contactLabel = if (contactPhone != null && contactName != null) "Call $contactName" else if (contactPhone != null) "Call Trusted Contact" else "Call Emergency Helpline (112)"

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.clickable {
                    val phoneToDial = contactPhone ?: "112"
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToDial"))
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
                        Text(text = contactLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF121C2A))
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = Color(0xFF787584))
                }
            }

            // Notify Parents
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.clickable {
                    viewModel.triggerImOk()
                    showNotifyParentsDialog = true
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
                        placeholder = { Text("••••", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF1D61F2),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
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
                            },
                            onError = { errorMsg ->
                                pinMessage = "❌ $errorMsg"
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

    if (showShareLocationDialog) {
        val sosMessage = "🚨 EMERGENCY SOS! I need help immediately. My current live location is: $mapLink\n(Sent via SafeSphere Emergency)"
        com.example.ui.components.SafetyShareDialog(
            title = "Share Live Location",
            subtitle = "Send emergency GPS location to your parents & contacts",
            message = sosMessage,
            recipientName = parentName,
            recipientPhone = parentPhone,
            isEmergency = true,
            onDismiss = { showShareLocationDialog = false }
        )
    }

    if (showNotifyParentsDialog) {
        val imOkMessage = "Hi $parentName, I wanted to let you know that I am safe and doing OK! 👍\nMy current location: $mapLink\n(Sent via SafeSphere Check-In)"
        com.example.ui.components.SafetyShareDialog(
            title = "Notify Parents - I'm OK",
            subtitle = "Forward \"I'm OK\" status to parents via WhatsApp or SMS",
            message = imOkMessage,
            recipientName = parentName,
            recipientPhone = parentPhone,
            isImOk = true,
            onDismiss = { showNotifyParentsDialog = false }
        )
    }
}
