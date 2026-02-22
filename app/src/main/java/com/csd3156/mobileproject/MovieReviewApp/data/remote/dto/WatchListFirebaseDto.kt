package com.csd3156.mobileproject.MovieReviewApp.data.remote.dto

import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistMovie
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class WatchListFirebaseDto(
    val id: String? = null,
    val uid : String? = null,
    val movieId : Long? = null,
    val movieTitle : String? = null,
    val posterPath: String? = null,
     val ratingAverage: Double? = 0.0,
     val releaseDate: String? = null,
     val firstGenre : String? = null,
    val createdAt : Timestamp? = null,
    val updatedAt : Timestamp? = null,
) {

    fun toDomain() : WatchlistMovie {
        return WatchlistMovie(
            movieId = movieId ?: 0,
            title = movieTitle ?: "UNTITLED MOVIE",
            posterUrl = posterPath ?: "",
            rating = ratingAverage ?: 0.0,
            releaseDate = releaseDate ?: "UNSPECIFIED",
            firstGenres = firstGenre,
            savedAt = updatedAt?.toInstant()?.toEpochMilli() ?: 0
        )
    }

}


data class CreateWatchListFirebaseDto(
    val uid : String?,
    val movieId : Long?,
    val movieTitle : String?,
    val posterPath: String?,
    val ratingAverage: Double?,
    val releaseDate: String? = null,
    val firstGenre : String? = null
){
    fun toMap() : Map<String, Any?>{
        val map = mutableMapOf<String, Any?>()
        map["uid"] = uid
        map["movieId"] = movieId
        map["movieTitle"] = movieTitle
        map["posterPath"] = posterPath
        map["ratingAverage"] = ratingAverage
        map["releaseDate"] = releaseDate
        map["firstGenre"] = firstGenre
        map["createdAt"] = FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map

    }
}

data class UpdateWatchListFirebaseDto(
    val id: String = "",
    val movieId : Long? = null,
    val movieTitle : String? = null,
    val posterPath: String? = null,
    val ratingAverage: Double? = null,
    val releaseDate: String? = null,
    val firstGenre : String? = null

){
    fun toMap() : Map<String, Any?>{
        val map = mutableMapOf<String, Any?>()
        movieId?.let { map["movieId"] = it }
        movieTitle?.let { map["movieTitle"] = it }
        posterPath?.let { map["posterPath"] = it }
        ratingAverage?.let { map["ratingAverage"] = it }
        releaseDate?.let { map["releaseDate"] = it}
        firstGenre?.let { map["firstGenre"] = it }
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map

    }
}