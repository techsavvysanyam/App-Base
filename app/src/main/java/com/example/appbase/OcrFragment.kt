package com.example.appbase

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.appbase.databinding.FragmentOcrBinding
import com.example.appbase.utils.Language
import com.example.appbase.utils.OcrUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OcrFragment : Fragment() {
    private var _binding: FragmentOcrBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var photoFile: File? = null
    private var currentLanguage: Language = Language.ENGLISH

    // Activity result launchers
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(context, getString(R.string.storage_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                processImageFromUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOcrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupLanguageSelection()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupUI() {
        binding.btnCameraCapture.setOnClickListener {
            // Hide any existing results
            binding.cardTextResult.visibility = View.GONE
            binding.imagePreview.visibility = View.GONE
            
            if (checkCameraPermission()) {
                startCamera()
            } else {
                requestCameraPermission()
            }
        }

        binding.btnGallerySelect.setOnClickListener {
            // Hide camera preview when gallery is selected
            binding.cardCameraPreview.visibility = View.GONE
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }

        binding.btnCopyText.setOnClickListener {
            copyTextToClipboard()
        }

        binding.btnShareText.setOnClickListener {
            shareText()
        }

        binding.btnCapturePhoto.setOnClickListener {
            captureImage()
        }
    }

    private fun setupLanguageSelection() {
        binding.chipEnglish.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) currentLanguage = Language.ENGLISH
        }

        binding.chipChinese.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) currentLanguage = Language.CHINESE
        }

        binding.chipJapanese.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) currentLanguage = Language.JAPANESE
        }

        binding.chipKorean.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) currentLanguage = Language.KOREAN
        }

        binding.chipDevanagari.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) currentLanguage = Language.DEVANAGARI
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        storagePermissionLauncher.launch(permission)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Connect preview to the PreviewView
                preview.surfaceProvider = binding.viewCameraPreview.surfaceProvider

                // Show camera preview
                binding.cardCameraPreview.visibility = View.VISIBLE

            } catch (_: Exception) {
                Toast.makeText(context, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        photoFile = createImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile!!).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Hide camera preview
                    binding.cardCameraPreview.visibility = View.GONE
                    
                    photoFile?.let { file ->
                        processImageFromFile(file)
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(context, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile("OCR_${timeStamp}_", ".jpg", storageDir)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun processImageFromFile(file: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            processImage(bitmap)
        } catch (_: Exception) {
            Toast.makeText(context, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImageFromUri(uri: Uri) {
        // Show progress
        binding.progressBar.visibility = View.VISIBLE

        // Use enhanced OCR processing
        lifecycleScope.launch {
            try {
                val result = OcrUtils.processImageForOcr(requireContext(), uri, currentLanguage)

                when (result) {
                    is OcrUtils.OcrResult.Success -> {
                        binding.progressBar.visibility = View.GONE
                        displayEnhancedResults(result)
                    }
                    is OcrUtils.OcrResult.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.text_recognition_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        // Show image preview
        binding.imagePreview.setImageBitmap(bitmap)
        binding.imagePreview.visibility = View.VISIBLE

        // Show progress
        binding.progressBar.visibility = View.VISIBLE

        // For camera capture, we'll use the basic approach for now
        // In a production app, you might want to save the file and use the enhanced processing
        performBasicTextRecognition(bitmap)
    }

    private fun performBasicTextRecognition(bitmap: Bitmap) {
        // This is a fallback for camera capture
        // In production, consider saving the file and using enhanced processing
        lifecycleScope.launch {
            try {
                val result = OcrUtils.processImageForOcr(requireContext(), createTempUri(bitmap), currentLanguage)

                when (result) {
                    is OcrUtils.OcrResult.Success -> {
                        binding.progressBar.visibility = View.GONE
                        displayEnhancedResults(result)
                    }
                    is OcrUtils.OcrResult.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.text_recognition_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createTempUri(bitmap: Bitmap): Uri {
        // Create a temporary file and return its URI using FileProvider
        val tempFile = File.createTempFile("temp_ocr", ".jpg", requireContext().cacheDir)
        tempFile.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            tempFile
        )
    }

    @SuppressLint("StringFormatMatches")
    private fun displayEnhancedResults(result: OcrUtils.OcrResult.Success) {
        // Show image preview if not already shown
        binding.imagePreview.visibility = View.VISIBLE

        // Display text results
        if (result.text.isBlank()) {
            binding.textRecognizedContent.text = getString(R.string.no_text_found)
        } else {
            binding.textRecognizedContent.text = result.text
        }

        // Display confidence score
        val confidenceScore = String.format(
            getString(R.string.confidence_score),
            (result.confidence * 100).toInt()
        )
        binding.textConfidenceScore.text = confidenceScore

        // Auto-detect and suggest language if different from selected
        val detectedLanguage = OcrUtils.detectLanguage(result.text)
        if (detectedLanguage != result.language) {
            // You could show a suggestion to switch to the detected language
            // For now, we'll just log it
            println("Detected language: ${detectedLanguage.displayName}, but using: ${result.language.displayName}")
        }

        // Show results
        binding.cardTextResult.visibility = View.VISIBLE
    }

    private fun copyTextToClipboard() {
        val text = binding.textRecognizedContent.text.toString()
        if (text.isNotBlank() && text != getString(R.string.no_text_found)) {
            val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
            val clip = ClipData.newPlainText("OCR Text", text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, getString(R.string.text_copied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareText() {
        val text = binding.textRecognizedContent.text.toString()
        if (text.isNotBlank() && text != getString(R.string.no_text_found)) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share Text"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}