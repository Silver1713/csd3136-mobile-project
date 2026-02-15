package com.csd3156.mobileproject.MovieReviewApp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movieId: Long,
    val author: String,
    val content: String,
    val rating: Double?,
    val createdAtMillis: Long,
    val photoPath: String?
)
