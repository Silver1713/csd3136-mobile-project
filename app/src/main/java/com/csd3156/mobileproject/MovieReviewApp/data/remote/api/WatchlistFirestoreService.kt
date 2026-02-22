package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.CreateWatchListFirebaseDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.ReviewFirebaseDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.WatchListFirebaseDto
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class WatchlistFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val ACCOUNT_COLLECTION_NAME = "accounts"
    private val WATCHLIST_COLLECTION_NAME = "watchlist"

    private val accountCollection get() = firestore.collection(ACCOUNT_COLLECTION_NAME)

    fun getWatchlistCollection(uid: String) =
        accountCollection.document(uid)
            .collection(WATCHLIST_COLLECTION_NAME)

    suspend fun addMovie(uid: String, movie: CreateWatchListFirebaseDto): RequestResult<String?> {
        return try {
            val watchlistCollection = getWatchlistCollection(uid) // Bubble and get caught if
            // user
            val newDoc : DocumentReference = watchlistCollection.add(movie.toMap())
                .await()
            RequestResult.Success(null, newDoc.id)
        } catch (e: Exception) {
            RequestResult.Error(e.message, e)
        }
    }

    suspend fun getMovies(uid: String): RequestResult<List<WatchListFirebaseDto>>{
        return try {
            val watchlistCollection = getWatchlistCollection(uid)
            val snapshot = watchlistCollection
                .get()
                .await()
            val movies = snapshot.documents.mapNotNull {
                it.toObject(WatchListFirebaseDto::class.java)
                    ?.copy(id = it.id)

            }
            RequestResult.Success(null, movies)
        } catch (e: Exception) {
            RequestResult.Error(e.message, e)
        }

    }

    suspend fun clearMovies(uid: String) : RequestResult<Unit> {
        return try {
            val watchlistCollection = getWatchlistCollection(uid)
            val movies = watchlistCollection
                .get().await()
            val chunkedDocs = movies.documents.chunked(500)
            for (chunk in chunkedDocs) {
                val batch = firestore.batch()
                for (document in chunk) {
                    batch.delete(document.reference)
                }
                batch.commit().await()
            }
            return RequestResult.Success(null, Unit)
        } catch (e: Exception) {
            RequestResult.Error(e.message, e)
        }
    }

    suspend fun deleteMovie(uid: String, movieId: String) : RequestResult<Unit> {
        return try {
            val watchlistCollection = getWatchlistCollection(uid)
            val movie = watchlistCollection
                .document(movieId)
                .delete()
                .await()
            RequestResult.Success(null, Unit)
        } catch (e: Exception) {
            RequestResult.Error(e.message, e)
        }
    }

}