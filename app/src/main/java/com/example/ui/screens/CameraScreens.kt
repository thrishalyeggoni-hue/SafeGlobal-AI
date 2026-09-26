package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.viewmodel.SafeSphereViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

// ══════════════════════════════════════════════════════════════════════
// 1. STUDENT CAMERA SHARE SCREEN (CameraX Preview + Live Frame Analyzer)
// ══════════════════════════════════════════════════════════════════════
@Composable
fun CameraShareScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activeSession by viewModel.activeCameraSession.collectAsState()
    val sessionId = activeSession?.sessionId.orEmpty()

    var lensFacing by remember {
        mutableIntStateOf(
            if (activeSession?.cameraFacing?.uppercase() == "FRONT") CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
        )
    }
    var sessionDurationSeconds by remember { mutableIntStateOf(0) }
    var framesStreamedCount by remember { mutableIntStateOf(0) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Sync remote lens flip requests from parent
    LaunchedEffect(activeSession?.cameraFacing) {
        val remoteFacing = activeSession?.cameraFacing?.uppercase()
        if (remoteFacing == "FRONT" && lensFacing != CameraSelector.LENS_FACING_FRONT) {
            lensFacing = CameraSelector.LENS_FACING_FRONT
        } else if (remoteFacing == "BACK" && lensFacing != CameraSelector.LENS_FACING_BACK) {
            lensFacing = CameraSelector.LENS_FACING_BACK
        }
    }

    // Timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            sessionDurationSeconds++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val infinite = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // CameraX Live Preview + Frame Analyzer
        key(lensFacing) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    var lastTransmittedTime = 0L

                    // Realtime image analyzer to encode camera frames and stream to parent
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val now = System.currentTimeMillis()
                        // Throttle frame streaming to ~5-8 fps (every 160ms) to ensure low latency and smooth delivery
                        if (now - lastTransmittedTime >= 160L && sessionId.isNotBlank()) {
                            try {
                                val bitmap = imageProxy.toBitmap()
                                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                                // Rotate bitmap if required
                                val finalBitmap = if (rotationDegrees != 0) {
                                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                } else {
                                    bitmap
                                }

                                // Downscale to 360x480 for fast lightweight transmission
                                val targetWidth = 360
                                val targetHeight = (360f * finalBitmap.height / finalBitmap.width).toInt().coerceAtLeast(240)
                                val scaledBitmap = Bitmap.createScaledBitmap(finalBitmap, targetWidth, targetHeight, true)

                                val stream = ByteArrayOutputStream()
                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 55, stream)
                                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

                                val facingStr = if (lensFacing == CameraSelector.LENS_FACING_FRONT) "FRONT" else "BACK"
                                viewModel.updateCameraFrame(sessionId, base64, facingStr)
                                lastTransmittedTime = now
                                framesStreamedCount++
                            } catch (e: Exception) {
                                // Ignore transient capture errors
                            }
                        }
                        imageProxy.close()
                    }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        // Fallback: bind preview only if analyzer fails
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (_: Exception) {}
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )
        }

        // Top Status Overlay: LIVE Badge + Timer + Flip Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pulsing LIVE indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.70f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .alpha(pulseAlpha)
                        )
                        val mins = sessionDurationSeconds / 60
                        val secs = sessionDurationSeconds % 60
                        Text(
                            text = "LIVE TO PARENT • ${String.format("%02d:%02d", mins, secs)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Camera Flip Button (Front / Back)
                IconButton(
                    onClick = {
                        val newFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                        lensFacing = newFacing
                        val facingStr = if (newFacing == CameraSelector.LENS_FACING_FRONT) "FRONT" else "BACK"
                        if (sessionId.isNotBlank()) {
                            viewModel.switchCameraFacing(sessionId, facingStr)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.70f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notice Banner indicating active lens
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val currentLensText = if (lensFacing == CameraSelector.LENS_FACING_FRONT) "Front Camera (Selfie/Face)" else "Back Camera (Surroundings)"
                Text(
                    text = "🔒 $currentLensText sharing with ${activeSession?.parentName ?: "Parent"}. You retain full privacy control.",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp
                )
            }
        }

        // Bottom Controls: End Sharing Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    if (sessionId.isNotBlank()) {
                        viewModel.endCameraSession(sessionId)
                    } else {
                        viewModel.navigateTo(ScreenDestination.STUDENT_DASHBOARD)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Camera Sharing", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// 2. PARENT CAMERA VIEW SCREEN (Live Real-time Camera Feed from Student)
// ══════════════════════════════════════════════════════════════════════
@Composable
fun CameraViewScreen(
    viewModel: SafeSphereViewModel,
    modifier: Modifier = Modifier
) {
    val activeSession by viewModel.activeCameraSession.collectAsState()
    val student = viewModel.selectedStudent.collectAsState().value

    val status = activeSession?.status ?: "PENDING"
    val isStreaming = status == "LIVE" || status == "ACCEPTED" || activeSession?.latestFrameBase64 != null
    val isEnded = status == "ENDED" || status == "DECLINED"
    val cameraFacing = activeSession?.cameraFacing?.uppercase() ?: "BACK"
    val sessionId = activeSession?.sessionId.orEmpty()

    val infinite = rememberInfiniteTransition(label = "pulse_live")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_live_alpha"
    )

    // Decode live base64 frame from child
    val liveBitmap = remember(activeSession?.latestFrameBase64) {
        val base64Str = activeSession?.latestFrameBase64
        if (base64Str.isNullOrBlank()) {
            null
        } else {
            try {
                val bytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        // Main Viewport: Live Image Stream or Placeholder State
        if (isStreaming && liveBitmap != null) {
            // Render Child's Live Camera Feed in Full Screen
            Image(
                bitmap = liveBitmap,
                contentDescription = "Child Live Camera Feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Loading / Waiting / Ended card centered
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when {
                            isEnded -> {
                                Icon(
                                    imageVector = Icons.Default.VideocamOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (status == "DECLINED") "Request Declined by Student" else "Camera Session Ended",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "The camera stream has ended. You can request access again anytime.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            isStreaming -> {
                                CircularProgressIndicator(
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.size(44.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "Connecting Live Video Feed...",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${student?.studentName ?: "Student"} accepted! Synchronizing encrypted frames...",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            else -> {
                                CircularProgressIndicator(
                                    color = Color(0xFF3B82F6),
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 3.5.dp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Request Sent to ${student?.studentName ?: "Student"}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "A consent prompt was delivered to the student's phone. Stream will begin once accepted.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Floating Control Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                IconButton(
                    onClick = {
                        if (sessionId.isNotBlank()) viewModel.endCameraSession(sessionId)
                        else viewModel.navigateTo(ScreenDestination.STUDENT_CONTROL_CENTER)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Live indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.70f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (liveBitmap != null) Color(0xFFEF4444) else Color(0xFFF59E0B))
                                .alpha(pulseAlpha)
                        )
                        Text(
                            text = if (liveBitmap != null) "LIVE • ${student?.studentName ?: "STUDENT"}" else "CONNECTING...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Flip Remote Camera Lens Button
                if (isStreaming) {
                    IconButton(
                        onClick = {
                            val nextFacing = if (cameraFacing == "FRONT") "BACK" else "FRONT"
                            viewModel.switchCameraFacing(sessionId, nextFacing)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Remote Lens",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lens and Telemetry Chip
            if (isStreaming) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.60f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Lens: ${if (cameraFacing == "FRONT") "Front Camera (Face)" else "Back Camera (Surroundings)"}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom Controls: Disconnect Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    if (sessionId.isNotBlank()) {
                        viewModel.endCameraSession(sessionId)
                    } else {
                        viewModel.navigateTo(ScreenDestination.STUDENT_CONTROL_CENTER)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (liveBitmap != null) Color(0xFFDC2626) else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = if (liveBitmap != null) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (liveBitmap != null) "Disconnect Live Stream" else "Return to Control Center",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
