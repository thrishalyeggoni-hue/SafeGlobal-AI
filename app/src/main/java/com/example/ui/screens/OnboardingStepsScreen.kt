package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.UserRole
import com.example.ui.components.SafeSphereEmblem
import com.example.ui.theme.darkTextFieldColors
import com.example.ui.viewmodel.SafeSphereViewModel
import kotlinx.coroutines.delay

@Composable
fun PhoneVerificationScreen(
    viewModel: SafeSphereViewModel,
    onSendOtp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phone by viewModel.phoneInput.collectAsState()
    val countryCode by viewModel.countryCode.collectAsState()
    val otpError by viewModel.otpError.collectAsState()
    val authState by viewModel.firebaseAuthState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val isSending = authState is FirebaseAuthManager.AuthState.SendingOtp
    val isOtpSent = authState is FirebaseAuthManager.AuthState.OtpSent

    // On OTP sent, auto-navigate
    LaunchedEffect(authState) {
        if (authState is FirebaseAuthManager.AuthState.OtpSent) {
            onSendOtp()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
            .testTag("phone_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
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
                text = "Enter your real mobile number to receive an OTP",
                fontSize = 13.sp,
                color = Color(0xFF4B5565),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Phone Input with Country Code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (otpError != null) Color(0xFFE11D48) else Color(0xFFE2E8F0),
                        RoundedCornerShape(12.dp)
                    )
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = countryCode,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Color(0xFFE2E8F0))
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        viewModel.phoneInput.value = it.filter { c -> c.isDigit() }.take(10)
                        viewModel.clearOtpError()
                    },
                    placeholder = { Text("10-digit mobile number", fontSize = 13.5.sp, color = Color(0xFF94A3B8)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
                        .weight(1f)
                        .testTag("phone_input_field")
                )
            }

            // Error message
            if (otpError != null) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = otpError!!,
                        fontSize = 12.sp,
                        color = Color(0xFFE11D48)
                    )
                }
            }

            // Firebase error
            if (authState is FirebaseAuthManager.AuthState.Error) {
                val errMsg = (authState as FirebaseAuthManager.AuthState.Error).message
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = errMsg,
                        fontSize = 12.sp,
                        color = Color(0xFFE11D48)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.sendRealOtp(context as Activity)
                },
                enabled = !isSending && phone.length == 10,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_otp_btn")
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("SENDING OTP...", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text("SEND OTP", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By continuing, you agree to our Privacy Policy and Terms of Service.\nA real SMS will be sent to your mobile number.",
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
    val phone by viewModel.phoneInput.collectAsState()
    val countryCode by viewModel.countryCode.collectAsState()
    val otpError by viewModel.otpError.collectAsState()
    val authState by viewModel.firebaseAuthState.collectAsState()
    val context = LocalContext.current

    // Individual OTP digit inputs
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val otpDigits = remember { mutableStateOf(Array(6) { "" }) }

    val isVerifying = authState is FirebaseAuthManager.AuthState.Verifying
    val isAuth = authState is FirebaseAuthManager.AuthState.Authenticated

    // Countdown timer for resend
    var resendSeconds by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (resendSeconds > 0) {
            delay(1000)
            resendSeconds--
        }
        canResend = true
    }

    // On verified, call onVerifyOtp
    LaunchedEffect(authState) {
        if (authState is FirebaseAuthManager.AuthState.Authenticated) {
            viewModel.onOtpVerified()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
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
                text = "Enter the 6-digit code sent to $countryCode$phone",
                fontSize = 13.sp,
                color = Color(0xFF4B5565),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // 6-digit OTP boxes — each is a real editable text field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..5) {
                    OutlinedTextField(
                        value = otpDigits.value[i],
                        onValueChange = { new ->
                            val digit = new.filter { it.isDigit() }.take(1)
                            val updated = otpDigits.value.copyOf()
                            updated[i] = digit
                            otpDigits.value = updated
                            // Update viewmodel state
                            viewModel.otpInputs.value = updated.toList()
                            viewModel.clearOtpError()
                            // Auto-advance focus
                            if (digit.isNotEmpty() && i < 5) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = if (i < 5) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { if (i < 5) focusRequesters[i + 1].requestFocus() },
                            onDone = {
                                val fullOtp = otpDigits.value.joinToString("")
                                viewModel.otpInputs.value = otpDigits.value.toList()
                                viewModel.verifyRealOtp(onSuccess = onVerifyOtp)
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF1D61F2),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedIndicatorColor = Color(0xFF1D61F2),
                            unfocusedIndicatorColor = if (otpDigits.value[i].isNotEmpty()) Color(0xFF10B981) else Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(width = 46.dp, height = 56.dp)
                            .focusRequester(focusRequesters[i])
                            .testTag("otp_box_$i")
                    )
                }
            }

            // Error
            if (otpError != null || authState is FirebaseAuthManager.AuthState.Error) {
                val errMsg = otpError ?: (authState as? FirebaseAuthManager.AuthState.Error)?.message ?: ""
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(text = errMsg, fontSize = 12.sp, color = Color(0xFFE11D48))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resend timer / button
            if (canResend) {
                Text(
                    text = "Resend OTP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D61F2),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            resendSeconds = 60
                            canResend = false
                            viewModel.resendRealOtp(context as Activity)
                        }
                )
            } else {
                Text(
                    text = "Resend OTP in 00:${resendSeconds.toString().padStart(2, '0')}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.otpInputs.value = otpDigits.value.toList()
                    viewModel.verifyRealOtp(onSuccess = onVerifyOtp)
                },
                enabled = !isVerifying && otpDigits.value.all { it.isNotEmpty() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_otp_btn")
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("VERIFYING...", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text("VERIFY CODE", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * Role Selection Screen — shown to new users after OTP verification.
 * Students and parents select their role before setting up their profile.
 * No fake pre-filled data.
 */
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

            SafeSphereEmblem(size = 64.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Who are you?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Select your role to set up your SafeSphere account",
                fontSize = 13.5.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Student Card
            RoleCard(
                icon = Icons.Default.School,
                title = "Student / Child",
                description = "Track your journeys, send I'm OK alerts, and stay connected with your family.",
                isSelected = selectedRole == UserRole.STUDENT,
                accentColor = Color(0xFF1D61F2),
                bgColor = Color(0xFFEFF6FF),
                onClick = { viewModel.selectedRole.value = UserRole.STUDENT },
                testTag = "select_student_role"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Card
            RoleCard(
                icon = Icons.Default.People,
                title = "Parent / Guardian",
                description = "Monitor family locations, approve journeys, and manage safe zones.",
                isSelected = selectedRole == UserRole.PARENT,
                accentColor = Color(0xFF065F46),
                bgColor = Color(0xFFF0FDF4),
                onClick = { viewModel.selectedRole.value = UserRole.PARENT },
                testTag = "select_parent_role"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("role_continue_btn")
            ) {
                Text("CONTINUE", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun RoleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) bgColor else Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) accentColor else Color(0xFF6B7280),
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 16.sp
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
