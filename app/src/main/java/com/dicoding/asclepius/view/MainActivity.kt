package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        if (selectedImageUri != null) {
            showImage()
        } else {
            showToast("Gagal memilih gambar.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        selectedImageUri = savedInstanceState?.getString("selectedImageUri")?.let { Uri.parse(it) }

        selectedImageUri?.let { showImage() }

        with(binding) {
            galleryButton.setOnClickListener { startGallery() }
            analyzeButton.setOnClickListener { analyzeImage() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let { outState.putString("selectedImageUri", it.toString()) }
    }

    private fun startGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun showImage() {
        selectedImageUri?.let { binding.previewImageView.setImageURI(it) }
            ?: showToast("Tidak ada gambar untuk ditampilkan.")
    }

    private fun analyzeImage() {
        selectedImageUri?.let {
            moveToResult(it)
        } ?: showToast("Pilih gambar terlebih dahulu.")
    }

    private fun moveToResult(imageUri: Uri) {
        Intent(this, ResultActivity::class.java).apply {
            putExtra("imageUri", imageUri.toString())
            startActivity(this)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
