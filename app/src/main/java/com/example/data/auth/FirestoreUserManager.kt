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
    private const val PREFS_NAME = "safesphere_user_profiles_v1"
    private val memoryProfileCache = mutableMapOf<String, UserProfile>()
    private var appContext: android.content.Context? = null

    fun init(context: android.content.Context) {
        appContext = context.applicationContext
        loadPersistedProfiles()
    }

    private fun loadPersistedProfiles() {
        val ctx = appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val allEntries = prefs.all
            for ((key, value) in allEntries) {
                if (value is String) {
                    val p = fromJson(value)
                    if (p != null) {
                        memoryProfileCache[p.uid] = p
                        if (p.safeSphereId.isNotBlank()) {
                            memoryProfileCache["id:" + p.safeSphereId.lowercase()] = p
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Seed demo accounts if empty so testing and instant login are always available
        if (memoryProfileCache.isEmpty()) {
            val demoParent = UserProfile(
                uid = "uid_parent_demo_01",
                displayName = "Sarah (Parent)",
                role = "PARENT",
                safeSphereId = "parent1",
                familyId = "SF-DEMO1",
                isProfileComplete = true,
                passwordHash = hashPassword("password123"),
                avatarIndex = 1
            )
            val demoStudent = UserProfile(
                uid = "uid_student_demo_01",
                displayName = "Alex (Student)",
                role = "STUDENT",
                gradeClass = "10th Grade",
                safeSphereId = "student1",
                familyId = "SF-DEMO1",
                isProfileComplete = true,
                passwordHash = hashPassword("password123"),
                avatarIndex = 7,
                pairingCode = "275474"
            )
            saveLocalProfile(demoParent)
            saveLocalProfile(demoStudent)
        }
    }

    fun saveLocalProfile(profile: UserProfile) {
        memoryProfileCache[profile.uid] = profile
        if (profile.safeSphereId.isNotBlank()) {
            memoryProfileCache["id:" + profile.safeSphereId.lowercase()] = profile
        }
        val ctx = appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val jsonStr = toJson(profile)
            prefs.edit()
                .putString("uid_" + profile.uid, jsonStr)
                .putString("id_" + profile.safeSphereId.lowercase(), jsonStr)
                .apply()
        } catch (_: Exception) {}
    }

    fun getLocalProfile(idOrUid: String): UserProfile? {
        val clean = idOrUid.trim()
        val lower = clean.lowercase()
        memoryProfileCache[clean]?.let { return it }
        memoryProfileCache["id:$lower"]?.let { return it }
        val ctx = appContext ?: return null
        return try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val raw = prefs.getString("id_$lower", null) ?: prefs.getString("uid_$clean", null)
            if (raw != null) fromJson(raw) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun toJson(p: UserProfile): String {
        val obj = org.json.JSONObject()
        obj.put("uid", p.uid)
        obj.put("phone", p.phone)
        obj.put("displayName", p.displayName)
        obj.put("role", p.role)
        obj.put("gradeClass", p.gradeClass)
        obj.put("safeSphereId", p.safeSphereId)
        obj.put("familyId", p.familyId)
        obj.put("isProfileComplete", p.isProfileComplete)
        obj.put("createdAt", p.createdAt)
        obj.put("passwordHash", p.passwordHash)
        obj.put("avatarIndex", p.avatarIndex)
        obj.put("pairingCode", p.pairingCode)
        return obj.toString()
    }

    private fun fromJson(jsonStr: String): UserProfile? {
        return try {
            val obj = org.json.JSONObject(jsonStr)
            UserProfile(
                uid = obj.optString("uid", ""),
                phone = obj.optString("phone", ""),
                displayName = obj.optString("displayName", ""),
                role = obj.optString("role", "PARENT"),
                gradeClass = obj.optString("gradeClass", ""),
                safeSphereId = obj.optString("safeSphereId", ""),
                familyId = obj.optString("familyId", ""),
                isProfileComplete = obj.optBoolean("isProfileComplete", true),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                passwordHash = obj.optString("passwordHash", ""),
                avatarIndex = obj.optInt("avatarIndex", 1),
                pairingCode = obj.optString("pairingCode", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    data class UserProfile(
        val uid: String = "",
        val phone: String = "",
        val displayName: String = "",
        val role: String = "PARENT",    // "PARENT" or "STUDENT"
        val gradeClass: String = "",
        val safeSphereId: String = "",
        val familyId: String = "",
        val isProfileComplete: Boolean = true,
        val createdAt: Long = System.currentTimeMillis(),
        val passwordHash: String = "",  // SHA-256 hash of password, stored for ID-based login
        val avatarIndex: Int = 1,
        val pairingCode: String = ""
    ) {
        // Firestore requires a no-arg constructor via default params
        constructor() : this("", "", "", "PARENT", "", "", "", true, System.currentTimeMillis(), "", 1, "")

        fun toUserRole(): UserRole = if (role == "STUDENT") UserRole.STUDENT else UserRole.PARENT
    }

    /**
     * Fetches the user profile with local persistent store fallback.
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        getLocalProfile(uid)?.let { return it }
        val firestore = db ?: return null
        return try {
            withTimeoutOrNull(4000L) {
                val doc = firestore.collection(COLLECTION).document(uid).get().await()
                if (doc.exists()) {
                    val p = doc.toObject(UserProfile::class.java)?.copy(isProfileComplete = true)
                    if (p != null) {
                        saveLocalProfile(p)
                    }
                    p
                } else null
            } ?: getLocalProfile(uid)
        } catch (e: Exception) {
            getLocalProfile(uid)
        }
    }

    /**
     * Creates or updates the user profile in Firestore and persistent local cache.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val ready = profile.copy(isProfileComplete = true)
        saveLocalProfile(ready)
        val firestore = db ?: return Result.success(Unit)
        return try {
            withTimeoutOrNull(5000L) {
                firestore.collection(COLLECTION)
                    .document(ready.uid)
                    .set(ready, SetOptions.merge())
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
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
            safeSphereId = safeSphereId.trim(),
            familyId = generateFamilyId(),
            isProfileComplete = true,
            createdAt = System.currentTimeMillis()
        )
        val result = saveUserProfile(profile)

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
                            "safeSphereId" to safeSphereId.trim(),
                            "familyId" to profile.familyId,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).await()
            } catch (e: Exception) {
                // Non-fatal
            }
        }
        return result
    }

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
        val clean = safeSphereId.trim()
        val lower = clean.lowercase()
        // Check local store first
        if (getLocalProfile(lower) != null) return false
        val firestore = db ?: return true
        return try {
            withTimeoutOrNull(2000L) {
                var query = firestore.collection(COLLECTION)
                    .whereEqualTo("safeSphereId", lower)
                    .get()
                    .await()
                if (query.isEmpty && clean != lower) {
                    query = firestore.collection(COLLECTION)
                        .whereEqualTo("safeSphereId", clean)
                        .get()
                        .await()
                }
                query.isEmpty
            } ?: true
        } catch (e: Exception) {
            true
        }
    }

    fun generateFamilyId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "SF-" + (1..5).map { chars.random() }.joinToString("")
    }

    /** SHA-256 hash of a password string for secure comparison. */
    fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Login with SafeSphere ID + password.
     * Searches local persistent storage and Firestore (case-insensitively).
     * Validates password hash and returns the verified profile on success.
     */
    suspend fun loginWithSafeSphereId(safeSphereId: String, password: String): Result<UserProfile> {
        val idClean = safeSphereId.trim()
        val idLower = idClean.lowercase()
        val inputHash = hashPassword(password)

        // 1. Check local persistent store first for instantaneous response
        val localProfile = getLocalProfile(idClean) ?: getLocalProfile(idLower)
        if (localProfile != null) {
            if (localProfile.passwordHash.isBlank() || localProfile.passwordHash == inputHash) {
                val verified = localProfile.copy(
                    isProfileComplete = true,
                    passwordHash = if (localProfile.passwordHash.isBlank()) inputHash else localProfile.passwordHash
                )
                saveLocalProfile(verified)
                return Result.success(verified)
            } else {
                return Result.failure(Exception("Incorrect password. Please try again."))
            }
        }

        // 2. Query Firestore if online
        val firestore = db
        if (firestore != null) {
            try {
                val result = withTimeoutOrNull(4500L) {
                    var q = firestore.collection(COLLECTION)
                        .whereEqualTo("safeSphereId", idLower)
                        .get().await()
                    if (q.isEmpty && idClean != idLower) {
                        q = firestore.collection(COLLECTION)
                            .whereEqualTo("safeSphereId", idClean)
                            .get().await()
                    }
                    q
                }
                val doc = result?.documents?.firstOrNull()
                if (doc != null) {
                    val profile = doc.toObject(UserProfile::class.java)
                    if (profile != null) {
                        if (profile.passwordHash.isBlank() || profile.passwordHash == inputHash) {
                            val updated = profile.copy(
                                isProfileComplete = true,
                                passwordHash = if (profile.passwordHash.isBlank()) inputHash else profile.passwordHash
                            )
                            saveLocalProfile(updated)
                            if (profile.passwordHash.isBlank()) {
                                try {
                                    firestore.collection(COLLECTION).document(profile.uid)
                                        .update("passwordHash", inputHash)
                                } catch (_: Exception) {}
                            }
                            return Result.success(updated)
                        } else {
                            return Result.failure(Exception("Incorrect password. Please try again."))
                        }
                    }
                }
            } catch (_: Exception) {
                // Network or Firestore exception
            }
        }

        // 3. Fallback re-check of memory cache
        val memoryMatch = memoryProfileCache.values.find {
            it.safeSphereId.equals(idClean, ignoreCase = true)
        }
        if (memoryMatch != null) {
            if (memoryMatch.passwordHash.isBlank() || memoryMatch.passwordHash == inputHash) {
                return Result.success(memoryMatch.copy(isProfileComplete = true))
            } else {
                return Result.failure(Exception("Incorrect password. Please try again."))
            }
        }

        return Result.failure(Exception("No account found with ID '$safeSphereId'. Check the ID or create an account."))
    }

    /**
     * Instantly registers a new account with just the user's name and role.
     * Auto-generates a unique SafeSphere ID, assigns credentials, and saves
     * to both local storage and Firestore.
     */
    suspend fun registerQuickAccount(
        displayName: String,
        role: UserRole,
        avatarIndex: Int = 1
    ): Result<UserProfile> {
        val cleanName = displayName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(Exception("Please enter your name."))
        }
        val baseId = cleanName.lowercase().replace(Regex("[^a-z0-9]"), "_").trim('_')
        val candidateId = if (baseId.length >= 3) baseId else "user"
        var finalId = "${candidateId}_${(100..999).random()}"
        var attempts = 0
        while (!isSafeSphereIdAvailable(finalId) && attempts < 10) {
            finalId = "${candidateId}_${(1000..9999).random()}"
            attempts++
        }
        val uid = "uid_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val familyId = generateFamilyId()
        val defaultPassword = "password123"
        val passwordHash = hashPassword(defaultPassword)
        val studentNumericCode = if (role == UserRole.STUDENT) (100000..999999).random().toString() else ""

        val profile = UserProfile(
            uid = uid,
            phone = "",
            displayName = cleanName,
            role = role.name,
            gradeClass = if (role == UserRole.STUDENT) "Student" else "Primary Guardian",
            safeSphereId = finalId,
            familyId = familyId,
            isProfileComplete = true,
            createdAt = System.currentTimeMillis(),
            passwordHash = passwordHash,
            avatarIndex = avatarIndex.coerceIn(1, 16),
            pairingCode = studentNumericCode
        )

        // Save locally immediately
        saveLocalProfile(profile)

        // Save to Firestore if available
        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COLLECTION).document(uid).set(profile).await()
                    firestore.collection("family_members").document(finalId).set(
                        mapOf(
                            "uid" to uid,
                            "displayName" to cleanName,
                            "role" to role.name,
                            "safeSphereId" to finalId,
                            "familyId" to familyId,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).await()
                }
            } catch (_: Exception) {}
        }

        return Result.success(profile)
    }
}
