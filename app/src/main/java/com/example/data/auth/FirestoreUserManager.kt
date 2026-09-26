package com.example.data.auth

import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Manages user profiles in Firestore.
 * After phone OTP is verified, the user's profile (name, role, grade) is stored
 * and fetched from Firestore — no fake local defaults.
 */
object FirestoreUserManager {

    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "users"

    data class UserProfile(
        val uid: String = "",
        val phone: String = "",
        val displayName: String = "",
        val role: String = "PARENT",    // "PARENT" or "STUDENT"
        val gradeClass: String = "",
        val safeSphereId: String = "",
        val familyId: String = "",
        val isProfileComplete: Boolean = false,
        val createdAt: Long = System.currentTimeMillis()
    ) {
        // Firestore requires a no-arg constructor via default params
        constructor() : this("", "", "", "PARENT", "", "", "", false, System.currentTimeMillis())

        fun toUserRole(): UserRole = if (role == "STUDENT") UserRole.STUDENT else UserRole.PARENT
    }

    /**
     * Fetches the user profile from Firestore.
     * Returns null if no profile exists yet (new user).
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = db.collection(COLLECTION).document(uid).get().await()
            if (doc.exists()) doc.toObject(UserProfile::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates or updates the user profile in Firestore.
     * Uses merge so partial updates don't overwrite existing fields.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            db.collection(COLLECTION)
                .document(profile.uid)
                .set(profile, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates an initial profile for a newly authenticated user.
     */
    suspend fun createInitialProfile(
        user: FirebaseUser,
        role: UserRole,
        displayName: String,
        safeSphereId: String,
        gradeClass: String
    ): Result<Unit> {
        val profile = UserProfile(
            uid = user.uid,
            phone = user.phoneNumber ?: "",
            displayName = displayName,
            role = role.name,
            gradeClass = gradeClass,
            safeSphereId = safeSphereId,
            familyId = generateFamilyId(),
            isProfileComplete = true,
            createdAt = System.currentTimeMillis()
        )
        return saveUserProfile(profile)
    }

    /**
     * Checks whether the user's SafeSphere ID is available (not taken by another user).
     */
    suspend fun isSafeSphereIdAvailable(safeSphereId: String): Boolean {
        return try {
            val query = db.collection(COLLECTION)
                .whereEqualTo("safeSphereId", safeSphereId)
                .get()
                .await()
            query.isEmpty
        } catch (e: Exception) {
            // On network error, optimistically allow (server will enforce uniqueness)
            true
        }
    }

    private fun generateFamilyId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "SF-" + (1..5).map { chars.random() }.joinToString("")
    }
}
