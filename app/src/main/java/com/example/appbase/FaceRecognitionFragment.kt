package com.example.appbase

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.appbase.databinding.FragmentFaceRecognitionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException
import kotlin.math.max

class FaceRecognitionFragment : Fragment() {
    private var _binding: FragmentFaceRecognitionBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private lateinit var faceDetector: FaceDetector

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            binding.textResults.text = getString(R.string.face_permission_required)
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickFromGallery()
        } else {
            binding.textResults.text = getString(R.string.error_permission)
        }
    }

    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri?.let { uri ->
                processImageUri(uri)
            }
        }
    }

    private val pickMediaLauncher = if (Build.VERSION.SDK_INT >= 33) {
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { processImageUri(it) }
        }
    } else null

    private val pickImageLegacyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) processImageUri(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceRecognitionBinding.inflate(inflater, container, false)
        setupDetector()
        setupUi()
        return binding.root
    }

    private fun setupDetector() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .enableTracking()
            .build()
        faceDetector = FaceDetection.getClient(options)
    }

    private fun setupUi() {
        binding.buttonCapture.text = getString(R.string.face_capture_image)
        binding.buttonPick.text = getString(R.string.face_pick_image)
        binding.textPlaceholder.visibility = View.VISIBLE
        binding.progressDetection.visibility = View.GONE
        binding.textFaceCount.text = ""
        binding.textFaceCount.visibility = View.GONE

        binding.buttonCapture.setOnClickListener {
            ensureCameraPermissionThenCapture()
        }
        binding.buttonPick.setOnClickListener {
            ensureStoragePermissionThenPick()
        }
        binding.buttonClear.setOnClickListener {
            clearUi()
        }
    }

    private fun ensureCameraPermissionThenCapture() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "captured_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        val resolver = requireContext().contentResolver
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        try {
            captureImageLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            binding.textResults.text = getString(R.string.error_generic)
        }
    }

    private fun ensureStoragePermissionThenPick() {
        if (Build.VERSION.SDK_INT >= 33) {
            // Photo Picker does not require storage permission on Android 13+
            pickFromGallery()
            return
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            pickFromGallery()
        } else {
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= 33) {
            pickMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            pickImageLegacyLauncher.launch(intent)
        }
    }

    private fun processImageUri(uri: Uri) {
        try {
            val bitmap = loadBitmap(uri)
            if (bitmap != null) {
                binding.imageInput.setImageBitmap(bitmap)
                binding.textPlaceholder.visibility = View.GONE
                binding.progressDetection.visibility = View.VISIBLE
                binding.textResults.text = getString(R.string.face_detecting)
                val image = InputImage.fromBitmap(bitmap, 0)
                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        drawDetections(bitmap, faces)
                        binding.textResults.text = buildResultSummary(faces)
                        binding.textFaceCount.text = "Faces: ${faces.size}"
                        binding.textFaceCount.visibility = View.VISIBLE
                        binding.progressDetection.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        binding.textResults.text = getString(R.string.error_generic)
                        binding.textFaceCount.text = ""
                        binding.textFaceCount.visibility = View.GONE
                        binding.progressDetection.visibility = View.GONE
                    }
            }
        } catch (_: Exception) {
            binding.textResults.text = getString(R.string.error_generic)
            binding.textFaceCount.text = ""
            binding.textFaceCount.visibility = View.GONE
            binding.progressDetection.visibility = View.GONE
        }
    }

    private fun buildResultSummary(faces: List<Face>): String {
        if (faces.isEmpty()) return getString(R.string.face_no_faces)
        val parts = faces.mapIndexed { index, face ->
            val bounds = face.boundingBox
            "Face ${index + 1}: x=${bounds.left}, y=${bounds.top}, w=${bounds.width()}, h=${bounds.height()}"
        }
        return parts.joinToString(separator = "\n")
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            val source = if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            source
        } catch (e: IOException) {
            null
        }
    }

    private fun drawDetections(original: Bitmap, faces: List<Face>) {
        if (faces.isEmpty()) {
            binding.overlay.setImageDrawable(null)
            binding.textPlaceholder.visibility = View.VISIBLE
            binding.textFaceCount.text = ""
            binding.textFaceCount.visibility = View.GONE
            return
        }
        val overlayBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(overlayBitmap)
        val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = max(original.width, original.height) * 0.005f
        }
        for (face in faces) {
            canvas.drawRect(face.boundingBox, paint)
        }
        binding.overlay.setImageBitmap(overlayBitmap)
    }

    private fun clearUi() {
        binding.imageInput.setImageDrawable(null)
        binding.overlay.setImageDrawable(null)
        binding.textPlaceholder.visibility = View.VISIBLE
        binding.textResults.text = ""
        binding.textFaceCount.text = ""
        binding.textFaceCount.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (this::faceDetector.isInitialized) {
            faceDetector.close()
        }
    }
}