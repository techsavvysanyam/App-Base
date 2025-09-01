package com.example.appbase.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Supported languages for OCR
 */
enum class Language(val code: String, val displayName: String) {
    ENGLISH("english", "English"),
    CHINESE("chinese", "Chinese"),
    JAPANESE("japanese", "Japanese"),
    KOREAN("korean", "Korean"),
    DEVANAGARI("devanagari", "Devanagari")
}

/**
 * Utility class for OCR operations with enhanced accuracy and language support
 */
class OcrUtils {

    companion object {

        /**
         * Get the appropriate text recognizer for the specified language
         */
        fun getTextRecognizer(language: Language) = when (language) {
            Language.CHINESE -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            Language.JAPANESE -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            Language.KOREAN -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            Language.DEVANAGARI -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            Language.ENGLISH -> TextRecognition.getClient(TextRecognizerOptions.Builder().build())
        }

        /**
         * Process image with enhanced preprocessing for better OCR accuracy
         */
        suspend fun processImageForOcr(
            context: Context,
            uri: Uri,
            language: Language = Language.ENGLISH
        ): OcrResult = withContext(Dispatchers.IO) {
            try {
                // Load and preprocess image
                val bitmap = loadBitmapFromUri(context, uri)
                val processedBitmap = preprocessImage(bitmap)

                // Perform OCR
                val recognizer = getTextRecognizer(language)
                val image = InputImage.fromBitmap(processedBitmap, 0)
                val visionText = recognizer.process(image).await()

                // Process and clean results
                val processedText = processOcrResults(visionText.text, visionText.textBlocks)

                OcrResult.Success(
                    text = processedText.cleanedText,
                    confidence = processedText.averageConfidence,
                    language = language,
                    textBlocks = visionText.textBlocks
                )

            } catch (e: Exception) {
                OcrResult.Error("Failed to process image: ${e.message}")
            }
        }

        /**
         * Load bitmap from URI with memory optimization
         */
        private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream?.use { BitmapFactory.decodeStream(it, null, options) }

            // Calculate sample size for memory optimization
            val maxSize = 1024
            var sampleSize = 1
            while (options.outWidth / sampleSize > maxSize || options.outHeight / sampleSize > maxSize) {
                sampleSize *= 2
            }

            // Load bitmap with calculated sample size
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            return context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, loadOptions)
            } ?: throw IOException("Failed to load image")
        }

        /**
         * Preprocess image for better OCR accuracy
         */
        private fun preprocessImage(bitmap: Bitmap): Bitmap {
            // Resize if too large
            val maxDimension = 2048
            var processedBitmap = bitmap

            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
                val matrix = Matrix().apply { postScale(scale, scale) }
                processedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
            }

            return processedBitmap
        }

        /**
         * Process and clean OCR results for better accuracy
         */
        private fun processOcrResults(
            rawText: String,
            textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock>
        ): ProcessedText {
            var cleanedText = rawText.trim()

            // Remove common OCR artifacts
            cleanedText = cleanedText.replace(Regex("[|\\/\\\\]"), "I")
            cleanedText = cleanedText.replace(Regex("[0O]"), "O")
            cleanedText = cleanedText.replace(Regex("[1Il]"), "I")
            cleanedText = cleanedText.replace(Regex("[5S]"), "S")
            cleanedText = cleanedText.replace(Regex("[8B]"), "B")

            // Fix common spacing issues
            cleanedText = cleanedText.replace(Regex("(?<=\\w)(?=\\d)"), " ")
            cleanedText = cleanedText.replace(Regex("(?<=\\d)(?=\\w)"), " ")

            // Calculate average confidence
            val averageConfidence = if (textBlocks.isNotEmpty()) {
                // ML Kit TextBlock doesn't have a confidence property in the current version
                // We'll use a default confidence based on text quality indicators
                val textQuality = when {
                    textBlocks.size > 5 -> 0.9f  // Multiple text blocks suggest good quality
                    cleanedText.length > 20 -> 0.8f  // Longer text suggests good quality
                    else -> 0.7f  // Default confidence
                }
                textQuality
            } else 0.0f

            return ProcessedText(cleanedText, averageConfidence)
        }

        /**
         * Auto-detect language from text content
         */
        fun detectLanguage(text: String): Language {
            val chinesePattern = Regex("[\\u4e00-\\u9fff]")
            val japanesePattern = Regex("[\\u3040-\\u309f\\u30a0-\\u30ff\\u4e00-\\u9fff]")
            val koreanPattern = Regex("[\\uac00-\\ud7af]")
            val devanagariPattern = Regex("[\\u0900-\\u097f]")

            return when {
                japanesePattern.containsMatchIn(text) -> Language.JAPANESE
                chinesePattern.containsMatchIn(text) -> Language.CHINESE
                koreanPattern.containsMatchIn(text) -> Language.KOREAN
                devanagariPattern.containsMatchIn(text) -> Language.DEVANAGARI
                else -> Language.ENGLISH
            }
        }
    }

    /**
     * Data class for processed OCR text
     */
    data class ProcessedText(
        val cleanedText: String,
        val averageConfidence: Float
    )

    /**
     * Sealed class for OCR results
     */
    sealed class OcrResult {
        data class Success(
            val text: String,
            val confidence: Float,
            val language: Language,
            val textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock>
        ) : OcrResult()

        data class Error(val message: String) : OcrResult()
    }
}
