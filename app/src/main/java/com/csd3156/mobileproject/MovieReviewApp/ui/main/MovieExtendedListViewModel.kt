package com.csd3156.mobileproject.MovieReviewApp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.common.Resource
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieExtendedListUiState(
    val section: MovieContentSection? = null,
    val movies: List<Movie> = emptyList(),
    val currentPage: Int = 0,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MovieExtendedListViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieExtendedListUiState(isInitialLoading = true))
    val uiState = _uiState.asStateFlow()

    fun start(section: MovieContentSection) {
        if (_uiState.value.section == section && _uiState.value.movies.isNotEmpty()) return
        if (section == MovieContentSection.MOVIE_RECOMMENDED) {
            _uiState.value = MovieExtendedListUiState(
                section = section,
                isInitialLoading = false,
                endReached = true
            )
            return
        }
        _uiState.value = MovieExtendedListUiState(
            section = section,
            isInitialLoading = true
        )
        loadPage(section = section, page = 1)
    }

    fun loadNextPage() {
        val state = _uiState.value
        val section = state.section ?: return
        if (state.isInitialLoading || state.isLoadingMore || state.endReached || state.currentPage <= 0) return
        loadPage(section = section, page = state.currentPage + 1)
    }

    private fun loadPage(section: MovieContentSection, page: Int) {
        viewModelScope.launch {
            _uiState.value = if (page == 1) {
                _uiState.value.copy(
                    section = section,
                    isInitialLoading = true,
                    isLoadingMore = false,
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    section = section,
                    isLoadingMore = true,
                    errorMessage = null
                )
            }

            val flow = when (section) {
                MovieContentSection.MOVIE_REC -> movieRepository.getPopularMovies(page = page)
                MovieContentSection.MOVIE_TRENDING -> movieRepository.getTrendingMovies(page = page)
                MovieContentSection.MOVIE_EXPLORE -> movieRepository.discoverMovies(
                    page = page,
                    sortBy = "vote_average.desc",
                    voteCountGte = 500,
                    includeAdult = false
                )
                MovieContentSection.MOVIE_RECOMMENDED -> {
                    _uiState.value = _uiState.value.copy(
                        section = section,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        endReached = true
                    )
                    return@launch
                }
            }

            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        val existing = if (page == 1) emptyList() else _uiState.value.movies
                        val existingIds = existing.map { it.id }.toHashSet()
                        val incoming = resource.data.filterNot { it.id in existingIds }
                        _uiState.value = _uiState.value.copy(
                            section = section,
                            movies = existing + incoming,
                            currentPage = page,
                            isInitialLoading = false,
                            isLoadingMore = false,
                            endReached = resource.data.isEmpty(),
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            section = section,
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
