package com.example.objectscanner.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.runtime.Composable
import com.example.objectscanner.R

// Resource-based colors that automatically adapt to theme changes
object AppColors {
    // Use these composable properties in Composable functions
    val Blue: Color @Composable get() = colorResource(R.color.blue)
    val White: Color @Composable get() = colorResource(R.color.white)
    val Black: Color @Composable get() = colorResource(R.color.black)
    val Background: Color @Composable get() = colorResource(R.color.backgroundcolor)
}
