package com.csd3156.mobileproject.MovieReviewApp.ui.movies.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.local.LocalReviewRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.data.repository.AccountRepository
import com.csd3156.mobileproject.MovieReviewApp.data.repository.MovieRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.data.repository.ReviewRepository
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Genre
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieVideo
import com.csd3156.mobileproject.MovieReviewApp.domain.model.WatchProvider
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.LocalReviewRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

data class MovieListUiState(
    val moviesPopular: List<Movie> = emptyList(),
    val moviesTrending: List<Movie> = emptyList(),
    val moviesSearchResults: List<Movie> = emptyList(),
    val searchQuery: String = "",
    val moviesByGenre: List<Movie> = emptyList(),
    val moviesDiscovered: List<Movie> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val selectedMovieDetails: MovieDetails? = null,
    val selectedMovieReviews: List<MovieReview> = emptyList(),
    val selectedMovieLocalReviews: List<MovieReview> = emptyList(),
    val selectedMovieVideos: List<MovieVideo> = emptyList(),
    val selectedMovieWatchProviders: List<WatchProvider> = emptyList(),
    val reviewPhotoPath: String? = null,
    val pendingCapturePath: String? = null,
    val isSubmittingReview: Boolean = false,
    val reviewSubmitStatus: String? = null,
    val reviewSubmitError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null

)

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val accountRepo : AccountRepository,
    private val repository: MovieRepository,
    private val localReviewRepository: LocalReviewRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

     val currentAccount : Flow<AccountDomain?> = accountRepo.getActiveAccountRoom()
    private val _uiState = MutableStateFlow(MovieListUiState(isLoading = true))
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()
    private var localReviewsJob: Job? = null

    init {

        loadGenres()
    }





    fun loadGenres() {
        viewModelScope.launch {
            repository.getMovieGenres().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            genres = resource.data,
                            isLoading = false,
                            errorMessage = null
                        )

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                }
            }
        }
    }

    fun loadMovieDetails(movieId: Long) {
        viewModelScope.launch {
            repository.getMovieDetails(movieId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            selectedMovieDetails = resource.data,
                            isLoading = false,
                            errorMessage = null
                        )

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                }
            }
        }
    }

    fun loadMovieReviews(movieId: Long, page: Int = 1) {
        viewModelScope.launch {
            repository.getMovieReviews(movieId = movieId, page = page).collect { resource ->

                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> {
                        _uiState.value =

                            _uiState.value.copy(
                                selectedMovieReviews = resource.data,
                                isLoading = false,
                                errorMessage = null
                            )

                        reviewRepository.refreshMovieReviews(movieId)
                    }




                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                }
            }
        }
    }

    fun loadMovieVideos(movieId: Long) {
        viewModelScope.launch {
            repository.getMovieVideos(movieId = movieId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            selectedMovieVideos = resource.data,
                            isLoading = false,
                            errorMessage = null
                        )

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                }
            }
        }
    }

    fun loadMovieWatchProviders(movieId: Long, countryCode: String = "US") {
        viewModelScope.launch {
            repository.getMovieWatchProviders(movieId = movieId, countryCode = countryCode).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            selectedMovieWatchProviders = resource.data,
                            isLoading = false,
                            errorMessage = null
                        )

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                }
            }
        }
    }

    fun observeLocalReviews(movieId: Long) {
        localReviewsJob?.cancel()
        localReviewsJob = viewModelScope.launch {
            reviewRepository.getCachedReviews(movieId).collect { reviews ->
                _uiState.value = _uiState.value.copy(selectedMovieLocalReviews = reviews)
            }
        }
    }

    suspend fun addLocalReview(movieId: Long, movieTitle: String?=null ,author: String, rating: Double?, content: String, photoPath: String?) : RequestResult<Unit> {
        if (content.isBlank()) {
            return RequestResult.Error("Review content is empty", Exception("Review content is empty"))
        }
        _uiState.value = _uiState.value.copy(
            isSubmittingReview = true,
            reviewSubmitStatus = if (photoPath.isNullOrBlank()) "Saving review..." else "Uploading photo...",
            reviewSubmitError = null
        )
        val result = reviewRepository.addReview(movieId,movieTitle ?: "Unknown Title", rating, content, photoPath)
        _uiState.value = when (result) {
            is RequestResult.Success -> _uiState.value.copy(
                isSubmittingReview = false,
                reviewSubmitStatus = null,
                reviewSubmitError = null
            )
            is RequestResult.Error -> _uiState.value.copy(
                isSubmittingReview = false,
                reviewSubmitStatus = null,
                reviewSubmitError = result.message ?: "Failed to submit review"
            )
        }
        return result
    }

    fun clearReviewSubmitError() {
        _uiState.value = _uiState.value.copy(reviewSubmitError = null)
    }

    fun setPendingCapturePath(path: String?) {
        val existingPending = _uiState.value.pendingCapturePath
        if (existingPending != null && existingPending != path) {
            deleteFileIfExists(existingPending)
        }
        _uiState.value = _uiState.value.copy(pendingCapturePath = path)
    }

    fun handleTakePictureResult(success: Boolean) {
        val pendingPath = _uiState.value.pendingCapturePath
        val currentPath = _uiState.value.reviewPhotoPath
        if (success) {
            if (currentPath != null && currentPath != pendingPath) {
                deleteFileIfExists(currentPath)
            }
            _uiState.value = _uiState.value.copy(
                reviewPhotoPath = pendingPath,
                pendingCapturePath = null
            )
        } else {
            pendingPath?.let(::deleteFileIfExists)
            _uiState.value = _uiState.value.copy(
                pendingCapturePath = null
            )
        }
    }

    fun clearReviewPhoto() {
        _uiState.value.reviewPhotoPath?.let(::deleteFileIfExists)
        _uiState.value = _uiState.value.copy(reviewPhotoPath = null)
    }

    fun clearReviewPhotoDraft() {
        _uiState.value.pendingCapturePath?.let(::deleteFileIfExists)
        _uiState.value.reviewPhotoPath?.let(::deleteFileIfExists)
        _uiState.value = _uiState.value.copy(
            pendingCapturePath = null,
            reviewPhotoPath = null
        )
    }

    fun resetReviewPhotoDraftState() {
        _uiState.value = _uiState.value.copy(
            pendingCapturePath = null,
            reviewPhotoPath = null
        )
    }

    private fun deleteFileIfExists(path: String) {
        runCatching {
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }
}
