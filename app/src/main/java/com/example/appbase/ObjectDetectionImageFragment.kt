package com.example.appbase

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.appbase.camera.GraphicOverlay
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.io.IOException

class ObjectDetectionImageFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var tvDetectionResults: TextView

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_object_detection_image, container, false)

        imageView = view.findViewById(R.id.imageView)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        graphicOverlay = view.findViewById(R.id.graphicOverlay)
        tvDetectionResults = view.findViewById<View>(R.id.detection_results_card).findViewById(R.id.tv_detection_results)

        selectImageButton.setOnClickListener {
            openImageChooser()
        }

        return view
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                imageView.setImageBitmap(bitmap)
                detectObjects(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun detectObjects(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val localModel = LocalModel.Builder()
            .setAssetFilePath("object_labeler.tflite")
            .build()

        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .build()

        val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

        objectDetector.process(image)
            .addOnSuccessListener { detectedObjects ->
                graphicOverlay.clear()
                val imageWidth = bitmap.width
                val imageHeight = bitmap.height

                // Calculate scaling factors to fit the image in the ImageView
                val scaleFactorX = imageView.width.toFloat() / imageWidth
                val scaleFactorY = imageView.height.toFloat() / imageHeight
                val scaleFactor = Math.min(scaleFactorX, scaleFactorY)

                val offsetX = (imageView.width - imageWidth * scaleFactor) / 2
                val offsetY = (imageView.height - imageHeight * scaleFactor) / 2

                graphicOverlay.setTransformation(scaleFactor, scaleFactor, offsetX, offsetY)

                val resultsText = StringBuilder()
                if (detectedObjects.isEmpty()) {
                    resultsText.append(getString(R.string.no_objects_detected))
                } else {
                    for (obj in detectedObjects) {
                        graphicOverlay.add(GraphicOverlay.ObjectGraphic(graphicOverlay, obj))
                        val labels = obj.labels.joinToString(", ") { label ->
                            "${label.text} (${String.format("%.2f", label.confidence * 100)}%)"
                        }
                        resultsText.append("Object: $labels\n")
                    }
                }
                tvDetectionResults.text = resultsText.toString()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                tvDetectionResults.text = getString(R.string.detection_failed)
            }
    }
}
