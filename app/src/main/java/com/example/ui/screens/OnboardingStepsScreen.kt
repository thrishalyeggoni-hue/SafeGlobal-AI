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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.SafeSphereEmblem
import com.example.ui.theme.darkTextFieldColors
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
            // Header with Back button
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

                Text(
                    text = "Verification",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
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
                text = "Enter your mobile number to get started",
                fontSize = 13.sp,
                color = Color(0xFF4B5565),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Phone Input with Country Code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+91",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(start = 4.dp, end = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color(0xFFE2E8F0))
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.phoneInput.value = it },
                    placeholder = { Text("Enter phone number", fontSize = 13.5.sp, color = Color(0xFF94A3B8)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        cursorColor = Color(0xFF1D61F2),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
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

                Text(
                    text = "Code Verification",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
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
                text = "Enter the 6-digit code sent to your phone",
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
                otpList.forEachIndexed { _, digit ->
                    Box(
                        modifier = Modifier
                            .size(46.dp, 52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1D61F2), RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Resend OTP in 00:48",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D61F2),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVerifyOtp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("verify_otp_btn")
            ) {
                Text("VERIFY CODE", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * Dedicated 2-Login Interface: One for Students and one for Parents.
 * Features crisp dark text input, strict role separation, and instant credential access.
 */
@Composable
fun RoleSelectionScreen(
    viewModel: SafeSphereViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLoginTab by remember { mutableStateOf(UserRole.STUDENT) }
    var passwordVisible by remember { mutableStateOf(false) }

    val studentId by viewModel.studentIdInput.collectAsState()
    val studentPassword by viewModel.studentPasswordInput.collectAsState()
    val parentId by viewModel.parentIdInput.collectAsState()
    val parentPassword by viewModel.parentPasswordInput.collectAsState()

    val isStudentTab = selectedLoginTab == UserRole.STUDENT

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .testTag("role_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Logo and Title
            SafeSphereEmblem(size = 64.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SafeSphere Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "Select your account type to sign in",
                fontSize = 13.5.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // TWO LOGINS TOGGLE TAB: Student Login | Parent Login
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Student Login Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isStudentTab) Color(0xFF1D61F2) else Color.Transparent)
                        .clickable { selectedLoginTab = UserRole.STUDENT }
                        .padding(vertical = 12.dp)
                        .testTag("tab_student_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Student",
                            tint = if (isStudentTab) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Student Login",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStudentTab) Color.White else Color(0xFF475569)
                        )
                    }
                }

                // Parent Login Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isStudentTab) Color(0xFF0F172A) else Color.Transparent)
                        .clickable { selectedLoginTab = UserRole.PARENT }
                        .padding(vertical = 12.dp)
                        .testTag("tab_parent_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Parent",
                            tint = if (!isStudentTab) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Parent Login",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isStudentTab) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LOGIN CARD FORM
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Role Badge & Description
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isStudentTab) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isStudentTab) Icons.Default.School else Icons.Default.People,
                                contentDescription = null,
                                tint = if (isStudentTab) Color(0xFF1D61F2) else Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isStudentTab) "Student Account" else "Parent / Guardian Account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (isStudentTab) "Track journeys and trigger instant safety alerts" else "Supervise family locations, approvals, and safe zones",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Field 1: Identifier
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isStudentTab) "Student ID or Mobile" else "Parent ID or Email",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = if (isStudentTab) studentId else parentId,
                            onValueChange = {
                                if (isStudentTab) viewModel.studentIdInput.value = it
                                else viewModel.parentIdInput.value = it
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User",
                                    tint = Color(0xFF64748B)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = darkTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isStudentTab) "student_id_input" else "parent_id_input")
                        )
                    }

                    // Field 2: Password
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Password",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = if (isStudentTab) studentPassword else parentPassword,
                            onValueChange = {
                                if (isStudentTab) viewModel.studentPasswordInput.value = it
                                else viewModel.parentPasswordInput.value = it
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock",
                                    tint = Color(0xFF64748B)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = darkTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isStudentTab) "student_password_input" else "parent_password_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Login Button
                    Button(
                        onClick = {
                            if (isStudentTab) {
                                viewModel.loginAsStudent()
                            } else {
                                viewModel.loginAsParent()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStudentTab) Color(0xFF1D61F2) else Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag(if (isStudentTab) "login_as_student_btn" else "login_as_parent_btn")
                    ) {
                        Text(
                            text = if (isStudentTab) "LOGIN AS STUDENT" else "LOGIN AS PARENT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Setup Account Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "New family account? ",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Set Up Profile",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D61F2),
                    modifier = Modifier
                        .clickable { onContinue() }
                        .padding(4.dp)
                )
            }
        }
    }
}
