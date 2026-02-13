package com.csd3156.mobileproject.MovieReviewApp.domain.model

data class MovieVideo(
    val id: String,
    val name: String,
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean,
    val publishedAt: String
) {
    val isYoutubeTrailer: Boolean
        get() = site.equals("YouTube", ignoreCase = true) && type.equals("Trailer", ignoreCase = true)

    val youtubeWatchUrl: String
        get() = "https://www.youtube.com/embed/$key"
}
