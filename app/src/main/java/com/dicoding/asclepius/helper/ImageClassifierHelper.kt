package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageClassifierHelper(private val context: Context) {

    private var interpreter: Interpreter? = initializeInterpreter()

    // Inisialisasi interpreter dengan model TFLite
    private fun initializeInterpreter(): Interpreter? {
        return Interpreter(setupImageClassifier())
    }

    private fun setupImageClassifier(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd("cancer_classification.tflite")
        FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
        }
    }

    fun classifyStaticImage(bitmap: Bitmap): FloatArray {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val output = Array(1) { FloatArray(2) }
        interpreter?.run(inputBuffer, output)
        return output[0]
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        return ByteBuffer.allocateDirect(4 * 224 * 224 * 3).apply {
            order(ByteOrder.nativeOrder())
            val intValues = IntArray(224 * 224)
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            intValues.forEach {
                putFloat(((it shr 16) and 0xFF) / 255.0f)
                putFloat(((it shr 8) and 0xFF) / 255.0f)
                putFloat((it and 0xFF) / 255.0f)
            }
        }
    }
}
