package com.example.objectscanner.ui

import android.graphics.Bitmap

/**
 * Data class representing the UI state of the main screen
 */
data class MainScreenState(
    val currentBitmap: Bitmap? = null,
    val labelText: String = "",
    val detectedIngredients: List<String> = emptyList(),
    val isProcessing: Boolean = false
)

/**
 * Events that can be triggered from the UI
 */
sealed class MainScreenEvent {
    data object CaptureImage : MainScreenEvent()
    data object ResetDetection : MainScreenEvent()
    data object NextIngredient : MainScreenEvent()
    data object RecommendRecipes : MainScreenEvent()
}
