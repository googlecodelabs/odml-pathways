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

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.codelabs.productimagesearch.databinding.ActivityObjectDetectorBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ObjectDetectorActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1000
        private const val REQUEST_IMAGE_GALLERY = 1001
        private const val TAKEN_BY_CAMERA_FILE_NAME = "MLKitDemo_"
        private const val IMAGE_PRESET_1 = "Preset1.jpg"
        private const val IMAGE_PRESET_2 = "Preset2.jpg"
        private const val IMAGE_PRESET_3 = "Preset3.jpg"
        private const val TAG = "MLKit-ODT"
    }

    private lateinit var viewBinding: ActivityObjectDetectorBinding
    private var cameraPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityObjectDetectorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        initViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // After taking camera, display to Preview
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> cameraPhotoUri?.let {
                    this.setViewAndDetect(
                        getBitmapFromUri(it)
                    )
                }
                REQUEST_IMAGE_GALLERY -> data?.data?.let { this.setViewAndDetect(getBitmapFromUri(it)) }
            }
        }
    }

    private fun initViews() {
        with(viewBinding) {
            ivPreset1.setImageBitmap(getBitmapFromAsset(IMAGE_PRESET_1))
            ivPreset2.setImageBitmap(getBitmapFromAsset(IMAGE_PRESET_2))
            ivPreset3.setImageBitmap(getBitmapFromAsset(IMAGE_PRESET_3))
            ivCapture.setOnClickListener { dispatchTakePictureIntent() }
            ivGalleryApp.setOnClickListener { choosePhotoFromGalleryApp() }
            ivPreset1.setOnClickListener { setViewAndDetect(getBitmapFromAsset(IMAGE_PRESET_1)) }
            ivPreset2.setOnClickListener { setViewAndDetect(getBitmapFromAsset(IMAGE_PRESET_2)) }
            ivPreset3.setOnClickListener { setViewAndDetect(getBitmapFromAsset(IMAGE_PRESET_3)) }

            // Default display
            setViewAndDetect(getBitmapFromAsset(IMAGE_PRESET_2))
        }
    }

    /**
     * Update the UI with the input image and start object detection
     */
    private fun setViewAndDetect(bitmap: Bitmap?) {
        bitmap?.let {
            // Clear the dots indicating the previous detection result
            viewBinding.ivPreview.drawDetectionResults(emptyList())
            
            // Display the input image on the screen.
            viewBinding.ivPreview.setImageBitmap(bitmap)

            // Run object detection and show the detection results.
            runObjectDetection(bitmap)
        }
    }

    /**
     * Detect Objects in a given Bitmap
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        // Step 1: create ML Kit's InputImage object
        val image = InputImage.fromBitmap(bitmap, 0)

        // Step 2: acquire detector object
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        val objectDetector = ObjectDetection.getClient(options)

        // Step 3: feed given image to detector and setup callback
        objectDetector.process(image)
            .addOnSuccessListener { results ->
                // Keep only the FASHION_GOOD objects
                val filteredResults = results.filter { result ->
                    result.labels.indexOfFirst { it.text == PredefinedCategory.FASHION_GOOD } != -1
                }

                // Visualize the detection result
                runOnUiThread {
                    viewBinding.ivPreview.drawDetectionResults(filteredResults)
                }

            }
            .addOnFailureListener {
                // Task failed with an exception
                Log.e(TAG, it.message.toString())
            }

    }

    /**
     * Show Camera App to take a picture based Intent
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(TAKEN_BY_CAMERA_FILE_NAME)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    cameraPhotoUri = FileProvider.getUriForFile(
                        this,
                        "com.google.codelabs.productimagesearch.fileprovider",
                        it
                    )
                    // Setting output file to take a photo
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
                    // Open camera based Intent.
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } ?: run {
                Toast.makeText(this, getString(R.string.camera_app_not_found), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    /**
     * Show gallery app to pick photo from intent.
     */
    private fun choosePhotoFromGalleryApp() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }, REQUEST_IMAGE_GALLERY)
    }

    /**
     * The output file will be stored on private storage of this app
     * By calling function getExternalFilesDir
     * This photo will be deleted when uninstall app.
     */
    @Throws(IOException::class)
    private fun createImageFile(fileName: String): File {
        // Create an image file name
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            fileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    /**
     * Method to copy asset files sample to private app folder.
     * Return the Uri of an output file.
     */
    private fun getBitmapFromAsset(fileName: String): Bitmap? {
        return try {
            BitmapFactory.decodeStream(assets.open(fileName))
        } catch (ex: IOException) {
            null
        }
    }

    /**
     * Function to get the Bitmap From Uri.
     * Uri is received by using Intent called to Camera or Gallery app
     * SuppressWarnings => we have covered this warning.
     */
    private fun getBitmapFromUri(imageUri: Uri): Bitmap? {
        val bitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
            } else {
                // Add Suppress annotation to skip warning by Android Studio.
                // This warning resolved by ImageDecoder function.
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }
        } catch (ex: IOException) {
            null
        }
        
        // Make a copy of the bitmap in a desirable format
        return bitmap?.copy(Bitmap.Config.ARGB_8888, false)
    }

    /**
     * Function to log information about object detected by ML Kit.
     */
    private fun debugPrint(detectedObjects: List<DetectedObject>) {
        detectedObjects.forEachIndexed { index, detectedObject ->
            val box = detectedObject.boundingBox

            Log.d(TAG, "Detected object: $index")
            Log.d(TAG, " trackingId: ${detectedObject.trackingId}")
            Log.d(TAG, " boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")
            detectedObject.labels.forEach {
                Log.d(TAG, " categories: ${it.text}")
                Log.d(TAG, " confidence: ${it.confidence}")
            }
        }
    }

}
