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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewDetailsUiState(
    val isLoading: Boolean = false,
    val selectedReview: MovieReview? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ReviewDetailsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {
    private var reviewJob: Job? = null
    private var lastLoadedKey: Pair<Long, String>? = null

    private val _uiState = MutableStateFlow(ReviewDetailsUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    fun loadReview(movieId: Long, reviewId: String) {
        val key = movieId to reviewId
        if (lastLoadedKey == key && _uiState.value.selectedReview != null) return
        lastLoadedKey = key

        reviewJob?.cancel()
        reviewJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val cachedReview = reviewRepository
                .getCachedReviews(movieId)
                .first()
                .firstOrNull { it.id == reviewId }

            if (cachedReview != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedReview = cachedReview,
                    errorMessage = null
                )
                return@launch
            }

            // Local Room reviews use "local-*" ids and should not trigger TMDB fetches.
            if (reviewId.startsWith("local-")) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedReview = null,
                    errorMessage = "Review unavailable"
                )
                return@launch
            }

            movieRepository.getMovieReviews(movieId = movieId, page = 1).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }

                    is Resource.Success -> {
                        val remoteReview = resource.data.firstOrNull { it.id == reviewId }
                        _uiState.value = if (remoteReview != null) {
                            _uiState.value.copy(
                                isLoading = false,
                                selectedReview = remoteReview,
                                errorMessage = null
                            )
                        } else {
                            _uiState.value.copy(
                                isLoading = false,
                                selectedReview = null,
                                errorMessage = "Review not found"
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            selectedReview = null,
                            errorMessage = resource.message ?: "Unable to load review details"
                        )
                    }
                }
            }
        }
    }
}
