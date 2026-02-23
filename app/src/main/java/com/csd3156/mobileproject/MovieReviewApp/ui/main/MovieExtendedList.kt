package com.csd3156.mobileproject.MovieReviewApp.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csd3156.mobileproject.MovieReviewApp.recommender.RecommenderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieExtendedListScreen(
    sectionKey: String,
    recommenderViewModel: RecommenderViewModel,
    onBack: () -> Unit,
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    vm: MovieExtendedListViewModel = hiltViewModel()
) {
    val section = sectionKey.toMovieContentSectionOrNull() ?: MovieContentSection.MOVIE_REC
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val recommendedMovies by recommenderViewModel.recommendedMovies.collectAsStateWithLifecycle()
    val isFetchingRecommendations by recommenderViewModel.isFetchingRecommendations.collectAsStateWithLifecycle()
    val isLoadingRecommendedMore by recommenderViewModel.isLoadingMoreRecommendations.collectAsStateWithLifecycle()
    val recommendationsEndReached by recommenderViewModel.recommendationsEndReached.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    LaunchedEffect(section) {
        if (section == MovieContentSection.MOVIE_RECOMMENDED) return@LaunchedEffect
        vm.start(section)
    }

    val currentMovies = if (section == MovieContentSection.MOVIE_RECOMMENDED) {
        recommendedMovies
    } else {
        uiState.movies
    }
    val isInitialLoading = if (section == MovieContentSection.MOVIE_RECOMMENDED) {
        (isFetchingRecommendations || isLoadingRecommendedMore) && recommendedMovies.isEmpty()
    } else {
        uiState.isInitialLoading
    }
    val isLoadingMore = if (section == MovieContentSection.MOVIE_RECOMMENDED) {
        isLoadingRecommendedMore
    } else {
        uiState.isLoadingMore
    }
    val endReached = if (section == MovieContentSection.MOVIE_RECOMMENDED) {
        recommendationsEndReached
    } else {
        uiState.endReached
    }
    val emptyMessage = if (section == MovieContentSection.MOVIE_RECOMMENDED) {
        "No recommendations yet. Add movies to your watchlist first."
    } else {
        uiState.errorMessage ?: "No movies found."
    }

    LaunchedEffect(gridState, currentMovies.size, endReached, isLoadingMore, isInitialLoading) {
        snapshotFlow { gridState.layoutInfo }
            .collect { layoutInfo ->
                val total = layoutInfo.totalItemsCount
                if (total == 0) return@collect
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@collect
                val nearEnd = lastVisible >= total - 4
                if (nearEnd) {
                    if (section == MovieContentSection.MOVIE_RECOMMENDED) {
                        recommenderViewModel.loadNextRecommendationsPage()
                    } else {
                        vm.loadNextPage()
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = section.toSectionTitle(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(padding)
        ) {
            when {
                isInitialLoading && currentMovies.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                currentMovies.isEmpty() -> {
                    Text(
                        text = emptyMessage,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 96.dp
                        ),
                        columns = GridCells.Adaptive(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(currentMovies) { index, movie ->
                            MovieCard(
                                id = index,
                                movie = movie,
                                isHorizontal = false,
                                withAdditionalLabel = true,
                                withReviewLabel = true,
                                withTrendingLabel = section == MovieContentSection.MOVIE_TRENDING
                            ) { onMovieClick(movie.id) }
                        }

                        if (isLoadingMore) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.toMovieContentSectionOrNull(): MovieContentSection? {
    return MovieContentSection.entries.firstOrNull { it.name == this }
}

private fun MovieContentSection.toSectionTitle(): String {
    return when (this) {
        MovieContentSection.MOVIE_REC -> "Popular"
        MovieContentSection.MOVIE_TRENDING -> "Trending Now"
        MovieContentSection.MOVIE_RECOMMENDED -> "Recommended"
        MovieContentSection.MOVIE_EXPLORE -> "Explore"
    }
}
