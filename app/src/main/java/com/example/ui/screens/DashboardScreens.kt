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
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
    onSwitchAccountRequest: () -> Unit,
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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .clickable { onSwitchAccountRequest() }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isParent) "Switch to Student" else "Switch to Parent",
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
            .background(Color(0xFFF8F9FF))
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

        // Role Isolation Security Banner (strictly segregated)
        RoleSecurityBanner(
            activeRole = UserRole.PARENT,
            onSwitchAccountRequest = { viewModel.switchAuthenticatedAccount(UserRole.STUDENT) }
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

                    Column {
                        Text(
                            text = "SAFESPHERE FAMILY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Good Morning, Sarah",
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

        // Green Safe Status Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6FFBBE)),
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF005236).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified",
                            tint = Color(0xFF002113),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Your family is safe",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002113)
                        )
                        Text(
                            text = "2 active devices • Geofence check intact",
                            fontSize = 11.5.sp,
                            color = Color(0xFF005236)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF006B49))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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

                // Member 1: Alex (Student)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEFF4FF))
                        .clickable { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) }
                        .padding(12.dp)
                        .testTag("member_card_alex")
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
                            Box(modifier = Modifier.size(44.dp)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(ALEX_AVATAR_URL)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Alex",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
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
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = "On trip",
                                        tint = Color(0xFF006398),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "On trip • 2h 15m remaining",
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

                // Member 2: You (Parent)
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(44.dp)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(SARAH_AVATAR_URL)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Sarah",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "You",
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
                                        text = "Active • Guarding SafeZone Home",
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
        }

        // Quick Actions Grid (4 round buttons)
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
                    QuickActionItem(
                        title = "Request\nJourney",
                        icon = Icons.Default.DirectionsCar,
                        bgColor = Color(0xFFCCE5FF),
                        iconTint = Color(0xFF006398),
                        onClick = { viewModel.navigateTo(ScreenDestination.REQUEST_JOURNEY) }
                    )

                    QuickActionItem(
                        title = "Set Safe\nZone",
                        icon = Icons.Default.PinDrop,
                        bgColor = Color(0xFFE4DFFF),
                        iconTint = Color(0xFF42349F),
                        onClick = { viewModel.navigateTo(ScreenDestination.SAFE_ZONES) }
                    )

                    QuickActionItem(
                        title = "Emergency\nSOS",
                        icon = Icons.Default.Warning,
                        bgColor = Color(0xFFFFDAD6),
                        iconTint = Color(0xFFBA1A1A),
                        onClick = { viewModel.navigateTo(ScreenDestination.EMERGENCY) }
                    )

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

                // Map View Box
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
                        contentDescription = "Seattle Telemetry Map",
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
                            .background(Color.White.copy(alpha = 0.92f))
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
                                        text = "Highland Way & 4th Ave",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF121C2A)
                                    )
                                    Text(
                                        text = "En route to North Community...",
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
            .background(Color(0xFFF8F9FF))
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

        // Role Isolation Security Banner (strictly segregated)
        RoleSecurityBanner(
            activeRole = UserRole.STUDENT,
            onSwitchAccountRequest = { viewModel.switchAuthenticatedAccount(UserRole.PARENT) }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ALEX_AVATAR_URL)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Alex",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )

                    Column {
                        Text(
                            text = "Hi Alex! 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121C2A)
                        )
                        Text(
                            text = "Stay safe. You've got this!",
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

        // Journey Active Green Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF006B49),
                            Color(0xFF005036)
                        )
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
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Active",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "JOURNEY ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF62EFB3),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "School → Home",
                                fontSize = 17.sp,
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
                            text = "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Progress Bar and Stats
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ends in 1h 20m",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "64% Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    LinearProgressIndicator(
                        progress = { 0.64f },
                        color = Color(0xFF4EDEA3),
                        trackColor = Color.Black.copy(alpha = 0.25f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                // ETA & Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ETA: 4:15 PM",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.triggerImOk() }
                    ) {
                        Text(
                            text = "Notify Parents of Stop",
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

        // Student Quick Controls (4 round buttons)
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
                    QuickActionItem(
                        title = "My\nJourney",
                        icon = Icons.Default.Explore,
                        bgColor = Color(0xFF006398),
                        iconTint = Color.White,
                        onClick = { viewModel.navigateTo(ScreenDestination.FAMILY_MAP) }
                    )

                    QuickActionItem(
                        title = "Safe\nZones",
                        icon = Icons.Default.LocationOn,
                        bgColor = Color(0xFF5A4EB8),
                        iconTint = Color.White,
                        onClick = { viewModel.navigateTo(ScreenDestination.SAFE_ZONES) }
                    )

                    QuickActionItem(
                        title = "Emergency\nSOS",
                        icon = Icons.Default.Warning,
                        bgColor = Color(0xFFBA1A1A),
                        iconTint = Color.White,
                        onClick = { viewModel.navigateTo(ScreenDestination.EMERGENCY) }
                    )

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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    text = "Location shared",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                                Text(
                                    text = "Shared with Mom & Dad via GPS",
                                    fontSize = 11.sp,
                                    color = Color(0xFF474552)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6FFBBE))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF005236)
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    text = "Camera verification",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121C2A)
                                )
                                Text(
                                    text = "Check-in confirmed at 3:10 PM",
                                    fontSize = 11.sp,
                                    color = Color(0xFF474552)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6FFBBE))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF005236)
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
