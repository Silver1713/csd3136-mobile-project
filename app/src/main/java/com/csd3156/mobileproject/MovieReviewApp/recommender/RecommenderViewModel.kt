package com.csd3156.mobileproject.MovieReviewApp.recommender

import android.R.attr.resource
import android.content.Context
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.local.LocalReviewRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.data.local.MovieReviewDatabase
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistMovie
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistRepository
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.TmdbApiService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.toDomain
import com.csd3156.mobileproject.MovieReviewApp.data.repository.MovieRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.csd3156.mobileproject.MovieReviewApp.domain.model.toMovie
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.LocalReviewRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import com.csd3156.mobileproject.MovieReviewApp.ui.movies.list.MovieListViewModel
import com.csd3156.mobileproject.MovieReviewApp.ui.movies.list.MovieListViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*

val db = remember { MovieReviewDatabase.getInstance(context) }
    val watchlistVM = remember { WatchlistViewModel(WatchlistRepository(db.watchlistDao())) }

 */

class RecommenderViewModel(
    private val applicationContext: Context,
    private val watchlistRepository: WatchlistRepository,
    private val movieRepository : MovieRepository
)  : ViewModel() {
    //Recommended movie data as stateflow
    private val _recommendedMovies = MutableStateFlow<List<Movie>>(emptyList())

    // Readonly version
    val recommendedMovies: StateFlow<List<Movie>> = _recommendedMovies.asStateFlow()

    init {  //Get the recommended movie list.
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            //Turn watchlistmovie into movie ids
            var userWatchListID = watchlistRepository.getAllWatchlist()
                .first()
                .map { it.movieId }

            //Turn IDs into movies
            var userWatchlist = mutableListOf<Movie>();
            userWatchListID.asFlow()
                .flatMapConcat { movieId ->
                    movieRepository.getMovieDetails(movieId)
                }
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {}
                        is Resource.Success -> userWatchlist.add(resource.data.toMovie())
                        is Resource.Error   -> {}
                    }
                }

            //Use recommender model.
            withContext(Dispatchers.Default){
                val results = Recommender.getInstance(applicationContext.applicationContext).GetRecommendations(userWatchlist, 50);
                var recommendedMovieList = mutableListOf<Movie>();
                results.asFlow()
                    .flatMapConcat { scoreResult ->
                        movieRepository.getMovieDetails(scoreResult.movieId)
                    }
                    .collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {}
                            is Resource.Success -> recommendedMovieList.add(resource.data.toMovie())
                            is Resource.Error   -> {}
                        }
                    }
                _recommendedMovies.value = recommendedMovieList;
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application context from extras
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

                // Initialize dependencies
                val movieRepository = MovieRepositoryImpl.create()
                val db = MovieReviewDatabase.getInstance(application)
                val watchListRepository = WatchlistRepository(db.watchlistDao())

                return RecommenderViewModel(
                    application,
                    watchListRepository,
                    movieRepository
                ) as T
            }
        }
    }
}