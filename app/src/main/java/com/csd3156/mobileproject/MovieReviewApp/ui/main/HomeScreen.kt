package com.csd3156.mobileproject.MovieReviewApp.ui.main

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csd3156.mobileproject.MovieReviewApp.R
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.ui.components.LoadImage
import com.csd3156.mobileproject.MovieReviewApp.ui.components.Sections
import com.csd3156.mobileproject.MovieReviewApp.ui.movies.list.MovieListViewModel
import kotlinx.serialization.Serializable


@Serializable
data object Main


enum class MovieContentSection {
    MOVIE_REC,
    MOVIE_EXPLORE,
    MOVIE_TRENDING
}


@Composable
fun HomeScreen(
    viewmodel: MovieListViewModel,
    modifier: Modifier = Modifier,
    onMovieClick: (Long) -> Unit
) {
    // Create HomeScreen ViewModel
    val homeVM: HomeScreenViewModel = viewModel(factory = HomeViewModelFactory())
    val isSearch by homeVM.isSearching.collectAsStateWithLifecycle()
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    // Main Screen UI Here
    // Div Start

    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        TitleSection(viewmodel)
        if (uiState.moviesSearchResults.isNotEmpty()) {
            homeVM.setIsSearching(true)
            LazyVerticalGrid(

                contentPadding = PaddingValues(16.dp),
                columns = GridCells.Adaptive(120.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)


            ) {
                items(uiState.moviesSearchResults
                    .sortedByDescending { it.releaseDate }){
                    movie -> MovieCard(
                    0,
                    movie,
                    false,
                    true,
                    false,
                    false

                ) { onMovieClick(movie.id) }

                }
            }

        } else {
            homeVM.setIsSearching(false)
        }
        if (!isSearch) {
            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState()
                )
            ) {
                Sections(
                    "Recommended for You",
                    null,
                    modifier = Modifier.padding(
                        top = 16.dp, bottom = 16.dp,
                        start = 16.dp, end = 16.dp
                    )
                )
                RowingMoviesContent(
                    viewmodel,
                    movieType = MovieContentSection.MOVIE_REC,
                    mod = Modifier.padding(0.dp),
                    onMovieClick = onMovieClick
                )
                Sections(
                    "Trending Now",
                    null,
                    modifier = Modifier.padding(
                        top = 16.dp, bottom = 16.dp,
                        start = 16.dp, end = 16.dp
                    )
                )
                RowingMoviesContent(
                    viewmodel,
                    movieType = MovieContentSection.MOVIE_TRENDING,
                    mod = Modifier,
                    onMovieClick = onMovieClick
                )
                Sections(
                    "Explore Genre",
                    null,
                    modifier = Modifier.padding(
                        top = 16.dp, bottom = 16.dp,
                        start = 16.dp, end = 16.dp
                    )
                )
                GridMovieContent(
                    movieListViewModel = viewmodel,
                    movieType = MovieContentSection.MOVIE_EXPLORE,
                    mod = Modifier,
                    onMovieClick = onMovieClick

                )
            }
        }


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun movieSearchBar(
    viewmodel: MovieListViewModel,
    onQueryChange: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (value: String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    DockedSearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = modifier,
                query = query,
                onQueryChange = {
                    query = it
                    onQueryChange(query)
                },
                onSearch = { onSearch(query) },
                expanded = false,
                onExpandedChange = { active = it },
                placeholder = {
                    Text(
                        "Search movies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        },
        expanded = false,
        onExpandedChange = { active = it },
        shape = RoundedCornerShape(18.dp),
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        // Expanded content — intentionally empty
    }
}

@Composable
fun TitleSection(movieListViewModel: MovieListViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Sections("Discover", "What to watch today?", Modifier.padding(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        makeProfileIcon(
            R.drawable.ic_launcher_background, Modifier
                .padding(16.dp)
                .size(64.dp)
        )

    }
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        movieSearchBar(movieListViewModel, { query ->
            movieListViewModel.searchMovies(query)
        }) { value ->
            movieListViewModel.searchMovies(value)
        }
    }
}

@Composable
fun makeProfileIcon(drawableId: Int, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
            modifier = Modifier.size(42.dp)
        )
    }
}


@Composable
fun GridMovieContent(
    movieListViewModel: MovieListViewModel,
    movieType: MovieContentSection = MovieContentSection.MOVIE_REC,
    mod: Modifier = Modifier,
    onMovieClick: (Long) -> Unit
) {
    val uiState by movieListViewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = mod) {
        LazyVerticalGrid(
            modifier = Modifier.height(600.dp),
            contentPadding = PaddingValues(16.dp),
            columns = GridCells.Adaptive(140.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)


        ) {
            items(uiState.moviesPopular) { movie ->
                MovieCard(
                    0,
                    movie,
                    false,
                    true,
                    false,
                    false
                ) { onMovieClick(movie.id) }


            }
        }
    }
}

@Composable
fun RowingMoviesContent(
    movieListViewModel: MovieListViewModel,
    movieType: MovieContentSection = MovieContentSection.MOVIE_REC,
    mod: Modifier = Modifier,
    onMovieClick: (Long) -> Unit
) {
    val uiState by movieListViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = mod
    ) {


        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            itemsIndexed(if (movieType == MovieContentSection.MOVIE_REC){
                uiState.moviesPopular
            }else{
                uiState.moviesTrending
            }) {
                    index, movie ->
                when (movieType) {
                    MovieContentSection.MOVIE_REC -> MovieCard(
                        index,
                        movie,
                        false,
                        true,
                        true,
                        false
                    ) { onMovieClick(movie.id) }

                    MovieContentSection.MOVIE_TRENDING -> MovieCard(
                        index,
                        movie,
                        true,
                        true,
                        false,
                        true
                    ) { onMovieClick(movie.id) }

                    MovieContentSection.MOVIE_EXPLORE -> {}
                    else -> {}
                }
            }
        }
    }

}

@Composable
fun MovieCard(
    id: Int,
    movie: Movie,
    isHorizontal: Boolean = false, withAdditionalLabel: Boolean = true,
    withReviewLabel: Boolean = true,
    withTrendingLabel: Boolean = false,
    posterSize: IntArray = intArrayOf(140, 240),
    onclick: () -> Unit
) {
    Card(
        modifier = if (isHorizontal) {
            Modifier

                .width(posterSize[1].dp)
        } else {
            Modifier

                .width(posterSize[0].dp)
        },
        onClick = onclick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier =
                    Modifier.shadow(
                        elevation = 2.dp,
                        spotColor = MaterialTheme.colorScheme.scrim,
                        ambientColor = MaterialTheme.colorScheme.scrim,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {

                LoadImage(
                    url = movie.posterUrl,
                    placeholder = R.drawable.ic_launcher_background,
                    contentScale = ContentScale.Crop,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isHorizontal) 3 / 2f else 2 / 3f)
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(
                            elevation = 10.dp,
                            spotColor = MaterialTheme.colorScheme.scrim,
                            ambientColor = MaterialTheme.colorScheme.scrim,
                            clip = true

                        ),
                )

                if (withReviewLabel) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        )

                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                tint = MaterialTheme.colorScheme.tertiary,

                                contentDescription = null,
                            )
                            Text(
                                String.format("%.1f", movie.rating),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                if (withTrendingLabel) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                        )

                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "#${id+1} Trending", textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            movie.title?.let {
                Text(
                    it, modifier = Modifier.padding(top = 10.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (withAdditionalLabel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                ) {
                    if (movie.genres?.isEmpty() == true){
                        "Unspecified"
                    } else {
                        movie.genres?.get(0)
                    }?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(Modifier.size(6.dp))

                    Box(
                        modifier = Modifier
                            .size(4.dp)              // dot size
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            )
                    )

                    Spacer(Modifier.size(6.dp))

                    Text(
                        movie.releaseDate?.take(4) ?: "0000",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelSmall
                    )

                }
            }
        }
    }
}
