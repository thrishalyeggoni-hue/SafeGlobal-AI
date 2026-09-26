package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SafeSphereEmblem
import com.example.ui.viewmodel.AuthSessionState
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Splash animation phases ────────────────────────────────────────────────
private enum class SplashPhase {
    GLOW_BURST,     // 0-400ms  radial burst of light from centre
    LOGO_REVEAL,    // 400-900ms logo drops in with spring bounce
    RING_EXPAND,    // 700-1300ms orbital ring expands outward
    TEXT_SLIDE,     // 1000-1500ms name/tagline slides up
    PARTICLES,      // continuous floating particles
    DONE
}

@Composable
fun SplashScreen(
    sessionState: AuthSessionState = AuthSessionState.CHECKING_SESSION,
    onSessionResolved: (AuthSessionState) -> Unit = {},
    onTimeoutOrNext: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // ── State ──────────────────────────────────────────────────────────────
    var phase by remember { mutableStateOf(SplashPhase.GLOW_BURST) }
    var minAnimationDone by remember { mutableStateOf(false) }

    // Glow burst
    val glowRadius = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    // Logo
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }

    // Orbit ring
    val ringRadius = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }

    // Text
    val textOffsetY = remember { Animatable(60f) }
    val textAlpha = remember { Animatable(0f) }

    // Tagline (delayed further)
    val taglineAlpha = remember { Animatable(0f) }
    val taglineOffsetY = remember { Animatable(30f) }

    // CTA button
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(20f) }

    // Continuous infinite animations
    val infinite = rememberInfiniteTransition(label = "splash_infinite")

    val orbitalRotation by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbital_rot"
    )

    val pulseBeat by infinite.animateFloat(
        initialValue = 0.95f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logo_pulse"
    )

    val particlePhase by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "particles"
    )

    val shimmerSweep by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer_sweep"
    )

    // ── Orchestrate animation sequence ─────────────────────────────────────
    LaunchedEffect(Unit) {
        // Phase 1: Glow burst
        launch {
            glowAlpha.animateTo(0.7f, tween(350, easing = EaseOutCubic))
            glowRadius.animateTo(1f, tween(400, easing = EaseOutCubic))
            delay(100)
            glowAlpha.animateTo(0.15f, tween(500))
        }

        // Phase 2: Logo reveal with spring-bounce
        delay(280)
        phase = SplashPhase.LOGO_REVEAL
        launch {
            logoScale.animateTo(1.1f, tween(350, easing = EaseOutBack))
            logoScale.animateTo(0.97f, tween(150))
            logoScale.animateTo(1.0f, tween(100))
        }
        launch { logoAlpha.animateTo(1f, tween(300)) }

        // Phase 3: Orbit ring expansion
        delay(200)
        phase = SplashPhase.RING_EXPAND
        launch {
            ringAlpha.animateTo(0.9f, tween(200))
            ringRadius.animateTo(1f, tween(500, easing = EaseOutCubic))
        }

        // Phase 4: Brand name slides up
        delay(350)
        phase = SplashPhase.TEXT_SLIDE
        launch {
            textOffsetY.animateTo(0f, tween(450, easing = EaseOutCubic))
            textAlpha.animateTo(1f, tween(400))
        }

        // Tagline
        delay(200)
        launch {
            taglineOffsetY.animateTo(0f, tween(380, easing = EaseOutCubic))
            taglineAlpha.animateTo(1f, tween(360))
        }

        // CTA button
        delay(200)
        launch {
            ctaOffsetY.animateTo(0f, tween(350, easing = EaseOutCubic))
            ctaAlpha.animateTo(1f, tween(350))
        }

        phase = SplashPhase.PARTICLES

        delay(800)
        minAnimationDone = true
    }

    LaunchedEffect(minAnimationDone, sessionState) {
        if (minAnimationDone && sessionState != AuthSessionState.CHECKING_SESSION) {
            delay(1000)
            android.util.Log.d("SafeSphereNav", "SplashScreen auto-resolving: sessionState=$sessionState")
            onSessionResolved(sessionState)
        }
    }

    // ── Particle seed data (stable, computed once) ─────────────────────────
    val particles = remember {
        List(28) { i ->
            val angle = (i * 360f / 28f)
            val radius = 0.28f + (i % 5) * 0.075f
            val size = 2.5f + (i % 4) * 1.5f
            val speed = 0.6f + (i % 3) * 0.2f
            val hue = 200f + (i % 6) * 25f
            Triple(Pair(angle, radius), Pair(size, speed), hue)
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        Color(0xFF071020),
                        Color(0xFF04090E)
                    ),
                    radius = 1800f
                )
            )
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {

        // ── Layer 1: Background star field (Canvas) ────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Static star dots (deterministic positions from index)
            repeat(80) { i ->
                val sx = ((i * 137.5f) % w)
                val sy = ((i * 97.3f + i * i * 0.3f) % h)
                val sr = 0.5f + (i % 4) * 0.4f
                val sa = 0.2f + (i % 5) * 0.12f
                drawCircle(
                    color = Color.White.copy(alpha = sa),
                    radius = sr,
                    center = Offset(sx, sy)
                )
            }
        }

        // ── Layer 2: Radial glow burst ─────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = size.minDimension * 0.9f * glowRadius.value
            if (maxR > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E90FF).copy(alpha = glowAlpha.value),
                            Color(0xFF0055CC).copy(alpha = glowAlpha.value * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = maxR
                    ),
                    radius = maxR,
                    center = Offset(cx, cy)
                )
            }
        }

        // ── Layer 3: Floating particles ────────────────────────────────────
        if (phase == SplashPhase.PARTICLES || phase == SplashPhase.TEXT_SLIDE || phase == SplashPhase.RING_EXPAND) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                particles.forEachIndexed { idx, (pos, sz, hue) ->
                    val (baseAngle, baseRadius) = pos
                    val (pSize, speed) = sz
                    val t = (particlePhase * speed + idx * 0.13f) % 1f
                    val r = baseRadius * size.minDimension * (0.5f + t * 0.35f)
                    val angle = (baseAngle + t * 40f) * (Math.PI / 180f)
                    val px = cx + r * cos(angle).toFloat()
                    val py = cy + r * sin(angle).toFloat()
                    val pa = (1f - t) * 0.65f

                    // Hue-based color using HSL approximation
                    val pColor = when ((hue.toInt() / 30) % 6) {
                        0 -> Color(0xFF38BDF8) // sky blue
                        1 -> Color(0xFF818CF8) // indigo
                        2 -> Color(0xFF34D399) // emerald
                        3 -> Color(0xFFFBBF24) // amber
                        4 -> Color(0xFFF87171) // rose
                        else -> Color(0xFFA78BFA) // violet
                    }
                    drawCircle(
                        color = pColor.copy(alpha = pa),
                        radius = pSize * (0.8f + t * 0.4f),
                        center = Offset(px, py)
                    )
                }
            }
        }

        // ── Layer 4: Expanding orbit ring ──────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val targetR = size.minDimension * 0.36f
            val r = targetR * ringRadius.value
            if (r > 0f && ringAlpha.value > 0f) {
                // Main orbit ring
                rotate(degrees = orbitalRotation, pivot = Offset(cx, cy)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF38BDF8).copy(alpha = 0.2f),
                                Color(0xFF818CF8).copy(alpha = ringAlpha.value * 0.7f),
                                Color(0xFF38BDF8).copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy)
                        ),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Shimmer arc on ring
                rotate(degrees = shimmerSweep, pivot = Offset(cx, cy)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF00E5FF).copy(alpha = ringAlpha.value * 0.9f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy)
                        ),
                        startAngle = -20f,
                        sweepAngle = 60f,
                        useCenter = false,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(cx - r, cy - r),
                        size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                    )
                }

                // Orbital dots on the ring
                repeat(6) { i ->
                    val dotAngle = (orbitalRotation + i * 60f) * (Math.PI / 180.0)
                    val dotX = cx + r * cos(dotAngle).toFloat()
                    val dotY = cy + r * sin(dotAngle).toFloat()
                    val dotColor = if (i % 2 == 0) Color(0xFF38BDF8) else Color(0xFF818CF8)
                    drawCircle(
                        color = dotColor.copy(alpha = ringAlpha.value * 0.85f),
                        radius = 2.5.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }
            }
        }

        // ── Layer 5 & 6: Unified Centered Brand Presentation (Intro Pic + SafeSphere Text) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .alpha(textAlpha.value.coerceAtLeast(logoAlpha.value)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Intro Picture & Emblem Badge Container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value * pulseBeat)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Intro Pic with Emblem - purely transparent with no glow or background
                SafeSphereEmblem(size = 118.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SafeSphere Brand Text — perfectly aligned directly beneath the pic
            Box(modifier = Modifier.padding(top = textOffsetY.value.dp)) {
                Text(
                    text = "SafeSphere",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.padding(top = taglineOffsetY.value.dp)) {
                Text(
                    text = "Family",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(taglineAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tagline shimmer separator
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(1.dp)
                    .alpha(taglineAlpha.value * 0.6f)
            ) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF38BDF8).copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.padding(top = taglineOffsetY.value.dp)) {
                Text(
                    text = "Verify · Monitor · Protect",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(taglineAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Session loading indicator or Get Started
            Box(
                modifier = Modifier
                    .alpha(ctaAlpha.value)
                    .offset(y = ctaOffsetY.value.dp),
                contentAlignment = Alignment.Center
            ) {
                if (sessionState == AuthSessionState.CHECKING_SESSION) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF38BDF8),
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Checking your session...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else if (sessionState == AuthSessionState.NOT_AUTHENTICATED) {
                    androidx.compose.material3.Button(
                        onClick = { onSessionResolved(sessionState) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D61F2)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("splash_continue_btn")
                    ) {
                        Text(
                            "Get Started",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }

        // ── Layer 7: Bottom accent gradient line ────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .alpha(taglineAlpha.value)
        ) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF1D61F2).copy(alpha = 0.8f),
                        Color(0xFF38BDF8).copy(alpha = 0.9f),
                        Color(0xFF818CF8).copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
        }

        // ── Layer 8: Version badge (subtle top-right corner) ───────────────
        Text(
            text = "v1.0",
            fontSize = 10.sp,
            color = Color(0xFF334155),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
                .alpha(taglineAlpha.value * 0.6f)
        )
    }
}
