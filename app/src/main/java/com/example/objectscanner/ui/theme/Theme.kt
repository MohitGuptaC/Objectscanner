package com.example.objectscanner.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define color schemes directly to avoid naming conflicts with the material3 functions
@Composable
private fun getAppLightColorScheme() = lightColorScheme(
    primary = AppColors.Blue,
    secondary = AppColors.Blue,
    tertiary = AppColors.Black,
    background = AppColors.Background,
    surface = AppColors.Background,
    onPrimary = AppColors.White,
    onSecondary = AppColors.White,
    onBackground = AppColors.Black,
    onSurface = AppColors.Black
)

@Composable
private fun getAppDarkColorScheme() = darkColorScheme(
    primary = AppColors.Blue,
    secondary = AppColors.Blue,
    tertiary = AppColors.White,
    background = AppColors.Black,
    surface = AppColors.Black,
    onPrimary = AppColors.Black,
    onSecondary = AppColors.Black,
    onBackground = AppColors.White,
    onSurface = AppColors.White
)

@Composable
fun ObjectScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getAppDarkColorScheme()
        else -> getAppLightColorScheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use the modern WindowCompat API approach for status bar
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetController = WindowCompat.getInsetsController(window, view)
            insetController.isAppearanceLightStatusBars = !darkTheme
            
            // For transparent status bar to use with edge-to-edge content
            // Setting color to transparent as system draws behind it
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
