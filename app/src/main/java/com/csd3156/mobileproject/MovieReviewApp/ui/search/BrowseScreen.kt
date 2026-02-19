package com.csd3156.mobileproject.MovieReviewApp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csd3156.mobileproject.MovieReviewApp.ui.components.Sections
import com.csd3156.mobileproject.MovieReviewApp.ui.main.MovieCard
import kotlinx.serialization.Serializable

@Serializable
data object SearchScreen

private enum class BrowseSortOption(val label: String, val sortBy: String) {
    Popularity(label = "Popularity", sortBy = "popularity.desc"),
    TopRated(label = "Top Rated", sortBy = "vote_average.desc"),
    MostReviewed(label = "Most Reviewed", sortBy = "vote_count.desc"),
    Newest(label = "Newest", sortBy = "primary_release_date.desc")
}

@Composable
fun BrowseScreen(
    browseScreenVM: browseMovieViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onMovieClick: (Long) -> Unit
){



    val uiState by browseScreenVM.uiState.collectAsStateWithLifecycle()
    val query = uiState.searchQuery
    var selectedSortName by rememberSaveable { mutableStateOf(BrowseSortOption.Popularity.name) }
    val selectedGenres = remember { mutableStateListOf<Long>() }

    val selectedSort = BrowseSortOption.entries.first { it.name == selectedSortName }
    val selectedGenreKey = selectedGenres.joinToString(",")

    LaunchedEffect(selectedSort.sortBy, selectedGenreKey, query.isBlank()) {
        if (query.isBlank()) {
            browseScreenVM.discoverMovies(
                sortBy = selectedSort.sortBy,
                genreIds = selectedGenres.takeIf { it.isNotEmpty() }?.toList(),
                voteCountGte = if (selectedSort == BrowseSortOption.TopRated) 200 else null
            )
        }
    }

    val movies = if (query.isBlank()) uiState.moviesDiscovered else uiState.moviesSearchResults

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Sections(
            title = "Browse",
            desc = "Search and filter by popularity, reviews, and genre"
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            value = query,
            onValueChange = {
                browseScreenVM.updateSearchQuery(it)
                if (it.isBlank()) {
                    browseScreenVM.clearSearchMovie()
                } else {
                    browseScreenVM.searchMovies(it)
                }
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = {
                        browseScreenVM.updateSearchQuery("")
                        browseScreenVM.clearSearchMovie()
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text("Search movie title...") },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Text(
            text = "Sort By",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BrowseSortOption.entries) { option ->
                FilterChip(
                    selected = selectedSort == option,
                    onClick = { selectedSortName = option.name },
                    label = { Text(option.label) }
                )
            }
        }

        Text(
            text = "Genres",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.genres) { genre ->
                val selected = selectedGenres.contains(genre.id)
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) {
                            selectedGenres.remove(genre.id)
                        } else {
                            selectedGenres.add(genre.id)
                        }
                    },
                    label = { Text(genre.name) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (query.isBlank()) "Browsing Results" else "Search Results",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${movies.size} titles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading && movies.isEmpty() -> CircularProgressIndicator()
                uiState.errorMessage != null && movies.isEmpty() -> Text(
                    text = uiState.errorMessage ?: "Failed to load movies",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                movies.isEmpty() -> Text(
                    text = "No movies found. Try a different search or filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(140.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(movies) { movie ->
                            MovieCard(
                                id = 0,
                                movie = movie,
                                withAdditionalLabel = true,
                                withReviewLabel = true,
                                withTrendingLabel = false
                            ) { onMovieClick(movie.id) }
                        }
                    }
                }
            }
        }
    }

}
