package com.csd3156.mobileproject.MovieReviewApp.ui.main

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun HomeScreen(viewmodel: MovieListViewModel = viewModel(), modifier: Modifier = Modifier) {
    // Main Screen UI Here
    // Div Start

    Column(
        modifier = modifier
    ) {
        TitleSection(viewmodel)
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
                mod = Modifier.padding(0.dp)
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
                mod = Modifier
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
                mod = Modifier

            )
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
                placeholder = { Text("Search") }
            )
        },
        expanded = false,
        onExpandedChange = { active = it }
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

        }) { value ->
            print(value)
        }
    }
}

@Composable
fun makeProfileIcon(drawableId: Int, modifier: Modifier) {
    Icon(
        imageVector = Icons.Filled.AccountCircle,
        tint = Color.Gray,
        contentDescription = null,
        modifier = modifier
            .clip(CircleShape)
    )
}


@Composable
fun GridMovieContent(
    movieListViewModel: MovieListViewModel,
    movieType: MovieContentSection = MovieContentSection.MOVIE_REC,
    mod: Modifier = Modifier
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
                ) {

                }


            }
        }
    }
}

@Composable
fun RowingMoviesContent(
    movieListViewModel: MovieListViewModel,
    movieType: MovieContentSection = MovieContentSection.MOVIE_REC,
    mod: Modifier = Modifier
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
                    ) {

                    }

                    MovieContentSection.MOVIE_TRENDING -> MovieCard(
                        index,
                        movie,
                        true,
                        true,
                        false,
                        true
                    ) {

                    }

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
    withTrendingLabel: Boolean = false, onclick: () -> Unit
) {
    Card(
        modifier = if (isHorizontal) {
            Modifier

                .width(240.dp)
        } else {
            Modifier

                .width(140.dp)
        },
        onClick = onclick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column {
            Box(
                modifier =
                    Modifier.shadow(
                        elevation = 2.dp,
                        spotColor = Color.Black,
                        ambientColor = Color.Black,
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
                            spotColor = Color.Black,
                            ambientColor = Color.Black,
                            clip = true

                        ),
                )

                if (withReviewLabel) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                        )

                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                tint = Color.Yellow,

                                contentDescription = null,
                            )
                            Text(
                                5.0.toString(), textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
                if (withTrendingLabel) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                        )

                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "#${id+1} Trending", textAlign = TextAlign.Center,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            movie.title?.let {
                Text(
                    it, modifier = Modifier.padding(top = 8.dp),
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }

            if (withAdditionalLabel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sci-Fi",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.size(6.dp))

                    Box(
                        modifier = Modifier
                            .size(4.dp)              // dot size
                            .background(
                                Color.Gray,
                                shape = CircleShape
                            )
                    )

                    Spacer(Modifier.size(6.dp))

                    Text(
                        movie.getFormattedTime(),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                }
            }
        }
    }
}