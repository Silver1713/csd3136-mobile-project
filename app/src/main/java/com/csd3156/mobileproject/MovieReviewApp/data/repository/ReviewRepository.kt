package com.csd3156.mobileproject.MovieReviewApp.data.repository

import com.csd3156.mobileproject.MovieReviewApp.data.local.ReviewDao
import com.csd3156.mobileproject.MovieReviewApp.data.local.ReviewEntity
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountDAO
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.AccountFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.ReviewFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseCreateDto
import com.csd3156.mobileproject.MovieReviewApp.di.IoDispatcher
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val accountAuth: FirebaseAuth,
    private val accountDAO: AccountDAO,
    private val reviewFirestoreService: ReviewFirestoreService,
    private val reviewDao: ReviewDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {




    suspend fun addReview(
        movieId: Long,
        movieTitle: String,
        rating: Double?,
        content: String,
        photoPath: String?
    ): RequestResult<Unit> {
        return try {
            val cachedCurrentAccount = accountDAO.getOne()
                .firstOrNull() ?: throw Exception("No active account")
            val userId = accountAuth.currentUser?.uid ?: throw Exception("No active account")
            //REMOTE ADD
            val createDtoObj: ReviewFirebaseCreateDto = ReviewFirebaseCreateDto(
                uid = userId,
                movieTitle = movieTitle,
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
                        movieTitle = movieTitle,
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
                            movieTitle = review.movieTitle ?: "Unknown Title",
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


     fun getCachedUserReviews() : Flow<List<MovieReview>> {
         return try {
             val uid = accountAuth.currentUser?.uid
             if (uid != null) {
                 reviewDao.observeUserReviews(uid).map { reviews ->
                     reviews.map { it.toDomain() }
                 }
             } else {
                 throw Exception("No active account")
             }
         } catch (e: Exception) {
             flowOf(emptyList())
         }
    }

    suspend fun refreshUserReviews(): RequestResult<Unit> {
        return try {
            val uid = accountAuth.currentUser?.uid
            if (uid != null) {
                val reviewsResult = reviewFirestoreService.getReviewsByUser(uid)
                when (reviewsResult) {
                    is RequestResult.Success -> {
                        reviewDao.deleteAll()
                        reviewsResult.data.forEach {
                            review ->
                            val entity = ReviewEntity(
                                id = 0,
                                movieId = review.movieId ?: 0,
                                movieTitle = review.reviewDto?.movieTitle ?: "Unknown Title",
                                author = review.reviewDto?.profileName ?: "Unknown",
                                content = review.reviewDto?.content ?: "",
                                rating = review.reviewDto?.rating ?: 0.0,
                                createdAtMillis = review.reviewDto?.createdAt?.toDate()?.time
                                    ?: System.currentTimeMillis(),
                                photoPath = review.reviewDto?.photoUrl,
                                reviewId = review.reviewId ?: "",
                                userId = review.reviewDto?.uid ?: ""
                            )
                            reviewDao.insert(entity)

                        }
                        RequestResult.Success(null, Unit)
                    }

                    is RequestResult.Error -> {
                        RequestResult.Error(reviewsResult.message, reviewsResult.exception)
                    }
                }
            }
            else {
                throw Exception("No active account selected")
            }

        } catch (e: Exception){
            RequestResult.Error(e.message, e)
        }
    }


    fun getCachedUserReviewCount(): Flow<Int> {
        val uid = accountAuth.currentUser?.uid
        if (uid != null) {
            return reviewDao.observeUserReviewCount(uid)
        }
        return emptyFlow()
    }



}