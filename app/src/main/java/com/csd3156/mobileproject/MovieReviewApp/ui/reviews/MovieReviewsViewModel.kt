package com.csd3156.mobileproject.MovieReviewApp.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.repository.ReviewRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieReviewsUiState(
    val localReviews: List<MovieReview> = emptyList(),
    val remoteReviews: List<MovieReview> = emptyList(),
    val currentPage: Int = 0,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MovieReviewsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieReviewsUiState(isInitialLoading = true))
    val uiState = _uiState.asStateFlow()

    private var currentMovieId: Long? = null
    private var localReviewsJob: Job? = null

    fun start(movieId: Long) {
        if (currentMovieId == movieId && (_uiState.value.localReviews.isNotEmpty() || _uiState.value.remoteReviews.isNotEmpty())) {
            return
        }
        currentMovieId = movieId
        _uiState.value = MovieReviewsUiState(isInitialLoading = true)

        observeLocalReviews(movieId)
        refreshLocalCache(movieId)
        loadPage(movieId = movieId, page = 1)
    }

    fun refresh(movieId: Long) {
        if (currentMovieId != movieId) {
            currentMovieId = movieId
            observeLocalReviews(movieId)
        }
        _uiState.value = _uiState.value.copy(
            remoteReviews = emptyList(),
            currentPage = 0,
            isInitialLoading = true,
            isLoadingMore = false,
            endReached = false,
            errorMessage = null
        )
        refreshLocalCache(movieId)
        loadPage(movieId = movieId, page = 1)
    }

    fun loadNextPage(movieId: Long) {
        val state = _uiState.value
        if (state.isInitialLoading || state.isLoadingMore || state.endReached || state.currentPage <= 0) return

        val nextPage = state.currentPage + 1
        loadPage(movieId = movieId, page = nextPage)
    }

    private fun observeLocalReviews(movieId: Long) {
        localReviewsJob?.cancel()
        localReviewsJob = viewModelScope.launch {
            reviewRepository.getCachedReviews(movieId).collect { reviews ->
                _uiState.value = _uiState.value.copy(localReviews = reviews)
            }
        }
    }

    private fun refreshLocalCache(movieId: Long) {
        viewModelScope.launch {
            reviewRepository.refreshMovieReviews(movieId)
        }
    }

    private fun loadPage(movieId: Long, page: Int) {
        viewModelScope.launch {
            _uiState.value = if (page == 1) {
                _uiState.value.copy(isInitialLoading = true, errorMessage = null)
            } else {
                _uiState.value.copy(isLoadingMore = true, errorMessage = null)
            }

            movieRepository.getMovieReviews(movieId = movieId, page = page).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        val incoming = resource.data
                        val existing = if (page == 1) emptyList() else _uiState.value.remoteReviews
                        val existingIds = existing.map { it.id }.toHashSet()
                        val merged = existing + incoming.filterNot { it.id in existingIds }

                        _uiState.value = _uiState.value.copy(
                            remoteReviews = merged,
                            currentPage = page,
                            endReached = incoming.isEmpty(),
                            isInitialLoading = false,
                            isLoadingMore = false,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isInitialLoading = false,
                            isLoadingMore = false,
                            errorMessage = resource.message
                        )
                    }
                }
            }
        }
    }
}
