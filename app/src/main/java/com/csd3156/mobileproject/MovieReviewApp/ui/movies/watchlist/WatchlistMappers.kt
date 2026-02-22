package com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist

import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie

fun WatchlistMovie.toMovie(): Movie {
    return Movie(
        id = movieId,
        title = title,
        overview = "",
        posterUrl = posterUrl,
        rating = rating,
        releaseDate = releaseDate,
        review = "",
        genres = emptyList(),
        watchTimeInSeconds = 0L,
        adult = null,
        originalLanguage = "",
        voteCount = null
    )
}
