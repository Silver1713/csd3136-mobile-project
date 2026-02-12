package com.csd3156.mobileproject.MovieReviewApp.domain.model

data class MovieReview(
    val id: String,
    val author: String,
    val content: String,
    val url: String,
    val rating: Double?,
    val createdAt: String
)
