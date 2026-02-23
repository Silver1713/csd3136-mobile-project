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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        private const val WATCHLIST_RECOMPUTE_DEBOUNCE_MS = 450L
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
    private var recommendationVersion = 0L

    init {
        observeWatchlistUpdates()
    }

    fun fetchData() {
        viewModelScope.launch {
            val userWatchlistIds = watchlistRepository.getAllWatchlist()
                .first()
                .map { it.movieId }
                .sorted()
            recomputeRecommendations(userWatchlistIds)
        }
    }

    fun loadNextRecommendationsPage() {
        loadNextRecommendationsPage(recommendationVersion)
    }

    private fun observeWatchlistUpdates() {
        viewModelScope.launch {
            watchlistRepository.getAllWatchlist()
                .map { watchlist -> watchlist.map { it.movieId }.sorted() }
                .distinctUntilChanged()
                .debounce(WATCHLIST_RECOMPUTE_DEBOUNCE_MS)
                .collectLatest { watchlistIds ->
                    recomputeRecommendations(watchlistIds)
                }
        }
    }

    private suspend fun recomputeRecommendations(userWatchlistIds: List<Long>) {
        val currentVersion = ++recommendationVersion

        _isFetchingRecommendations.value = true
        _isLoadingMoreRecommendations.value = false
        _allRecommendationMovieIds.value = emptyList()
        _recommendedMovies.value = emptyList()
        nextRecommendationIndex = 0
        _recommendationsEndReached.value = false

        try {
            val userWatchlist = mutableListOf<Movie>()
            for (movieId in userWatchlistIds) {
                if (currentVersion != recommendationVersion) return
                val movie = fetchMovieById(movieId)
                if (movie != null) {
                    userWatchlist.add(movie)
                }
            }

            if (currentVersion != recommendationVersion) return
            val recommendationMovieIds = withContext(Dispatchers.Default) {
                reccomender.GetRecommendations(userWatchlist, MAX_RECOMMENDATIONS)
                    .map { it.movieId }
            }

            if (currentVersion != recommendationVersion) return
            _allRecommendationMovieIds.value = recommendationMovieIds
            _recommendationsEndReached.value = recommendationMovieIds.isEmpty()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (currentVersion != recommendationVersion) return
            _allRecommendationMovieIds.value = emptyList()
            _recommendedMovies.value = emptyList()
            nextRecommendationIndex = 0
            _recommendationsEndReached.value = true
        } finally {
            if (currentVersion == recommendationVersion) {
                _isFetchingRecommendations.value = false
            }
        }

        if (currentVersion == recommendationVersion) {
            loadNextRecommendationsPage(currentVersion)
        }
    }

    private fun loadNextRecommendationsPage(expectedVersion: Long) {
        if (_isFetchingRecommendations.value || _isLoadingMoreRecommendations.value || _recommendationsEndReached.value) {
            return
        }
        viewModelScope.launch {
            if (expectedVersion != recommendationVersion) return@launch

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
                    if (expectedVersion != recommendationVersion) return@launch
                    val movie = fetchMovieById(movieId)
                    if (movie != null) {
                        pageMovies.add(movie)
                    }
                }

                if (expectedVersion != recommendationVersion) return@launch
                val existingIds = _recommendedMovies.value.map { it.id }.toHashSet()
                val uniqueIncoming = pageMovies.filterNot { it.id in existingIds }
                _recommendedMovies.value = _recommendedMovies.value + uniqueIncoming
                _recommendationsEndReached.value = nextRecommendationIndex >= allRecommendationIds.size
            } finally {
                if (expectedVersion == recommendationVersion) {
                    _isLoadingMoreRecommendations.value = false
                }
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
