package com.csd3156.mobileproject.MovieReviewApp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import java.time.Instant
import java.time.ZoneId

@Entity(tableName = "movie_reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movieId: Long,
    val author: String,
    val content: String,
    val rating: Double?,
    val createdAtMillis: Long,
    val photoPath: String? = null,
    val reviewId : String? = null,
    val userId : String? = null,
    val movieTitle: String? = null,
){
    fun toDomain(): MovieReview {
        return MovieReview(
            id = "local-$id",
            movieTitle = movieTitle,
            author = author,
            content = content,
            url = "",
            rating = rating,
            createdAt = Instant.ofEpochMilli(createdAtMillis),
            photoPath = photoPath
        )
    }
}
