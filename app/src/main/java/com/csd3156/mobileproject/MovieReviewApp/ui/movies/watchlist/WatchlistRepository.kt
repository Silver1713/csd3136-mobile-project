package com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist

import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class WatchlistRepository @Inject constructor(
    private val dao: WatchlistDao
) {
    suspend fun addToWatchlist(movie: Movie) {
        dao.insert(
            WatchlistMovie(
                movieId = movie.id,
                title = movie.title.orEmpty(),
                posterUrl = movie.posterUrl.orEmpty(),
                releaseDate = movie.releaseDate.orEmpty(),
                rating = movie.rating
            )
        )
    }

    suspend fun removeFromWatchlist(movieId: Long) {
        dao.deleteByMovieId(movieId)
    }

    fun isInWatchlist(movieId: Long) = dao.isInWatchlist(movieId)

    fun getAllWatchlist() = dao.getAllWatchlist()


    fun getWatchlistCount() : Flow<Int> = dao.getWatchlistCount()
}
