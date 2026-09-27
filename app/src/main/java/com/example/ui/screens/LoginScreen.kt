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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.viewmodel.SafeSphereViewModel
import kotlinx.coroutines.launch

/**
 * SafeSphere Login Screen
 * - Role selector (Parent / Child) at top
 * - Sign In: SafeSphere ID + Password
 * - Create Account: navigates to onboarding flow
 * No Gmail / Firebase email auth.
 */
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

@Composable
fun LoginScreen(
    viewModel: SafeSphereViewModel,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedRole by remember { mutableStateOf(UserRole.PARENT) }
    var safeSphereId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newAccountName by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember(selectedRole) { mutableIntStateOf(if (selectedRole == UserRole.PARENT) 1 else 7) }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val loginError by viewModel.loginErrorMessage.collectAsState()

    LaunchedEffect(loginError) {
        if (!loginError.isNullOrBlank()) { localError = loginError; isLoading = false }
    }
    LaunchedEffect(selectedTab, selectedRole) {
        localError = null
        viewModel.loginErrorMessage.value = null
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF06101F), Color(0xFF0E1E38), Color(0xFF152644)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape)
                    .background(Color(0xFF1D61F2).copy(alpha = 0.12f))
                    .border(2.dp, Color(0xFF1D61F2).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("SafeSphere", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(
                "Family Safety — Always Connected", fontSize = 13.sp,
                color = Color(0xFF94A3B8), textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(32.dp))

            // Role selector
            Text("I am a", fontSize = 13.sp, color = Color(0xFF94A3B8), modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RolePill(
                    label = "Parent", emoji = "👨‍👩‍👧",
                    selected = selectedRole == UserRole.PARENT,
                    onClick = { selectedRole = UserRole.PARENT; localError = null; viewModel.loginErrorMessage.value = null },
                    modifier = Modifier.weight(1f)
                )
                RolePill(
                    label = "Child", emoji = "🎒",
                    selected = selectedRole == UserRole.STUDENT,
                    onClick = { selectedRole = UserRole.STUDENT; localError = null; viewModel.loginErrorMessage.value = null },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))

            // Auth card
            Box(
                modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp)).background(Color(0xFF1A2847))
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFF0F1A30))
                    ) {
                        AuthTabItem("Sign In", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                        AuthTabItem("Create Account", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
                    }
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 4 }).togetherWith(fadeOut() + slideOutVertically { -it / 4 })
                        },
                        label = "auth_tab",
                        modifier = Modifier.padding(24.dp)
                    ) { tab ->
                        when (tab) {
                            0 -> SignInContent(
                                safeSphereId = safeSphereId,
                                onIdChange = { safeSphereId = it.trim(); localError = null },
                                password = password,
                                onPasswordChange = { password = it; localError = null },
                                showPassword = showPassword,
                                onTogglePassword = { showPassword = !showPassword },
                                isLoading = isLoading,
                                localError = localError,
                                roleName = if (selectedRole == UserRole.PARENT) "Parent" else "Child",
                                onQuickFill = { id, pwd, role ->
                                    safeSphereId = id
                                    password = pwd
                                    selectedRole = role
                                    localError = null
                                },
                                onSignIn = {
                                    val idClean = safeSphereId.trim()
                                    val pwdClean = password.trim()
                                    val err = validateIdPassword(idClean, pwdClean)
                                    if (err != null) { localError = err } else {
                                        isLoading = true; localError = null
                                        scope.launch {
                                            viewModel.loginWithSafeSphereId(
                                                safeSphereId = idClean,
                                                password = pwdClean,
                                                expectedRole = selectedRole,
                                                onSuccess = {
                                                    isLoading = false
                                                    onAuthenticated()
                                                },
                                                onError = { msg ->
                                                    isLoading = false
                                                    localError = msg
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                            else -> CreateAccountContent(
                                role = selectedRole,
                                fullName = newAccountName,
                                onNameChange = { newAccountName = it; localError = null },
                                selectedAvatarIndex = selectedAvatarIndex,
                                onAvatarSelect = { selectedAvatarIndex = it },
                                isLoading = isLoading,
                                localError = localError,
                                onCreateAccount = {
                                    val clean = newAccountName.trim()
                                    if (clean.isBlank()) {
                                        localError = "Please enter your name."
                                    } else {
                                        isLoading = true
                                        localError = null
                                        scope.launch {
                                            viewModel.registerQuickAccount(
                                                name = clean,
                                                role = selectedRole,
                                                avatarIndex = selectedAvatarIndex,
                                                onSuccess = {
                                                    isLoading = false
                                                    onAuthenticated()
                                                },
                                                onError = { msg ->
                                                    isLoading = false
                                                    localError = msg
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "By continuing you agree to SafeSphere's Terms & Privacy Policy",
                fontSize = 11.sp, color = Color(0xFF475569),
                textAlign = TextAlign.Center, lineHeight = 16.sp
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SignInContent(
    safeSphereId: String, onIdChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    showPassword: Boolean, onTogglePassword: () -> Unit,
    isLoading: Boolean, localError: String?, roleName: String,
    onQuickFill: (String, String, UserRole) -> Unit,
    onSignIn: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Welcome back, $roleName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Sign in with your SafeSphere ID", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = safeSphereId, onValueChange = onIdChange,
            label = { Text("SafeSphere ID", fontSize = 12.sp) }, singleLine = true,
            leadingIcon = { Icon(Icons.Default.Badge, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = ssColors(), modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password, onValueChange = onPasswordChange,
            label = { Text("Password", fontSize = 12.sp) }, singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color(0xFF64748B))
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = ssColors(), modifier = Modifier.fillMaxWidth()
        )
        localError?.let { ErrorBox(it) }

        // Quick demo fill buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF22355A))
                    .clickable { onQuickFill("parent1", "password123", UserRole.PARENT) }
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("👨‍👩‍👧 Parent Demo", fontSize = 11.5.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF22355A))
                    .clickable { onQuickFill("student1", "password123", UserRole.STUDENT) }
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🎒 Child Demo", fontSize = 11.5.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Medium)
            }
        }

        Button(
            onClick = onSignIn, enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D61F2)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun CreateAccountContent(
    role: UserRole,
    fullName: String,
    onNameChange: (String) -> Unit,
    selectedAvatarIndex: Int,
    onAvatarSelect: (Int) -> Unit,
    isLoading: Boolean,
    localError: String?,
    onCreateAccount: () -> Unit
) {
    val roleTitle = if (role == UserRole.PARENT) "Parent" else "Child"
    val cleanName = fullName.trim()
    val baseId = cleanName.lowercase().replace(Regex("[^a-z0-9]"), "_").trim('_')
    val previewId = if (baseId.length >= 2) "${baseId}_101" else "your_id"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Create $roleTitle Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Enter your name to start using SafeSphere right away.", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = onNameChange,
            label = { Text("Full Name", fontSize = 12.sp) },
            placeholder = { Text(if (role == UserRole.PARENT) "e.g. John Doe" else "e.g. Alex Smith", fontSize = 12.sp, color = Color(0xFF64748B)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = ssColors(),
            modifier = Modifier.fillMaxWidth()
        )

        // Choose from 16 profile avatars
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Choose Your Avatar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Avatar #$selectedAvatarIndex of 16", fontSize = 11.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Medium)
            }
            Text("Select your personal portrait from the 16 SafeSphere avatars", fontSize = 11.sp, color = Color(0xFF94A3B8))
            com.example.ui.components.AvatarPickerRow(
                selectedAvatarIndex = selectedAvatarIndex,
                onAvatarSelected = onAvatarSelect
            )
        }

        // Dynamic ID Preview Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F1A30))
                .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Badge, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                Text(
                    text = if (cleanName.isNotBlank()) "Your SafeSphere ID: $previewId" else "Your unique SafeSphere ID will be generated automatically",
                    fontSize = 12.sp,
                    color = if (cleanName.isNotBlank()) Color(0xFF93C5FD) else Color(0xFF64748B),
                    fontWeight = if (cleanName.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        localError?.let { ErrorBox(it) }

        Button(
            onClick = onCreateAccount,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    text = "Create $roleTitle Account & Enter",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RolePill(label: String, emoji: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(46.dp).clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFF1D61F2).copy(alpha = 0.25f) else Color(0xFF1A2847))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFF60A5FA) else Color(0xFF2D4070),
                shape = RoundedCornerShape(14.dp)
            ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$emoji $label", fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color(0xFF60A5FA) else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun AuthTabItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(48.dp).clickable(onClick = onClick)
            .then(
                if (selected) Modifier.background(Brush.verticalGradient(listOf(Color(0xFF1D61F2).copy(alpha = 0.2f), Color.Transparent)))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label, fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color(0xFF60A5FA) else Color(0xFF64748B)
            )
            if (selected) {
                Box(modifier = Modifier.padding(top = 3.dp).size(width = 28.dp, height = 2.dp).background(Color(0xFF1D61F2), RoundedCornerShape(1.dp)))
            }
        }
    }
}

@Composable
private fun ErrorBox(message: String) {
    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f), RoundedCornerShape(12.dp)).padding(12.dp)
        ) {
            Text(message, fontSize = 12.5.sp, color = Color(0xFFFCA5A5), lineHeight = 17.sp)
        }
    }
}

@Composable
private fun ssColors() = TextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF60A5FA),
    focusedContainerColor = Color(0xFF0F1A30), unfocusedContainerColor = Color(0xFF0F1A30),
    focusedIndicatorColor = Color(0xFF1D61F2), unfocusedIndicatorColor = Color(0xFF334155),
    focusedLabelColor = Color(0xFF60A5FA), unfocusedLabelColor = Color(0xFF64748B)
)

private fun validateIdPassword(id: String, password: String): String? {
    if (id.isBlank()) return "Please enter your SafeSphere ID."
    if (id.length < 4) return "SafeSphere ID must be at least 4 characters."
    if (password.length < 6) return "Password must be at least 6 characters."
    return null
}
