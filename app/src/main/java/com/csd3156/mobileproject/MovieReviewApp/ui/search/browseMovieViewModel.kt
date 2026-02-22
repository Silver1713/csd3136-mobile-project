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
    val searchPage: Int = 0,
    val discoverPage: Int = 0,
    val searchEndReached: Boolean = false,
    val discoverEndReached: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null

    )

@HiltViewModel
class browseMovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {
    private data class DiscoverParams(
        val sortBy: String? = null,
        val genreIds: List<Long>? = null,
        val releaseDateGte: String? = null,
        val releaseDateLte: String? = null,
        val voteAverageGte: Double? = null,
        val voteAverageLte: Double? = null,
        val voteCountGte: Int? = null,
        val runtimeGte: Int? = null,
        val runtimeLte: Int? = null,
        val includeAdult: Boolean = false
    )

    private val _uiState = MutableStateFlow(browseUIState(isLoading = true))
    val uiState: StateFlow<browseUIState> = _uiState.asStateFlow()
    private var discoverParams = DiscoverParams()


    init {
        loadGenres()
    }

    fun clearSearchMovie() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            moviesSearchResults = emptyList(),
            searchPage = 0,
            searchEndReached = false,
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
                searchPage = 0,
                searchEndReached = false,
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
                            _uiState.value.copy(
                                isLoading = page == 1,
                                isLoadingMore = page > 1,
                                errorMessage = null
                            )

                        is Resource.Success -> {
                            val existing = if (page == 1) emptyList() else _uiState.value.moviesSearchResults
                            val existingIds = existing.map { it.id }.toHashSet()
                            val incoming = resource.data.filterNot { it.id in existingIds }
                            _uiState.value = _uiState.value.copy(
                                moviesSearchResults = existing + incoming,
                                searchPage = page,
                                searchEndReached = resource.data.isEmpty(),
                                isLoading = false,
                                isLoadingMore = false,
                                errorMessage = null
                            )
                        }

                        is Resource.Error -> _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
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
        discoverParams = DiscoverParams(
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
        )
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
                        _uiState.value.copy(
                            isLoading = page == 1,
                            isLoadingMore = page > 1,
                            errorMessage = null
                        )

                    is Resource.Success -> {
                        val existing = if (page == 1) emptyList() else _uiState.value.moviesDiscovered
                        val existingIds = existing.map { it.id }.toHashSet()
                        val incoming = resource.data.filterNot { it.id in existingIds }
                        _uiState.value = _uiState.value.copy(
                            moviesDiscovered = existing + incoming,
                            discoverPage = page,
                            discoverEndReached = resource.data.isEmpty(),
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
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

    fun loadNextSearch() {
        val state = _uiState.value
        if (state.searchQuery.isBlank() || state.isLoadingMore || state.searchEndReached || state.searchPage <= 0) return
        searchMovies(
            query = state.searchQuery,
            page = state.searchPage + 1
        )
    }

    fun loadNextDiscover() {
        val state = _uiState.value
        if (state.searchQuery.isNotBlank() || state.isLoadingMore || state.discoverEndReached || state.discoverPage <= 0) return
        discoverMovies(
            page = state.discoverPage + 1,
            sortBy = discoverParams.sortBy,
            genreIds = discoverParams.genreIds,
            releaseDateGte = discoverParams.releaseDateGte,
            releaseDateLte = discoverParams.releaseDateLte,
            voteAverageGte = discoverParams.voteAverageGte,
            voteAverageLte = discoverParams.voteAverageLte,
            voteCountGte = discoverParams.voteCountGte,
            runtimeGte = discoverParams.runtimeGte,
            runtimeLte = discoverParams.runtimeLte,
            includeAdult = discoverParams.includeAdult
        )
    }
}
