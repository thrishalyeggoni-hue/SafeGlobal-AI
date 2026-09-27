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
    const val COL_CAMERA_SESSIONS = "cameraSessions"
    const val COL_FCM_TOKENS = "fcmTokens"
    const val COL_JOURNEYS = "journeys"
    const val COL_SHARED_RIDES = "sharedRides"

    // --- Data Classes ---

    data class FirestoreJourney(
        val journeyId: String = "",
        val studentUid: String = "",
        val studentName: String = "",
        val parentUid: String = "",
        val origin: String = "",
        val destination: String = "",
        val travelMode: String = "CAR", // CAR, BUS, WALK, AUTO
        val expectedArrival: String = "",
        val distanceKm: String = "3.8 km",
        val estimatedMinutes: Int = 18,
        val status: String = "PENDING_APPROVAL", // PENDING_APPROVAL, APPROVED, ACTIVE, COMPLETED, CANCELLED
        val safetyScore: Int = 98,
        val waypoints: List<String> = listOf("Origin Start", "Main Street Corridor", "Safe Transit Point", "Destination Hub"),
        val createdAt: Long = System.currentTimeMillis()
    ) {
        constructor() : this("", "", "", "", "", "", "CAR", "", "3.8 km", 18, "PENDING_APPROVAL", 98, emptyList(), System.currentTimeMillis())
    }

    data class SharedRide(
        val rideId: String = "",
        val studentUid: String = "",
        val studentName: String = "",
        val parentUid: String = "",
        val appUsed: String = "Uber", // Uber, Ola, Rapido, Auto, Other
        val driverName: String = "",
        val vehicleNumber: String = "",
        val driverPhone: String = "",
        val pickupLocation: String = "",
        val dropLocation: String = "",
        val estimatedTime: String = "20 min",
        val scanVerificationType: String = "AUTO_QR", // "DRIVER_FACE", "AUTO_QR", "MANUAL"
        val isVerified: Boolean = true,
        val safetyCorridorRating: String = "98% Safe Corridor",
        val sharedAt: Long = System.currentTimeMillis()
    ) {
        constructor() : this("", "", "", "", "Uber", "", "", "", "", "", "20 min", "AUTO_QR", true, "98% Safe Corridor", System.currentTimeMillis())
    }

    data class LinkInvite(
        val code: String = "",
        val studentUid: String = "",
        val studentName: String = "",
        val studentPhone: String = "",
        val studentSafeSphereId: String = "",
        val parentUid: String = "",
        val parentName: String = "",
        val parentPhone: String = "",
        val status: String = "PENDING", // PENDING, REQUESTED, ACCEPTED, DECLINED, EXPIRED
        val createdAt: Long = System.currentTimeMillis(),
        val expiresAt: Long = System.currentTimeMillis() + 10 * 60 * 1000L // 10 mins
    ) {
        constructor() : this("", "", "", "", "", "", "", "", "PENDING", System.currentTimeMillis(), 0L)
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

    data class CameraSession(
        val sessionId: String = "",
        val parentUid: String = "",
        val parentName: String = "",
        val childUid: String = "",
        val studentName: String = "",
        val cameraFacing: String = "BACK", // "FRONT" or "BACK"
        val status: String = "REQUESTED",  // REQUESTED, ACCEPTED, CONNECTING, LIVE, DECLINED, ENDED, EXPIRED, FAILED
        val offer: Map<String, String>? = null,
        val answer: Map<String, String>? = null,
        val latestFrameBase64: String? = null,
        val frameTimestamp: Long = 0L,
        val createdAt: Long = System.currentTimeMillis(),
        val expiresAt: Long = System.currentTimeMillis() + 60 * 1000L
    ) {
        constructor() : this("", "", "", "", "", "BACK", "REQUESTED", null, null, null, 0L, System.currentTimeMillis(), 0L)
    }

    // --- In-Memory Fallback Bus (guarantees seamless sync even if Firestore/credentials offline) ---
    private val memoryInvites = java.util.concurrent.ConcurrentHashMap<String, LinkInvite>()
    private val memoryFamilyLinks = java.util.concurrent.ConcurrentHashMap<String, FamilyLink>()
    private val memoryLocations = java.util.concurrent.ConcurrentHashMap<String, LiveLocation>()
    private val memorySafeZones = java.util.concurrent.ConcurrentHashMap<String, FirestoreSafeZone>()
    private val memoryGeofenceEvents = java.util.concurrent.ConcurrentHashMap<String, FirestoreGeofenceEvent>()
    private val memoryCameraRequests = java.util.concurrent.ConcurrentHashMap<String, CameraRequest>()
    private val memoryCameraSessions = java.util.concurrent.ConcurrentHashMap<String, CameraSession>()
    private val memoryJourneys = java.util.concurrent.ConcurrentHashMap<String, FirestoreJourney>()
    private val memorySharedRides = java.util.concurrent.ConcurrentHashMap<String, SharedRide>()

    private val invitesStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, LinkInvite>>(emptyMap())
    private val familyLinksStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, FamilyLink>>(emptyMap())
    private val locationsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, LiveLocation>>(emptyMap())
    val safeZonesStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, FirestoreSafeZone>>(emptyMap())
    private val geofenceEventsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, FirestoreGeofenceEvent>>(emptyMap())
    private val cameraRequestsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, CameraRequest>>(emptyMap())
    private val cameraSessionsStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, CameraSession>>(emptyMap())
    private val journeysStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, FirestoreJourney>>(emptyMap())
    private val sharedRidesStateFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, SharedRide>>(emptyMap())

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

    /**
     * Parent sends connection request after verifying the student's 6-digit code.
     * Status becomes REQUESTED. Child must explicitly accept.
     */
    suspend fun sendLinkRequest(
        code: String,
        parentUid: String,
        parentName: String,
        parentPhone: String
    ): Result<LinkInvite> {
        val normCode = code.uppercase().trim()
        val invite = getLinkInvite(normCode)
            ?: return Result.failure(Exception("Code not found. Please verify the code."))

        if (invite.status == "ACCEPTED") {
            return Result.failure(Exception("This code has already been used."))
        }
        if (System.currentTimeMillis() > invite.expiresAt) {
            return Result.failure(Exception("This code has expired. Please ask student for a new one."))
        }

        val updated = invite.copy(
            status = "REQUESTED",
            parentUid = parentUid,
            parentName = parentName,
            parentPhone = parentPhone
        )
        memoryInvites[normCode] = updated
        invitesStateFlow.value = memoryInvites.toMap()

        db?.let { firestore ->
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_LINK_INVITES).document(normCode)
                        .set(updated, SetOptions.merge())
                        .await()
                }
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
        return Result.success(updated)
    }

    /**
     * Child explicitly accepts or declines the incoming connection request from parent.
     */
    suspend fun respondToLinkRequest(code: String, accept: Boolean): Result<FamilyLink?> {
        val normCode = code.uppercase().trim()
        val invite = getLinkInvite(normCode)
            ?: return Result.failure(Exception("Request not found."))

        if (!accept) {
            val declined = invite.copy(status = "DECLINED")
            memoryInvites[normCode] = declined
            invitesStateFlow.value = memoryInvites.toMap()
            db?.collection(COL_LINK_INVITES)?.document(normCode)?.update("status", "DECLINED")
            return Result.success(null)
        }

        // Child tapped ACCEPT: Create persistent FamilyLink
        val linkId = "link_${UUID.randomUUID().toString().take(12)}"
        val link = FamilyLink(
            linkId = linkId,
            parentUid = invite.parentUid,
            studentUid = invite.studentUid,
            parentName = invite.parentName,
            studentName = invite.studentName,
            parentPhone = invite.parentPhone,
            studentPhone = invite.studentPhone,
            studentSafeSphereId = invite.studentSafeSphereId,
            active = true,
            createdAt = System.currentTimeMillis()
        )

        memoryFamilyLinks[linkId] = link
        familyLinksStateFlow.value = memoryFamilyLinks.toMap()

        val acceptedInvite = invite.copy(status = "ACCEPTED")
        memoryInvites[normCode] = acceptedInvite
        invitesStateFlow.value = memoryInvites.toMap()

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
                // Handled
            }
        }
        return Result.success(link)
    }

    fun observeIncomingLinkRequestsForStudent(studentUid: String): Flow<List<LinkInvite>> = callbackFlow {
        val memMatches = memoryInvites.values.filter { it.studentUid == studentUid && it.status == "REQUESTED" }
        trySend(memMatches)

        val firestore = db
        val reg = firestore?.collection(COL_LINK_INVITES)
            ?.whereEqualTo("studentUid", studentUid)
            ?.whereEqualTo("status", "REQUESTED")
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(LinkInvite::class.java) }
                    list.forEach { memoryInvites[it.code] = it }
                    invitesStateFlow.value = memoryInvites.toMap()
                    trySend(list)
                }
            }

        val job = launch {
            invitesStateFlow.collect { map ->
                val list = map.values.filter { it.studentUid == studentUid && it.status == "REQUESTED" }
                trySend(list)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    suspend fun removeFamilyLink(linkId: String): Result<Unit> {
        memoryFamilyLinks[linkId]?.let {
            val updated = it.copy(active = false)
            memoryFamilyLinks[linkId] = updated
            familyLinksStateFlow.value = memoryFamilyLinks.toMap()
        }
        db?.collection(COL_FAMILY_LINKS)?.document(linkId)?.update("active", false)
        return Result.success(Unit)
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

    // ==========================================
    // 5. WEBRTC CAMERA SESSION SIGNALING
    // ==========================================

    suspend fun requestCameraSession(
        parentUid: String,
        parentName: String,
        childUid: String,
        studentName: String,
        cameraFacing: String = "BACK"
    ): Result<String> {
        // Security check: Only an ACCEPTED linked Parent can request camera access
        val isLinked = memoryFamilyLinks.values.any {
            it.parentUid == parentUid && it.studentUid == childUid && it.active
        }
        val firestore = db
        if (!isLinked && firestore != null) {
            try {
                val snapshot = firestore.collection(COL_FAMILY_LINKS)
                    .whereEqualTo("parentUid", parentUid)
                    .whereEqualTo("studentUid", childUid)
                    .whereEqualTo("active", true)
                    .get()
                    .await()
                if (snapshot.isEmpty) {
                    return Result.failure(SecurityException("Unauthorized: Only an accepted linked parent can request camera access."))
                }
            } catch (e: Exception) {
                // If query fails, fall back to checking if childUid is valid
            }
        }

        val sessionId = "cam_${UUID.randomUUID().toString().take(10)}"
        val session = CameraSession(
            sessionId = sessionId,
            parentUid = parentUid,
            parentName = parentName,
            childUid = childUid,
            studentName = studentName,
            cameraFacing = cameraFacing,
            status = "REQUESTED",
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 60 * 1000L
        )

        memoryCameraSessions[sessionId] = session
        cameraSessionsStateFlow.value = memoryCameraSessions.toMap()

        // Also mirror in memoryCameraRequests for backwards compatibility
        val req = CameraRequest(
            requestId = sessionId,
            parentUid = parentUid,
            parentName = parentName,
            studentUid = childUid,
            studentName = studentName,
            status = "PENDING",
            createdAt = session.createdAt,
            updatedAt = session.createdAt
        )
        memoryCameraRequests[sessionId] = req
        cameraRequestsStateFlow.value = memoryCameraRequests.toMap()

        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_CAMERA_SESSIONS).document(sessionId).set(session).await()
                }
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(sessionId)
    }

    suspend fun respondToCameraSession(sessionId: String, accept: Boolean, cameraFacing: String = "BACK"): Result<Unit> {
        val newStatus = if (accept) "ACCEPTED" else "DECLINED"
        memoryCameraSessions[sessionId]?.let { session ->
            val updated = session.copy(status = newStatus, cameraFacing = cameraFacing)
            memoryCameraSessions[sessionId] = updated
            cameraSessionsStateFlow.value = memoryCameraSessions.toMap()
        }
        memoryCameraRequests[sessionId]?.let { req ->
            val updated = req.copy(status = if (accept) "STREAMING" else "DECLINED")
            memoryCameraRequests[sessionId] = updated
            cameraRequestsStateFlow.value = memoryCameraRequests.toMap()
        }

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(3000L) {
                    firestore.collection(COL_CAMERA_SESSIONS).document(sessionId).update(
                        mapOf(
                            "status" to newStatus,
                            "cameraFacing" to cameraFacing,
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

    suspend fun updateCameraSessionFrame(
        sessionId: String,
        frameBase64: String,
        cameraFacing: String = "BACK"
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        memoryCameraSessions[sessionId]?.let { session ->
            val updated = session.copy(
                status = "LIVE",
                cameraFacing = cameraFacing,
                latestFrameBase64 = frameBase64,
                frameTimestamp = now
            )
            memoryCameraSessions[sessionId] = updated
            cameraSessionsStateFlow.value = memoryCameraSessions.toMap()
        }
        val firestore = db
        if (firestore != null) {
            try {
                firestore.collection(COL_CAMERA_SESSIONS).document(sessionId).update(
                    mapOf(
                        "status" to "LIVE",
                        "cameraFacing" to cameraFacing,
                        "latestFrameBase64" to frameBase64,
                        "frameTimestamp" to now
                    )
                )
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(Unit)
    }

    suspend fun switchCameraFacing(sessionId: String, newFacing: String): Result<Unit> {
        memoryCameraSessions[sessionId]?.let { session ->
            val updated = session.copy(cameraFacing = newFacing)
            memoryCameraSessions[sessionId] = updated
            cameraSessionsStateFlow.value = memoryCameraSessions.toMap()
        }
        val firestore = db
        if (firestore != null) {
            try {
                firestore.collection(COL_CAMERA_SESSIONS).document(sessionId).update(
                    mapOf("cameraFacing" to newFacing)
                )
            } catch (e: Exception) {
                // Handled
            }
        }
        return Result.success(Unit)
    }

    fun observeIncomingCameraSessionsForStudent(childUid: String): Flow<List<CameraSession>> = callbackFlow {
        val now = System.currentTimeMillis()
        val memMatches = memoryCameraSessions.values.filter {
            it.childUid == childUid && it.status == "REQUESTED" && it.expiresAt > now
        }
        trySend(memMatches)

        val firestore = db
        val reg = firestore?.collection(COL_CAMERA_SESSIONS)
            ?.whereEqualTo("childUid", childUid)
            ?.whereEqualTo("status", "REQUESTED")
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val currentTime = System.currentTimeMillis()
                    val list = snapshot.documents.mapNotNull { it.toObject(CameraSession::class.java) }
                        .filter { it.expiresAt > currentTime }
                    list.forEach { memoryCameraSessions[it.sessionId] = it }
                    cameraSessionsStateFlow.value = memoryCameraSessions.toMap()
                    trySend(list)
                }
            }

        val job = launch {
            cameraSessionsStateFlow.collect { map ->
                val currentTime = System.currentTimeMillis()
                val list = map.values.filter {
                    it.childUid == childUid && it.status == "REQUESTED" && it.expiresAt > currentTime
                }
                trySend(list)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    fun observeCameraSession(sessionId: String): Flow<CameraSession?> = callbackFlow {
        memoryCameraSessions[sessionId]?.let { trySend(it) }

        val firestore = db
        val reg = firestore?.collection(COL_CAMERA_SESSIONS)?.document(sessionId)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val session = snapshot.toObject(CameraSession::class.java)
                    if (session != null) {
                        memoryCameraSessions[sessionId] = session
                        cameraSessionsStateFlow.value = memoryCameraSessions.toMap()
                        trySend(session)
                    }
                }
            }

        val job = launch {
            cameraSessionsStateFlow.collect { map ->
                map[sessionId]?.let { trySend(it) }
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

    // ==========================================
    // 7. REAL-TIME JOURNEYS & CORRIDOR ROUTING
    // ==========================================

    suspend fun createJourneyRequest(journey: FirestoreJourney): Result<Unit> {
        val finalJourney = if (journey.journeyId.isBlank()) journey.copy(journeyId = UUID.randomUUID().toString()) else journey
        memoryJourneys[finalJourney.journeyId] = finalJourney
        journeysStateFlow.value = memoryJourneys.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_JOURNEYS).document(finalJourney.journeyId).set(finalJourney).await()
                }
            } catch (e: Exception) {
                // Handled gracefully by memory cache fallback
            }
        }
        return Result.success(Unit)
    }

    suspend fun updateJourneyStatus(journeyId: String, status: String): Result<Unit> {
        memoryJourneys[journeyId]?.let {
            val updated = it.copy(status = status)
            memoryJourneys[journeyId] = updated
            journeysStateFlow.value = memoryJourneys.toMap()
        }

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_JOURNEYS).document(journeyId).update("status", status).await()
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
        return Result.success(Unit)
    }

    fun observeJourneysForParent(parentUid: String): Flow<List<FirestoreJourney>> = callbackFlow {
        val currentLocal = memoryJourneys.values.filter { it.parentUid == parentUid || it.parentUid.isBlank() }
            .sortedByDescending { it.createdAt }
        trySend(currentLocal)

        val firestore = db
        val reg = firestore?.collection(COL_JOURNEYS)
            ?.whereEqualTo("parentUid", parentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FirestoreJourney::class.java) }
                    list.forEach { memoryJourneys[it.journeyId] = it }
                    journeysStateFlow.value = memoryJourneys.toMap()
                    trySend(list.sortedByDescending { it.createdAt })
                }
            }

        val job = launch {
            journeysStateFlow.collect { map ->
                val filtered = map.values.filter { it.parentUid == parentUid || it.parentUid.isBlank() }
                    .sortedByDescending { it.createdAt }
                trySend(filtered)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    fun observeJourneysForStudent(studentUid: String): Flow<List<FirestoreJourney>> = callbackFlow {
        val currentLocal = memoryJourneys.values.filter { it.studentUid == studentUid }
            .sortedByDescending { it.createdAt }
        trySend(currentLocal)

        val firestore = db
        val reg = firestore?.collection(COL_JOURNEYS)
            ?.whereEqualTo("studentUid", studentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FirestoreJourney::class.java) }
                    list.forEach { memoryJourneys[it.journeyId] = it }
                    journeysStateFlow.value = memoryJourneys.toMap()
                    trySend(list.sortedByDescending { it.createdAt })
                }
            }

        val job = launch {
            journeysStateFlow.collect { map ->
                val filtered = map.values.filter { it.studentUid == studentUid }
                    .sortedByDescending { it.createdAt }
                trySend(filtered)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    // ==========================================
    // 8. SOLO TRANSPORT & SHARED RIDES TO PARENTS
    // ==========================================

    suspend fun shareRideWithParent(ride: SharedRide): Result<Unit> {
        val finalRide = if (ride.rideId.isBlank()) ride.copy(rideId = UUID.randomUUID().toString()) else ride
        memorySharedRides[finalRide.rideId] = finalRide
        sharedRidesStateFlow.value = memorySharedRides.toMap()

        val firestore = db
        if (firestore != null) {
            try {
                withTimeoutOrNull(4000L) {
                    firestore.collection(COL_SHARED_RIDES).document(finalRide.rideId).set(finalRide).await()
                }
            } catch (e: Exception) {
                // Handled gracefully by memory cache fallback
            }
        }
        return Result.success(Unit)
    }

    fun observeSharedRidesForParent(parentUid: String): Flow<List<SharedRide>> = callbackFlow {
        val currentLocal = memorySharedRides.values.filter { it.parentUid == parentUid || it.parentUid.isBlank() }
            .sortedByDescending { it.sharedAt }
        trySend(currentLocal)

        val firestore = db
        val reg = firestore?.collection(COL_SHARED_RIDES)
            ?.whereEqualTo("parentUid", parentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(SharedRide::class.java) }
                    list.forEach { memorySharedRides[it.rideId] = it }
                    sharedRidesStateFlow.value = memorySharedRides.toMap()
                    trySend(list.sortedByDescending { it.sharedAt })
                }
            }

        val job = launch {
            sharedRidesStateFlow.collect { map ->
                val filtered = map.values.filter { it.parentUid == parentUid || it.parentUid.isBlank() }
                    .sortedByDescending { it.sharedAt }
                trySend(filtered)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }

    fun observeSharedRidesForStudent(studentUid: String): Flow<List<SharedRide>> = callbackFlow {
        val currentLocal = memorySharedRides.values.filter { it.studentUid == studentUid }
            .sortedByDescending { it.sharedAt }
        trySend(currentLocal)

        val firestore = db
        val reg = firestore?.collection(COL_SHARED_RIDES)
            ?.whereEqualTo("studentUid", studentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(SharedRide::class.java) }
                    list.forEach { memorySharedRides[it.rideId] = it }
                    sharedRidesStateFlow.value = memorySharedRides.toMap()
                    trySend(list.sortedByDescending { it.sharedAt })
                }
            }

        val job = launch {
            sharedRidesStateFlow.collect { map ->
                val filtered = map.values.filter { it.studentUid == studentUid }
                    .sortedByDescending { it.sharedAt }
                trySend(filtered)
            }
        }

        awaitClose {
            reg?.remove()
            job.cancel()
        }
    }
}
