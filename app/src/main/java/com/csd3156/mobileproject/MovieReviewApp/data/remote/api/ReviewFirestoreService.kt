package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseCreateDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseUpdateDto
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val parentCollection : String = "movies"
    private  val reviewCollection : String = "reviews"

    private val movieCollections get() = firestore.collection(parentCollection)

    private fun getReviewCollection(movieId: Int) =
        movieCollections.document(movieId.toString())
            .collection(reviewCollection)



    suspend fun getReviews(movieId: Int) : RequestResult<List<ReviewFirebaseDto>> {
        return try {
            val reviewCollection = getReviewCollection(movieId)
            val snapshot = reviewCollection
                .get()
                .await()
            val reviews = snapshot.documents.mapNotNull {
                it.toObject(ReviewFirebaseDto::class.java)
                    ?.copy(id = it.id)
            }
            RequestResult.Success(null, reviews)
        } catch (e: Exception){
            return RequestResult.Error(e.message, e)


        }
    }


    suspend fun getReview(movieId: Int, reviewId: String) : RequestResult<ReviewFirebaseDto?> {
        return try {
            val reviewCollection = getReviewCollection(movieId)
            val snapshot = reviewCollection
                .document(reviewId)
                .get()
                .await()
            if (!snapshot.exists()){
                throw Exception("Review not found")
            }
            val review = snapshot.toObject(ReviewFirebaseDto::class.java)
            val reviewWithId = review?.copy(id = snapshot.id) ?:
            throw Exception("Review is malformed")
            RequestResult.Success(null, reviewWithId)


        } catch (e: Exception){
            return RequestResult.Error(e.message, e)
        }
    }





    // Create Review
    suspend fun createReview(movieId: Int, review: ReviewFirebaseCreateDto) : RequestResult<String?> {
        return try {
            val reviewCollection = getReviewCollection(movieId)
            val docRef = reviewCollection
                .add(review.toMap())
                .await()

            RequestResult.Success(null, docRef.id)

        } catch (
            e: FirebaseException
        ){
            return RequestResult.Error(e.message, e)
        }
    }


    suspend fun updateReview(
        movieId: Int,
        reviewId: String,
        review: ReviewFirebaseUpdateDto
    )
    : RequestResult<Boolean> {
        return try {
            val reviewCollection = getReviewCollection(movieId)
            reviewCollection
                .document(reviewId)
                .update(review.toMap())
                .await()
            RequestResult.Success(null, true)
        } catch (e: Exception){
            return RequestResult.Error(e.message, e)
        }
    }


    suspend fun deleteReview(movieId: Int, reviewId: String) : RequestResult<Boolean> {
        return try {
            val reviewCollection = getReviewCollection(movieId)
            reviewCollection
                .document(reviewId)
                .delete()
                .await()
            RequestResult.Success(null, true)
        } catch (e: Exception){
            return RequestResult.Error(e.message, e)
        }
    }

    suspend fun clearReviews(movieId: Int) : RequestResult<Boolean> {
        return try {
            val reviewCollection = getReviewCollection(movieId)
            val reviews = reviewCollection
                .get()
                .await()

           val chunkedDocs = reviews.documents.chunked(500)
            chunkedDocs.forEach {
                chunk ->
                val batch = firestore.batch()
                chunk.forEach {
                    document->
                    batch.delete(document.reference)
                }
                batch.commit().await()
            }
            RequestResult.Success(null, true)
        } catch (e: Exception){
            return RequestResult.Error(e.message, e)

        }
    }






}