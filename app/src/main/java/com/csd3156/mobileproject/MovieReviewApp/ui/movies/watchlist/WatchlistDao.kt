package com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: WatchlistMovie)

    @Query("DELETE FROM watchlist_movies WHERE movieId = :movieId")
    suspend fun deleteByMovieId(movieId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE movieId = :movieId)")
    fun isInWatchlist(movieId: Long): Flow<Boolean>

    @Query("SELECT * FROM watchlist_movies ORDER BY savedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistMovie>>

    @Query("SELECT COUNT(*) FROM watchlist_movies")
    fun getWatchlistCount() : Flow<Int>
}
