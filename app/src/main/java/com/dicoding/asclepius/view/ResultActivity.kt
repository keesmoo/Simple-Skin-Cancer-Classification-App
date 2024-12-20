package com.dicoding.asclepius.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var classifier: ImageClassifierHelper
    private lateinit var viewModel: ResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater).also { setContentView(it.root) }

        classifier = ImageClassifierHelper(this)
        viewModel = ViewModelProvider(this).get(ResultViewModel::class.java)

        viewModel.imageUri = viewModel.imageUri ?: intent.getStringExtra("imageUri")?.let { Uri.parse(it) }

        viewModel.imageUri?.let { uri ->
            uriToBitmap(uri)?.let { bmp ->
                binding.resultImage.setImageBitmap(bmp)
                analyzeImage(bmp)
            }
        } ?: run {
            binding.resultText.text = getString(R.string.error_processing_image)
            showToast("Gagal memuat gambar")
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        try {
            val results = classifier.classifyStaticImage(bitmap)
            val (nonCancerProbability, cancerProbability) = results
            val threshold = 0.60
            val confidenceLevel = (cancerProbability * 100).toInt()

            val resultText = if (cancerProbability >= threshold) {
                "Cancer Detected with $confidenceLevel% Confidence Level"
            } else {
                "Non Cancer Detected with $confidenceLevel% Confidence Level"
            }

            binding.resultText.text = resultText
            Log.d("Prediction", "Prediction: $resultText")

        } catch (e: Exception) {
            Log.e("PredictionError", "Error during image analysis", e)
            binding.resultText.text = getString(R.string.error_processing_image)
            showToast("Terjadi kesalahan dalam memproses gambar. Silakan coba lagi.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
