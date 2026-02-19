package com.csd3156.mobileproject.MovieReviewApp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Genre
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class browseUIState(
    val searchQuery: String = "",
    val moviesSearchResults: List<Movie> = emptyList(),
    val moviesDiscovered: List<Movie> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null

    )

@HiltViewModel
class browseMovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(browseUIState(isLoading = true))
    val uiState: StateFlow<browseUIState> = _uiState.asStateFlow()


    init {
        loadGenres()
    }

    fun clearSearchMovie() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            moviesSearchResults = emptyList(),
            errorMessage = null
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
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
            repository.searchMovies(query = query, page = page, includeAdult = includeAdult)
                .collect { resource ->
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
}