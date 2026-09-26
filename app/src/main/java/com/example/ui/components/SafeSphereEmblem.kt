package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun SafeSphereEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // High-fidelity vector fallback drawn directly on Canvas
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // Top petal - Yellow (#F59E0B / #FBBF24)
            drawPath(
                path = Path().apply {
                    moveTo(cx, cy * 0.15f)
                    cubicTo(cx + w * 0.22f, cy * 0.45f, cx + w * 0.18f, cy * 0.7f, cx, cy * 0.65f)
                    cubicTo(cx - w * 0.18f, cy * 0.7f, cx - w * 0.22f, cy * 0.45f, cx, cy * 0.15f)
                    close()
                },
                color = Color(0xFFFBBF24)
            )

            // Right petal - Coral (#EF4444 / #F87171)
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.88f, cy * 0.55f)
                    cubicTo(cx + w * 0.2f, cy + h * 0.25f, cx + w * 0.05f, cy + h * 0.1f, cx + w * 0.15f, cy)
                    cubicTo(cx + w * 0.25f, cy - h * 0.2f, cx + w * 0.45f, cy * 0.2f, w * 0.88f, cy * 0.55f)
                    close()
                },
                color = Color(0xFFF87171)
            )

            // Bottom Right petal - Sky Blue (#0EA5E9)
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.75f, h * 0.92f)
                    cubicTo(cx + w * 0.05f, cy + h * 0.4f, cx, cy + h * 0.15f, cx + w * 0.1f, cy + h * 0.15f)
                    cubicTo(cx + w * 0.25f, cy + h * 0.2f, cx + w * 0.45f, cy + h * 0.45f, w * 0.75f, h * 0.92f)
                    close()
                },
                color = Color(0xFF0EA5E9)
            )

            // Bottom Left petal - Vibrant Blue (#2563EB)
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.25f, h * 0.92f)
                    cubicTo(cx - w * 0.45f, cy + h * 0.45f, cx - w * 0.25f, cy + h * 0.2f, cx - w * 0.1f, cy + h * 0.15f)
                    cubicTo(cx, cy + h * 0.15f, cx - w * 0.05f, cy + h * 0.4f, w * 0.25f, h * 0.92f)
                    close()
                },
                color = Color(0xFF2563EB)
            )

            // Left petal - Emerald Green (#10B981)
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.12f, cy * 0.55f)
                    cubicTo(cx - w * 0.45f, cy * 0.2f, cx - w * 0.25f, cy - h * 0.2f, cx - w * 0.15f, cy)
                    cubicTo(cx - w * 0.05f, cy + h * 0.1f, cx - w * 0.2f, cy + h * 0.25f, w * 0.12f, cy * 0.55f)
                    close()
                },
                color = Color(0xFF10B981)
            )

            // Center Human Silhouette (Head)
            drawCircle(
                color = Color.White,
                radius = w * 0.08f,
                center = Offset(cx, cy * 0.72f)
            )

            // Center Human Silhouette (Body & Open Arms)
            drawPath(
                path = Path().apply {
                    moveTo(cx, cy * 0.88f)
                    cubicTo(cx + w * 0.2f, cy * 0.82f, cx + w * 0.28f, cy * 0.52f, cx + w * 0.28f, cy * 0.52f)
                    cubicTo(cx + w * 0.15f, cy * 0.72f, cx + w * 0.08f, cy * 0.95f, cx + w * 0.05f, h * 0.78f)
                    lineTo(cx + w * 0.04f, h * 0.86f)
                    lineTo(cx - w * 0.04f, h * 0.86f)
                    lineTo(cx - w * 0.05f, h * 0.78f)
                    cubicTo(cx - w * 0.08f, cy * 0.95f, cx - w * 0.15f, cy * 0.72f, cx - w * 0.28f, cy * 0.52f)
                    cubicTo(cx - w * 0.28f, cy * 0.52f, cx - w * 0.2f, cy * 0.82f, cx, cy * 0.88f)
                    close()
                },
                color = Color.White,
                style = Fill
            )
        }

        // Canvas-drawn vector logo is transparent — no background box.
        // (The remote AsyncImage was removed because its PNG had a white square background.)
    }
}
