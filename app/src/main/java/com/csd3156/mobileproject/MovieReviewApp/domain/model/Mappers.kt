package com.csd3156.mobileproject.MovieReviewApp.domain.model

fun MovieDetails.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        rating = rating,
        releaseDate = releaseDate,
        review = "",
        genres = genres.map { it.name },
        watchTimeInSeconds = 0L
    )
}
