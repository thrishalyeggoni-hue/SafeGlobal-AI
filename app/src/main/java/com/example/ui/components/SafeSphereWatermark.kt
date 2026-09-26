package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

// Base watermark opacity (crisp transparent contour lines)
private const val BASE_WATERMARK_OPACITY = 0.22f

enum class WatermarkMode {
    NORMAL,
    TRACKING,
    JOURNEY,
    GEOFENCE_ALERT,
    OFFLINE
}

/**
 * Dynamic Topographic Watermark:
 * Uses the exact attached topographic contour pattern as the base and elevates it
 * with 4 signature reactive states:
 *
 * 1. Continuous: Topography drifts very slowly with micro-scale & parallax (5-12px over 25s).
 * 2. GPS Update: Soft pulse ripple travels outward through the contours from the beacon node.
 * 3. Journey: A tiny glowing location point moves along a faint route through the contours.
 * 4. Geofence Exit: An urgent alert wave propagates across the contours.
 * 5. Embedded status text reacting dynamically.
 *
 * Non-interactive: Touch events pass through to underlying UI.
 */
@Composable
fun SafeSphereWatermark(
    modifier: Modifier = Modifier,
    mode: WatermarkMode = WatermarkMode.NORMAL,
    gpsUpdateTrigger: Long = 0L,
    geofenceAlertTrigger: Long = 0L,
    statusOverride: String? = null
) {
    val infinite = rememberInfiniteTransition(label = "wm_infinite")

    // ① Continuous slow motion: 8-12px drift over 28s
    val driftX by infinite.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wm_drift_x"
    )

    val driftY by infinite.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wm_drift_y"
    )

    // Subtle breathing scale (100% -> 102.5% -> 100%)
    val breathingScale by infinite.animateFloat(
        initialValue = 1.00f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wm_scale"
    )

    // ③ Journey travel progress (0f to 1f)
    val journeyProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wm_journey_progress"
    )

    // ② GPS Update ripple state (triggers on new timestamp)
    var gpsRippleProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(gpsUpdateTrigger) {
        if (gpsUpdateTrigger > 0L) {
            val startTime = System.currentTimeMillis()
            val duration = 2000f
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= duration) {
                    gpsRippleProgress = 0f
                    break
                }
                gpsRippleProgress = elapsed / duration
                delay(16)
            }
        }
    }

    // ④ Geofence alert wave state
    var alertWaveProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(geofenceAlertTrigger) {
        if (geofenceAlertTrigger > 0L) {
            val startTime = System.currentTimeMillis()
            val duration = 2500f
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= duration) {
                    alertWaveProgress = 0f
                    break
                }
                alertWaveProgress = elapsed / duration
                delay(16)
            }
        }
    }

    val statusText = statusOverride ?: when (mode) {
        WatermarkMode.NORMAL -> "● PROTECTION ACTIVE"
        WatermarkMode.TRACKING -> "◎ LIVE GPS ACTIVE"
        WatermarkMode.JOURNEY -> "→ JOURNEY MONITORING"
        WatermarkMode.GEOFENCE_ALERT -> "⚠️ GEOFENCE EXITED"
        WatermarkMode.OFFLINE -> "○ STANDBY"
    }

    val statusColor = when (mode) {
        WatermarkMode.NORMAL -> Color(0xFF1D61F2)
        WatermarkMode.TRACKING -> Color(0xFF0D9488)
        WatermarkMode.JOURNEY -> Color(0xFF4F46E5)
        WatermarkMode.GEOFENCE_ALERT -> Color(0xFFDC2626)
        WatermarkMode.OFFLINE -> Color(0xFF64748B)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        // ── 1. BASE LAYER: Topographic contour image with slow drift & breathing scale ──
        Image(
            painter = painterResource(id = R.drawable.safesphere_watermark),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = driftX.dp, y = driftY.dp)
                .scale(breathingScale)
                .alpha(BASE_WATERMARK_OPACITY)
        )

        // ── 2. DYNAMIC CANVAS: Ripples, Route Point, and Beacon ────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Center of topographic beacon (located in a quiet natural contour saddle)
            val beaconCenter = Offset(w * 0.42f, h * 0.52f)


            // B. ② GPS Update Ripple through contours
            if (gpsRippleProgress > 0f) {
                val maxRadius = w * 0.75f
                val currentRadius = gpsRippleProgress * maxRadius
                val rippleAlpha = (1f - gpsRippleProgress) * 0.45f
                drawCircle(
                    color = Color(0xFF06B6D4).copy(alpha = rippleAlpha),
                    radius = currentRadius,
                    center = beaconCenter,
                    style = Stroke(width = 3.5f * (1f - gpsRippleProgress * 0.5f))
                )
                // Secondary trailing ripple
                if (gpsRippleProgress > 0.25f) {
                    val secondaryProgress = (gpsRippleProgress - 0.25f) / 0.75f
                    drawCircle(
                        color = Color(0xFF3B82F6).copy(alpha = (1f - secondaryProgress) * 0.30f),
                        radius = secondaryProgress * maxRadius * 0.75f,
                        center = beaconCenter,
                        style = Stroke(width = 2f)
                    )
                }
            }

            // C. ④ Geofence Exit Wave (Urgent crimson/amber propagation)
            if (alertWaveProgress > 0f) {
                val waveRadius = alertWaveProgress * w * 1.1f
                val waveAlpha = (1f - alertWaveProgress) * 0.65f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x00EF4444),
                            Color(0xFFEF4444).copy(alpha = waveAlpha),
                            Color(0x00DC2626)
                        ),
                        center = beaconCenter,
                        radius = waveRadius.coerceAtLeast(10f)
                    ),
                    radius = waveRadius,
                    center = beaconCenter
                )
                drawCircle(
                    color = Color(0xFFDC2626).copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = beaconCenter,
                    style = Stroke(width = 4f * (1f - alertWaveProgress))
                )
            }

            // D. ③ Journey Mode: Moving point along contour line
            if (mode == WatermarkMode.JOURNEY) {
                val routePath = Path().apply {
                    moveTo(w * 0.15f, h * 0.75f)
                    cubicTo(
                        w * 0.30f, h * 0.65f,
                        w * 0.55f, h * 0.58f,
                        w * 0.85f, h * 0.40f
                    )
                }

                // Faint route line
                drawPath(
                    path = routePath,
                    color = Color(0xFF6366F1).copy(alpha = 0.20f),
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )

                // Tiny glowing point moving along the route
                val pointX = (1 - journeyProgress) * (1 - journeyProgress) * (w * 0.15f) +
                        2 * (1 - journeyProgress) * journeyProgress * (w * 0.45f) +
                        journeyProgress * journeyProgress * (w * 0.85f)
                val pointY = (1 - journeyProgress) * (1 - journeyProgress) * (h * 0.75f) +
                        2 * (1 - journeyProgress) * journeyProgress * (h * 0.60f) +
                        journeyProgress * journeyProgress * (h * 0.40f)

                drawCircle(
                    color = Color(0xFF818CF8).copy(alpha = 0.35f),
                    radius = 9f,
                    center = Offset(pointX, pointY)
                )
                drawCircle(
                    color = Color(0xFF4F46E5),
                    radius = 4f,
                    center = Offset(pointX, pointY)
                )
            }
        }
    }
}

/**
 * High-level wrapper with dynamic watermark support
 */
@Composable
fun WithSafeSphereWatermark(
    modifier: Modifier = Modifier,
    mode: WatermarkMode = WatermarkMode.NORMAL,
    gpsUpdateTrigger: Long = 0L,
    geofenceAlertTrigger: Long = 0L,
    statusOverride: String? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Clean, consistent light canvas behind watermark
    ) {
        SafeSphereWatermark(
            mode = mode,
            gpsUpdateTrigger = gpsUpdateTrigger,
            geofenceAlertTrigger = geofenceAlertTrigger,
            statusOverride = statusOverride
        )
        content()
    }
}
