package com.example.appbase.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.objects.DetectedObject

class GraphicOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val graphics: MutableList<Graphic> = ArrayList()
    internal val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5.0f
    }

    internal val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 50.0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (graphic in graphics) {
            graphic.draw(canvas)
        }
    }

    fun clear() {
        graphics.clear()
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        graphics.add(graphic)
        postInvalidate()
    }

    abstract class Graphic(internal val overlay: GraphicOverlay) {
        abstract fun draw(canvas: Canvas)
    }

    internal var imageScaleX: Float = 1.0f
    internal var imageScaleY: Float = 1.0f
    internal var imageOffsetX: Float = 0f
    internal var imageOffsetY: Float = 0f

    fun setTransformation(scaleX: Float, scaleY: Float, offsetX: Float, offsetY: Float) {
        this.imageScaleX = scaleX
        this.imageScaleY = scaleY
        this.imageOffsetX = offsetX
        this.imageOffsetY = offsetY
        postInvalidate()
    }

    class ObjectGraphic(overlay: GraphicOverlay, private val detectedObject: DetectedObject) : Graphic(overlay) {

        override fun draw(canvas: Canvas) {
            val rect = detectedObject.boundingBox
            val mappedBox = RectF(
                rect.left * overlay.imageScaleX + overlay.imageOffsetX,
                rect.top * overlay.imageScaleY + overlay.imageOffsetY,
                rect.right * overlay.imageScaleX + overlay.imageOffsetX,
                rect.bottom * overlay.imageScaleY + overlay.imageOffsetY
            )
            canvas.drawRect(mappedBox, overlay.boxPaint)

            val labels = detectedObject.labels.joinToString(", ") { label ->
                "${label.text} (${String.format("%.2f", label.confidence * 100)}%)"
            }
            canvas.drawText(labels, mappedBox.left, mappedBox.top - 10, overlay.textPaint)
        }
    }
}
