package com.csd3156.mobileproject.MovieReviewApp.domain.repository

import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import kotlinx.coroutines.flow.Flow

interface LocalReviewRepository {
    fun getReviewsForMovie(movieId: Long): Flow<List<MovieReview>>
    suspend fun addReview(movieId: Long, author: String, rating: Double?, content: String, photoPath: String?)
}
