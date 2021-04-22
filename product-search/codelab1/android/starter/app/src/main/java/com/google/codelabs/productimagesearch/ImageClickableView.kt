/**
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codelabs.productimagesearch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.google.mlkit.vision.objects.DetectedObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * Customize ImageView which can be clickable on some Detection Result Bound.
 */
class ImageClickableView : AppCompatImageView {

    companion object {
        private const val TAG = "ImageClickableView"
        private const val CLICKABLE_RADIUS = 40f
        private const val SHADOW_RADIUS = 10f
    }

    private val dotPaint = createDotPaint()
    private var onObjectClickListener: ((cropBitmap: Bitmap) -> Unit)? = null

    // This variable is used to hold the actual size of bounding box detection result due to
    // the ratio might changed after Bitmap fill into ImageView
    private var transformedResults = listOf<TransformedDetectionResult>()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    /**
     * Callback when user click to detection result rectangle.
     */
    fun setOnObjectClickListener(listener: ((objectImage: Bitmap) -> Unit)) {
        this.onObjectClickListener = listener
    }

    /**
     * Draw white circle at the center of each detected object on the image
     */
    fun drawDetectionResults(results: List<DetectedObject>) {
        (drawable as? BitmapDrawable)?.bitmap?.let { srcImage ->
            // Get scale size based width/height
            val scaleFactor =
                max(srcImage.width / width.toFloat(), srcImage.height / height.toFloat())
            // Calculate the total padding (based center inside scale type)
            val diffWidth = abs(width - srcImage.width / scaleFactor) / 2
            val diffHeight = abs(height - srcImage.height / scaleFactor) / 2

            // Transform the original Bounding Box to actual bounding box based the display size of ImageView.
            transformedResults = results.map { result ->
                // Calculate to create new coordinates of Rectangle Box match on ImageView.
                val actualRectBoundingBox = RectF(
                    (result.boundingBox.left / scaleFactor) + diffWidth,
                    (result.boundingBox.top / scaleFactor) + diffHeight,
                    (result.boundingBox.right / scaleFactor) + diffWidth,
                    (result.boundingBox.bottom / scaleFactor) + diffHeight
                )
                val dotCenter = PointF(
                    (actualRectBoundingBox.right + actualRectBoundingBox.left) / 2,
                    (actualRectBoundingBox.bottom + actualRectBoundingBox.top) / 2,
                )
                // Transform to new object to hold the data inside.
                // This object is necessary to avoid performance
                TransformedDetectionResult(actualRectBoundingBox, result.boundingBox, dotCenter)
            }
            Log.d(
                TAG,
                "srcImage: ${srcImage.width}/${srcImage.height} - imageView: ${width}/${height} => scaleFactor: $scaleFactor"
            )
            // Invalid to re-draw the canvas
            // Method onDraw will be called with new data.
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Getting detection results and draw the dot view onto detected object.
        transformedResults.forEach { result ->
            // Draw Dot View
            canvas.drawCircle(result.dotCenter.x, result.dotCenter.y, CLICKABLE_RADIUS, dotPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y
                val index =
                    transformedResults.indexOfFirst {
                        val dx = (touchX - it.dotCenter.x).toDouble().pow(2.0)
                        val dy = (touchY - it.dotCenter.y).toDouble().pow(2.0)
                        (dx + dy) < CLICKABLE_RADIUS.toDouble().pow(2.0)
                    }
                // If a matching object found, call the objectClickListener
                if (index != -1) {
                    cropBitMapBasedResult(transformedResults[index])?.let {
                        onObjectClickListener?.invoke(it)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * This function will be used to crop the segment of Bitmap based touching by user.
     */
    private fun cropBitMapBasedResult(result: TransformedDetectionResult): Bitmap? {
        // Crop image from Original Bitmap with Original Rect Bounding Box
        (drawable as? BitmapDrawable)?.bitmap?.let {
            return Bitmap.createBitmap(
                it,
                result.originalBoxRectF.left,
                result.originalBoxRectF.top,
                result.originalBoxRectF.width(),
                result.originalBoxRectF.height()
            )
        }
        return null
    }

    /**
     * Return Dot Paint to draw circle
     */
    private fun createDotPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(SHADOW_RADIUS, 0F, 0F, Color.BLACK)
        // Force to use software to render by disable hardware acceleration.
        // Important: the shadow will not work without this line.
        setLayerType(LAYER_TYPE_SOFTWARE, this)
    }
}

/**
 * This class holds the transformed data
 * @property: actualBoxRectF: The bounding box after calculated
 * @property: originalBoxRectF: The original bounding box (Before transformed), use for crop bitmap.
 */
data class TransformedDetectionResult(
    val actualBoxRectF: RectF,
    val originalBoxRectF: Rect,
    val dotCenter: PointF
)
