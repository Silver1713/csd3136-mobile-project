package com.csd3156.mobileproject.MovieReviewApp.data.remote.dto

import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.time.Instant


data class ReviewFirebaseDtoWithMeta(
    val reviewId : String?,
    val movieId: Long?,
    val reviewDto : ReviewFirebaseDto? = null
) {
    fun getDto() : ReviewFirebaseDto? {
        return reviewDto
    }
}
data class ReviewFirebaseDto(
    val id: String? = null,
    val uid : String? = null,
    val movieTitle: String? = null,
    val profileName: String? = null,
    val username: String? = null,
    val content: String? = null,
    val photoUrl: String? = null,
    val rating: Double? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null

){
    fun toMap() : Map<String, Any?>{
        val map = mutableMapOf<String, Any?>()
        map["id"] = id
        map["uid"] = uid
        map["profileName"] = profileName
        map["username"] = username
        map["content"] = content
        map["photoUrl"] = photoUrl
        map["rating"] = rating
        map["createdAt"] = FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        map["movieTitle"] = movieTitle
        return map
    }


    fun toDomain() : MovieReview{
        return MovieReview(
            id = id ?: "",
            author = profileName ?: "UNNAMED",
            content = content ?: "",
            url = "INTERNAL_APP",
            rating = rating ?: 0.0,
            createdAt = updatedAt?.toDate()?.toInstant() ?: Instant.now(),
            photoPath = photoUrl,
            movieTitle =  movieTitle
        )
    }
}

data class ReviewFirebaseCreateDto(
    val uid : String? = null,
    val profileName: String? = null,
    val movieTitle: String? = null,
    val username: String? = null,
    val content: String? = null,
    val photoUrl: String? = null,
    val rating: Double? = null,

){
    fun toMap() : Map<String, Any?>{
       val map = mutableMapOf<String, Any?>()
        map["uid"] = uid
        map["profileName"] = profileName
        map["movieTitle"] = movieTitle
        map["username"] = username
        map["content"] = content
        map["photoUrl"] = photoUrl
        map["rating"] = rating
        map["createdAt"] = FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map

    }
}

data class ReviewFirebaseUpdateDto(
    val movieTitle: String? = null,
    val content: String? = null,
    val photoUrl: String? = null,
    val rating: Double? = null,
){
    fun toMap() : Map<String, Any?>{
        val map = mutableMapOf<String, Any?>()
        movieTitle?.let { map["movieTitle"] = it }
        content?.let { map["content"] = it }
        photoUrl?.let { map["photoUrl"] = it }
        rating?.let { map["rating"] = it }
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map
    }
}

/*
data class ReviewDto(
    val id: String,
    val author: String?,
    val content: String?,
    val url: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "author_details") val authorDetails: AuthorDetailsDto? = null
)
 */