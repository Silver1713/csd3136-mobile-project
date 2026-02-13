package com.csd3156.mobileproject.MovieReviewApp.domain.repository

import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Genre
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieVideo
import com.csd3156.mobileproject.MovieReviewApp.domain.model.WatchProvider
import kotlinx.coroutines.flow.Flow

// A repository contract updated to live under domain.repository and to return Flow<Resource<List<Movie>>>
interface MovieRepository {
    fun getPopularMovies(page: Int = 1): Flow<Resource<List<Movie>>>
    fun getTrendingMovies(timeWindow: String = "day", page: Int = 1): Flow<Resource<List<Movie>>>
    fun searchMovies(
        query: String,
        page: Int = 1,
        includeAdult: Boolean = false,
        year: Int? = null,
        primaryReleaseYear: Int? = null
    ): Flow<Resource<List<Movie>>>
    fun getMoviesByGenre(genreIds: List<Long>, page: Int = 1): Flow<Resource<List<Movie>>>
    fun discoverMovies(
        page: Int = 1,
        sortBy: String? = null,
        genreIds: List<Long>? = null,
        releaseDateGte: String? = null,
        releaseDateLte: String? = null,
        voteAverageGte: Double? = null,
        voteAverageLte: Double? = null,
        voteCountGte: Int? = null,
        runtimeGte: Int? = null,
        runtimeLte: Int? = null,
        includeAdult: Boolean = false
    ): Flow<Resource<List<Movie>>>
    fun getMovieGenres(): Flow<Resource<List<Genre>>>
    fun getMovieDetails(movieId: Long): Flow<Resource<MovieDetails>>
    fun getMovieReviews(movieId: Long, page: Int = 1): Flow<Resource<List<MovieReview>>>
    fun getMovieVideos(movieId: Long): Flow<Resource<List<MovieVideo>>>
    fun getMovieWatchProviders(movieId: Long, countryCode: String = "US"): Flow<Resource<List<WatchProvider>>>
}
