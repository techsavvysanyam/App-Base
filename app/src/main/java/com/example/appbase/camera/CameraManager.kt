package com.example.appbase.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Function to convert ImageProxy to Bitmap
private fun convertImageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    private var customAnalyzer: ((ImageProxy) -> Unit)? = null

    suspend fun initializeCamera(): ProcessCameraProvider {
        return suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener({
                    cameraProvider = future.get()
                    continuation.resume(future.get())
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }
    
    fun setImageAnalyzer(analyzer: (ImageProxy) -> Unit) {
        customAnalyzer = analyzer
        if (cameraProvider != null) {
            updateImageAnalyzer()
        }
    }
    
    private fun updateImageAnalyzer() {
        val cameraProvider = cameraProvider ?: return
        
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    customAnalyzer?.invoke(imageProxy) ?: imageProxy.close()
                }
            }
        
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                imageAnalyzer
            )
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }
    
    fun startCamera(previewView: PreviewView) {
        ProcessCameraProvider.getInstance(context).also { future ->
            future.addListener({
                cameraProvider = future.get()
                startCameraInternal(previewView)
            }, ContextCompat.getMainExecutor(context))
        }
    }
    
    private fun startCameraInternal(previewView: PreviewView) {
        val cameraProvider = cameraProvider ?: return
        
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    customAnalyzer?.invoke(imageProxy) ?: imageProxy.close()
                }
            }
        
        try {
            cameraProvider.unbindAll()
            
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                imageAnalyzer
            )
            
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }
    
    fun captureImage(
        onImageCaptured: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError("Image capture not initialized")
            return
        }
        
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = convertImageProxyToBitmap(image)
                    image.close()
                    bitmap?.let { onImageCaptured(it) }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onError("Image capture failed: ${exception.message}")
                }
            }
        )
    }
    
    fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
    
}
