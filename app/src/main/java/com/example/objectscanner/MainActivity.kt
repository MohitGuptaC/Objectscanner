package com.example.objectscanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import androidx.core.graphics.scale

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val MAX_IMAGE_SIZE = 1024 // Maximum dimension for image processing
        private const val DEBUG = true // Set this to false in production
    }

    private lateinit var objectImage: ImageView
    private lateinit var labelText: TextView
    private lateinit var captureImgBtn: Button
    private lateinit var resetBtn: Button
    private lateinit var recommendBtn: Button
    private lateinit var nextBtn: Button
    private var imageLabeler: com.google.mlkit.vision.label.ImageLabeler? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private val detectedIngredients = mutableListOf<String>()
    private var currentBitmap: Bitmap? = null
    private var currentIngredientIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupImageLabeler()
        setupCameraLauncher()
        setupClickListeners()
        checkCameraPermission()
        
        // Initialize button states
        updateButtonStates()
    }

    private fun initializeViews() {
        objectImage = findViewById(R.id.objectImage)
        labelText = findViewById(R.id.labelText)
        captureImgBtn = findViewById(R.id.captureImgBtn)
        resetBtn = findViewById(R.id.resetBtn)
        recommendBtn = findViewById(R.id.recommendBtn)
        nextBtn = findViewById(R.id.nextBtn)
    }

    private fun setupImageLabeler() {
        try {
            val localModel = LocalModel.Builder()
                .setAssetFilePath("metadata.tflite")
                .build()

            val customOptions = CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .build()

            imageLabeler = com.google.mlkit.vision.label.ImageLabeling.getClient(customOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up image labeler: ${e.message}")
            Toast.makeText(this, getString(R.string.error_initializing_recognition), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleCameraResult(result.data)
            } else {
                labelText.text = getString(R.string.image_capture_cancelled)
            }
        }
    }

    private fun updateButtonStates() {
        // Capture button: enabled when no image
        captureImgBtn.isEnabled = currentBitmap == null
        
        // Reset button: enabled when we have an image
        resetBtn.isEnabled = currentBitmap != null
        
        // Next button: enabled when we have an image
        nextBtn.isEnabled = currentBitmap != null
        
        // Recommend button: enabled when we have detected ingredients
        recommendBtn.isEnabled = detectedIngredients.isNotEmpty()
    }

    private fun handleCameraResult(data: Intent?) {
        try {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                val optimizedBitmap = optimizeBitmap(imageBitmap)
                if (optimizedBitmap != null) {
                    currentBitmap = optimizedBitmap
                    objectImage.setImageBitmap(currentBitmap)
                    labelImage(currentBitmap)
                    updateButtonStates()
                } else {
                    labelText.text = getString(R.string.error_optimizing_image)
                }
            } else {
                labelText.text = getString(R.string.unable_to_capture_image)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling camera result: ${e.message}")
            labelText.text = getString(R.string.error_processing_image)
        }
    }

    private fun optimizeBitmap(bitmap: Bitmap): Bitmap? {
        return try {
            // Calculate scale factor
            val scale = if (bitmap.width > MAX_IMAGE_SIZE || bitmap.height > MAX_IMAGE_SIZE) {
                val scaleX = MAX_IMAGE_SIZE.toFloat() / bitmap.width
                val scaleY = MAX_IMAGE_SIZE.toFloat() / bitmap.height
                scaleX.coerceAtMost(scaleY)
            } else {
                1f
            }

            if (scale < 1f) {
                bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt())
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error optimizing bitmap: ${e.message}")
            null
        }
    }

    private fun setupClickListeners() {
        captureImgBtn.setOnClickListener {
            launchCamera()
        }

        resetBtn.setOnClickListener {
            resetDetection()
        }

        recommendBtn.setOnClickListener {
            navigateToRecommendations()
        }

        nextBtn.setOnClickListener {
            labelText.text = getString(R.string.press_capture_to_scan_next)
            labelText.visibility = View.VISIBLE
            
            // Enable capture and disable next
            captureImgBtn.isEnabled = true
            nextBtn.isEnabled = false
        }
    }

    private fun launchCamera() {
        try {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(takePictureIntent)
            } else {
                Toast.makeText(this, getString(R.string.no_camera_app_found), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching camera: ${e.message}")
            Toast.makeText(this, getString(R.string.error_accessing_camera), Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetDetection() {
        detectedIngredients.clear()
        currentBitmap?.recycle()
        currentBitmap = null
        labelText.text = getString(R.string.reset_complete)
        objectImage.setImageDrawable(null)
        currentIngredientIndex = 0
        updateButtonStates()
    }

    private fun labelImage(bitmap: Bitmap?) {
        if (bitmap == null) {
            labelText.text = getString(R.string.no_image_to_process)
            return
        }

        if (imageLabeler == null) {
            labelText.text = getString(R.string.image_recognition_not_initialized)
            return
        }

        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            imageLabeler?.process(inputImage)
                ?.addOnSuccessListener { labels ->
                    handleLabelSuccess(labels)
                }
                ?.addOnFailureListener { e ->
                    handleLabelFailure(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error labeling image: ${e.message}")
            labelText.text = getString(R.string.error_processing_image)
        }
    }

    private fun handleLabelSuccess(labels: List<com.google.mlkit.vision.label.ImageLabel>) {
        if (DEBUG) {
            labels.forEach { label ->
                Log.d(TAG, "Detected Label: '${label.text}', Confidence: ${label.confidence}")
            }
        }

        if (labels.isNotEmpty()) {
            val ingredient = labels[0].text.trim().lowercase()
            detectedIngredients.add(ingredient)
            labelText.text = getString(R.string.detected_label, ingredient)
            labelText.visibility = View.VISIBLE
            updateButtonStates()
        } else {
            labelText.text = getString(R.string.no_ingredient_detected)
            updateButtonStates()
        }
    }

    private fun handleLabelFailure(e: Exception) {
        Log.e(TAG, "Error in image labeling: ${e.message}")
        labelText.text = getString(R.string.error_processing_image)
        updateButtonStates()
    }

    private fun navigateToRecommendations() {
        try {
            if (detectedIngredients.isEmpty()) {
                Toast.makeText(this, "No ingredients detected yet", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(this, RecipeRecommendationActivity::class.java)
            intent.putStringArrayListExtra("detectedIngredients", ArrayList(detectedIngredients))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error opening recommendations", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to recommendations: ${e.message}")
            Toast.makeText(this, "Error opening recommendations", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBitmap?.recycle()
        currentBitmap = null
    }
}