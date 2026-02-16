package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.MovieListDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.MovieDetailsDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.MovieImagesDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewListDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.MovieVideosDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.WatchProvidersResponseDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.GenreListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// A full retrofit interface with base url and image base url constants
interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int = 1): MovieListDto

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "day",
        @Query("page") page: Int = 1
    ): MovieListDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("year") year: Int? = null,
        @Query("primary_release_year") primaryReleaseYear: Int? = null
    ): MovieListDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String? = null,
        @Query("with_genres") withGenres: String? = null,
        @Query("release_date.gte") releaseDateGte: String? = null,
        @Query("release_date.lte") releaseDateLte: String? = null,
        @Query("vote_average.gte") voteAverageGte: Double? = null,
        @Query("vote_average.lte") voteAverageLte: Double? = null,
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("with_runtime.gte") runtimeGte: Int? = null,
        @Query("with_runtime.lte") runtimeLte: Int? = null,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieListDto

    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(@Path("movie_id") movieId: Long): MovieDetailsDto

    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(@Path("movie_id") movieId: Long): MovieImagesDto

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(@Path("movie_id") movieId: Long): MovieVideosDto

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Long,
        @Query("page") page: Int = 1
    ): ReviewListDto

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(@Path("movie_id") movieId: Long): WatchProvidersResponseDto

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"   //TMDB API to request data from
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"  //Combined with image filepaths to get the actual image
    }
}
