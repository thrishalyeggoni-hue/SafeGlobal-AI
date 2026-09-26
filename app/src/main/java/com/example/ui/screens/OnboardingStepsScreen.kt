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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.WaterWaveLoadingIndicator
import com.example.ui.viewmodel.SafeSphereViewModel

@Composable
fun PhoneVerificationScreen(
    viewModel: SafeSphereViewModel,
    onSendOtp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phone by viewModel.phoneInput.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .testTag("phone_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Header with Back button and Water wave loading indicator near top center
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0E1726)
                    )
                }

                WaterWaveLoadingIndicator(
                    label = "Phone Verification (Step 1)"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Phone Verification",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0E1726)
            )

            Text(
                text = "Enter your mobile number\nto get started",
                fontSize = 13.sp,
                color = Color(0xFF4B5565),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Phone Input with Country Code and Flag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE3E8EF), RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🇮🇳",
                    fontSize = 18.sp
                )
                Text(
                    text = "+91",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0E1726),
                    modifier = Modifier.padding(start = 6.dp, end = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color(0xFFE3E8EF))
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.phoneInput.value = it },
                    placeholder = { Text("Enter phone number", fontSize = 13.5.sp, color = Color(0xFF8896AB)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input_field")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSendOtp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A65FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_otp_btn")
            ) {
                Text("SEND OTP", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By continuing, you agree to our Privacy Policy and Terms of Service.",
                fontSize = 11.sp,
                color = Color(0xFF8896AB),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun OtpVerificationScreen(
    viewModel: SafeSphereViewModel,
    onVerifyOtp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val otpList by viewModel.otpInputs.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .testTag("otp_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0E1726)
                    )
                }

                WaterWaveLoadingIndicator(
                    label = "OTP Verification (Step 2)"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verify OTP",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0E1726)
            )

            Text(
                text = "Enter the 6-digit code sent to\n+91 98765 43210",
                fontSize = 13.sp,
                color = Color(0xFF4B5565),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // 6-digit OTP boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                otpList.forEachIndexed { index, digit ->
                    Box(
                        modifier = Modifier
                            .size(46.dp, 52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF0A65FF), RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F9FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0E1726)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Resend OTP (00:48)",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0A65FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVerifyOtp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A65FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("verify_otp_btn")
            ) {
                Text("VERIFY", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun RoleSelectionScreen(
    viewModel: SafeSphereViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedRole by viewModel.selectedRole.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .testTag("role_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0E1726)
                    )
                }

                WaterWaveLoadingIndicator(
                    label = "Choose Your Role (Step 3)"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose Your Role",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0E1726)
            )

            Text(
                text = "Are you a parent/guardian\nor a student?",
                fontSize = 13.sp,
                color = Color(0xFF4B5565),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Option 1: Parent / Guardian Card
            val isParentSelected = selectedRole == UserRole.PARENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isParentSelected) 2.dp else 1.dp,
                        color = if (isParentSelected) Color(0xFF0A65FF) else Color(0xFFE3E8EF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(if (isParentSelected) Color(0xFFF4F8FF) else Color.White)
                    .clickable {
                        viewModel.selectedRole.value = UserRole.PARENT
                    }
                    .padding(16.dp)
                    .testTag("role_parent_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEBF3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Parent",
                            tint = Color(0xFF0A65FF),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Parent / Guardian",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0E1726)
                        )
                        Text(
                            text = "Monitor, manage and keep your family safe",
                            fontSize = 11.5.sp,
                            color = Color(0xFF4B5565),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Student Card
            val isStudentSelected = selectedRole == UserRole.STUDENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isStudentSelected) 2.dp else 1.dp,
                        color = if (isStudentSelected) Color(0xFF0A65FF) else Color(0xFFE3E8EF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(if (isStudentSelected) Color(0xFFF4F8FF) else Color.White)
                    .clickable {
                        viewModel.selectedRole.value = UserRole.STUDENT
                    }
                    .padding(16.dp)
                    .testTag("role_student_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Student",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Student",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0E1726)
                        )
                        Text(
                            text = "Get safety support and share your journey",
                            fontSize = 11.5.sp,
                            color = Color(0xFF4B5565),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A65FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("role_continue_btn")
            ) {
                Text("CONTINUE", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
