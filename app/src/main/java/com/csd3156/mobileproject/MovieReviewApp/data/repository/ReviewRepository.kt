package com.csd3156.mobileproject.MovieReviewApp.data.repository

import com.csd3156.mobileproject.MovieReviewApp.data.local.ReviewDao
import com.csd3156.mobileproject.MovieReviewApp.data.local.ReviewEntity
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.ReviewFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseCreateDto
import com.csd3156.mobileproject.MovieReviewApp.di.IoDispatcher
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val accountRepository: AccountRepository,
    private val reviewFirestoreService: ReviewFirestoreService,
    private val reviewDao: ReviewDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {




    suspend fun addReview(
        movieId: Long,
        rating: Double?,
        content: String,
        photoPath: String?
    ): RequestResult<Unit> {
        return try {
            val cachedCurrentAccount = accountRepository.getActiveAccountRoom()
                .firstOrNull() ?: throw Exception("No active account")
            val userId = cachedCurrentAccount.uid
            //REMOTE ADD
            val createDtoObj: ReviewFirebaseCreateDto = ReviewFirebaseCreateDto(
                uid = userId,
                profileName = cachedCurrentAccount.name,
                username = cachedCurrentAccount.username,
                content = content.trim(),
                photoUrl = photoPath,
                rating = rating
            )

            val reviewResult = reviewFirestoreService.createReview(movieId.toInt(), createDtoObj)
            when (reviewResult) {
                is RequestResult.Success -> {
                    //Add to Room database (LOCAL CACHE)
                    val entity = ReviewEntity(
                        movieId = movieId,
                        author = cachedCurrentAccount.name ?: "YOU",
                        content = content.trim(),
                        rating = rating,
                        createdAtMillis = System.currentTimeMillis(),
                        photoPath = photoPath,
                        id = 0,
                        reviewId = reviewResult.data,
                        userId = userId
                    )
                    withContext(ioDispatcher) {
                        reviewDao.insert(entity)
                    }
                    RequestResult.Success(null, Unit)
                }

                is RequestResult.Error -> {
                    throw Exception(reviewResult.message)
                }
            }
        } catch (e: Exception) {
            RequestResult.Error(e.message, e)
        }
    }


    suspend fun refreshMovieReviews(movieId: Long): RequestResult<Unit> {
        when (val reviews = reviewFirestoreService.getReviews(movieId.toInt())) {
            is RequestResult.Success -> {
                // Clear db
                withContext(ioDispatcher) {
                    reviewDao.deleteMovieReviews(movieId)
                    reviews.data.forEach { review ->
                        val entity = ReviewEntity(
                            id = 0,
                            movieId = movieId,
                            author = review.profileName ?: "YOU",
                            content = review.content ?: "",
                            rating = review.rating ?: 0.0,
                            createdAtMillis = review.createdAt?.toDate()?.time
                                ?: System.currentTimeMillis(),
                            photoPath = review.photoUrl,
                            reviewId = review.id ?: "",
                            userId = review.uid ?: ""
                        )
                        reviewDao.insert(entity)
                    }
                }
                return RequestResult.Success(null, Unit)

            }

            is RequestResult.Error -> {
                return RequestResult.Error(reviews.message, reviews.exception)
            }

        }


    }

     fun getCachedReviews(movieId: Long) : Flow<List<MovieReview>> {
         return reviewDao.observeMovieReviews(movieId).map { reviews ->
             reviews.map { it.toDomain() }
         }
    }


}