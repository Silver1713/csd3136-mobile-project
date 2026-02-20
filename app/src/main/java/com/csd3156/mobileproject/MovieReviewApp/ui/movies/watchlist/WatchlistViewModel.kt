package com.csd3156.mobileproject.MovieReviewApp.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repo: WatchlistRepository
) : ViewModel() {

    fun isSaved(movieId: Long): Flow<Boolean> = repo.isInWatchlist(movieId)

    fun toggle(movie: Movie, isCurrentlySaved: Boolean) {
        viewModelScope.launch {
            if (isCurrentlySaved) repo.removeFromWatchlist(movie.id)
            else repo.addToWatchlist(movie)
        }
    }

    val watchlist = repo.getAllWatchlist()
}
