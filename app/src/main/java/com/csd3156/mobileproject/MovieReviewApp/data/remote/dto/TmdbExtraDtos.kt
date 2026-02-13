package com.csd3156.mobileproject.MovieReviewApp.data.remote.dto

import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.TmdbApiService
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Genre
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieVideo
import com.csd3156.mobileproject.MovieReviewApp.domain.model.WatchProvider
import com.squareup.moshi.Json

data class GenreDto(
    val id: Long,
    val name: String
)

data class GenreListDto(
    val genres: List<GenreDto>
)

fun GenreDto.toDomain(): Genre = Genre(id = id, name = name)

data class MovieDetailsDto(
    val id: Long,
    val title: String?,
    val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "release_date") val releaseDate: String?,
    val genres: List<GenreDto> = emptyList(),
    val runtime: Long?,
    @Json(name = "original_language") val originalLanguage: String?,
    val tagline: String?,
    val status: String?,
    val popularity: Double?,
    val adult: Boolean?
)

fun MovieDetailsDto.toDomain(): MovieDetails = MovieDetails(
    id = id,
    title = title.orEmpty(),
    overview = overview.orEmpty(),
    posterUrl = posterPath?.let { TmdbApiService.IMAGE_BASE_URL + it }.orEmpty(),
    backdropUrl = backdropPath?.let { TmdbApiService.IMAGE_BASE_URL + it }.orEmpty(),
    rating = voteAverage ?: 0.0,
    voteCount = voteCount ?: 0,
    releaseDate = releaseDate.orEmpty(),
    genres = genres.map { it.toDomain() },
    runtimeMinutes = runtime ?: 0L,
    originalLanguage = originalLanguage.orEmpty(),
    tagline = tagline.orEmpty(),
    status = status.orEmpty(),
    popularity = popularity ?: 0.0,
    adult = adult ?: false
)

data class MovieImagesDto(
    val id: Long,
    val backdrops: List<ImageItemDto> = emptyList(),
    val logos: List<ImageItemDto> = emptyList(),
    val posters: List<ImageItemDto> = emptyList()
)

data class ImageItemDto(
    @Json(name = "aspect_ratio") val aspectRatio: Double?,
    val height: Int?,
    @Json(name = "iso_639_1") val iso6391: String?,
    @Json(name = "file_path") val filePath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    val width: Int?
)

data class MovieVideosDto(
    val id: Long,
    val results: List<VideoItemDto> = emptyList()
)

data class VideoItemDto(
    @Json(name = "iso_639_1") val iso6391: String?,
    @Json(name = "iso_3166_1") val iso31661: String?,
    val name: String?,
    val key: String?,
    val site: String?,
    val size: Int?,
    val type: String?,
    val official: Boolean?,
    @Json(name = "published_at") val publishedAt: String?,
    val id: String?
)

fun VideoItemDto.toDomain(): MovieVideo = MovieVideo(
    id = id.orEmpty(),
    name = name.orEmpty(),
    key = key.orEmpty(),
    site = site.orEmpty(),
    type = type.orEmpty(),
    official = official ?: false,
    publishedAt = publishedAt.orEmpty()
)

data class ReviewListDto(
    val id: Long,
    val page: Int,
    val results: List<ReviewDto> = emptyList(),
    @Json(name = "total_pages") val totalPages: Int? = null,
    @Json(name = "total_results") val totalResults: Int? = null
)

data class ReviewDto(
    val id: String,
    val author: String?,
    val content: String?,
    val url: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "author_details") val authorDetails: AuthorDetailsDto? = null
)

data class AuthorDetailsDto(
    val rating: Double? = null
)

data class WatchProvidersResponseDto(
    val id: Long,
    val results: Map<String, CountryWatchProvidersDto> = emptyMap()
)

data class CountryWatchProvidersDto(
    val link: String?,
    val flatrate: List<ProviderDto> = emptyList(),
    val rent: List<ProviderDto> = emptyList(),
    val buy: List<ProviderDto> = emptyList()
)

data class ProviderDto(
    @Json(name = "provider_id") val providerId: Long,
    @Json(name = "provider_name") val providerName: String?,
    @Json(name = "logo_path") val logoPath: String?
)

fun ReviewDto.toDomain(): MovieReview = MovieReview(
    id = id,
    author = author.orEmpty(),
    content = content.orEmpty(),
    url = url.orEmpty(),
    rating = authorDetails?.rating,
    createdAt = createdAt.orEmpty()
)

fun CountryWatchProvidersDto.toDomain(countryCode: String): List<WatchProvider> {
    val allProviders = mutableListOf<WatchProvider>()
    flatrate.forEach { provider ->
        allProviders.add(provider.toDomain(type = "flatrate", countryCode = countryCode, link = link))
    }
    rent.forEach { provider ->
        allProviders.add(provider.toDomain(type = "rent", countryCode = countryCode, link = link))
    }
    buy.forEach { provider ->
        allProviders.add(provider.toDomain(type = "buy", countryCode = countryCode, link = link))
    }
    return allProviders
}

private fun ProviderDto.toDomain(type: String, countryCode: String, link: String?): WatchProvider =
    WatchProvider(
        id = providerId,
        name = providerName.orEmpty(),
        logoUrl = logoPath?.let { TmdbApiService.IMAGE_BASE_URL + it }.orEmpty(),
        type = type,
        countryCode = countryCode,
        link = link.orEmpty()
    )
