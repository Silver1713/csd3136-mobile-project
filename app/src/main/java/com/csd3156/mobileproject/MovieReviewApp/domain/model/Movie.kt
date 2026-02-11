package com.csd3156.mobileproject.MovieReviewApp.domain.model

// Domain model to define the movie data used across the different layers of the project
data class Movie(
    val id: Long,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val rating: Double,
    val releaseDate: String,
    val review: String
)
