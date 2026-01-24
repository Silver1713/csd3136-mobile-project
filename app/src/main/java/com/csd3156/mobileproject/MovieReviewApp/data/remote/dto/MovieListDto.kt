package com.csd3156.mobileproject.MovieReviewApp.data.remote.dto

import com.squareup.moshi.Json

data class MovieListDto(
    val page: Int,
    val results: List<MovieDto>,
    @Json(name = "total_pages") val totalPages: Int?,
    @Json(name = "total_results") val totalResults: Int?
)
