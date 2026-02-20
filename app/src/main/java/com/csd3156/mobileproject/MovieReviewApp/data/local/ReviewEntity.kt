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
    val userId : String? = null
){
    fun toDomain(): MovieReview {
        val displayDate = Instant.ofEpochMilli(createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()

        return MovieReview(
            id = "local-$id",
            author = author,
            content = content,
            url = "",
            rating = rating,
            createdAt = displayDate,
            photoPath = photoPath
        )
    }
}
