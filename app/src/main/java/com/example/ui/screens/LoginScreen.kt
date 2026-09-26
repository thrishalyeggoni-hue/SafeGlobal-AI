package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthManager
import com.example.ui.viewmodel.SafeSphereViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: SafeSphereViewModel,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val authState by FirebaseAuthManager.authState.collectAsState()

    // 0 = Sign In, 1 = Register
    var selectedTab by remember { mutableIntStateOf(0) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var resetSent by remember { mutableStateOf(false) }

    val isLoading = authState is FirebaseAuthManager.AuthState.Loading

    // Clear error when tab switches
    LaunchedEffect(selectedTab) {
        localError = null
        resetSent = false
    }

    // React to auth state changes
    LaunchedEffect(authState) {
        when (val s = authState) {
            is FirebaseAuthManager.AuthState.Authenticated -> onAuthenticated()
            is FirebaseAuthManager.AuthState.Error -> localError = s.message
            else -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A1628), Color(0xFF112240), Color(0xFF1A2F50))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // ── Logo / Brand ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1D61F2).copy(alpha = 0.15f))
                    .border(1.5.dp, Color(0xFF1D61F2).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "SafeSphere",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Family Safety — Always Connected",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(36.dp))

            // ── Auth Card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Column {
                    // Tabs: Sign In / Register
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF60A5FA),
                        modifier = Modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.testTag("tab_signin")
                        ) {
                            Text(
                                text = "Sign In",
                                modifier = Modifier.padding(vertical = 14.dp),
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color(0xFF60A5FA) else Color(0xFF64748B)
                            )
                        }
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.testTag("tab_register")
                        ) {
                            Text(
                                text = "Register",
                                modifier = Modifier.padding(vertical = 14.dp),
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color(0xFF60A5FA) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Tab content
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 4 }).togetherWith(
                                fadeOut() + slideOutVertically { -it / 4 }
                            )
                        },
                        label = "auth_tab",
                        modifier = Modifier.padding(24.dp)
                    ) { tab ->
                        when (tab) {
                            0 -> SignInContent(
                                email = email,
                                onEmailChange = { email = it; localError = null },
                                password = password,
                                onPasswordChange = { password = it; localError = null },
                                showPassword = showPassword,
                                onTogglePassword = { showPassword = !showPassword },
                                isLoading = isLoading,
                                localError = localError,
                                resetSent = resetSent,
                                onSignIn = {
                                    localError = validateEmailPassword(email, password)
                                    if (localError == null) {
                                        scope.launch { FirebaseAuthManager.signInWithEmail(email, password) }
                                    }
                                },
                                onForgotPassword = {
                                    if (email.isBlank()) {
                                        localError = "Enter your email above first."
                                    } else {
                                        scope.launch {
                                            FirebaseAuthManager.sendPasswordReset(email)
                                            resetSent = true
                                            localError = null
                                        }
                                    }
                                }
                            )
                            1 -> RegisterContent(
                                email = email,
                                onEmailChange = { email = it; localError = null },
                                password = password,
                                onPasswordChange = { password = it; localError = null },
                                confirmPassword = confirmPassword,
                                onConfirmChange = { confirmPassword = it; localError = null },
                                showPassword = showPassword,
                                onTogglePassword = { showPassword = !showPassword },
                                isLoading = isLoading,
                                localError = localError,
                                onRegister = {
                                    localError = validateRegistration(email, password, confirmPassword)
                                    if (localError == null) {
                                        scope.launch { FirebaseAuthManager.registerWithEmail(email, password) }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "By continuing you agree to SafeSphere's Terms & Privacy Policy",
                fontSize = 11.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Sign In Content ────────────────────────────────────────────────────────────

@Composable
private fun SignInContent(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    showPassword: Boolean, onTogglePassword: () -> Unit,
    isLoading: Boolean,
    localError: String?,
    resetSent: Boolean,
    onSignIn: () -> Unit,
    onForgotPassword: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Welcome back",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Sign in to your SafeSphere account",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(Modifier.height(4.dp))

        AuthTextField(
            value = email,
            onChange = onEmailChange,
            label = "Email address",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        AuthTextField(
            value = password,
            onChange = onPasswordChange,
            label = "Password",
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null,
                        tint = Color(0xFF64748B)
                    )
                }
            }
        )

        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                if (resetSent) "✓ Reset email sent!" else "Forgot password?",
                fontSize = 12.sp,
                color = if (resetSent) Color(0xFF34D399) else Color(0xFF60A5FA)
            )
        }

        localError?.let { ErrorBanner(it) }

        Button(
            onClick = onSignIn,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("email_signin_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ── Register Content ───────────────────────────────────────────────────────────

@Composable
private fun RegisterContent(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmChange: (String) -> Unit,
    showPassword: Boolean, onTogglePassword: () -> Unit,
    isLoading: Boolean,
    localError: String?,
    onRegister: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Create your account",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Join SafeSphere to protect your family",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(Modifier.height(4.dp))

        AuthTextField(
            value = email,
            onChange = onEmailChange,
            label = "Email address",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        AuthTextField(
            value = password,
            onChange = onPasswordChange,
            label = "Password (min 8 chars)",
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null,
                        tint = Color(0xFF64748B)
                    )
                }
            }
        )

        AuthTextField(
            value = confirmPassword,
            onChange = onConfirmChange,
            label = "Confirm password",
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation()
        )

        localError?.let { ErrorBanner(it) }

        Button(
            onClick = onRegister,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("email_register_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Create Account", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ── Shared Composables ────────────────────────────────────────────────────────

@Composable
private fun AuthTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        leadingIcon = { Icon(icon, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF60A5FA),
            focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF0F172A),
            focusedIndicatorColor = Color(0xFF1D61F2), unfocusedIndicatorColor = Color(0xFF334155),
            focusedLabelColor = Color(0xFF60A5FA), unfocusedLabelColor = Color(0xFF64748B)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ErrorBanner(message: String) {
    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(message, fontSize = 12.5.sp, color = Color(0xFFFCA5A5), lineHeight = 17.sp)
        }
    }
}

// ── Validators ────────────────────────────────────────────────────────────────

private fun validateEmailPassword(email: String, password: String): String? {
    if (email.isBlank()) return "Please enter your email address."
    if (!email.contains("@") || !email.contains(".")) return "Please enter a valid email address."
    if (password.length < 6) return "Password must be at least 6 characters."
    return null
}

private fun validateRegistration(email: String, password: String, confirm: String): String? {
    if (email.isBlank()) return "Please enter your email address."
    if (!email.contains("@") || !email.contains(".")) return "Please enter a valid email address."
    if (password.length < 8) return "Password must be at least 8 characters."
    if (password != confirm) return "Passwords do not match."
    return null
}
