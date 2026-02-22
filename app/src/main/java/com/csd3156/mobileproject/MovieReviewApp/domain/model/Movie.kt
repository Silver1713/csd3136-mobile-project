package com.csd3156.mobileproject.MovieReviewApp.domain.model

// Domain model to define the movie data used across the different layers of the project
data class Movie(
    val id: Long,
    val title: String?,
    val genres : List<String>?,
    val overview: String?,
    val posterUrl: String?,
    val rating: Double,
    val releaseDate: String,
    val review: String?,
    val watchTimeInSeconds : Long,
    val originalLanguage: String,
    val adult: Boolean?,
    val voteCount: Int?
){
    fun getFormattedTime() : String{
        val hours = watchTimeInSeconds / 3600
        val min = watchTimeInSeconds % 3600 / 60
        return "${hours}h ${min}m"
    }
}
