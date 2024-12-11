package com.example.objectscanner

import android.content.Context
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter

class TFLiteModel(context: Context) {

    private var interpreter: Interpreter? = null

    init {
        // Load the model from assets
        val modelPath = "metadata.tflite"  // Make sure this matches the filename in assets
        val assetManager = context.assets

        // Open the model file and load it into a MappedByteBuffer
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())

        // Initialize the Interpreter with the MappedByteBuffer
        interpreter = Interpreter(modelBuffer)
    }

    // Function to use the model (for example, to classify an image)
    fun classify(input: ByteArray): FloatArray {
        val output = FloatArray(10)  // Adjust output size based on your model's output
        interpreter?.run(input, output)
        return output
    }

    // Cleanup resources
    fun close() {
        interpreter?.close()
    }
}
