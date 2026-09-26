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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConsentScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isUnderstood by remember { mutableStateOf(true) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val features = listOf(
        Pair("Location sharing", Icons.Default.LocationOn),
        Pair("Journey monitoring", Icons.Default.DirectionsCar),
        Pair("Geofence alerts", Icons.Default.Security),
        Pair("Camera-based verification", Icons.Default.CameraAlt),
        Pair("Emergency alerts", Icons.Default.Notifications),
        Pair("Trusted contact communication", Icons.Default.People),
        Pair("Driver identity verification", Icons.Default.Badge)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .testTag("consent_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Back button and Water wave loading indicator near top center
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("consent_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0E1726)
                    )
                }

                Text(
                    text = "Safety Consent",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Title & Subtitle
            Text(
                text = "Family Safety &\nConsent Notice",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0E1726),
                lineHeight = 28.sp,
                letterSpacing = (-0.3).sp
            )

            Text(
                text = "SafeSphere contains sensitive features including:",
                fontSize = 12.5.sp,
                color = Color(0xFF4B5565),
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            // Features List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                features.forEach { (title, icon) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEBF3FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = Color(0xFF0A65FF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = title,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0E1726)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Red Warning Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF2F2))
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Parents/guardians must obtain the student's knowledge and consent before using monitoring, location, camera or verification features.",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626),
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Permissions Disclaimer Note
            Text(
                text = "Camera and location permissions must be granted by the device user. SafeSphere does not provide hidden camera monitoring.",
                fontSize = 10.5.sp,
                color = Color(0xFF8896AB),
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Privacy Link
            Text(
                text = "Read Privacy Information",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0A65FF),
                modifier = Modifier
                    .clickable { showPrivacyDialog = true }
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Consent Agreement Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { isUnderstood = !isUnderstood }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = isUnderstood,
                    onCheckedChange = { isUnderstood = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0A65FF)),
                    modifier = Modifier.testTag("consent_checkbox")
                )
                Text(
                    text = "I understand",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0E1726)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Button
            Button(
                onClick = onContinue,
                enabled = isUnderstood,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A65FF),
                    disabledContainerColor = Color(0xFFB0CDFF)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("consent_continue_btn")
            ) {
                Text(
                    text = "CONTINUE",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("SafeSphere Privacy & Mutual Consent", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "SafeSphere operates on mutual respect and shared autonomy. No feature runs stealthily or without device authorization. All location sharing and camera verifications require explicit affirmative consent.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Understood", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
