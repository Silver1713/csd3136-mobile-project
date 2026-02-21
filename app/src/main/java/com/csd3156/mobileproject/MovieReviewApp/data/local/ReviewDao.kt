package com.csd3156.mobileproject.MovieReviewApp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM movie_reviews WHERE movieId = :movieId ORDER BY createdAtMillis DESC")
    fun observeMovieReviews(movieId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM movie_reviews WHERE userId = :uid ORDER BY createdAtMillis DESC")
    fun observeUserReviews(uid: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM movie_reviews")
    fun observeAllReviews(): Flow<List<ReviewEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewEntity)


    @Query("DELETE FROM movie_reviews WHERE movieId = :movieId")
    suspend fun deleteMovieReviews(movieId: Long)

    @Query("DELETE FROM movie_reviews")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM movie_reviews WHERE userId = :userId")
    fun observeUserReviewCount(userId: String): Flow<Int>
}
