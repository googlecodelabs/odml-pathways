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
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.max

/**
 * Customize ImageView which can be clickable on some Detection Result Bound.
 */
class ImageClickableView : AppCompatImageView {

    companion object {
        private const val TAG = "ImageClickableView"
        private const val DEFAULT_MARGIN = 15f      // Default margin between Text and Bounding Box.
    }

    private val boundingPaint = createBoundingPaint()
    private val textPaint = createTextPaint()
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
     * Draw the detection results onto ImageView
     */
    fun drawDetectionResults(results: List<BoxWithText>) {
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
                        (result.box.left / scaleFactor) + diffWidth,
                        (result.box.top / scaleFactor) + diffHeight,
                        (result.box.right / scaleFactor) + diffWidth,
                        (result.box.bottom / scaleFactor) + diffHeight
                )
                // Calculate Text coordinate to draw onto ImageView
                val staticLayout = createStaticLayout(result.text, actualRectBoundingBox)
                // Transform to new object to hold the data inside.
                // This object is necessary to avoid performance
                TransformedDetectionResult(
                        actualRectBoundingBox,
                        result.box,
                        staticLayout
                )
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
        // Getting detection results and draw bounding box on its image.
        transformedResults.forEach { result ->
            // Draw Bounding Box
            canvas.drawRect(result.actualBoxRectF, boundingPaint)
            // Draw Text & adding margin between Text & Bound
            canvas.save() // Save current state
            canvas.translate(
                    result.actualBoxRectF.left + DEFAULT_MARGIN,
                    result.actualBoxRectF.top + DEFAULT_MARGIN
            )
            result.staticLayout.draw(canvas)
            canvas.restore() // Restore state that saved above.
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y
                val index =
                    transformedResults.indexOfFirst { it.actualBoxRectF.contains(touchX, touchY) }

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
     * Use Static Layout to draw the Text, this view can able to wrap the content to multiple line
     * when exceed width size.
     */
    private fun createStaticLayout(textLabel: String, boxRectF: RectF): StaticLayout {
        val alignment = Layout.Alignment.ALIGN_NORMAL
        val spacingMultiplier = 1f
        val spacingAddition = 0f
        val includePadding = false
        val textWidth = abs(boxRectF.width() - DEFAULT_MARGIN * 2).toInt()
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(
                    textLabel,
                    0,
                    textLabel.length,
                    textPaint,
                    textWidth
            ).setAlignment(alignment)
                    .setLineSpacing(spacingAddition, spacingMultiplier)
                    .setIncludePad(includePadding)
                    .build()
        } else {
            // Use old way to create new one StaticLayout for Android < M
            @Suppress("DEPRECATION")
            StaticLayout(
                    textLabel,
                    textPaint,
                    textWidth,
                    alignment,
                    spacingMultiplier,
                    spacingAddition,
                    includePadding
            )
        }
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
     * Return Bounding Paint
     */
    private fun createBoundingPaint() = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 8f
        style = Paint.Style.STROKE
        textAlign = Paint.Align.LEFT
    }

    /**
     * Return Text Paint
     */
    private fun createTextPaint() = TextPaint().apply {
        color = Color.GREEN
        strokeWidth = 1f
        textSize = 30f
        style = Paint.Style.FILL_AND_STROKE
        textAlign = Paint.Align.LEFT
    }
}

/**
 * This class holds the transformed data from BoxWithText for easier detection of clicked object.
 * @property: actualBoxRectF: The bounding box after calculated
 * @property: originalBoxRectF: The original bounding box (Before transformed), use for crop bitmap.
 * @property: staticLayout: Static Layout to draw Text onto ImageView.
 */
data class TransformedDetectionResult(
        val actualBoxRectF: RectF,
        val originalBoxRectF: Rect,
        val staticLayout: StaticLayout
)

/**
 * This class is used to transform the output data to general data format.
 */
data class BoxWithText(val box: Rect, val text: String)