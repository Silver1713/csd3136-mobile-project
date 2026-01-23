package com.csd3156.mobileproject.MovieReviewApp.domain.repository

import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository{
    fun getPopularMovies(): Flow<List<Movie>>
    fun getMovieReviews(movieId: Long): Flow<List<String>>
}