package com.csd3156.mobileproject.MovieReviewApp.data.local

import android.content.Context
import com.csd3156.mobileproject.MovieReviewApp.di.IoDispatcher
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.LocalReviewRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class LocalReviewRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalReviewRepository {

    override fun getReviewsForMovie(movieId: Long): Flow<List<MovieReview>> {
        return reviewDao.observeMovieReviews(movieId).map { reviews ->
            reviews.map { it.toDomain() }
        }
    }

    override suspend fun addReview(
        movieId: Long,
        author: String,
        rating: Double?,
        content: String,
        photoPath: String?
    ) {
        val entity = ReviewEntity(
            movieId = movieId,
            author = author.ifBlank { "You" },
            content = content.trim(),
            rating = rating,
            createdAtMillis = System.currentTimeMillis(),
            photoPath = photoPath,
            id = 0,

        )
        withContext(ioDispatcher) {
            reviewDao.insert(entity)
        }
    }

    private fun ReviewEntity.toDomain(): MovieReview {


        return MovieReview(
            id = "local-$id",
            author = author,
            content = content,
            url = "",
            rating = rating,
            createdAt = Instant.ofEpochMilli(createdAtMillis),
            photoPath = photoPath
        )
    }
}
