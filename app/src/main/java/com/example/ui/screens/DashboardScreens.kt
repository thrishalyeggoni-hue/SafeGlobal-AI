package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShareLocation
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Videocam
import com.example.data.repository.FirestoreSafetyManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserRole
import com.example.ui.components.TopCenterBrandedLoadingIndicator
import com.example.ui.components.WaterWaveLoadingIndicator
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination

const val SARAH_AVATAR_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuBepeTT9JgUhM_4UA9RfwDJaKbp46wuanseosEJlXGHRbdLfCCv2e78KL3HuJSUD_sdZRH05LwXxuonC5NT8zeqQ5GJAXDZn_cpyT1Tku0ypDraOJYN1j-mTnQwAfolLIle1y2FCvEjPO9PCXvz-Ld7j-QeZwHulp_oobzvSPZWJiBL45iDDZIsWHhBOUTCuJv6N7XmICWF10-5emaEw4zGY7AlSHeQw0Zhi8ov8fZ0sid-Pr0OLKHkQQ"
const val ALEX_AVATAR_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuDuUDbOskAQGe9wBBbJV0PWsQl8o--8l8tJM8C67YeBzCgMGBohBvwFYRc1o-HedyG-xkYnytddSTHGZBbV3YnW8sz2MERzceDeXDpGMKey75OjU5fbAodHu34D5OrABpQ13HauPt_Gom9g50jNUU24fj_hZP8xcmfqR-GJmOYURMKj3DhiAmv-lpY8Q7t2Too5QNxL75_MW8K3E0DaJMK-i4lwCOlz25xG6CIdNRoH6qI0xA2e2LT5kg"
const val MAP_PREVIEW_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuA5M71byQCisxmdNnL9pdkBu4GtwGLYvoWFy4qUF07avfES8Y39y2XZQk5XaUAeyq4x0AJ5zzr0wSyFO3OcBzWRXesEvKzdpJ6mDDZAbevhI2PEPV6FEMDmqktBJpRx0CrYvu6h5Flfbdmxow2VQyevv52m01abUrFtnIhBvieqAwh86BLIUhk4uQSgwLPa9ppXsL_dcevAIFqfPAjJQH_Kw0yXWsZyO1IR5lCw67V0hvGnT5r47Zxmig"

@Composable
fun RoleSecurityBanner(
    activeRole: UserRole,
    onSwitchAccountRequest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isParent = activeRole == UserRole.PARENT
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isParent) Color(0xFFEFF6FF) else Color(0xFFF0FDF4))
            .border(
                width = 1.dp,
                color = if (isParent) Color(0xFFBFDBFE) else Color(0xFFBBF7D0),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("role_security_banner")
    ) {
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
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isParent) Color(0xFF1652F0).copy(alpha = 0.12f) else Color(0xFF006B49).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isParent) Icons.Default.VerifiedUser else Icons.Default.School,
                        contentDescription = "Role Icon",
                        tint = if (isParent) Color(0xFF1652F0) else Color(0xFF006B49),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isParent) "Parent Account" else "Student Account",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isParent) Color(0xFF1E3A8A) else Color(0xFF065F46)
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Role Locked",
                            tint = if (isParent) Color(0xFF3B82F6) else Color(0xFF10B981),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = if (isParent) {
                            "Student view restricted • Full guardian telemetry"
                        } else {
                            "Parent dashboard strictly locked • Student safe corridor"
                        },
                        fontSize = 10.5.sp,
                        color = Color(0xFF474552)
                    )
                }
            }

            // Role badge only — NO switch button (strict role isolation)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isParent) Color(0xFFEFF6FF) else Color(0xFFF0FDF4))
                    .border(1.dp,
                        if (isParent) Color(0xFF3B82F6) else Color(0xFF10B981),
                        RoundedCornerShape(12.dp))
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isParent) "🛡 Parent" else "🎒 Student",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isParent) Color(0xFF1652F0) else Color(0xFF006B49)
                )
            }
        }
    }
}

@Composable
fun ParentDashboardScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val latestJourney by viewModel.latestJourney.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Branded top loading animation near top center
        TopCenterBrandedLoadingIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = "All Protections Armed",
            onSyncClick = { viewModel.triggerDataSync() }
        )

        // Role Isolation Security Banner (strictly segregated — no switch)
        RoleSecurityBanner(
            activeRole = UserRole.PARENT
        )

        // Header Greeting with Avatar & Notif
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(SARAH_AVATAR_URL)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Sarah Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )

                    val firestoreProfile by viewModel.firestoreProfile.collectAsState()
                    val parentDisplayName = firestoreProfile?.displayName.orEmpty().ifBlank { "Parent" }

                    Column {
                        Text(
                            text = "SAFESPHERE FAMILY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Good Morning, $parentDisplayName",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                        Text(
                            text = "Role: Parent • All protections armed",
                            fontSize = 11.5.sp,
                            color = Color(0xFF474552)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF4FF))
                        .clickable { viewModel.navigateTo(ScreenDestination.EMERGENCY) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF121C2A),
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBA1A1A))
                    )
                }
            }
        }

        val linkedStudents by viewModel.linkedStudents.collectAsState()
        val incomingJourneys by viewModel.incomingJourneysForParent.collectAsState()
        val incomingSharedRides by viewModel.incomingSharedRidesForParent.collectAsState()

        // Real Connected Students Section (Two-Phone Family Safety)
        if (linkedStudents.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(ScreenDestination.ENTER_LINK_CODE) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color(0xFF2563EB))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Connect Student Device", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text(text = "Enter student's 6-digit code to enable real-time Leaflet tracking, safe zones & camera check.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2563EB))
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LINKED STUDENTS (${linkedStudents.size})",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "+ Link Another",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.ENTER_LINK_CODE) }
                    )
                }

                linkedStudents.forEach { student ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectStudent(student)
                                viewModel.navigateTo(ScreenDestination.STUDENT_CONTROL_CENTER)
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = student.studentName.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1D4ED8))
                                    }
                                    Column {
                                        Text(text = student.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                        Text(text = "ID: ${student.studentSafeSphereId.ifBlank { "Verified" }}", fontSize = 11.5.sp, color = Color(0xFF64748B))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "🟢 Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.selectStudent(student)
                                        viewModel.navigateTo(ScreenDestination.FAMILY_MAP)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Map", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.selectStudent(student)
                                        viewModel.navigateTo(ScreenDestination.STUDENT_CONTROL_CENTER)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Controls", color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // INCOMING JOURNEYS FOR PARENT (Real-time Permission & Route Display)
        // =====================================================================
        if (incomingJourneys.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JOURNEY PERMISSION REQUESTS (${incomingJourneys.size})",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                }

                incomingJourneys.forEach { journey ->
                    val isPending = journey.status == "PENDING"
                    val isApproved = journey.status == "APPROVED"

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPending) Color(0xFFFFFBEB) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isPending) Color(0xFFF59E0B) else Color(0xFF10B981)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPending) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = if (isPending) Color(0xFFD97706) else Color(0xFF16A34A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "${journey.studentName.ifBlank { "Student" }}'s Journey Request",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Mode: ${journey.travelMode} • ETA: ${journey.expectedArrival.ifBlank { "20 min" }}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isPending) Color(0xFFF59E0B) else Color(0xFF16A34A)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = journey.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Origin to Destination Route Preview
                            PredictedRouteCard(
                                origin = journey.origin.ifBlank { "University Campus" },
                                destination = journey.destination.ifBlank { "Home" },
                                distanceKm = "4.2 km",
                                duration = journey.expectedArrival.ifBlank { "20 min" },
                                safetyScore = 98,
                                travelMode = journey.travelMode
                            )

                            // Actions if Pending
                            if (isPending) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.denyJourney(journey.journeyId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF1F5F9)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Deny", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.approveJourney(journey.journeyId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF16A34A)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(2f)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Approve Journey", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (isApproved) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Approved by you • Real-time AI Corridor Monitored",
                                        fontSize = 12.sp,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // LIVE SHARED SOLO TRANSPORT RIDES (With Auto QR / Face Verification)
        // =====================================================================
        if (incomingSharedRides.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "LIVE SOLO TRANSPORT RIDES (${incomingSharedRides.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                incomingSharedRides.forEach { ride ->
                    val context = LocalContext.current
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2563EB)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalTaxi,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "${ride.studentName.ifBlank { "Student" }}'s Solo Transit",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${ride.appUsed} • ${ride.vehicleNumber}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF2563EB)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (ride.scanVerificationType == "AUTO_QR") "✓ Auto QR"
                                        else if (ride.scanVerificationType == "DRIVER_FACE") "✓ Face Scan"
                                        else "✓ Verified",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }

                            // Driver info details pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Driver: ${ride.driverName.ifBlank { "Verified Driver" }}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        if (ride.driverPhone.isNotBlank()) {
                                            Text(
                                                text = "Mobile: ${ride.driverPhone}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    if (ride.driverPhone.isNotBlank()) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ride.driverPhone}"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                horizontal = 10.dp,
                                                vertical = 6.dp
                                            )
                                        ) {
                                            Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call Driver", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Predicted Route Card
                            PredictedRouteCard(
                                origin = ride.pickupLocation.ifBlank { "Pickup Point" },
                                destination = ride.dropLocation.ifBlank { "Destination" },
                                distanceKm = "5.2 km",
                                duration = ride.estimatedTime.ifBlank { "22 min" },
                                safetyScore = 99,
                                travelMode = ride.appUsed
                            )

                            // Track On Map Button
                            Button(
                                onClick = { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Track Live on Family Map", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Green Safe Status Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (linkedStudents.isNotEmpty()) Color(0xFF6FFBBE) else Color(0xFFEFF4FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (linkedStudents.isNotEmpty()) Color(0xFF005236).copy(alpha = 0.15f) else Color(0xFFCCE5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (linkedStudents.isNotEmpty()) Icons.Default.VerifiedUser else Icons.Default.AddLink,
                            contentDescription = "Status",
                            tint = if (linkedStudents.isNotEmpty()) Color(0xFF002113) else Color(0xFF006398),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (linkedStudents.isNotEmpty()) "Your family is protected" else "Setup Family Linkage",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (linkedStudents.isNotEmpty()) Color(0xFF002113) else Color(0xFF1E293B)
                        )
                        Text(
                            text = if (linkedStudents.isNotEmpty()) "${linkedStudents.size} active device${if (linkedStudents.size > 1) "s" else ""} • Geofence check intact"
                                   else "No child linked • Link student device to monitor",
                            fontSize = 11.5.sp,
                            color = if (linkedStudents.isNotEmpty()) Color(0xFF005236) else Color(0xFF64748B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (linkedStudents.isNotEmpty()) Color(0xFF006B49) else Color(0xFFCBD5E1))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (linkedStudents.isNotEmpty()) "Active" else "Standby",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (linkedStudents.isNotEmpty()) Color.White else Color(0xFF334155)
                    )
                }
            }
        }

        // Family Members Section
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Family Members",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF121C2A)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.FAMILY_MEMBERS) }
                    ) {
                        Text(
                            text = "View All",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF006398)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View all",
                            tint = Color(0xFF006398),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Member 1: You (Parent)
                val currentParentProfile by viewModel.firestoreProfile.collectAsState()
                val parentDisplayName = currentParentProfile?.displayName?.ifBlank { null } ?: "You"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEFF4FF))
                        .clickable { viewModel.navigateTo(ScreenDestination.PROFILE) }
                        .padding(12.dp)
                ) {
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF43359F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "$parentDisplayName (You)",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF121C2A)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE4DFFF))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Parent",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF43359F)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = Color(0xFF006B49),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Primary Guardian • Monitoring Active",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF474552)
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = Color(0xFF474552),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // If linked students exist, render them!
                if (linkedStudents.isNotEmpty()) {
                    linkedStudents.forEach { student ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEFF4FF))
                                .clickable { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) }
                                .padding(12.dp)
                        ) {
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
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF006398)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = student.studentName.ifBlank { "Student" },
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF121C2A)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFCCE5FF))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Student",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF004B73)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BatteryChargingFull,
                                                contentDescription = "Battery",
                                                tint = Color(0xFF006B49),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Protected • GPS Active",
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF474552)
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = Color(0xFF474552),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Empty state: unlinked child prompt
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .clickable { viewModel.navigateTo(ScreenDestination.ENTER_LINK_CODE) }
                            .padding(14.dp)
                    ) {
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E7FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AddLink, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(
                                        text = "No Child Linked Yet",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Tap to enter 6-digit code or scan QR from student app",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Quick Actions Grid — Parent-only features
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121C2A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Parent-only: Family Map (track members)
                    QuickActionItem(
                        title = "Track\nFamily",
                        icon = Icons.Default.ShareLocation,
                        bgColor = Color(0xFFCCE5FF),
                        iconTint = Color(0xFF006398),
                        onClick = { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) }
                    )

                    // Parent-only: Safe Zones management
                    QuickActionItem(
                        title = "Safe\nZones",
                        icon = Icons.Default.PinDrop,
                        bgColor = Color(0xFFE4DFFF),
                        iconTint = Color(0xFF42349F),
                        onClick = { viewModel.navigateTo(ScreenDestination.SAFE_ZONES) }
                    )

                    // Parent-only: Emergency SOS
                    QuickActionItem(
                        title = "Emergency\nSOS",
                        icon = Icons.Default.Warning,
                        bgColor = Color(0xFFFFDAD6),
                        iconTint = Color(0xFFBA1A1A),
                        onClick = { viewModel.navigateTo(ScreenDestination.EMERGENCY) }
                    )

                    // Parent-only: Settings
                    QuickActionItem(
                        title = "Settings\n ",
                        icon = Icons.Default.Settings,
                        bgColor = Color(0xFFDEE9FC),
                        iconTint = Color(0xFF121C2A),
                        onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                    )
                }
            }
        }

        // Live Telemetry Snippet Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.testTag("telemetry_map_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Radar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Live Telemetry",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                    }
                    Text(
                        text = "Updated 30s ago",
                        fontSize = 11.5.sp,
                        color = Color(0xFF474552)
                    )
                }

                // Telemetry View Box
                if (linkedStudents.isNotEmpty()) {
                    val firstStudent = linkedStudents.first()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(MAP_PREVIEW_URL)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Live Telemetry Map",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Overlay pill at bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.94f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = "Navigation",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "${firstStudent.studentName} • Live Location",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF121C2A)
                                        )
                                        Text(
                                            text = "Active Sync • GPS Transmitting",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF474552)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF006398))
                                        .clickable { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Track",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ShareLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                            }
                            Text("No Telemetry Stream Active", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            Text(
                                text = "Link a student device to monitor real-time GPS telemetry, geofence status, and travel routes.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.navigateTo(ScreenDestination.ENTER_LINK_CODE) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddLink, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Link Child Phone", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF121C2A),
            lineHeight = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun StudentDashboardScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Branded top loading animation near top center
        TopCenterBrandedLoadingIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = "Journey Sharing Active",
            onSyncClick = { viewModel.triggerDataSync() }
        )

        // Role Isolation Security Banner (strictly segregated — no switch)
        RoleSecurityBanner(
            activeRole = UserRole.STUDENT
        )

        // Header Greeting with Avatar
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val studentProfile by viewModel.firestoreProfile.collectAsState()
                val studentDisplayName = studentProfile?.displayName?.ifBlank { null } ?: "Student"
                val linkedParents by viewModel.linkedParents.collectAsState()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF006398)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Student",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Hi $studentDisplayName! 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                        Text(
                            text = if (linkedParents.isNotEmpty()) "Connected to ${linkedParents.first().parentName}" else "Stay safe. You've got this!",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF6FFBBE))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Protected",
                            tint = Color(0xFF005236),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Protected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF005236)
                        )
                    }
                }
            }
        }

        val context = LocalContext.current
        val isTrackingActive by viewModel.isLocationTrackingActive.collectAsState()
        val incomingCameraReq by viewModel.incomingCameraRequestForStudent.collectAsState()
        val linkedParents by viewModel.linkedParents.collectAsState()
        val studentActiveJourneys by viewModel.studentActiveJourneys.collectAsState()
        val studentActiveRides by viewModel.studentActiveRides.collectAsState()

        // 1. URGENT: Incoming Camera Check Request Dialog/Card
        if (incomingCameraReq != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(text = "Camera Check Request", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF991B1B))
                            Text(text = "${incomingCameraReq!!.parentName} is requesting a surroundings check", fontSize = 12.sp, color = Color(0xFF7F1D1D))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.respondToCameraRequest(incomingCameraReq!!.requestId, false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Decline", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.respondToCameraRequest(incomingCameraReq!!.requestId, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept & Stream", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. REAL STUDENT LOCATION TRACKING TOGGLE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isTrackingActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isTrackingActive) Color(0xFF16A34A) else Color(0xFF64748B)
                        )
                    }
                    Column {
                        Text(
                            text = if (isTrackingActive) "Live GPS Transmitting" else "Live Location Standby",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isTrackingActive) "Real-time updates sent to parents" else "Tap toggle to broadcast GPS to parents",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Switch(
                    checked = isTrackingActive,
                    onCheckedChange = { viewModel.toggleStudentLocationTracking(context) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF16A34A)
                    )
                )
            }
        }

        // 3. LINK WITH PARENT BUTTON
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(ScreenDestination.LINK_CODE_GENERATOR) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6366F1))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text(
                            text = if (linkedParents.isNotEmpty()) "Linked to Guardian ✓" else "Link Parent Phone",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = if (linkedParents.isNotEmpty()) "Connected to ${linkedParents.first().parentName}" else "Generate 6-digit code for your parent",
                            fontSize = 11.5.sp,
                            color = Color(0xFF4338CA)
                        )
                    }
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF6366F1))
            }
        }

        // =====================================================================
        // REAL-TIME STUDENT ACTIVE JOURNEYS & SHARED RIDES DISPLAY
        // =====================================================================
        if (studentActiveJourneys.isNotEmpty()) {
            studentActiveJourneys.forEach { journey ->
                val isApproved = journey.status == "APPROVED"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = if (isApproved) listOf(Color(0xFF006B49), Color(0xFF005036))
                                else listOf(Color(0xFF1E3A8A), Color(0xFF172554))
                            )
                        )
                        .padding(18.dp)
                        .testTag("student_journey_active_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isApproved) Icons.Default.Verified else Icons.Default.DirectionsCar,
                                        contentDescription = "Active",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (isApproved) "JOURNEY ACTIVE" else "JOURNEY REQUEST SENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isApproved) Color(0xFF62EFB3) else Color(0xFF93C5FD),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "${journey.origin.ifBlank { "Origin" }} → ${journey.destination.ifBlank { "Destination" }}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isApproved) "Approved" else "Waiting Approval",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Embedded Predicted Route Display from Source to Destination
                        PredictedRouteCard(
                            origin = journey.origin.ifBlank { "Origin" },
                            destination = journey.destination.ifBlank { "Destination" },
                            distanceKm = "4.2 km",
                            duration = journey.expectedArrival.ifBlank { "20 min" },
                            safetyScore = 98,
                            travelMode = journey.travelMode
                        )

                        // ETA & Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ETA: ${journey.expectedArrival.ifBlank { "20 min" }}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.triggerImOk() }
                            ) {
                                Text(
                                    text = "Send 'I'm OK' Ping",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (studentActiveRides.isNotEmpty()) {
            studentActiveRides.forEach { ride ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocalTaxi, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                Column {
                                    Text("SOLO TRANSPORT ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    Text("${ride.appUsed} • ${ride.vehicleNumber}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF16A34A))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Shared with Parent ✓", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        PredictedRouteCard(
                            origin = ride.pickupLocation.ifBlank { "Pickup" },
                            destination = ride.dropLocation.ifBlank { "Drop" },
                            distanceKm = "5.2 km",
                            duration = ride.estimatedTime.ifBlank { "22 min" },
                            safetyScore = 99,
                            travelMode = ride.appUsed
                        )
                    }
                }
            }
        }

        // Student Quick Controls — student-only features
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Controls",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121C2A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Student-only: My Journey (share/track own journey)
                    QuickActionItem(
                        title = "My\nJourney",
                        icon = Icons.Default.Explore,
                        bgColor = Color(0xFF006398),
                        iconTint = Color.White,
                        onClick = { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) }
                    )

                    // Student-only: Request Journey approval from parent
                    QuickActionItem(
                        title = "Request\nJourney",
                        icon = Icons.Default.DirectionsCar,
                        bgColor = Color(0xFF5A4EB8),
                        iconTint = Color.White,
                        onClick = { viewModel.navigateTo(ScreenDestination.REQUEST_JOURNEY) }
                    )

                    // Student-only: Solo Transport Verification (Uber/Ola/Rapido)
                    QuickActionItem(
                        title = "Solo\nTransport",
                        icon = Icons.Default.LocalTaxi,
                        bgColor = Color(0xFFD97706),
                        iconTint = Color.White,
                        onClick = { viewModel.navigateTo(ScreenDestination.SOLO_TRANSPORT) }
                    )

                    // Both: Emergency SOS
                    QuickActionItem(
                        title = "Emergency\nSOS",
                        icon = Icons.Default.Warning,
                        bgColor = Color(0xFFBA1A1A),
                        iconTint = Color.White,
                        onClick = { viewModel.navigateTo(ScreenDestination.EMERGENCY) }
                    )
                }
            }
        }

        // Safety Status Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Safety Status",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF121C2A)
                    )
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Status",
                        tint = Color(0xFF006398),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Item 1: Location shared
                val isParentLinked = linkedParents.isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(12.dp)
                ) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCCE5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShareLocation,
                                    contentDescription = "Location",
                                    tint = Color(0xFF006398),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Location Sharing",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                                Text(
                                    text = if (isParentLinked) "Shared with ${linkedParents.first().parentName} via GPS"
                                           else "Standby • Link a parent device",
                                    fontSize = 11.sp,
                                    color = Color(0xFF474552)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isParentLinked) Color(0xFF6FFBBE) else Color(0xFFE2E8F0))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isParentLinked) "Active" else "Standby",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isParentLinked) Color(0xFF005236) else Color(0xFF475569),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Item 2: Camera verification
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF4FF))
                        .padding(12.dp)
                ) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE4DFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Camera",
                                    tint = Color(0xFF42349F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Camera Verification",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                                Text(
                                    text = if (isParentLinked) "Guardian remote safety link ready"
                                           else "Standby • Awaiting guardian link",
                                    fontSize = 11.sp,
                                    color = Color(0xFF474552)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isParentLinked) Color(0xFF6FFBBE) else Color(0xFFE2E8F0))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isParentLinked) "Active" else "Standby",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isParentLinked) Color(0xFF005236) else Color(0xFF475569),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Mutual Privacy Control
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEFF4FF))
                .padding(12.dp)
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
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Privacy",
                        tint = Color(0xFF005036),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "You have full mutual privacy control",
                        fontSize = 11.5.sp,
                        color = Color(0xFF474552)
                    )
                }
                Text(
                    text = "Manage",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
