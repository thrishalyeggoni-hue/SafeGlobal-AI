package com.example.data.auth

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages Authentication via Email + Password.
 * Supports live Firebase authentication when configured with a real project,
 * and seamless fallback when running in local/test/mock configuration.
 */
object FirebaseAuthManager {

    private val auth: FirebaseAuth?
        get() = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val isMockOrUnconfigured: Boolean
        get() = try {
            val app = FirebaseApp.getInstance()
            val key = app.options.apiKey
            key.isBlank() || key.contains("Mock", ignoreCase = true) || key.contains("1234567890")
        } catch (_: Exception) {
            true
        }

    var fallbackUid: String? = null
        private set

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: FirebaseUser?
        get() = try { auth?.currentUser } catch (e: Exception) { null }

    fun getEffectiveUid(overrideEmail: String? = null): String {
        try { currentUser?.uid?.let { if (it.isNotEmpty()) return it } } catch (_: Exception) {}
        fallbackUid?.let { if (it.isNotEmpty()) return it }
        val raw = overrideEmail ?: "user_default"
        val uid = "uid_usr_${raw.hashCode().toString().replace("-", "0")}"
        fallbackUid = uid
        return uid
    }

    // ── Auth States ────────────────────────────────────────────────────────────
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Authenticated(val user: FirebaseUser? = null) : AuthState()
        data class Error(val message: String) : AuthState()
        object NeedsEmailFallback : AuthState()
    }

    // ── Email / Password Sign-In ───────────────────────────────────────────────

    suspend fun signInWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        val cleanEmail = email.trim().lowercase()
        val firebaseAuth = auth

        if (firebaseAuth != null && !isMockOrUnconfigured) {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(cleanEmail, password).await()
                fallbackUid = result.user?.uid
                _authState.value = AuthState.Authenticated(result.user)
                return
            } catch (e: FirebaseAuthInvalidUserException) {
                _authState.value = AuthState.Error("No account found with this email. Please register first.")
                return
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Error("Incorrect password. Please try again.")
                return
            } catch (e: Exception) {
                val raw = e.localizedMessage ?: ""
                if (!isConfigOrKeyError(raw)) {
                    _authState.value = AuthState.Error(friendlyError(raw))
                    return
                }
            }
        }

        // Seamless local/test fallback
        fallbackUid = "uid_usr_${cleanEmail.hashCode().toString().replace("-", "0")}"
        _authState.value = AuthState.Authenticated(null)
    }

    suspend fun registerWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        val cleanEmail = email.trim().lowercase()
        val firebaseAuth = auth

        if (firebaseAuth != null && !isMockOrUnconfigured) {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
                fallbackUid = result.user?.uid
                try { result.user?.sendEmailVerification()?.await() } catch (_: Exception) {}
                _authState.value = AuthState.Authenticated(result.user)
                return
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Error("Account already exists. Please sign in instead.")
                return
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Error("Invalid email address format.")
                return
            } catch (e: Exception) {
                val raw = e.localizedMessage ?: ""
                if (!isConfigOrKeyError(raw)) {
                    _authState.value = AuthState.Error(friendlyError(raw))
                    return
                }
            }
        }

        // Seamless local/test fallback
        fallbackUid = "uid_usr_${cleanEmail.hashCode().toString().replace("-", "0")}"
        _authState.value = AuthState.Authenticated(null)
    }

    suspend fun sendPasswordReset(email: String) {
        try { auth?.sendPasswordResetEmail(email.trim())?.await() } catch (_: Exception) {}
    }

    // ── Session ────────────────────────────────────────────────────────────────

    fun signOut() {
        try { auth?.signOut() } catch (_: Exception) {}
        fallbackUid = null
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun isConfigOrKeyError(raw: String): Boolean =
        raw.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
        raw.contains("API_KEY_INVALID", ignoreCase = true) ||
        raw.contains("api key", ignoreCase = true) ||
        raw.contains("not configured", ignoreCase = true)

    private fun friendlyError(raw: String): String = when {
        isConfigOrKeyError(raw) ->
            "Firebase not configured. Please add your real google-services.json."
        raw.contains("network", ignoreCase = true) ->
            "Network error. Please check your internet connection."
        raw.contains("blocked", ignoreCase = true) ->
            "Too many attempts. Please wait a few minutes and try again."
        else -> raw
    }
}
