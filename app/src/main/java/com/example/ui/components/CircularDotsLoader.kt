package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reusable Circular Dots Loading spinner matching the exact SafeSphere loading design.
 * Consists of 12 circular dots distributed along an orbit with fading opacities,
 * spinning smoothly 360 degrees.
 */
@Composable
fun CircularDotsLoader(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    dotRadiusRatio: Float = 0.09f,
    primaryColor: Color = Color(0xFF1652F0)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_rotation_transition")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    Canvas(
        modifier = modifier
            .testTag("circular_dots_loader")
            .size(size)
            .graphicsLayer { rotationZ = rotationAngle }
    ) {
        val count = 12
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val orbitRadius = (this.size.width / 2f) * 0.76f
        val dotRadius = this.size.width * dotRadiusRatio

        for (i in 0 until count) {
            val angleDeg = (i * 360f / count)
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val dotX = center.x + (orbitRadius * cos(angleRad)).toFloat()
            val dotY = center.y + (orbitRadius * sin(angleRad)).toFloat()

            // Opacity gradually decreases counterclockwise
            val alpha = (1f - (i.toFloat() / count) * 0.85f).coerceIn(0.12f, 1f)

            drawCircle(
                color = primaryColor.copy(alpha = alpha),
                radius = dotRadius,
                center = Offset(dotX, dotY)
            )
        }
    }
}
