package com.example.appbase

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.appbase.camera.GraphicOverlay
import com.example.appbase.databinding.FragmentObjectDetectionLiveBinding
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import androidx.camera.core.ExperimentalGetImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalGetImage
class ObjectDetectionLiveFragment : Fragment() {

    private var _binding: FragmentObjectDetectionLiveBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var objectDetector: ObjectDetector
    private lateinit var tvDetectionResults: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentObjectDetectionLiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDetectionResults = binding.detectionResultsCard.root.findViewById<TextView>(R.id.tv_detection_results)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val localModel = LocalModel.Builder()
            .setAssetFilePath("object_labeler.tflite")
            .build()

        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .build()

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            val scaleX = binding.previewView.width.toFloat() / mediaImage.height
                            val scaleY = binding.previewView.height.toFloat() / mediaImage.width
                            binding.graphicOverlay.setTransformation(scaleX, scaleY, 0f, 0f)
                            objectDetector.process(image)
                                .addOnSuccessListener { detectedObjects ->
                                    if (_binding == null) {
                                        imageProxy.close()
                                        return@addOnSuccessListener
                                    }
                                    binding.graphicOverlay.clear()
                                    val resultsText = StringBuilder()
                                    if (detectedObjects.isEmpty()) {
                                        resultsText.append(getString(R.string.no_objects_detected))
                                    } else {
                                        for (detectedObject in detectedObjects) {
                                            binding.graphicOverlay.add(GraphicOverlay.ObjectGraphic(binding.graphicOverlay, detectedObject))
                                            val labels = detectedObject.labels.joinToString(", ") { label ->
                                                "${label.text} (${String.format("%.2f", label.confidence * 100)}%)"
                                            }
                                            resultsText.append("Object: $labels\n")
                                        }
                                    }
                                    tvDetectionResults.text = resultsText.toString()
                                    imageProxy.close()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Object detection failed", e)
                                    tvDetectionResults.text = getString(R.string.detection_failed)
                                    imageProxy.close()
                                }
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        objectDetector.close()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
        _binding = null
    }

    companion object {
        private const val TAG = "ObjectDetectionLive"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
