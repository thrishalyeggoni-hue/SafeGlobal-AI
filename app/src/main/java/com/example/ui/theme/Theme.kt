package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BaseDarkColorScheme = darkColorScheme(
    primary = AccentViolet,
    secondary = BrandBlue,
    tertiary = SafeEmerald,
    background = Color(0xFF10141D),
    surface = Color(0xFF121824),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

private val BaseLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandLightBlue,
    onPrimaryContainer = BrandNavy,
    secondary = AccentViolet,
    onSecondary = Color.White,
    tertiary = SafeEmerald,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = OnSurface,
    surface = SurfaceContainerLowest,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineColor,
    error = AlertRed,
    errorContainer = AlertRedBg,
    onError = Color.White,
    onErrorContainer = AlertRed
)

@Composable
fun SafeSphereTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryAccent: Color = BrandBlue,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        BaseDarkColorScheme.copy(primary = primaryAccent)
    } else {
        BaseLightColorScheme.copy(
            primary = primaryAccent,
            primaryContainer = primaryAccent.copy(alpha = 0.12f)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = SurfaceLight.toArgb()
                it.navigationBarColor = SurfaceLight.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
