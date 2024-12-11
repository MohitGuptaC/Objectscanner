package com.example.objectscanner

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity. result. contract. ActivityResultContracts
import androidx. activity. result. ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import android.content.res.AssetFileDescriptor
import android.provider.MediaStore

class MainActivity : AppCompatActivity() {

    private lateinit var objectImage: ImageView
    private lateinit var labelText: TextView
    private lateinit var captureImgBtn: Button
    private lateinit var tfliteInterpreter: Interpreter
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        objectImage = findViewById(R.id.objectImage)
        labelText = findViewById(R.id.labelText)
        captureImgBtn = findViewById(R.id.captureImgBtn)

        checkCameraPermission()

        // Load the TensorFlow Lite model
        tfliteInterpreter = Interpreter(loadModelFile())

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val extras = result.data?.extras
                val imageBitmap = extras?.getParcelable<Bitmap>("data")
                if (imageBitmap != null) {
                    objectImage.setImageBitmap(imageBitmap)
                    labelImage(imageBitmap)
                } else {
                    labelText.text = "Unable to capture image"
                }
            }
        }

        captureImgBtn.setOnClickListener {
            val clickPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (clickPicture.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(clickPicture)
            }
        }
    }

    // Load the .tflite model file from assets
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd("model.tflite")
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Process the captured image and run inference
    private fun labelImage(bitmap: Bitmap) {
        // Preprocess the image for inference (resize, normalize, etc.)
        val input = preprocessImage(bitmap)

        // Run inference
        val output = Array(1) { FloatArray(10) }  // Adjust output size according to your model
        tfliteInterpreter.run(input, output)

        // Display the result
        displayLabel(output)
    }

    // Preprocess image (resize, normalize, etc.) to match model input
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val width = 224  // Change based on your model input dimensions
        val height = 224
        val buffer = ByteBuffer.allocateDirect(4 * width * height * 3)
        buffer.rewind()

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = resizedBitmap.getPixel(x, y)
                buffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f) // Red
                buffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // Green
                buffer.putFloat((pixel and 0xFF) / 255.0f)           // Blue
            }
        }
        return buffer
    }

    // Display the label on the screen (using the most confident result)
    private fun displayLabel(output: Array<FloatArray>) {
        val result = output[0]
        val label = "Label: ${result[0]}" // Adjust based on the output format of your model
        labelText.text = label
    }

    // Check if the camera permission is granted
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
        }
    }
}
