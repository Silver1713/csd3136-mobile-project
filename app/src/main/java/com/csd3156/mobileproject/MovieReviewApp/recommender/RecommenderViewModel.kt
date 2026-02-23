package com.csd3156.mobileproject.MovieReviewApp.recommender

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.model.toMovie
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

/*

val db = remember { MovieReviewDatabase.getInstance(context) }
    val watchlistVM = remember { WatchlistViewModel(WatchlistRepository(db.watchlistDao())) }

 */

@HiltViewModel
class RecommenderViewModel  @Inject constructor(
    private val reccomender : Recommender,
    private val watchlistRepository: WatchlistRepository,
    private val movieRepository : MovieRepository
)  : ViewModel() {
    companion object {
        private const val MAX_RECOMMENDATIONS = 60
        private const val RECOMMENDATION_PAGE_SIZE = 12
    }

    // Recommended movie data as stateflow.
    private val _recommendedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val recommendedMovies: StateFlow<List<Movie>> = _recommendedMovies.asStateFlow()

    private val _isFetchingRecommendations = MutableStateFlow(false)
    val isFetchingRecommendations: StateFlow<Boolean> = _isFetchingRecommendations.asStateFlow()

    private val _isLoadingMoreRecommendations = MutableStateFlow(false)
    val isLoadingMoreRecommendations: StateFlow<Boolean> = _isLoadingMoreRecommendations.asStateFlow()

    private val _recommendationsEndReached = MutableStateFlow(false)
    val recommendationsEndReached: StateFlow<Boolean> = _recommendationsEndReached.asStateFlow()

    private val _allRecommendationMovieIds = MutableStateFlow<List<Long>>(emptyList())
    private var nextRecommendationIndex = 0

    init {
        fetchData()
    }

    fun fetchData() {
        if (_isFetchingRecommendations.value) return
        viewModelScope.launch {
            _isFetchingRecommendations.value = true
            try {
                val userWatchlistIds = watchlistRepository.getAllWatchlist()
                    .first()
                    .map { it.movieId }

                val userWatchlist = mutableListOf<Movie>()
                for (movieId in userWatchlistIds) {
                    val movie = fetchMovieById(movieId)
                    if (movie != null) {
                        userWatchlist.add(movie)
                    }
                }

                val recommendationMovieIds = withContext(Dispatchers.Default) {
                    reccomender.GetRecommendations(userWatchlist, MAX_RECOMMENDATIONS)
                        .map { it.movieId }
                }

                _allRecommendationMovieIds.value = recommendationMovieIds
                _recommendedMovies.value = emptyList()
                nextRecommendationIndex = 0
                _recommendationsEndReached.value = recommendationMovieIds.isEmpty()
            } catch (_: Exception) {
                _allRecommendationMovieIds.value = emptyList()
                _recommendedMovies.value = emptyList()
                nextRecommendationIndex = 0
                _recommendationsEndReached.value = true
            } finally {
                _isFetchingRecommendations.value = false
            }

            loadNextRecommendationsPage()
        }
    }

    fun loadNextRecommendationsPage() {
        if (_isFetchingRecommendations.value || _isLoadingMoreRecommendations.value || _recommendationsEndReached.value) {
            return
        }
        viewModelScope.launch {
            val allRecommendationIds = _allRecommendationMovieIds.value
            if (nextRecommendationIndex >= allRecommendationIds.size) {
                _recommendationsEndReached.value = true
                return@launch
            }

            _isLoadingMoreRecommendations.value = true
            try {
                val endExclusive = min(nextRecommendationIndex + RECOMMENDATION_PAGE_SIZE, allRecommendationIds.size)
                val pageMovieIds = allRecommendationIds.subList(nextRecommendationIndex, endExclusive)
                nextRecommendationIndex = endExclusive

                val pageMovies = mutableListOf<Movie>()
                for (movieId in pageMovieIds) {
                    val movie = fetchMovieById(movieId)
                    if (movie != null) {
                        pageMovies.add(movie)
                    }
                }

                val existingIds = _recommendedMovies.value.map { it.id }.toHashSet()
                val uniqueIncoming = pageMovies.filterNot { it.id in existingIds }
                _recommendedMovies.value = _recommendedMovies.value + uniqueIncoming
                _recommendationsEndReached.value = nextRecommendationIndex >= allRecommendationIds.size
            } finally {
                _isLoadingMoreRecommendations.value = false
            }
        }
    }

    private suspend fun fetchMovieById(movieId: Long): Movie? {
        return try {
            when (
                val resource = movieRepository.getMovieDetails(movieId)
                    .first { it is Resource.Success || it is Resource.Error }
            ) {
                is Resource.Success -> resource.data.toMovie()
                is Resource.Error -> null
                is Resource.Loading -> null
            }
        } catch (_: Exception) {
            null
        }
    }

}
