package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SafeSphereEmblem
import com.example.ui.components.TopCenterBrandedLoadingIndicator
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeoutOrNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.96f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logo_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200)
        onTimeoutOrNext()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("splash_screen_root")
            .clickable { onTimeoutOrNext() }
    ) {
        // Branded loading animation near top center
        TopCenterBrandedLoadingIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp),
            isLoading = true,
            label = "Initializing SafeSphere"
        )

        // Center Branding Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                SafeSphereEmblem(size = 130.dp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Title and Brand
            Text(
                text = "SafeSphere",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F2444),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Family",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF122E57),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tagline
            Text(
                text = "Verify. Monitor. Protect.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4C5F7A)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onTimeoutOrNext,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1652F0)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("splash_continue_btn")
            ) {
                Text("Get Started", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Bottom Decorative Pastel Gradient Wave
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height

            // Wave 1: Soft green/cyan
            val wave1 = Path().apply {
                moveTo(0f, h * 0.4f)
                cubicTo(w * 0.25f, h * 0.7f, w * 0.5f, h * 0.15f, w * 0.75f, h * 0.45f)
                cubicTo(w * 0.88f, h * 0.6f, w * 0.95f, h * 0.5f, w, h * 0.4f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = wave1,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x66BBF7D0),
                        Color(0xB3E0F2FE),
                        Color(0x80BAE6FD)
                    )
                )
            )

            // Wave 2: Softer overlay
            val wave2 = Path().apply {
                moveTo(0f, h * 0.55f)
                cubicTo(w * 0.22f, h * 0.35f, w * 0.48f, h * 0.75f, w * 0.72f, h * 0.5f)
                cubicTo(w * 0.88f, h * 0.35f, w * 0.95f, h * 0.6f, w, h * 0.5f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = wave2,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x4DCCFBF1),
                        Color(0x99DBEAFE)
                    )
                )
            )
        }
    }
}
