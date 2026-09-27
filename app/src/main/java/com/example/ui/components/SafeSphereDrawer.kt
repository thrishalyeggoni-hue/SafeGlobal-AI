package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmergencyShare
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val destination: ScreenDestination?,   // null = logout
    val badge: String? = null,
    val section: String? = null            // section header
)

@Composable
fun SafeSphereDrawer(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val isOpen by viewModel.isDrawerOpen.collectAsState()
    val activeRole by viewModel.activeDashboardRole.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val firestoreProfile by viewModel.firestoreProfile.collectAsState()

    val isParent = activeRole == UserRole.PARENT
    val displayName = firestoreProfile?.displayName ?: if (isParent) "Parent" else "Student"
    val roleLabel = if (isParent) "Parent Account" else "Student Account"
    val primaryColor = MaterialTheme.colorScheme.primary

    // Animated offset: slides from -300dp (off-screen) to 0dp (visible)
    val offsetX by animateDpAsState(
        targetValue = if (isOpen) 0.dp else (-300.dp),
        animationSpec = tween(durationMillis = 280),
        label = "drawer_offset"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.45f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "scrim_alpha"
    )

    // Define drawer items based on role
    val drawerItems: List<DrawerItem> = if (isParent) {
        listOf(
            DrawerItem("Dashboard", Icons.Default.Home, ScreenDestination.PARENT_DASHBOARD),
            DrawerItem("Family Map", Icons.Default.Map, ScreenDestination.FAMILY_MAP),
            DrawerItem("Family Members", Icons.Default.FamilyRestroom, ScreenDestination.FAMILY_MEMBERS),
            DrawerItem("Safe Zones", Icons.Default.Fence, ScreenDestination.SAFE_ZONES),
            DrawerItem("Safety Timeline", Icons.Default.Timeline, ScreenDestination.SAFETY_TIMELINE),
            DrawerItem("Emergency", Icons.Default.EmergencyShare, ScreenDestination.EMERGENCY),
            DrawerItem("Profile", Icons.Default.Person, ScreenDestination.PROFILE),
            DrawerItem("Settings", Icons.Default.Settings, ScreenDestination.SETTINGS),
        )
    } else {
        listOf(
            DrawerItem("Dashboard", Icons.Default.Home, ScreenDestination.STUDENT_DASHBOARD),
            DrawerItem("Family Map", Icons.Default.Map, ScreenDestination.FAMILY_MAP),
            DrawerItem("Request Journey", Icons.Default.ExploreOff, ScreenDestination.REQUEST_JOURNEY),
            DrawerItem("Safety Timeline", Icons.Default.Timeline, ScreenDestination.SAFETY_TIMELINE),
            DrawerItem("Emergency", Icons.Default.EmergencyShare, ScreenDestination.EMERGENCY),
            DrawerItem("Profile", Icons.Default.Person, ScreenDestination.PROFILE),
            DrawerItem("Settings", Icons.Default.Settings, ScreenDestination.SETTINGS),
        )
    }

    if (isOpen || scrimAlpha > 0f) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Scrim (dark overlay) — tapping it closes the drawer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(scrimAlpha)
                    .background(Color.Black)
                    .clickable(enabled = isOpen) { viewModel.closeDrawer() }
            )

            // Drawer panel
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .offset { IntOffset(x = offsetX.roundToPx(), y = 0) }
                    .shadow(elevation = 16.dp)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    // Header with gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isParent) {
                                        listOf(Color(0xFF1652F0), Color(0xFF3B82F6))
                                    } else {
                                        listOf(Color(0xFF065F46), Color(0xFF10B981))
                                    }
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Column {
                            // Avatar circle
                            val userAvatarIndex = firestoreProfile?.avatarIndex ?: if (isParent) 1 else 7
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = SafeSphereAvatarHelper.getAvatarDrawable(userAvatarIndex)),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = roleLabel,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Navigation items
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        drawerItems.forEach { item ->
                            val isSelected = currentScreen == item.destination
                            DrawerNavItem(
                                item = item,
                                isSelected = isSelected,
                                primaryColor = primaryColor,
                                onClick = {
                                    viewModel.closeDrawer()
                                    item.destination?.let { viewModel.navigateTo(it) }
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE2E8F0)
                    )

                    // Logout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.closeDrawer()
                                viewModel.logout()
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Logout",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE11D48)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    item: DrawerItem,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) primaryColor else Color(0xFF374151)
    val iconColor = if (isSelected) primaryColor else Color(0xFF6B7280)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(primaryColor)
            )
        } else {
            Spacer(modifier = Modifier.width(3.dp))
        }

        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = item.label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        item.badge?.let { badge ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(primaryColor)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
