package com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist

import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.FirebaseAuthService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.WatchlistFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.CreateWatchListFirebaseDto
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class WatchlistRepository @Inject constructor(
    private val accountAuth: FirebaseAuthService,
    private val watchlistFirestoreService: WatchlistFirestoreService,
    private val dao: WatchlistDao
) {
    suspend fun addToWatchlist(movie: Movie) {
//
        val uid = accountAuth.GetActiveUserID() ?: throw Exception("User not logged in")
        val createDto: CreateWatchListFirebaseDto = CreateWatchListFirebaseDto(
            uid = uid,
            movieId = movie.id,
            movieTitle = movie.title.orEmpty(),
            posterPath = movie.posterUrl.orEmpty(),
            ratingAverage = movie.rating,
            releaseDate = movie.releaseDate,
            firstGenre = movie.genres?.firstOrNull() ?: "Unspecified"

        )

        when (val result = watchlistFirestoreService.addMovie(uid, createDto)) {
            is RequestResult.Success -> {
                //Create Local DAO
                dao.insert(
                    WatchlistMovie(

                        movieId = movie.id,
                        title = movie.title.orEmpty(),
                        posterUrl = movie.posterUrl.orEmpty(),
                        releaseDate = movie.releaseDate.orEmpty(),
                        rating = movie.rating,
                        firstGenres = movie.genres?.firstOrNull() ?: "Unspecified"
                    )
                )
            }

            is RequestResult.Error -> {
                throw Exception(result.message)

            }
        }

    }

    suspend fun removeFromWatchlist(movieId: Long) {
        val uid = accountAuth.GetActiveUserID() ?: throw Exception("User not logged in")
        when (val result = watchlistFirestoreService.deleteMovie(uid, movieId.toString())) {
            is RequestResult.Success -> {
                dao.deleteByMovieId(movieId)
            }

            is RequestResult.Error -> {
                throw Exception(result.message)
            }
        }

    }




    fun isInWatchlist(movieId: Long) = dao.isInWatchlist(movieId)

    fun getAllWatchlist() = dao.getAllWatchlist()


    suspend fun refreshWatchlist() {
        val uid = accountAuth.GetActiveUserID() ?: throw Exception("User not logged in")
        when (val result = watchlistFirestoreService.getMovies(uid)) {
            is RequestResult.Success -> {
                // Clear db
                dao.clearAll()
                result.data.forEach {
                    dao.insert(
                        WatchlistMovie(
                            movieId = it.movieId ?: 0,
                            title = it.movieTitle ?: "Unknown Title",
                            posterUrl = it.posterPath ?: "",
                            rating = it.ratingAverage ?: 0.0,
                            releaseDate = it.releaseDate ?: "Unknown",
                            firstGenres = it.firstGenre ?: "Unknown"
                        )
                    )
                }

            }
            is RequestResult.Error -> {
                throw Exception(result.message)
            }
        }
    }


    fun getWatchlistCount(): Flow<Int> = dao.getWatchlistCount()
}
