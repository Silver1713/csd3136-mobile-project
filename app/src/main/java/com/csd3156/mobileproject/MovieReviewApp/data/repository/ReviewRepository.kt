package com.csd3156.mobileproject.MovieReviewApp.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.csd3156.mobileproject.MovieReviewApp.data.local.ReviewDao
import com.csd3156.mobileproject.MovieReviewApp.data.local.ReviewEntity
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountDAO
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.ReviewFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.ReviewStorageService
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
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseUpdateDto
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.io.File

class ReviewRepository @Inject constructor(
    private val accountAuth: FirebaseAuth,
    private val accountDAO: AccountDAO,
    private val reviewFirestoreService: ReviewFirestoreService,
    private val reviewDao: ReviewDao,
    private val reviewCloudStoreService : ReviewStorageService,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun localToCompressJPEG(
        path: String?,
        maxSizePx: Int = 1080,
        quality: Int = 80
    ): ByteArray? {
        val safePath = path ?: return null
        val original = BitmapFactory.decodeFile(safePath) ?: return null
        val oriented = applyExifOrientation(safePath, original)
        val resized = resizeKeepAspect(oriented, maxSizePx)

        return try {
            ByteArrayOutputStream().use { out ->
                resized.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.toByteArray()
            }
        } finally {
            if (resized !== oriented) resized.recycle()
            if (oriented !== original) oriented.recycle()
            original.recycle()
        }
    }



    private fun resizeKeepAspect(
        bitmap: Bitmap,
        maxSizePx : Int
    ) : Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxSizePx && h <= maxSizePx) {
            return bitmap
        }
        val scale = if (w >= h) maxSizePx.toFloat() / w else maxSizePx.toFloat() / h
        val scaleW = (scale * w).roundToInt().coerceAtLeast(1)
        val scaleH = (scale * h).roundToInt().coerceAtLeast(1)
        return bitmap.scale(scaleW, scaleH)
    }

    private fun applyExifOrientation(path: String, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        val orientation = runCatching {
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(90f)
            }
            else -> return bitmap
        }

        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrElse { bitmap }
    }

    suspend fun addReview(
        movieId: Long,
        movieTitle: String,
        rating: Double?,
        content: String,
        photoPath: String?
    ): RequestResult<Unit> {
        val localTempPhotoPath = photoPath?.takeIf {
            it.isNotBlank() && !it.startsWith("http", ignoreCase = true)
        }
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
            var currentPath = photoPath
            val reviewResult = reviewFirestoreService.createReview(movieId.toInt(), createDtoObj)
            when (reviewResult) {
                is RequestResult.Success -> {
                    // Upload to cloud for photo using that review
                    // REMOTE UPLOAD MULTIMEDIA
                    if (photoPath?.isNotEmpty() ?: false) {
                        val localPath = photoPath ?: throw Exception("No photo path")
                        val cloudData : ByteArray = localToCompressJPEG(localPath) ?:
                        throw Exception("Failed to compress image")
                        if (reviewResult.data != null) {
                            val uploadResult = reviewCloudStoreService.uploadImage(userId,
                                reviewResult.data,
                                cloudData)
                            if (uploadResult is RequestResult.Error) {
                                throw Exception(uploadResult.message)
                            } else if (uploadResult is RequestResult.Success) {
                                val updateDto : ReviewFirebaseUpdateDto = ReviewFirebaseUpdateDto(
                                    photoUrl = uploadResult.data
                                )

                                val updateResult = reviewFirestoreService.updateReview(
                                    movieId.toInt(),
                                    reviewResult.data,
                                    updateDto
                                )

                                if (updateResult is RequestResult.Error) {
                                    throw Exception(updateResult.message)
                                } else if (updateResult is RequestResult.Success) {
                                    currentPath = uploadResult.data
                                }

                            }
                        }


                    }



                    //Add to Room database (LOCAL CACHE)
                    val entity = ReviewEntity(
                        movieId = movieId,
                        movieTitle = movieTitle,
                        author = cachedCurrentAccount.name ?: "YOU",
                        content = content.trim(),
                        rating = rating,
                        createdAtMillis = System.currentTimeMillis(),
                        photoPath = currentPath,
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
            Log.d("ReviewRepository", "addReview: ${e.message}")
            RequestResult.Error(e.message, e)
        } finally {
            localTempPhotoPath?.let(::deleteLocalFileIfExists)
        }
    }

    private fun deleteLocalFileIfExists(path: String) {
        runCatching {
            val file = File(path)
            if (file.exists()) file.delete()
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


    suspend fun uploadReviewCloudImage(reviewId: String, imageUri : Uri) : String? {
        val uid = accountAuth.currentUser?.uid
        if (uid != null) {
            val result = reviewCloudStoreService.uploadImage(uid, reviewId, imageUri)
            if (result is RequestResult.Success) {
                return result.data
            }
            return null
        }
        return null
    }
    suspend fun deleteReviewCloudImage(reviewId: String) : Boolean {
        val uid = accountAuth.currentUser?.uid
        if (uid != null) {
            val result = reviewCloudStoreService.deleteImage(uid, reviewId)
            return result is RequestResult.Success
        }
        return false
    }
}
