package com.csd3156.mobileproject.MovieReviewApp.domain.model

data class MovieDetails(
    val id: Long,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val backdropUrl: String,
    val rating: Double,
    val voteCount: Int,
    val releaseDate: String,
    val genres: List<Genre>,
    val runtimeMinutes: Long,
    val originalLanguage: String,
    val tagline: String,
    val status: String,
    val popularity: Double,
    val adult: Boolean
) {
    fun getFormattedRuntime(): String {
        val hours = runtimeMinutes / 60
        val minutes = runtimeMinutes % 60
        return "${hours}h ${minutes}m"
    }
}
