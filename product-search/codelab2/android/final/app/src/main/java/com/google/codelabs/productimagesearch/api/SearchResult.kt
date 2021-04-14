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

/**
 * Class to mapping with search result response
 */
data class SearchResultResponse(val responses: List<Response>?)

data class Response(
    val productSearchResults: ProductSearchResults?
)

data class ProductSearchResults(
    val indexTime: String,
    val results: List<Result>,
    val productGroupedResults: List<ProductGroupedResult>
)

data class ProductGroupedResult(
    val boundingPoly: BoundingPoly,
    val results: List<Result>
)

data class BoundingPoly(
    val normalizedVertices: List<NormalizedVertex>
)

data class NormalizedVertex(
    val x: Double,
    val y: Double
)

data class Result(
    val product: Product,
    val score: Double,
    val image: String
)

data class Product(
    val name: String,
    val displayName: String,
    val productCategory: String,
    val productLabels: List<ProductLabel>
)

data class ProductLabel(
    val key: String,
    val value: String
)


/**
 * Transformed product search result.
 */
data class ProductSearchResult(
    val imageId: String,
    val score: Double,
    val label: String,
    val name: String,
    var imageUri: String? = null
)