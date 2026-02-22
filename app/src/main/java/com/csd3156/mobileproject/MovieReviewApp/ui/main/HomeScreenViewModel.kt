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
    val popularPage: Int = 0,
    val trendingPage: Int = 0,
    val discoverPage: Int = 0,
    val isLoadingPopularMore: Boolean = false,
    val isLoadingTrendingMore: Boolean = false,
    val isLoadingDiscoverMore: Boolean = false,
    val popularEndReached: Boolean = false,
    val trendingEndReached: Boolean = false,
    val discoverEndReached: Boolean = false,
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
    }



    fun refresh(){
        _uiState.value = _uiState.value.copy(
            moviesPopular = emptyList(),
            moviesTrending = emptyList(),
            moviesDiscovered = emptyList(),
            popularPage = 0,
            trendingPage = 0,
            discoverPage = 0,
            popularEndReached = false,
            trendingEndReached = false,
            discoverEndReached = false,
            isLoadingPopularMore = false,
            isLoadingTrendingMore = false,
            isLoadingDiscoverMore = false
        )
        loadPopularPage(1)
        loadTrendingPage(1)
        loadDiscoverPage(1)
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
                        if (page == 1) {
                            _uiState.value.copy(isLoading = true, errorMessage = null)
                        } else {
                            _uiState.value.copy(isLoadingDiscoverMore = true, errorMessage = null)
                        }

                    is Resource.Success -> {
                        val existing = if (page == 1) emptyList() else _uiState.value.moviesDiscovered
                        val existingIds = existing.map { it.id }.toHashSet()
                        val incoming = resource.data.filterNot { it.id in existingIds }
                        _uiState.value = _uiState.value.copy(
                            moviesDiscovered = existing + incoming,
                            discoverPage = page,
                            discoverEndReached = resource.data.isEmpty(),
                            isLoading = false,
                            isLoadingDiscoverMore = false,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isLoadingDiscoverMore = false,
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

    fun loadNextPopular() {
        val state = _uiState.value
        if (state.isLoadingPopularMore || state.popularEndReached || state.popularPage <= 0) return
        loadPopularPage(state.popularPage + 1)
    }

    fun loadNextTrending() {
        val state = _uiState.value
        if (state.isLoadingTrendingMore || state.trendingEndReached || state.trendingPage <= 0) return
        loadTrendingPage(state.trendingPage + 1)
    }

    fun loadNextDiscover() {
        val state = _uiState.value
        if (state.isLoadingDiscoverMore || state.discoverEndReached || state.discoverPage <= 0) return
        loadDiscoverPage(state.discoverPage + 1)
    }

    private fun loadPopularPage(page: Int) {
        viewModelScope.launch {
            movieRepo.getPopularMovies(page = page).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        if (page == 1) {
                            _uiState.value.copy(isLoading = true, errorMessage = null)
                        } else {
                            _uiState.value.copy(isLoadingPopularMore = true, errorMessage = null)
                        }

                    is Resource.Success -> {
                        val existing = if (page == 1) emptyList() else _uiState.value.moviesPopular
                        val existingIds = existing.map { it.id }.toHashSet()
                        val incoming = resource.data.filterNot { it.id in existingIds }
                        _uiState.value = _uiState.value.copy(
                            moviesPopular = existing + incoming,
                            popularPage = page,
                            popularEndReached = resource.data.isEmpty(),
                            isLoading = false,
                            isLoadingPopularMore = false,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isLoadingPopularMore = false,
                            errorMessage = resource.message
                        )
                }
            }
        }
    }

    private fun loadTrendingPage(page: Int) {
        viewModelScope.launch {
            movieRepo.getTrendingMovies(page = page).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value =
                        if (page == 1) {
                            _uiState.value.copy(isLoading = true, errorMessage = null)
                        } else {
                            _uiState.value.copy(isLoadingTrendingMore = true, errorMessage = null)
                        }

                    is Resource.Success -> {
                        val existing = if (page == 1) emptyList() else _uiState.value.moviesTrending
                        val existingIds = existing.map { it.id }.toHashSet()
                        val incoming = resource.data.filterNot { it.id in existingIds }
                        _uiState.value = _uiState.value.copy(
                            moviesTrending = existing + incoming,
                            trendingPage = page,
                            trendingEndReached = resource.data.isEmpty(),
                            isLoading = false,
                            isLoadingTrendingMore = false,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isLoadingTrendingMore = false,
                            errorMessage = resource.message
                        )
                }
            }
        }
    }

    private fun loadDiscoverPage(page: Int) {
        discoverMovies(
            page = page,
            sortBy = "vote_average.desc",
            voteCountGte = 500,
            includeAdult = false
        )
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
