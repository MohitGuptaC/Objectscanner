package com.example.objectscanner

import android.content.Context
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Interpreter.Options

/**
 * A class to handle TensorFlow Lite model operations.
 * This class loads a TFLite model from assets and provides methods to run inference.
 *
 * @property outputSize The size of the output array for model predictions
 * @property modelPath The path to the TFLite model file in assets
 * @property numThreads Number of threads to use for inference (default: 4)
 */
class TFLiteModel(
    context: Context,
    private val outputSize: Int = 15,  // Default output size, adjust based on your model
    private val modelPath: String = "metadata.tflite",  // Path to model in assets
    private val numThreads: Int = 4
) {
    companion object {
        private const val TAG = "TFLiteModel"
    }

    private var interpreter: Interpreter? = null
    private var isInitialized = false

    init {
        try {
            initializeInterpreter(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TFLite model: ${e.message}")
            throw RuntimeException("Failed to initialize TFLite model", e)
        }
    }

    private fun initializeInterpreter(context: Context) {
        try {
            // Load the model from assets
            val assetManager = context.assets

            // Open the model file and load it into a MappedByteBuffer
            val fileDescriptor = assetManager.openFd(modelPath)
            val inputStream = fileDescriptor.createInputStream()
            val fileChannel = inputStream.channel
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())

            // Configure interpreter options
            val options = Options().apply {
                setNumThreads(numThreads)
            }

            // Initialize the Interpreter with the MappedByteBuffer
            interpreter = Interpreter(modelBuffer, options)
            isInitialized = true
            Log.d(TAG, "TFLite model initialized successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Error loading model: ${e.message}")
            throw e
        }
    }

    /**
     * Runs inference on the input data using the loaded TFLite model.
     *
     * @param input The input data as a ByteArray
     * @return The model's output as a FloatArray of size [outputSize]
     * @throws IllegalStateException if the model is not initialized
     */
    fun classify(input: ByteArray): FloatArray {
        if (!isInitialized) {
            throw IllegalStateException("TFLite model is not initialized")
        }

        return try {
            val output = FloatArray(outputSize)
            interpreter?.run(input, output)
            output
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification: ${e.message}")
            throw RuntimeException("Error during classification", e)
        }
    }

    /**
     * Cleans up resources by closing the TFLite interpreter.
     */
    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            isInitialized = false
            Log.d(TAG, "TFLite model resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter: ${e.message}")
        }
    }

    /**
     * Checks if the model is initialized and ready for inference.
     */
    fun isModelReady(): Boolean = isInitialized
}
