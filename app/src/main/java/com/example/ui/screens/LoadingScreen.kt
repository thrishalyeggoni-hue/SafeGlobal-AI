package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularDotsLoader
import com.example.ui.components.SafeSphereEmblem
import com.example.ui.components.WaterWaveLoadingIndicator
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2400)
        onLoaded()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("loading_screen_root")
            .clickable { onLoaded() }
    ) {
        // Water loading animation near top center
        WaterWaveLoadingIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp),
            label = "Connecting SafeSphere Hub"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Logo & Brand Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 96.dp)
            ) {
                SafeSphereEmblem(size = 96.dp)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SafeSphere",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F2444),
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Family",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF1A3860),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            // Center & Lower Section: Animated Circular Dot Spinner & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 90.dp)
            ) {
                CircularDotsLoader(
                    size = 64.dp,
                    primaryColor = Color(0xFF1652F0)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Your family's safety\nis our priority...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                    color = Color(0xFF1E3A61),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLoaded,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F0FE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("loading_continue_btn")
                ) {
                    Text("Skip to Notice", color = Color(0xFF1652F0), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
