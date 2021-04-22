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

package com.google.codelabs.productimagesearch.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.codelabs.productimagesearch.ProductSearchActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class ProductSearchAPIClient(context: Context){

    companion object {
        const val VISION_API_PRODUCT_MAX_RESULT = 5

        // Define the product search backend
        // Option 1: Use the demo project that we have already deployed for you
        const val VISION_API_URL =
            "https://us-central1-odml-codelabs.cloudfunctions.net/productSearch"
        const val VISION_API_KEY = ""
        const val VISION_API_PROJECT_ID = "odml-codelabs"
        const val VISION_API_LOCATION_ID = "us-east1"
        const val VISION_API_PRODUCT_SET_ID = "product_set0"

        // Option 2: Go through the Vision API Product Search quickstart and deploy to your project.
        // Fill in the const below with your project info.
//        const val VISION_API_URL = "https://vision.googleapis.com/v1"
//        const val VISION_API_KEY = "YOUR_API_KEY"
//        const val VISION_API_PROJECT_ID = "YOUR_PROJECT_ID"
//        const val VISION_API_LOCATION_ID = "YOUR_LOCATION_ID"
//        const val VISION_API_PRODUCT_SET_ID = "YOUR_PRODUCT_SET_ID"
    }

    // Instantiate the RequestQueue.
    private val requestQueue = Volley.newRequestQueue(context)

    /**
     * Convert an image to its Base64 representation
     */
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        return ""
    }

    /**
     * Use Product Search API to search with the given query image
     * Call the projects.locations.images.annotate endpoint.
     */
    fun annotateImage(image: Bitmap): Task<List<ProductSearchResult>> {
        return TaskCompletionSource<List<ProductSearchResult>>().task
    }

    /**
     * Fetch and transform product image
     * Call the projects.locations.products.referenceImages.get endpoint
     */
    private fun fetchReferenceImage(searchResult: ProductSearchResult): Task<ProductSearchResult> {
        return TaskCompletionSource<ProductSearchResult>().task
    }

    /**
     * Convert the JSON API response to a list of ProductSearchResult objects
     */
    @Throws(JsonSyntaxException::class)
    private fun apiResponseToObject(response: JSONObject): List<ProductSearchResult> {
        // Parse response JSON string into objects.
        val productSearchResults = mutableListOf<ProductSearchResult>()

        val searchResult =
            Gson().fromJson(response.toString(), SearchResultResponse::class.java)
        Log.d(ProductSearchActivity.TAG, "results: $searchResult")
        searchResult.responses?.get(0)?.productSearchResults?.results?.forEach { result ->
            productSearchResults.add(
                ProductSearchResult(
                    result.image,
                    result.score,
                    result.product.productLabels.joinToString { "${it.key} - ${it.value}" },
                    result.product.name
                )
            )
        }

        return productSearchResults
    }
}
