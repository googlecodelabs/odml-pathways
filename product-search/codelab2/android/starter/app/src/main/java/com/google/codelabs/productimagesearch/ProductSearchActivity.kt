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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.codelabs.productimagesearch.api.ProductSearchAPIClient
import com.google.codelabs.productimagesearch.databinding.ActivityProductSearchBinding
import com.google.codelabs.productimagesearch.api.ProductSearchResult

class ProductSearchActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ProductSearchActivity"
        const val CROPPED_IMAGE_FILE_NAME = "MLKitCroppedFile_"
        const val REQUEST_TARGET_IMAGE_PATH = "REQUEST_TARGET_IMAGE_PATH"
    }

    private lateinit var viewBinding: ActivityProductSearchBinding
    private lateinit var apiClient: ProductSearchAPIClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityProductSearchBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        initViews()

        // Receive the query image and show it on the screen
        intent.getStringExtra(REQUEST_TARGET_IMAGE_PATH)?.let { absolutePath ->
            viewBinding.ivQueryImage.setImageBitmap(BitmapFactory.decodeFile(absolutePath))
        }

        // Initialize an API client for Vision API Product Search
        apiClient = ProductSearchAPIClient(this)
    }

    private fun initViews() {
        // Setup RecyclerView
        with(viewBinding.recyclerView) {
            setHasFixedSize(true)
            adapter = ProductSearchAdapter()
            layoutManager =
                LinearLayoutManager(
                    this@ProductSearchActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
        }

        // Events
        viewBinding.btnSearch.setOnClickListener {
            // Display progress
            viewBinding.progressBar.visibility = View.VISIBLE
            (viewBinding.ivQueryImage.drawable as? BitmapDrawable)?.bitmap?.let {
                searchByImage(it)
            }
        }
    }

    /**
     * Use Product Search API to search with the given query image
     */
    private fun searchByImage(queryImage: Bitmap) {

    }

    /**
     * Show search result.
     */
    private fun showSearchResult(result: List<ProductSearchResult>) {
        viewBinding.progressBar.visibility = View.GONE

        // Update the recycler view to display the search result.
        (viewBinding.recyclerView.adapter as? ProductSearchAdapter)?.submitList(
            result
        )
    }

    /**
     * Show Error Response
     */
    private fun showErrorResponse(message: String?) {
        viewBinding.progressBar.visibility = View.GONE
        // Show the error when calling API.
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }


}

/**
 * Adapter RecyclerView
 */
class ProductSearchAdapter :
    ListAdapter<ProductSearchResult, ProductSearchAdapter.ProductViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<ProductSearchResult>() {
            override fun areItemsTheSame(
                oldItem: ProductSearchResult,
                newItem: ProductSearchResult
            ) = oldItem.imageId == newItem.imageId && oldItem.imageUri == newItem.imageUri

            override fun areContentsTheSame(
                oldItem: ProductSearchResult,
                newItem: ProductSearchResult
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
    )

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    /**
     * ViewHolder to hold the data inside
     */
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Bind data to views
         */
        @SuppressLint("SetTextI18n")
        fun bind(product: ProductSearchResult) {
            with(itemView) {
                findViewById<TextView>(R.id.tvProductName).text = "Name: ${product.name}"
                findViewById<TextView>(R.id.tvProductScore).text = "Similarity score: ${product.score}"
                findViewById<TextView>(R.id.tvProductLabel).text = "Labels: ${product.label}"
                // Show the image using Glide
                Glide.with(itemView).load(product.imageUri).into(findViewById(R.id.ivProduct))
            }
        }
    }
}
