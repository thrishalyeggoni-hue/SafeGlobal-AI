package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Top-center branded loading + status pill.
 *
 * LOADING STATE  → 12 circular dots arranged on a clock-face arc; each dot's
 *                  opacity trails from 1.0 (head) to 0.08 (tail), creating a
 *                  clean sweep-spinner matching the SafeSphere mockup design.
 *
 * IDLE STATE     → Steady green security beacon with a slow breathing pulse ring,
 *                  plus optional SYNC chip.
 *
 * Slides in/out from the top with fade so it never feels jarring.
 */
@Composable
fun TopCenterBrandedLoadingIndicator(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    label: String = "All Protections Armed",
    showBadge: Boolean = true,
    onSyncClick: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "safesphere_pill")

        // Full rotation: 0 → 360° over 1 200 ms (smooth, not jerky)
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "dot_rotation"
        )

        // Idle beacon breathing (scale 0.85 → 1.20)
        val beaconPulse by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.20f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "beacon_pulse"
        )

        // ── outer pill container ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .testTag("top_center_branded_loading_indicator")
                .shadow(
                    elevation = if (isLoading) 8.dp else 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = if (isLoading) Color(0x4400C6FF) else Color(0x1A000000)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isLoading) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                )
                .border(
                    width = 1.dp,
                    color = if (isLoading) Color(0x6600C6FF) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    enabled = onSyncClick != null && !isLoading,
                    onClick = { onSyncClick?.invoke() }
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ── LEFT INDICATOR ──────────────────────────────────────────
                if (isLoading) {
                    // Circular dot spinner (12 dots, clock-face sweep)
                    CircularDotSpinner(
                        rotationAngle = rotationAngle,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    // Idle green security beacon
                    Box(
                        modifier = Modifier.size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Breathing pulse ring
                        Box(
                            modifier = Modifier
                                .size((14 * beaconPulse).dp)
                                .clip(CircleShape)
                                .background(Color(0x2210B981))
                        )
                        // Solid core
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                }

                // ── LABEL ───────────────────────────────────────────────────
                Text(
                    text = if (isLoading) label else label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLoading) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                    letterSpacing = 0.2.sp,
                    maxLines = 1
                )

                // ── SYNC CHIP (idle only) ───────────────────────────────────
                if (onSyncClick != null && !isLoading && showBadge) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .clickable { onSyncClick.invoke() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SYNC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 12-dot circular sweep spinner.
 *
 * Each dot sits on the rim of the circle at [index * 30°] offset by [rotationAngle].
 * Opacity trails from 1.0 (head dot) down to ~0.08 (tail dot) — just like the
 * standard iOS/SafeSphere loading reference.
 *
 * @param rotationAngle  live animated angle (0..360) from the caller
 * @param dotCount       number of dots on the circle face (default 12)
 * @param dotColor       base colour of the dots (default SafeSphere sky-blue)
 */
@Composable
fun CircularDotSpinner(
    rotationAngle: Float,
    modifier: Modifier = Modifier,
    dotCount: Int = 12,
    dotColor: Color = Color(0xFF38BDF8)
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val dotRadius = radius * 0.165f          // ~16 % of half-size
        val orbitRadius = radius - dotRadius      // orbit rim

        repeat(dotCount) { index ->
            // angle for this dot, with the rotation offset applied
            val angleDeg = (index * (360f / dotCount) + rotationAngle) % 360f
            val angleRad = (angleDeg * PI / 180).toFloat()

            // opacity: head dot (last index) → 1.0, tail (index 0) → 0.08
            // We map index 0 = oldest (lowest alpha) → index dotCount-1 = newest (max alpha)
            val fraction = index.toFloat() / (dotCount - 1)   // 0 → 1
            val alpha = 0.08f + fraction * 0.92f              // 0.08 → 1.0

            val cx = center.x + orbitRadius * cos(angleRad)
            val cy = center.y + orbitRadius * sin(angleRad)

            drawCircle(
                color = dotColor.copy(alpha = alpha),
                radius = dotRadius,
                center = Offset(cx, cy)
            )
        }
    }
}
