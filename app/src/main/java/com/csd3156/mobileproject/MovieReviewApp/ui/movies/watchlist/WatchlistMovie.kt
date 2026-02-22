package com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_movies")
data class WatchlistMovie(
    @PrimaryKey val movieId: Long,
    val title: String,
    val posterUrl: String,
    val releaseDate: String,
    val rating: Double,
    val savedAt: Long = System.currentTimeMillis(),
    val firstGenres: String? = "Unspecified"
)

