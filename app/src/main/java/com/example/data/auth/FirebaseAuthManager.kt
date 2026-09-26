package com.example.data.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

/**
 * Manages real Firebase Phone Authentication (SMS OTP).
 * No fake data — every OTP is sent to the real phone number via Firebase.
 */
object FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Stored verification ID from Firebase (used to confirm OTP)
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: FirebaseUser? get() = auth.currentUser

    sealed class AuthState {
        object Idle : AuthState()
        object SendingOtp : AuthState()
        object OtpSent : AuthState()
        object Verifying : AuthState()
        data class Authenticated(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    /**
     * Sends a real OTP to the given phone number via Firebase.
     * phoneNumber must include country code, e.g. "+919876543210"
     */
    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.SendingOtp

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification completed
                _authState.value = AuthState.Verifying
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                val msg = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format. Please check and retry."
                    else -> e.localizedMessage ?: "OTP sending failed. Check your network."
                }
                _authState.value = AuthState.Error(msg)
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                resendToken = token
                _authState.value = AuthState.OtpSent
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Resends OTP using the stored resend token.
     */
    fun resendOtp(phoneNumber: String, activity: Activity) {
        val token = resendToken ?: run {
            sendOtp(phoneNumber, activity)
            return
        }

        _authState.value = AuthState.SendingOtp

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Resend failed.")
            }

            override fun onCodeSent(vId: String, t: PhoneAuthProvider.ForceResendingToken) {
                verificationId = vId
                resendToken = t
                _authState.value = AuthState.OtpSent
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verifies the 6-digit OTP entered by the user against the Firebase verification ID.
     */
    fun verifyOtp(otpCode: String) {
        val vId = verificationId ?: run {
            _authState.value = AuthState.Error("Session expired. Please request a new OTP.")
            return
        }

        if (otpCode.length != 6 || !otpCode.all { it.isDigit() }) {
            _authState.value = AuthState.Error("Please enter a valid 6-digit OTP.")
            return
        }

        _authState.value = AuthState.Verifying
        val credential = PhoneAuthProvider.getCredential(vId, otpCode)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    _authState.value = AuthState.Authenticated(user)
                } ?: run {
                    _authState.value = AuthState.Error("Authentication succeeded but user is null.")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(
                    when (e) {
                        is FirebaseAuthInvalidCredentialsException -> "Wrong OTP. Please check the code and try again."
                        else -> e.localizedMessage ?: "Verification failed."
                    }
                )
            }
    }

    fun signOut() {
        auth.signOut()
        verificationId = null
        resendToken = null
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
