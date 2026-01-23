package com.csd3156.mobileproject.MovieReviewApp.ui.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

data class MovieListUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FakeMovieRepository : MovieRepository {
    override fun getPopularMovies() = flow {
        emit(
            listOf(
                Movie(
                    id = 1L,
                    title = "The Sample Awakens",
                    overview = "Placeholder overview",
                    posterUrl = "",
                    rating = 8.5,
                    releaseDate = "2024-01-01",
                    review = "Great mock!"
                ),
                Movie(
                    id = 2L,
                    title = "Mockbuster 2",
                    overview = "Another placeholder movie.",
                    posterUrl = "",
                    rating = 7.2,
                    releaseDate = "2023-05-10",
                    review = "Sequel to the mock."
                )
            )
        )
    }

    override fun getMovieReviews(movieId: Long) = flow {
        emit(listOf("Awesome!", "Needs more plot."))
    }
}

class MovieListViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieListUiState(isLoading = true))
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            repository.getPopularMovies()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) }
                .catch { error ->
                    _uiState.value = MovieListUiState(
                        movies = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load movies"
                    )
                }
                .collect { movies ->
                    _uiState.value = MovieListUiState(movies = movies, isLoading = false)
                }
        }
    }
}

class MovieListViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MovieListViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        val repository = FakeMovieRepository()
        @Suppress("UNCHECKED_CAST")
        return MovieListViewModel(repository) as T
    }
}