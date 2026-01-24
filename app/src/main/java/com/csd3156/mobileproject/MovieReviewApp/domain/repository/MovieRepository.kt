package com.csd3156.mobileproject.MovieReviewApp.domain.repository

import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import kotlinx.coroutines.flow.Flow

// A repository contract updated to live under domain.repository and to return Flow<Resource<List<Movie>>>
interface MovieRepository {
    fun getPopularMovies(): Flow<Resource<List<Movie>>>
}
