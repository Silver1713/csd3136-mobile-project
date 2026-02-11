package com.csd3156.mobileproject.MovieReviewApp.data.remote.dto

import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.TmdbApiService
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.squareup.moshi.Json

// DTOs to convert TMDB responses into domain Movie
data class MovieDto(
    val id: Long,
    val title: String?,
    val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "release_date") val releaseDate: String?
)

fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title.orEmpty(),
    overview = overview.orEmpty(),
    posterUrl = posterPath?.let { TmdbApiService.IMAGE_BASE_URL + it }.orEmpty(),
    rating = voteAverage ?: 0.0,
    releaseDate = releaseDate.orEmpty(),
    review = ""
)
