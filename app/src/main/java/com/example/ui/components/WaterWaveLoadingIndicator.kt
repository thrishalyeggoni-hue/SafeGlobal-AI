package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Ultra-smooth, responsive water wave loading animation positioned near top center.
 * Reacts to data processing and API calls with fluid wave dynamics, particle ripples,
 * and clear, lag-free status messaging.
 */
@Composable
fun WaterWaveLoadingIndicator(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    label: String = "All Protections Armed",
    showBadge: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_wave_infinite_transition")

    // Forward undulating wave phase (speed accelerates during active loading)
    val waveDuration = if (isLoading) 1600 else 2800
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(waveDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_1"
    )

    // Counter undulating wave phase
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -(2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isLoading) 2200 else 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_2"
    )

    // Droplet vertical oscillation
    val dropletOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "droplet_bounce"
    )

    // Dynamic water level height based on loading status
    val animatedWaterLevel by animateFloatAsState(
        targetValue = if (isLoading) 0.52f else 0.38f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "animated_water_level"
    )

    Column(
        modifier = modifier
            .testTag("water_loading_animation_top_center")
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Adaptive water pill container
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .height(30.dp)
                .shadow(
                    elevation = if (isLoading) 4.dp else 2.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0x331652F0)
                )
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFE8F2FF))
                .border(
                    width = 1.dp,
                    color = if (isLoading) Color(0xFF60A5FA) else Color(0xFFBFDBFE),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated Canvas drawing layered liquid water waves
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(15.dp))
            ) {
                val width = size.width
                val height = size.height
                val baseWaterLevel = height * (1f - animatedWaterLevel)
                val amplitude = height * (if (isLoading) 0.25f else 0.14f)

                // Background soft cyan translucent wave
                val backPath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, baseWaterLevel)
                    var x = 0f
                    val step = 4f
                    while (x <= width) {
                        val y = baseWaterLevel + amplitude * sin((x / width) * 2 * Math.PI + wavePhase2).toFloat()
                        lineTo(x, y)
                        x += step
                    }
                    lineTo(width, height)
                    close()
                }
                drawPath(
                    path = backPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x6638BDF8),
                            Color(0x800284C7)
                        )
                    )
                )

                // Foreground vibrant fluid wave
                val frontPath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, baseWaterLevel)
                    var x = 0f
                    val step = 4f
                    while (x <= width) {
                        val y = baseWaterLevel + amplitude * sin((x / width) * 2 * Math.PI + wavePhase1).toFloat()
                        lineTo(x, y)
                        x += step
                    }
                    lineTo(width, height)
                    close()
                }
                drawPath(
                    path = frontPath,
                    brush = Brush.verticalGradient(
                        colors = if (isLoading) {
                            listOf(Color(0xCC00B0FF), Color(0xEE1652F0))
                        } else {
                            listOf(Color(0x9938BDF8), Color(0xCC0284C7))
                        }
                    )
                )

                // Subtle water crest light highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 2.dp.toPx(),
                    center = Offset(width * 0.25f, baseWaterLevel + dropletOffset)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = 1.5.dp.toPx(),
                    center = Offset(width * 0.72f, baseWaterLevel - dropletOffset)
                )
            }

            // Foreground Text & Water Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                if (isLoading) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Loading water droplet",
                        tint = Color(0xFF1652F0),
                        modifier = Modifier
                            .size(13.dp)
                            .scale(1f + dropletOffset * 0.05f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }

                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0C2340),
                    letterSpacing = 0.15.sp
                )
            }
        }
    }
}
