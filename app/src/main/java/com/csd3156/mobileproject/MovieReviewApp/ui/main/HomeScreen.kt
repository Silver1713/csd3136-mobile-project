package com.csd3156.mobileproject.MovieReviewApp.ui.main
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csd3156.mobileproject.MovieReviewApp.R
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import com.csd3156.mobileproject.MovieReviewApp.domain.model.Movie
import com.csd3156.mobileproject.MovieReviewApp.recommender.RecommenderViewModel
import com.csd3156.mobileproject.MovieReviewApp.ui.components.LoadImage
import com.csd3156.mobileproject.MovieReviewApp.ui.components.Sections
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
    modifier: Modifier = Modifier,
    recommenderViewModel: RecommenderViewModel,
    onMovieClick: (Long) -> Unit,
    onSearchSubmit: (String) -> Unit = {},
    onSectionSeeMore: (MovieContentSection) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Create HomeScreen ViewModel
    val homeVM: HomeScreenViewModel = hiltViewModel()
    val isSearch by homeVM.isSearching.collectAsStateWithLifecycle()
    val uiState by homeVM.uiState.collectAsStateWithLifecycle()
    val accountInfo by homeVM.accountInfo.collectAsStateWithLifecycle(null)
    val searchQuery = uiState.searchQuery
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
        TitleSection(
            homeViewModel = homeVM,
            accountInfo = accountInfo,
            searchQuery = searchQuery,
            onSearchSubmit = onSearchSubmit,
            onProfileClick = onProfileClick
        )
        if (searchQuery.isNotBlank()) {
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
                HomeSectionHeader(
                    title = "Popular",
                    modifier = Modifier.padding(
                        top = 16.dp, bottom = 8.dp,
                        start = 16.dp, end = 16.dp
                    ),
                    onSeeMore = { onSectionSeeMore(MovieContentSection.MOVIE_REC) }
                )
                RowingMoviesContent(
                    homeVM,
                    movieType = MovieContentSection.MOVIE_REC,
                    mod = Modifier.padding(0.dp),
                    onMovieClick = onMovieClick,
                    recommenderViewModel
                )
                HomeSectionHeader(
                    title = "Trending Now",
                    modifier = Modifier.padding(
                        top = 16.dp, bottom = 8.dp,
                        start = 16.dp, end = 16.dp
                    ),
                    onSeeMore = { onSectionSeeMore(MovieContentSection.MOVIE_TRENDING) }
                )
                RowingMoviesContent(
                    homeVM,
                    movieType = MovieContentSection.MOVIE_TRENDING,
                    mod = Modifier,
                    onMovieClick = onMovieClick,
                    recommenderViewModel
                )
                HomeSectionHeader(
                    title = "Explore",
                    modifier = Modifier.padding(
                        top = 16.dp, bottom = 8.dp,
                        start = 16.dp, end = 16.dp
                    ),
                    onSeeMore = { onSectionSeeMore(MovieContentSection.MOVIE_EXPLORE) }
                )
                GridMovieContent(
                    homeViewModel = homeVM,
                    movieType = MovieContentSection.MOVIE_EXPLORE,
                    modifier = Modifier,
                    onMovieClick = onMovieClick

                )
            }
        }


    }

}

@Composable
private fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeMore: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onSeeMore) {
            Text(
                text = "See more",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieSearchBar(
    query: String,
    onQueryChange: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (value: String) -> Unit,
    onOpenSearch: (value: String) -> Unit
) {
    DockedSearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = modifier,
                query = query,
                onQueryChange = {
                    onQueryChange(it)
                },
                onSearch = {
                    onSearch(query)
                    onOpenSearch(query)
                },
                expanded = false,
                onExpandedChange = { },
                placeholder = {
                    Text(
                        "Search movies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        onSearch(query)
                        onOpenSearch(query)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Open search"
                        )
                    }
                }
            )
        },
        expanded = false,
        onExpandedChange = { },
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
fun TitleSection(
    homeViewModel: HomeScreenViewModel,
    accountInfo: AccountDomain? = null,
    searchQuery: String,
    onSearchSubmit: (String) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var isProfileMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Sections("Discover", "What to watch today?", Modifier.padding(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        Box {
            MakeProfileIcon(
                profileUrl = accountInfo?.profileUrl,
                displayName = accountInfo?.name,
                username = accountInfo?.username,
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
            ) { isProfileMenuExpanded = true }

            DropdownMenu(
                expanded = isProfileMenuExpanded,
                onDismissRequest = { isProfileMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Log out") },
                    onClick = {
                        isProfileMenuExpanded = false
                        onProfileClick()
                    }
                )
            }
        }

    }
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        MovieSearchBar(
            query = searchQuery,
            onQueryChange = { query ->
                if (query.isEmpty()) {
                    homeViewModel.clearSearchMovie()
                } else {
                    homeViewModel.updateSearchQuery(query)
                    homeViewModel.searchMovies(query)
                }
            },
            onSearch = { value ->
                homeViewModel.updateSearchQuery(value)
                homeViewModel.searchMovies(value)
            },
            onOpenSearch = { value ->
                onSearchSubmit(value)
            }
        )
    }
}

@Composable
fun MakeProfileIcon(
    profileUrl: String? = null,
    displayName: String? = null,
    username: String? = null,
    modifier: Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            if (!profileUrl.isNullOrBlank()) {
                LoadImage(
                    url = profileUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = displayName?.firstOrNull()?.uppercase()
                        ?: username?.firstOrNull()?.uppercase()
                        ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
fun GridMovieContent(
    homeViewModel: HomeScreenViewModel,
    movieType: MovieContentSection = MovieContentSection.MOVIE_REC,
    modifier: Modifier = Modifier,
    onMovieClick: (Long) -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    LaunchedEffect(
        gridState,
        uiState.moviesDiscovered.size,
        uiState.isLoadingDiscoverMore,
        uiState.discoverEndReached
    ) {
        if (movieType != MovieContentSection.MOVIE_EXPLORE) return@LaunchedEffect
        snapshotFlow { gridState.layoutInfo }
            .collect { layoutInfo ->
                val total = layoutInfo.totalItemsCount
                if (total == 0) return@collect
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@collect
                val nearEnd = lastVisible >= total - 4
                if (nearEnd) {
                    homeViewModel.loadNextDiscover()
                }
            }
    }

    Column(modifier = modifier) {
        LazyVerticalGrid(
            state = gridState,
            modifier = Modifier.height(600.dp),
            contentPadding = PaddingValues(16.dp),
            columns = GridCells.Adaptive(140.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)


        ) {
            items(
                if (movieType == MovieContentSection.MOVIE_EXPLORE) {
                    uiState.moviesDiscovered
                } else {
                    uiState.moviesPopular
                }
            ) { movie ->
                MovieCard(
                    0,
                    movie,
                    false,
                    true,
                    false,
                    false
                ) { onMovieClick(movie.id) }


            }
            if (movieType == MovieContentSection.MOVIE_EXPLORE && uiState.isLoadingDiscoverMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun RowingMoviesContent(
    homeViewModel: HomeScreenViewModel,
    movieType: MovieContentSection = MovieContentSection.MOVIE_REC,
    mod: Modifier = Modifier,
    onMovieClick: (Long) -> Unit,
    recommenderViewModel: RecommenderViewModel
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val recommendedMovies by recommenderViewModel.recommendedMovies.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(
        listState,
        movieType,
        uiState.moviesPopular.size,
        uiState.moviesTrending.size,
        uiState.isLoadingPopularMore,
        uiState.isLoadingTrendingMore,
        uiState.popularEndReached,
        uiState.trendingEndReached
    ) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val total = layoutInfo.totalItemsCount
                if (total == 0) return@collect
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@collect
                val nearEnd = lastVisible >= total - 3
                if (!nearEnd) return@collect
                when (movieType) {
                    MovieContentSection.MOVIE_REC -> homeViewModel.loadNextPopular()
                    MovieContentSection.MOVIE_TRENDING -> homeViewModel.loadNextTrending()
                    else -> Unit
                }
            }
    }

    Column(
        modifier = mod
    ) {


        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(movieType == MovieContentSection.MOVIE_REC && recommendedMovies.isNotEmpty())
            {
                itemsIndexed(recommendedMovies)
                {
                    index, movie ->
                    MovieCard(
                        index,
                        movie,
                        false,
                        true,
                        true,
                        false
                    ) { onMovieClick(movie.id) }
                }
            }
            //If cannot find recommendations yet.
            else{
                itemsIndexed(if (movieType == MovieContentSection.MOVIE_REC){
                    uiState.moviesPopular
                }else{
                    uiState.moviesTrending
                }) { index, movie ->
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

            val isLoadingMore = when (movieType) {
                MovieContentSection.MOVIE_REC -> uiState.isLoadingPopularMore
                MovieContentSection.MOVIE_TRENDING -> uiState.isLoadingTrendingMore
                MovieContentSection.MOVIE_EXPLORE -> false
            }

            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .height(220.dp)
                            .width(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
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
