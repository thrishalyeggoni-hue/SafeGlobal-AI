package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SafeSphereEmblem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val telemetryStatuses = listOf(
    "INITIALIZING QUANTUM ENCRYPTION...",
    "CONNECTING DEFENSE SATELLITE LINK...",
    "CALIBRATING FAMILY GEO-GRID...",
    "SYNCHRONIZING REAL-TIME TELEMETRY...",
    "ARMING EMERGENCY BEACON NETWORK...",
    "ALL SAFESPHERE PROTOCOLS READY"
)

/**
 * High-end cinematic loading screen featuring:
 * - Cosmic particle field & atmospheric radial glows
 * - Multi-layered rotating holographic radar rings
 * - Sweeping sonar pulse beam & expanding energy waves
 * - Floating SafeSphere emblem with breathing neon aura
 * - Real-time animated cybernetic telemetry readout
 * - Glowing futuristic progress bar with electric spark
 */
@Composable
fun LoadingScreen(
    onLoaded: () -> Unit,
    modifier: Modifier = Modifier,
    durationMs: Long = 3200L
) {
    // ── Progress State ──────────────────────────────────────────────────────────
    var progress by remember { mutableFloatStateOf(0f) }
    var currentStatusIndex by remember { mutableIntStateOf(0) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Smoothly animate progress 0 -> 100%
        launch {
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs.toInt(), easing = EaseInOutCubic)
            ) {
                progress = value
            }
            delay(150)
            onLoaded()
        }

        // Cycle telemetry messages
        launch {
            val stepTime = durationMs / (telemetryStatuses.size)
            for (i in telemetryStatuses.indices) {
                currentStatusIndex = i
                delay(stepTime)
            }
        }
    }

    // ── Continuous Loop Animations ──────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "cinematic_loading")

    // Slow clockwise rotation for outer telemetry ring (9000ms)
    val outerRingRot by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "outer_rot"
    )

    // Counter-clockwise rotation for middle gradient ring (4500ms)
    val middleRingRot by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing), RepeatMode.Restart),
        label = "middle_rot"
    )

    // Radar sweep line (2200ms)
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "radar_rot"
    )

    // Core breathing glow (1600ms)
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "core_pulse"
    )

    // Energy shockwave expansion (2000ms)
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "shockwave"
    )

    // Floating bobbing motion for the center emblem (2400ms)
    val verticalBob by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "bobbing"
    )

    // Cosmic stardust particles
    val particles = remember {
        List(36) {
            CinematicParticle(
                x = (10..90).random() / 100f,
                y = (10..90).random() / 100f,
                radius = (12..35).random() / 10f,
                speed = (15..45).random() / 10f,
                alpha = (30..85).random() / 100f,
                color = if (it % 3 == 0) Color(0xFF38BDF8) else if (it % 2 == 0) Color(0xFF818CF8) else Color(0xFFF8FAFC)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F264A), // Center deep cyber blue
                        Color(0xFF071326), // Mid space navy
                        Color(0xFF020712)  // Deep cosmic dark
                    ),
                    center = Offset.Unspecified,
                    radius = 1600f
                )
            )
            .testTag("cinematic_loading_screen")
    ) {
        // ── Canvas: Cosmic Particles, Atmospheric Glows & Radar Rings ───────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.40f

            // 1. Dual Atmospheric Glow Blooms
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.16f * corePulse), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = 260.dp.toPx()
                ),
                radius = 260.dp.toPx(),
                center = Offset(cx, cy)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4F46E5).copy(alpha = 0.22f * corePulse), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = 180.dp.toPx()
                ),
                radius = 180.dp.toPx(),
                center = Offset(cx, cy)
            )

            // 2. Cosmic Stardust Particles
            particles.forEach { p ->
                val py = (p.y + (wavePhase * 0.1f * p.speed)) % 1f
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * (0.6f + 0.4f * sin(wavePhase * 2 * PI.toFloat()))),
                    radius = p.radius.dp.toPx(),
                    center = Offset(p.x * w, py * h)
                )
            }

            // 3. Shockwave Sonic Waves (Expanding Rings)
            for (i in 0..1) {
                val waveFraction = (wavePhase + i * 0.5f) % 1f
                val ringRad = 60.dp.toPx() + waveFraction * 130.dp.toPx()
                val waveAlpha = ((1f - waveFraction) * 0.35f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = waveAlpha),
                    radius = ringRad,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }

            // 4. Outer Telemetry HUD Ring with Dashes & Orbiting Satellites
            val outerRadius = 142.dp.toPx()
            rotate(outerRingRot, pivot = Offset(cx, cy)) {
                // Dashed HUD Ring
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.28f),
                    radius = outerRadius,
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f, 8f, 16f))
                    )
                )

                // 4 Orbiting Satellite Nodes with bright glowing aura
                for (angleDeg in listOf(0f, 90f, 180f, 270f)) {
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val nodeX = cx + (outerRadius * cos(rad)).toFloat()
                    val nodeY = cy + (outerRadius * sin(rad)).toFloat()

                    // Glow aura
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                        radius = 6.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                    // Core point
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // 5. Middle Glowing Energy Sweep Arc
            val midRadius = 114.dp.toPx()
            rotate(middleRingRot, pivot = Offset(cx, cy)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0x00000000),
                            Color(0xFF3B82F6).copy(alpha = 0.3f),
                            Color(0xFF06B6D4).copy(alpha = 0.8f),
                            Color(0xFF00E5FF)
                        ),
                        center = Offset(cx, cy)
                    ),
                    startAngle = 0f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(cx - midRadius, cy - midRadius),
                    size = Size(midRadius * 2, midRadius * 2)
                )

                // Leading glowing head of the arc
                val leadAngleRad = Math.toRadians(260.0)
                val headX = cx + (midRadius * cos(leadAngleRad)).toFloat()
                val headY = cy + (midRadius * sin(leadAngleRad)).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                    radius = 9.dp.toPx(),
                    center = Offset(headX, headY)
                )
            }

            // 6. High-Tech Sonar Radar Sweep
            rotate(radarAngle, pivot = Offset(cx, cy)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00E5FF).copy(alpha = 0.18f),
                            Color(0xFF38BDF8).copy(alpha = 0.45f)
                        ),
                        center = Offset(cx, cy)
                    ),
                    startAngle = -45f,
                    sweepAngle = 45f,
                    useCenter = true,
                    topLeft = Offset(cx - midRadius, cy - midRadius),
                    size = Size(midRadius * 2, midRadius * 2)
                )
                // Radar beam line
                drawLine(
                    color = Color(0xFFE0F2FE),
                    start = Offset(cx, cy),
                    end = Offset(cx + midRadius, cy),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 7. High-tech HUD Cardinal Ticks (Top, Bottom, Left, Right)
            val tickLen = 10.dp.toPx()
            val tickRadius = 158.dp.toPx()
            drawLine(Color(0xFF38BDF8).copy(alpha = 0.5f), Offset(cx, cy - tickRadius), Offset(cx, cy - tickRadius - tickLen), 1.5.dp.toPx())
            drawLine(Color(0xFF38BDF8).copy(alpha = 0.5f), Offset(cx, cy + tickRadius), Offset(cx, cy + tickRadius + tickLen), 1.5.dp.toPx())
            drawLine(Color(0xFF38BDF8).copy(alpha = 0.5f), Offset(cx - tickRadius, cy), Offset(cx - tickRadius - tickLen, cy), 1.5.dp.toPx())
            drawLine(Color(0xFF38BDF8).copy(alpha = 0.5f), Offset(cx + tickRadius, cy), Offset(cx + tickRadius + tickLen, cy), 1.5.dp.toPx())
        }

        // ── Foreground UI Layer ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: App Branding & Status Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 52.dp)
            ) {
                // Cybernetic Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFF0F1E38).copy(alpha = 0.8f))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Text(
                            text = "SECURE PROTOCOL ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF67E8F9),
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }

            // Middle Section: Floating SafeSphere Emblem with breathing glow
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(corePulse),
                contentAlignment = Alignment.Center
            ) {
                // Glassmorphism pedestal backplate
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B1933).copy(alpha = 0.65f))
                        .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), CircleShape)
                )

                // SafeSphere Hero Emblem with vertical hover bobbing
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .offset(y = verticalBob.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SafeSphereEmblem(size = 94.dp)
                }
            }

            // Bottom Section: Telemetry, Progress Bar & Percentage
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 44.dp)
            ) {
                // Brand Title with Cinematic Letter Spacing
                Text(
                    text = "SAFESPHERE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "FAMILY DEFENSE & SAFETY GRID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.8.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Real-time Telemetry Status Text
                Text(
                    text = telemetryStatuses.getOrElse(currentStatusIndex) { "INITIALIZING..." },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(20.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Cinematic Glowing Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                ) {
                    // Filled glowing progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress.coerceIn(0.01f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2563EB),
                                        Color(0xFF06B6D4),
                                        Color(0xFF00E5FF)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Percentage readout & Satellite Sync Status
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYNCING...",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Optional Quick Skip button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onLoaded
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("loading_skip_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Enter SafeSphere",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class CinematicParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color
)
