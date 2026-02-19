package com.csd3156.mobileproject.MovieReviewApp.domain.model

import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseDto
import com.google.firebase.Timestamp
import java.time.format.DateTimeFormatter
import java.util.Date

data class MovieReview(
    val id: String,
    val author: String,
    val content: String,
    val url: String,
    val rating: Double?,
    val createdAt: String,
    val photoPath: String? = null,
    val uid : String? = null,
    val username : String? = null
){
    fun ToFirebaseDto() : ReviewFirebaseDto{
        return ReviewFirebaseDto(
            id = id,
            uid= uid,
            username = username,
            profileName = author,
            content = content,
            photoUrl = photoPath,
            rating = rating,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()

        )
    }


}
