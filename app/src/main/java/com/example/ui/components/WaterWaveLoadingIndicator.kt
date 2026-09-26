package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium, branded top-center loading and telemetry indicator inspired by top-tier
 * applications (Apple Dynamic Island, Uber Safety Shield, Stripe).
 * Completely replaces all previous water-themed elements with a sleek, hardware-accelerated
 * orbital radar, status beacon, and dynamic state transitions.
 */
@Composable
fun TopCenterBrandedLoadingIndicator(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    label: String = "All Protections Armed",
    showBadge: Boolean = true,
    onSyncClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "branded_loading_transition")

    // High-speed smooth rotating radar arc (900ms cycle)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )

    // Breathing pulse for security aura
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Shimmer bar glide offset (0f to 1f)
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_glide"
    )

    Box(
        modifier = modifier
            .testTag("top_center_branded_loading_indicator")
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow when active
        if (isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x3338BDF8),
                                Color(0x0038BDF8)
                            )
                        )
                    )
            )
        }

        // Main Sleek Capsule Pill
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .height(34.dp)
                .shadow(
                    elevation = if (isLoading) 6.dp else 2.dp,
                    shape = RoundedCornerShape(17.dp),
                    ambientColor = if (isLoading) Color(0x33000000) else Color(0x1A000000)
                )
                .clip(RoundedCornerShape(17.dp))
                .background(
                    if (isLoading) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                )
                .border(
                    width = 1.2.dp,
                    brush = if (isLoading) {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00D2FF),
                                Color(0xFF3B82F6),
                                Color(0xFF6366F1)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFE2E8F0),
                                Color(0xFFCBD5E1)
                            )
                        )
                    },
                    shape = RoundedCornerShape(17.dp)
                )
                .clickable(
                    enabled = onSyncClick != null,
                    onClick = { onSyncClick?.invoke() }
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading) {
                    // Precision Orbital Radar Arc Spinner
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val strokeWidth = 2.2.dp.toPx()
                        val arcRadius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // Subtle track
                        drawCircle(
                            color = Color(0x3338BDF8),
                            radius = arcRadius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )

                        // Active rotating arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF38BDF8),
                                    Color(0xFF00E5FF)
                                ),
                                center = center
                            ),
                            startAngle = rotationAngle,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                            size = Size(arcRadius * 2, arcRadius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                } else {
                    // Steady green security beacon with pulse ring
                    Box(
                        modifier = Modifier.size(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp * pulseScale)
                                .clip(CircleShape)
                                .background(Color(0x3310B981))
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                }

                // Crisp, high-contrast label
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLoading) Color(0xFFF8FAFC) else Color(0xFF1E293B),
                    letterSpacing = 0.2.sp
                )

                if (onSyncClick != null && !isLoading) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SYNC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1652F0)
                        )
                    }
                }
            }

            // Bottom micro-progress line shimmer during active loading
            if (isLoading) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(17.dp))
                ) {
                    val w = size.width
                    val h = size.height
                    val barWidth = w * 0.4f
                    val startX = (shimmerOffset * (w + barWidth)) - barWidth
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF00E5FF),
                                Color(0xFF38BDF8),
                                Color.Transparent
                            ),
                            startX = startX,
                            endX = startX + barWidth
                        ),
                        topLeft = Offset(0f, h - 2.dp.toPx()),
                        size = Size(w, 2.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Compatibility alias forwarding to [TopCenterBrandedLoadingIndicator],
 * ensuring zero breaking changes across screens while completely removing the water animation.
 */
@Composable
fun WaterWaveLoadingIndicator(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    label: String = "All Protections Armed",
    showBadge: Boolean = true
) {
    TopCenterBrandedLoadingIndicator(
        modifier = modifier,
        isLoading = isLoading,
        label = label,
        showBadge = showBadge
    )
}
