package com.csd3156.mobileproject.MovieReviewApp.ui.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.repository.MovieRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Genre
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.model.WatchProvider
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovieListUiState(
    val moviesPopular: List<Movie> = emptyList(),
    val moviesTrending: List<Movie> = emptyList(),
    val moviesSearchResults: List<Movie> = emptyList(),
    val moviesByGenre: List<Movie> = emptyList(),
    val moviesDiscovered: List<Movie> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val selectedMovieDetails: MovieDetails? = null,
    val selectedMovieReviews: List<MovieReview> = emptyList(),
    val selectedMovieWatchProviders: List<WatchProvider> = emptyList(),
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
        loadGenres()
        // Explore lane uses a high-confidence top-rated discover query.
        discoverMovies(
            sortBy = "vote_average.desc",
            voteCountGte = 500,
            includeAdult = false
        )
    }

    fun refresh() {
        viewModelScope.launch {
            repository.getPopularMovies().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            moviesPopular = resource.data,
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

        viewModelScope.launch {
            repository.getTrendingMovies().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            moviesTrending = resource.data,
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

    fun clearSearchMovie(){
        _uiState.value = _uiState.value.copy(moviesSearchResults = emptyList(), errorMessage = null)
    }
    fun searchMovies(query: String, page: Int = 1, includeAdult: Boolean = false) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(moviesSearchResults = emptyList(), errorMessage = null)
            return
        }
        viewModelScope.launch {
            repository.searchMovies(query = query, page = page, includeAdult = includeAdult).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            moviesSearchResults = resource.data,
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

    fun loadMoviesByGenre(genreIds: List<Long>, page: Int = 1) {
        viewModelScope.launch {
            repository.getMoviesByGenre(genreIds = genreIds, page = page).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            moviesByGenre = resource.data,
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

    fun discoverMovies(
        page: Int = 1,
        sortBy: String? = null,
        genreIds: List<Long>? = null,
        releaseDateGte: String? = null,
        releaseDateLte: String? = null,
        voteAverageGte: Double? = null,
        voteAverageLte: Double? = null,
        voteCountGte: Int? = null,
        runtimeGte: Int? = null,
        runtimeLte: Int? = null,
        includeAdult: Boolean = false
    ) {
        viewModelScope.launch {
            repository.discoverMovies(
                page = page,
                sortBy = sortBy,
                genreIds = genreIds,
                releaseDateGte = releaseDateGte,
                releaseDateLte = releaseDateLte,
                voteAverageGte = voteAverageGte,
                voteAverageLte = voteAverageLte,
                voteCountGte = voteCountGte,
                runtimeGte = runtimeGte,
                runtimeLte = runtimeLte,
                includeAdult = includeAdult
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        _uiState.value.copy(isLoading = true, errorMessage = null)

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            moviesDiscovered = resource.data,
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

                    is Resource.Success -> _uiState.value =
                        _uiState.value.copy(
                            selectedMovieReviews = resource.data,
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
