package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.FirestoreSafetyManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var studentUid: String = ""
    private var studentName: String = ""
    private var lastKnownLocation: Location? = null

    // Cache of student's safe zones for local geofence evaluation
    private var activeSafeZones: List<FirestoreSafetyManager.FirestoreSafeZone> = emptyList()
    // Tracking previous presence in zones: zoneId -> Boolean (wasInside)
    private val zonePresenceMap = mutableMapOf<String, Boolean>()

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_STUDENT_UID = "EXTRA_STUDENT_UID"
        const val EXTRA_STUDENT_NAME = "EXTRA_STUDENT_NAME"

        private const val CHANNEL_ID = "safesphere_live_location_channel"
        private const val NOTIFICATION_ID = 4001
        private const val PREFS_NAME = "location_tracking_service_prefs"
        private const val PREF_KEY_STUDENT_UID = "pref_student_uid"
        private const val PREF_KEY_STUDENT_NAME = "pref_student_name"
        private const val PREF_KEY_IS_TRACKING = "pref_is_tracking"

        private val _isTrackingActive = MutableStateFlow(false)
        val isTrackingActive = _isTrackingActive.asStateFlow()

        private val _currentLocation = MutableStateFlow<Location?>(null)
        val currentLocation = _currentLocation.asStateFlow()

        fun startService(context: Context, studentUid: String, studentName: String) {
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                android.util.Log.w("LocationTrackingService", "Cannot start LocationTrackingService: location permission not granted")
                return
            }

            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_STUDENT_UID, studentUid)
                putExtra(EXTRA_STUDENT_NAME, studentName)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("LocationTrackingService", "Failed to start service", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, LocationTrackingService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.e("LocationTrackingService", "Failed to stop service", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onNewLocation(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        when (intent?.action) {
            ACTION_START -> {
                studentUid = intent.getStringExtra(EXTRA_STUDENT_UID) ?: ""
                studentName = intent.getStringExtra(EXTRA_STUDENT_NAME) ?: "Student"
                prefs.edit()
                    .putString(PREF_KEY_STUDENT_UID, studentUid)
                    .putString(PREF_KEY_STUDENT_NAME, studentName)
                    .putBoolean(PREF_KEY_IS_TRACKING, true)
                    .apply()
                startForegroundTracking()
                listenToSafeZones()
            }
            ACTION_STOP -> {
                prefs.edit().putBoolean(PREF_KEY_IS_TRACKING, false).apply()
                stopTracking()
                stopSelf()
            }
            else -> {
                // If restarted by Android system after process death (START_STICKY null intent)
                val wasTracking = prefs.getBoolean(PREF_KEY_IS_TRACKING, false)
                val savedUid = prefs.getString(PREF_KEY_STUDENT_UID, null)
                val savedName = prefs.getString(PREF_KEY_STUDENT_NAME, "Student") ?: "Student"
                val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (wasTracking && !savedUid.isNullOrBlank() && (hasFine || hasCoarse)) {
                    studentUid = savedUid
                    studentName = savedName
                    startForegroundTracking()
                    listenToSafeZones()
                } else {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeSphere Live Location",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live safety tracking status for parents"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SafeSphere Protection Active")
            .setContentText("Transmitting real-time location to authorized parents")
            .setSmallIcon(R.drawable.safesphere_watermark)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startForegroundTracking() {
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            android.util.Log.w("LocationTrackingService", "Cannot start location FGS: permissions not granted")
            stopSelf()
            return
        }

        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("LocationTrackingService", "Failed to start foreground service", e)
            stopSelf()
            return
        }

        _isTrackingActive.value = true

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 seconds interval
        ).apply {
            setMinUpdateIntervalMillis(3000L) // fastest 3 seconds
            setMinUpdateDistanceMeters(2f) // update on 2m movement
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            _isTrackingActive.value = false
        }
    }

    private fun listenToSafeZones() {
        if (studentUid.isBlank()) return
        serviceScope.launch {
            FirestoreSafetyManager.observeSafeZonesForStudent(studentUid).collect { zones ->
                activeSafeZones = zones
            }
        }
    }

    private fun onNewLocation(location: Location) {
        lastKnownLocation = location
        _currentLocation.value = location

        val batteryLevel = getBatteryLevel()
        val isMoving = (location.speed > 0.8f) // > 0.8 m/s (~3 km/h = walking)

        val liveLoc = FirestoreSafetyManager.LiveLocation(
            studentUid = studentUid,
            studentName = studentName,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            speed = location.speed,
            bearing = location.bearing,
            altitude = location.altitude,
            batteryLevel = batteryLevel,
            isMoving = isMoving,
            timestamp = System.currentTimeMillis()
        )

        serviceScope.launch {
            // 1. Sync live location to Firestore
            FirestoreSafetyManager.updateStudentLocation(liveLoc)

            // 2. Evaluate geofences
            evaluateGeofences(location)
        }
    }

    private suspend fun evaluateGeofences(currentLoc: Location) {
        if (activeSafeZones.isEmpty()) return

        for (zone in activeSafeZones) {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLoc.latitude,
                currentLoc.longitude,
                zone.latitude,
                zone.longitude,
                results
            )
            val distance = results[0]
            val isInside = distance <= zone.radiusMeters

            val wasInside = zonePresenceMap[zone.zoneId]

            if (wasInside != null) {
                if (wasInside && !isInside && zone.alertOnExit) {
                    // EXIT EVENT
                    val event = FirestoreSafetyManager.FirestoreGeofenceEvent(
                        studentUid = studentUid,
                        studentName = studentName,
                        zoneId = zone.zoneId,
                        zoneName = zone.name,
                        eventType = "EXIT",
                        latitude = currentLoc.latitude,
                        longitude = currentLoc.longitude,
                        timestamp = System.currentTimeMillis()
                    )
                    FirestoreSafetyManager.logGeofenceEvent(event)
                } else if (!wasInside && isInside && zone.alertOnEntry) {
                    // ENTER EVENT
                    val event = FirestoreSafetyManager.FirestoreGeofenceEvent(
                        studentUid = studentUid,
                        studentName = studentName,
                        zoneId = zone.zoneId,
                        zoneName = zone.name,
                        eventType = "ENTER",
                        latitude = currentLoc.latitude,
                        longitude = currentLoc.longitude,
                        timestamp = System.currentTimeMillis()
                    )
                    FirestoreSafetyManager.logGeofenceEvent(event)
                }
            }

            zonePresenceMap[zone.zoneId] = isInside
        }
    }

    private fun getBatteryLevel(): Int {
        val bm = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
    }

    private fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // Ignore
        }
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_KEY_IS_TRACKING, false).apply()
        _isTrackingActive.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
