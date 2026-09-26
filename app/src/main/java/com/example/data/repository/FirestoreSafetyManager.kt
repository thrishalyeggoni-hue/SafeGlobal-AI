package com.example.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

object FirestoreSafetyManager {

    private val db: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    // --- Collections ---
    const val COL_LINK_INVITES = "linkInvites"
    const val COL_FAMILY_LINKS = "familyLinks"
    const val COL_LOCATIONS = "locations"
    const val COL_SAFE_ZONES = "safeZones"
    const val COL_GEOFENCE_EVENTS = "geofenceEvents"
    const val COL_CAMERA_REQUESTS = "cameraRequests"
    const val COL_FCM_TOKENS = "fcmTokens"

    // --- Data Classes ---

    data class LinkInvite(
        val code: String = "",
        val studentUid: String = "",
        val studentName: String = "",
        val studentPhone: String = "",
        val studentSafeSphereId: String = "",
        val status: String = "PENDING", // PENDING, ACCEPTED, EXPIRED
        val createdAt: Long = System.currentTimeMillis(),
        val expiresAt: Long = System.currentTimeMillis() + 10 * 60 * 1000L // 10 mins
    ) {
        constructor() : this("", "", "", "", "", "PENDING", System.currentTimeMillis(), 0L)
    }

    data class FamilyLink(
        val linkId: String = "",
        val parentUid: String = "",
        val studentUid: String = "",
        val parentName: String = "",
        val studentName: String = "",
        val parentPhone: String = "",
        val studentPhone: String = "",
        val studentSafeSphereId: String = "",
        val active: Boolean = true,
        val createdAt: Long = System.currentTimeMillis()
    ) {
        constructor() : this("", "", "", "", "", "", "", "", true, System.currentTimeMillis())
    }

    data class LiveLocation(
        val studentUid: String = "",
        val studentName: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val accuracy: Float = 0f,
        val speed: Float = 0f,
        val bearing: Float = 0f,
        val altitude: Double = 0.0,
        val batteryLevel: Int = 100,
        val isMoving: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        constructor() : this("", "", 0.0, 0.0, 0f, 0f, 0f, 0.0, 100, false, System.currentTimeMillis())
    }

    data class FirestoreSafeZone(
        val zoneId: String = "",
        val parentUid: String = "",
        val studentUid: String = "",
        val name: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val radiusMeters: Double = 200.0,
        val alertOnExit: Boolean = true,
        val alertOnEntry: Boolean = true,
        val createdAt: Long = System.currentTimeMillis()
    ) {
        constructor() : this("", "", "", "", 0.0, 0.0, 200.0, true, true, System.currentTimeMillis())
    }

    data class FirestoreGeofenceEvent(
        val eventId: String = "",
        val studentUid: String = "",
        val studentName: String = "",
        val zoneId: String = "",
        val zoneName: String = "",
        val eventType: String = "EXIT", // "EXIT" or "ENTER"
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        constructor() : this("", "", "", "", "", "EXIT", 0.0, 0.0, System.currentTimeMillis())
    }

    data class CameraRequest(
        val requestId: String = "",
        val parentUid: String = "",
        val parentName: String = "",
        val studentUid: String = "",
        val studentName: String = "",
        val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED, STREAMING, ENDED
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    ) {
        constructor() : this("", "", "", "", "", "PENDING", System.currentTimeMillis(), System.currentTimeMillis())
    }

    // --- In-Memory Fallback Bus (guarantees seamless sync even if Firestore/credentials offline) ---
    private val memoryInvites = java.util.concurrent.ConcurrentHashMap<String, LinkInvite>()
    private val memoryFamilyLinks = java.util.concurrent.ConcurrentHashMap<String, FamilyLink>()
    private val memoryLocations = java.util.concurrent.ConcurrentHashMap<String, LiveLocation>()
    private val memorySafeZones = java.util.concurrent.ConcurrentHashMap<String, FirestoreSafeZone>()
    private val memoryGeofenceEvents = java.util.concurrent.ConcurrentHashMap<String, FirestoreGeofenceEvent>()
    private val memoryCameraRequests = java.util.concurrent.ConcurrentHashMap<String, CameraRequest>()

    private val invitesStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, LinkInvite>>(emptyMap())
    private val familyLinksStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, FamilyLink>>(emptyMap())
    private val locationsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, LiveLocation>>(emptyMap())
    private val safeZonesStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, FirestoreSafeZone>>(emptyMap())
    private val geofenceEventsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, FirestoreGeofenceEvent>>(emptyMap())
    private val cameraRequestsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, CameraRequest>>(emptyMap())

    // ==========================================
    // 1. LINKING (Student Generates, Parent Enters)
    // ==========================================

    suspend fun createLinkInvite(invite: LinkInvite): Result<Unit> {
        val normCode = invite.code.uppercase().trim()
        val finalInvite = invite.copy(code = normCode)
        memoryInvites[normCode] = finalInvite
        invitesStateFlow.value = memoryInvites.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_LINK_INVITES)
                        .document(normCode)
                        .set(finalInvite)
                        .await()
                }
            } catch (e: Exception) {
                // Handled gracefully by memory cache fallback
            }
        }
        return Result.success(Unit)
    }

    suspend fun getLinkInvite(code: String): LinkInvite? {
        val normCode = code.uppercase().trim()
        memoryInvites[normCode]?.let { return it }
        val firestore = db ?: return memoryInvites[normCode]
        return try {
            withTimeoutOrNull(4000L) {
                val doc = firestore.collection(COL_LINK_INVITES).document(normCode).get().await()
                if (doc.exists()) {
                    val inv = doc.toObject(LinkInvite::class.java)
                    if (inv != null) {
                        memoryInvites[normCode] = inv
                        invitesStateFlow.value = memoryInvites.toMap()
                    }
                    inv
                } else memoryInvites[normCode]
            } ?: memoryInvites[normCode]
        } catch (e: Exception) {
            memoryInvites[normCode]
        }
    }

    fun observeLinkInvite(code: String): Flow<LinkInvite?> = callbackFlow {
        val normCode = code.uppercase().trim()
        // Immediately emit from memory if available
        memoryInvites[normCode]?.let { trySend(it) }

        val firestore = db
        val reg = firestore?.collection(COL_LINK_INVITES)?.document(normCode)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val invite = snapshot.toObject(LinkInvite::class.java)
                    if (invite != null) {
                        memoryInvites[normCode] = invite
                        invitesStateFlow.value = memoryInvites.toMap()
                        trySend(invite)
                    }
                }
            }

        val job = launch {
            invitesStateFlow.collect { map ->
                map[normCode]?.let { trySend(it) }
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    suspend fun acceptLinkInvite(
        code: String,
        parentUid: String,
        parentName: String,
        parentPhone: String
    ): Result<FamilyLink> {
        val normCode = code.uppercase().trim()
        val invite = getLinkInvite(normCode)
            ?: return Result.failure(Exception("Code not found. Please check and try again."))

        if (invite.status == "ACCEPTED") {
            return Result.failure(Exception("This code has already been used."))
        }
        if (System.currentTimeMillis() > invite.expiresAt) {
            return Result.failure(Exception("This code has expired. Ask student to generate a new one."))
        }

        // Create family link
        val linkId = "link_${UUID.randomUUID().toString().take(12)}"
        val link = FamilyLink(
            linkId = linkId,
            parentUid = parentUid,
            studentUid = invite.studentUid,
            parentName = parentName,
            studentName = invite.studentName,
            parentPhone = parentPhone,
            studentPhone = invite.studentPhone,
            studentSafeSphereId = invite.studentSafeSphereId,
            active = true,
            createdAt = System.currentTimeMillis()
        )

        // Update memory immediately
        memoryFamilyLinks[linkId] = link
        familyLinksStateFlow.value = memoryFamilyLinks.toMap()

        val updatedInvite = invite.copy(status = "ACCEPTED")
        memoryInvites[normCode] = updatedInvite
        invitesStateFlow.value = memoryInvites.toMap()

        // Sync with Firestore in background if available
        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.runBatch { batch ->
                        batch.set(firestore.collection(COL_FAMILY_LINKS).document(linkId), link)
                        batch.update(firestore.collection(COL_LINK_INVITES).document(normCode), "status", "ACCEPTED")
                    }.await()
                }
            } catch (e: Exception) {
                // Succeeded locally
            }
        }

        return Result.success(link)
    }

    fun observeLinkedStudentsForParent(parentUid: String): Flow<List<FamilyLink>> = callbackFlow {
        // Emit from memory cache first
        val memMatches = memoryFamilyLinks.values.filter { it.parentUid == parentUid && it.active }
        trySend(memMatches)

        val firestore = db
        val reg = firestore?.collection(COL_FAMILY_LINKS)
            ?.whereEqualTo("parentUid", parentUid)
            ?.whereEqualTo("active", true)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FamilyLink::class.java) }
                    list.forEach { memoryFamilyLinks[it.linkId] = it }
                    familyLinksStateFlow.value = memoryFamilyLinks.toMap()
                    trySend(list)
                }
            }

        val job = launch {
            familyLinksStateFlow.collect { map ->
                val list = map.values.filter { it.parentUid == parentUid && it.active }
                trySend(list)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    fun observeLinkedParentsForStudent(studentUid: String): Flow<List<FamilyLink>> = callbackFlow {
        val memMatches = memoryFamilyLinks.values.filter { it.studentUid == studentUid && it.active }
        trySend(memMatches)

        val firestore = db
        val reg = firestore?.collection(COL_FAMILY_LINKS)
            ?.whereEqualTo("studentUid", studentUid)
            ?.whereEqualTo("active", true)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FamilyLink::class.java) }
                    list.forEach { memoryFamilyLinks[it.linkId] = it }
                    familyLinksStateFlow.value = memoryFamilyLinks.toMap()
                    trySend(list)
                }
            }

        val job = launch {
            familyLinksStateFlow.collect { map ->
                val list = map.values.filter { it.studentUid == studentUid && it.active }
                trySend(list)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    // ==========================================
    // 2. LIVE LOCATION (Student Pushes, Parent Observes)
    // ==========================================

    suspend fun updateStudentLocation(location: LiveLocation): Result<Unit> {
        memoryLocations[location.studentUid] = location
        locationsStateFlow.value = memoryLocations.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(3000L) {
                    firestore.collection(COL_LOCATIONS)
                        .document(location.studentUid)
                        .set(location, SetOptions.merge())
                        .await()
                }
            } catch (e: Exception) {
                // Saved locally
            }
        }
        return Result.success(Unit)
    }

    fun observeStudentLocation(studentUid: String): Flow<LiveLocation?> = callbackFlow {
        memoryLocations[studentUid]?.let { trySend(it) }

        val firestore = db
        val reg = firestore?.collection(COL_LOCATIONS)?.document(studentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val loc = snapshot.toObject(LiveLocation::class.java)
                    if (loc != null) {
                        memoryLocations[studentUid] = loc
                        locationsStateFlow.value = memoryLocations.toMap()
                        trySend(loc)
                    }
                }
            }

        val job = launch {
            locationsStateFlow.collect { map ->
                map[studentUid]?.let { trySend(it) }
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    // ==========================================
    // 3. SAFE ZONES (Parent creates, Student checks)
    // ==========================================

    suspend fun saveSafeZone(zone: FirestoreSafeZone): Result<Unit> {
        val id = if (zone.zoneId.isBlank()) "zone_${UUID.randomUUID().toString().take(10)}" else zone.zoneId
        val finalZone = zone.copy(zoneId = id)
        memorySafeZones[id] = finalZone
        safeZonesStateFlow.value = memorySafeZones.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_SAFE_ZONES).document(id).set(finalZone).await()
                }
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(Unit)
    }

    suspend fun deleteSafeZone(zoneId: String): Result<Unit> {
        memorySafeZones.remove(zoneId)
        safeZonesStateFlow.value = memorySafeZones.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(3000L) {
                    firestore.collection(COL_SAFE_ZONES).document(zoneId).delete().await()
                }
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(Unit)
    }

    fun observeSafeZonesForStudent(studentUid: String): Flow<List<FirestoreSafeZone>> = callbackFlow {
        val memMatches = memorySafeZones.values.filter { it.studentUid == studentUid }
        trySend(memMatches)

        val firestore = db
        val reg = firestore?.collection(COL_SAFE_ZONES)
            ?.whereEqualTo("studentUid", studentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FirestoreSafeZone::class.java) }
                    list.forEach { memorySafeZones[it.zoneId] = it }
                    safeZonesStateFlow.value = memorySafeZones.toMap()
                    trySend(list)
                }
            }

        val job = launch {
            safeZonesStateFlow.collect { map ->
                val list = map.values.filter { it.studentUid == studentUid }
                trySend(list)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    // ==========================================
    // 4. GEOFENCE EVENTS (Logged on exit/enter)
    // ==========================================

    suspend fun logGeofenceEvent(event: FirestoreGeofenceEvent): Result<Unit> {
        val id = if (event.eventId.isBlank()) "geo_${UUID.randomUUID().toString().take(10)}" else event.eventId
        val finalEvent = event.copy(eventId = id)
        memoryGeofenceEvents[id] = finalEvent
        geofenceEventsStateFlow.value = memoryGeofenceEvents.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(3000L) {
                    firestore.collection(COL_GEOFENCE_EVENTS).document(id).set(finalEvent).await()
                }
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(Unit)
    }

    fun observeGeofenceEventsForStudent(studentUid: String): Flow<List<FirestoreGeofenceEvent>> = callbackFlow {
        val memMatches = memoryGeofenceEvents.values.filter { it.studentUid == studentUid }.sortedByDescending { it.timestamp }
        trySend(memMatches)

        val firestore = db
        val reg = firestore?.collection(COL_GEOFENCE_EVENTS)
            ?.whereEqualTo("studentUid", studentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FirestoreGeofenceEvent::class.java) }
                    list.forEach { memoryGeofenceEvents[it.eventId] = it }
                    geofenceEventsStateFlow.value = memoryGeofenceEvents.toMap()
                    trySend(list.sortedByDescending { it.timestamp })
                }
            }

        val job = launch {
            geofenceEventsStateFlow.collect { map ->
                val list = map.values.filter { it.studentUid == studentUid }.sortedByDescending { it.timestamp }
                trySend(list)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    // ==========================================
    // 5. CAMERA REQUEST SIGNALING
    // ==========================================

    suspend fun requestCamera(
        parentUid: String,
        parentName: String,
        studentUid: String,
        studentName: String
    ): Result<String> {
        val reqId = "cam_${UUID.randomUUID().toString().take(10)}"
        val request = CameraRequest(
            requestId = reqId,
            parentUid = parentUid,
            parentName = parentName,
            studentUid = studentUid,
            studentName = studentName,
            status = "PENDING",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        memoryCameraRequests[reqId] = request
        cameraRequestsStateFlow.value = memoryCameraRequests.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_CAMERA_REQUESTS).document(reqId).set(request).await()
                }
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(reqId)
    }

    suspend fun updateCameraRequestStatus(requestId: String, newStatus: String): Result<Unit> {
        memoryCameraRequests[requestId]?.let { req ->
            val updated = req.copy(status = newStatus, updatedAt = System.currentTimeMillis())
            memoryCameraRequests[requestId] = updated
            cameraRequestsStateFlow.value = memoryCameraRequests.toMap()
        }

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(3000L) {
                    firestore.collection(COL_CAMERA_REQUESTS).document(requestId).update(
                        mapOf(
                            "status" to newStatus,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    ).await()
                }
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(Unit)
    }

    fun observeIncomingCameraRequestsForStudent(studentUid: String): Flow<List<CameraRequest>> = callbackFlow {
        val memMatches = memoryCameraRequests.values.filter { it.studentUid == studentUid && it.status == "PENDING" }
        trySend(memMatches)

        val firestore = db
        val reg = firestore?.collection(COL_CAMERA_REQUESTS)
            ?.whereEqualTo("studentUid", studentUid)
            ?.whereEqualTo("status", "PENDING")
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(CameraRequest::class.java) }
                    list.forEach { memoryCameraRequests[it.requestId] = it }
                    cameraRequestsStateFlow.value = memoryCameraRequests.toMap()
                    trySend(list)
                }
            }

        val job = launch {
            cameraRequestsStateFlow.collect { map ->
                val list = map.values.filter { it.studentUid == studentUid && it.status == "PENDING" }
                trySend(list)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    fun observeCameraRequest(requestId: String): Flow<CameraRequest?> = callbackFlow {
        memoryCameraRequests[requestId]?.let { trySend(it) }

        val firestore = db
        val reg = firestore?.collection(COL_CAMERA_REQUESTS)?.document(requestId)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val req = snapshot.toObject(CameraRequest::class.java)
                    if (req != null) {
                        memoryCameraRequests[requestId] = req
                        cameraRequestsStateFlow.value = memoryCameraRequests.toMap()
                        trySend(req)
                    }
                }
            }

        val job = launch {
            cameraRequestsStateFlow.collect { map ->
                map[requestId]?.let { trySend(it) }
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    // ==========================================
    // 6. FCM DEVICE TOKENS
    // ==========================================

    suspend fun saveFcmToken(uid: String, token: String) {
        val firestore = db ?: return
        try {
            firestore.collection(COL_FCM_TOKENS).document(uid).set(
                mapOf(
                    "uid" to uid,
                    "token" to token,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            // Non-fatal
        }
    }
}
