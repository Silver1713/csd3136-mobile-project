package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.MovieListDto
import retrofit2.http.GET
import retrofit2.http.Query

// A full retrofit interface with base url and image base url constants
interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int = 1): MovieListDto

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    }
}
