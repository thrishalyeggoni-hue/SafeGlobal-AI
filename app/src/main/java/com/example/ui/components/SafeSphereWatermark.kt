package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset

/**
 * SafeSphere app-wide diagonal watermark.
 *
 * Renders:
 *  - A diagonal "SafeSphere Family" text stamp (centre of screen, -30°)
 *  - A subtle animated shimmer sweep across the diagonal stamp
 *  - A bottom-right corner branding badge
 *
 * Usage: wrap any screen content inside [SafeSphereWatermark] or overlay it
 * on top of the Scaffold content in MainActivity.
 */
@Composable
fun SafeSphereWatermark(
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "watermark_anim")

    // Slow shimmer sweep on the diagonal text
    val shimmerAlpha by infinite.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wm_shimmer"
    )

    // Corner badge pulse
    val badgePulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wm_badge_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("safesphere_watermark")
    ) {
        // ── Diagonal centre stamp ──────────────────────────────────────────
        Text(
            text = "SafeSphere Family",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1D61F2),
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .rotate(-30f)
                .alpha(shimmerAlpha)
        )

        // Sub-text below the main stamp
        Text(
            text = "Verify · Monitor · Protect",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1D61F2),
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .rotate(-30f)
                .alpha(shimmerAlpha * 0.7f)
                .padding(top = 36.dp)
        )

        // ── Bottom-right corner branding badge ─────────────────────────────
        Text(
            text = "SafeSphere\nFamily  ·  v1.0",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1D61F2),
            lineHeight = 13.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 70.dp)
                .alpha(badgePulse)
        )

        // ── Top-left tiny badge ────────────────────────────────────────────
        Text(
            text = "🛡 SafeSphere AI",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F2444),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 56.dp)
                .alpha(badgePulse * 0.65f)
        )
    }
}

/**
 * Convenience wrapper: places your [content] in a [Box] and overlays the
 * [SafeSphereWatermark] on top, so the watermark appears on every screen
 * without modifying individual screens.
 */
@Composable
fun WithSafeSphereWatermark(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
        SafeSphereWatermark()
    }
}
