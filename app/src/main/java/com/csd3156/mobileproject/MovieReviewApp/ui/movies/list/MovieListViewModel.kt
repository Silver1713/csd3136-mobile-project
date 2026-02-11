package com.csd3156.mobileproject.MovieReviewApp.ui.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.repository.MovieRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovieListUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MovieListViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieListUiState(isLoading = true))
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.getPopularMovies().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value = MovieListUiState(
                        movies = resource.data,
                        isLoading = false,
                        errorMessage = null
                    )

                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = resource.message
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory {
            val repository = MovieRepositoryImpl.create()
            return MovieListViewModelFactory(repository)
        }
    }
}

class MovieListViewModelFactory(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MovieListViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return MovieListViewModel(repository) as T
    }
}
