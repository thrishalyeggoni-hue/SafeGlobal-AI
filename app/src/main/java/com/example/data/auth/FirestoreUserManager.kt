package com.example.data.auth

import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Manages user profiles in Firestore.
 * After phone OTP is verified, the user's profile (name, role, grade) is stored
 * and fetched from Firestore — no fake local defaults.
 */
object FirestoreUserManager {

    private val db: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    private const val COLLECTION = "users"
    private val memoryProfileCache = mutableMapOf<String, UserProfile>()

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
     * Fetches the user profile from Firestore, with memory/local fallback.
     * Returns null if no profile exists yet (new user).
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        memoryProfileCache[uid]?.let { return it }
        val firestore = db ?: return null
        return try {
            withTimeoutOrNull(6000L) {
                val doc = firestore.collection(COLLECTION).document(uid).get().await()
                if (doc.exists()) {
                    val p = doc.toObject(UserProfile::class.java)
                    if (p != null) memoryProfileCache[uid] = p
                    p
                } else null
            } ?: memoryProfileCache[uid]
        } catch (e: Exception) {
            memoryProfileCache[uid]
        }
    }

    /**
     * Creates or updates the user profile in Firestore and memory cache.
     * Uses merge so partial updates don't overwrite existing fields.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        memoryProfileCache[profile.uid] = profile
        val firestore = db ?: return Result.success(Unit) // Offline/local mode fallback
        return try {
            withTimeoutOrNull(6000L) {
                firestore.collection(COLLECTION)
                    .document(profile.uid)
                    .set(profile, SetOptions.merge())
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Still succeeded in local memory cache
            Result.success(Unit)
        }
    }

    /**
     * Creates an initial profile for a user.
     */
    suspend fun createInitialProfile(
        uid: String,
        phone: String,
        role: UserRole,
        displayName: String,
        safeSphereId: String,
        gradeClass: String
    ): Result<Unit> {
        val profile = UserProfile(
            uid = uid,
            phone = phone,
            displayName = displayName,
            role = role.name,
            gradeClass = gradeClass,
            safeSphereId = safeSphereId,
            familyId = generateFamilyId(),
            isProfileComplete = true,
            createdAt = System.currentTimeMillis()
        )
        // Store in users collection
        val result = saveUserProfile(profile)

        // Store each member in family_members collection in cloud database
        db?.let { firestore ->
            try {
                firestore.collection("family_members")
                    .document(safeSphereId.ifEmpty { uid })
                    .set(
                        mapOf(
                            "uid" to uid,
                            "phone" to phone,
                            "displayName" to displayName,
                            "role" to role.name,
                            "gradeClass" to gradeClass,
                            "safeSphereId" to safeSphereId,
                            "familyId" to profile.familyId,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
            } catch (e: Exception) {
                // Non-fatal
            }
        }
        return result
    }

    /**
     * Creates an initial profile for a newly authenticated Firebase user.
     */
    suspend fun createInitialProfile(
        user: FirebaseUser,
        role: UserRole,
        displayName: String,
        safeSphereId: String,
        gradeClass: String
    ): Result<Unit> = createInitialProfile(
        uid = user.uid,
        phone = user.phoneNumber ?: "",
        role = role,
        displayName = displayName,
        safeSphereId = safeSphereId,
        gradeClass = gradeClass
    )

    /**
     * Checks whether the user's SafeSphere ID is available (not taken by another user).
     */
    suspend fun isSafeSphereIdAvailable(safeSphereId: String): Boolean {
        val firestore = db ?: return true
        return try {
            withTimeoutOrNull(1500L) {
                val query = firestore.collection(COLLECTION)
                    .whereEqualTo("safeSphereId", safeSphereId)
                    .get()
                    .await()
                query.isEmpty
            } ?: true
        } catch (e: Exception) {
            // On network error, optimistically allow (server will enforce uniqueness)
            true
        }
    }

    fun generateFamilyId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "SF-" + (1..5).map { chars.random() }.joinToString("")
    }
}
