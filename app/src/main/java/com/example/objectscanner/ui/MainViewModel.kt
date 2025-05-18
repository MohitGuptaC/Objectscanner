package com.example.objectscanner.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import com.example.objectscanner.R
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions

class MainViewModel : ViewModel() {

    var state by mutableStateOf(MainScreenState())
        private set

    companion object {
        private const val TAG = "MainViewModel"
        private const val MAX_IMAGE_SIZE = 1024
        private const val DEBUG = true
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
    }

    private val imageLabeler by lazy {
        try {
            val localModel = LocalModel.Builder()
                .setAssetFilePath("metadata.tflite")
                .build()

            val customOptions = CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .setMaxResultCount(3)
                .build()

            ImageLabeling.getClient(customOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up image labeler: ${e.message}")
            null
        }
    }

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ResetDetection -> resetDetection()
            is MainScreenEvent.NextIngredient -> prepareForNextIngredient()
            is MainScreenEvent.CaptureImage,
            is MainScreenEvent.RecommendRecipes -> {} // Handled in Activity
        }
    }

    fun handleImageResult(bitmap: Bitmap?, context: Context) {
        try {
            if (bitmap == null) {
                state = state.copy(
                    labelText = context.getString(R.string.unable_to_capture_image)
                )
                return
            }

            // Recycle current bitmap before assigning a new one
            state.currentBitmap?.recycle()

            val optimizedBitmap = optimizeBitmap(bitmap)
            if (optimizedBitmap != null) {
                state = state.copy(
                    currentBitmap = optimizedBitmap,
                    isProcessing = true
                )
                labelImage(optimizedBitmap, context)
            } else {
                state = state.copy(
                    labelText = context.getString(R.string.error_optimizing_image),
                    isProcessing = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling camera result: ${e.message}")
            state = state.copy(
                labelText = context.getString(R.string.error_processing_image),
                isProcessing = false
            )
        }
    }

    private fun optimizeBitmap(bitmap: Bitmap): Bitmap? {
        return try {
            if (bitmap.width <= MAX_IMAGE_SIZE && bitmap.height <= MAX_IMAGE_SIZE &&
                bitmap.config == BITMAP_CONFIG
            ) {
                return bitmap
            }

            val scaleX = MAX_IMAGE_SIZE.toFloat() / bitmap.width
            val scaleY = MAX_IMAGE_SIZE.toFloat() / bitmap.height
            val scale = scaleX.coerceAtMost(scaleY)

            val scaledWidth = (bitmap.width * scale).toInt()
            val scaledHeight = (bitmap.height * scale).toInt()

            val scaledBitmap = bitmap.scale(scaledWidth, scaledHeight)

            val finalBitmap = if (scaledBitmap.config != BITMAP_CONFIG) {
                scaledBitmap.copy(BITMAP_CONFIG, false)
            } else {
                scaledBitmap
            }

            if (bitmap != finalBitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }

            if (scaledBitmap != finalBitmap && !scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }

            finalBitmap

        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing bitmap: ${e.message}")
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            null
        }
    }

    private fun labelImage(bitmap: Bitmap, context: Context) {
        val currentImageLabeler = imageLabeler
        if (currentImageLabeler == null) {
            state = state.copy(
                labelText = context.getString(R.string.image_recognition_not_initialized),
                isProcessing = false
            )
            return
        }

        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            state = state.copy(
                labelText = context.getString(R.string.processing_image)
            )

            currentImageLabeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    handleLabelSuccess(labels, context)
                }
                .addOnFailureListener { e ->
                    handleLabelFailure(e, context)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error labeling image: ${e.message}")
            state = state.copy(
                labelText = context.getString(R.string.error_processing_image),
                isProcessing = false
            )
        }
    }

    private fun handleLabelSuccess(
        labels: List<com.google.mlkit.vision.label.ImageLabel>,
        context: Context
    ) {
        if (DEBUG) {
            labels.take(3).forEach { label ->
                Log.d(TAG, "Detected Label: '${label.text}', Confidence: ${label.confidence}")
            }
        }

        if (labels.isNotEmpty()) {
            val ingredient = labels[0].text.trim().lowercase()
            val updatedIngredients = state.detectedIngredients.toMutableList()

            if (!updatedIngredients.contains(ingredient)) {
                updatedIngredients.add(ingredient)
            }

            state = state.copy(
                labelText = context.getString(R.string.detected_label, ingredient),
                detectedIngredients = updatedIngredients,
                isProcessing = false
            )
        } else {
            state = state.copy(
                labelText = context.getString(R.string.no_ingredient_detected),
                isProcessing = false
            )
        }
    }

    private fun handleLabelFailure(e: Exception, context: Context) {
        Log.e(TAG, "Error in image labeling: ${e.message}")
        state = state.copy(
            labelText = context.getString(R.string.error_processing_image),
            isProcessing = false
        )
    }

    private fun resetDetection() {
        state.currentBitmap?.recycle()
        state = MainScreenState()
    }

    private fun prepareForNextIngredient() {
        state.currentBitmap?.recycle()
        state = state.copy(
            currentBitmap = null,
            labelText = ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        state.currentBitmap?.recycle()
        imageLabeler?.close()
    }
}