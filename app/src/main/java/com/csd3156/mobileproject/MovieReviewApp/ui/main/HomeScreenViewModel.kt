package com.csd3156.mobileproject.MovieReviewApp.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.data.local.LocalReviewRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.data.repository.AccountRepository
import com.csd3156.mobileproject.MovieReviewApp.data.repository.MovieRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Genre
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeScreenUIState(
    val moviesPopular: List<Movie> = emptyList(),
    val moviesTrending: List<Movie> = emptyList(),
    val moviesDiscovered: List<Movie> = emptyList(),
    val moviesSearchResults: List<Movie> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
    )

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val movieRepo : MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUIState())
    val uiState: StateFlow<HomeScreenUIState> = _uiState.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun setIsSearching(value: Boolean) {
        _isSearching.value = value
    }

    init {
        refresh()
        loadGenres()
        discoverMovies(
            sortBy = "vote_average.desc",
            voteCountGte = 500,
            includeAdult = false
        )
    }



    fun refresh(){
        viewModelScope.launch {
            movieRepo.getPopularMovies().collect { resource ->
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
            movieRepo.getTrendingMovies().collect { resource ->
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

    fun loadGenres() {
        viewModelScope.launch {
            movieRepo.getMovieGenres().collect { resource ->
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
            movieRepo.discoverMovies(
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

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun clearSearchMovie(){
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            moviesSearchResults = emptyList(),
            errorMessage = null
        )
    }

    fun searchMovies(query: String, page: Int = 1, includeAdult: Boolean = false) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = "",
                moviesSearchResults = emptyList(),
                errorMessage = null
            )
            return
        }
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            movieRepo.searchMovies(query = query, page = page, includeAdult = includeAdult).collect { resource ->
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





    @Deprecated(
        message = "Replace factory with hilt",
        replaceWith = ReplaceWith("HomeScreenViewModel.provideFactory")

    )
    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val movieRepository = MovieRepositoryImpl.create()
            return HomeViewModelFactory(movieRepository)
        }
    }

}

@Deprecated("Replace factory with Hilt")
class HomeViewModelFactory(
    private val movieRepo: MovieRepository
) : ViewModelProvider.Factory {
    @Deprecated("Replace factory with Hilt")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return HomeScreenViewModel(movieRepo) as T
    }
}
