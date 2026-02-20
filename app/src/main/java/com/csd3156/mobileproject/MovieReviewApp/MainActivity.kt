package com.csd3156.mobileproject.MovieReviewApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.csd3156.mobileproject.MovieReviewApp.ui.main.Main
import com.csd3156.mobileproject.MovieReviewApp.ui.main.HomeScreen
import com.csd3156.mobileproject.MovieReviewApp.ui.movies.details.MovieDetailScreen
import com.csd3156.mobileproject.MovieReviewApp.ui.movies.list.MovieListViewModel
import com.csd3156.mobileproject.MovieReviewApp.ui.search.SearchScreen
import com.csd3156.mobileproject.MovieReviewApp.ui.search.BrowseScreen
import com.csd3156.mobileproject.MovieReviewApp.ui.theme.MovieReviewAppTheme
import kotlinx.serialization.Serializable

import com.csd3156.mobileproject.MovieReviewApp.ui.profile.Profile
import com.csd3156.mobileproject.MovieReviewApp.ui.profile.ProfileScreen
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.csd3156.mobileproject.MovieReviewApp.ui.AppViewModel
import com.csd3156.mobileproject.MovieReviewApp.ui.login.AccountScreen
import com.csd3156.mobileproject.MovieReviewApp.ui.login.AccountViewModel
import com.csd3156.mobileproject.MovieReviewApp.ui.login.accountScreen
import com.csd3156.mobileproject.MovieReviewApp.ui.search.browseMovieViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.csd3156.mobileproject.MovieReviewApp.ui.watchlist.Watchlist
import com.csd3156.mobileproject.MovieReviewApp.ui.watchlist.WatchlistScreen




@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MovieReviewAppTheme(darkTheme = true) {
                val navController = rememberNavController()
                val appViewModel = hiltViewModel<AppViewModel>()
                val rootState by appViewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                    },
                    bottomBar = {
                        if (rootState.isLoggedIn){
                            BottomBar(navController)
                        }
                    }
                ) {

                    MovieReviewNavHost(rootVM = appViewModel, navController, modifier = Modifier.padding(it),
                        startDestination = AccountScreen)
                }
            }
        }
    }
}

@Composable
fun MovieReviewNavHost(rootVM: AppViewModel, controller: NavHostController ,modifier: Modifier = Modifier, startDestination: Any = Main) {
    val context = LocalContext.current
    val movieVM : MovieListViewModel = hiltViewModel()
    val accountVM : AccountViewModel = hiltViewModel()
    val searchQuery : MutableState<String?> = rememberSaveable { mutableStateOf(null) }
    val accountSelected by accountVM.activeUser.collectAsStateWithLifecycle(null)

    NavHost(navController = controller, startDestination = startDestination) {
        composable <AccountScreen>{
            accountScreen(
                accountVM = accountVM,
                modifier = modifier,
            ) {
                account ->
                rootVM.loginAccount(account.uid)
                controller.navigate(Main){
                    popUpTo(AccountScreen){
                        inclusive = true
                    }
                }

            }
        }
        composable<Main>{
            HomeScreen(
                modifier = modifier,
                onMovieClick = { movieId ->
                    controller.navigate(MovieDetailsDestination(movieId))
                },
                onSearchSubmit = { query ->
                    searchQuery.value  = query
                    controller.navigate(SearchScreen) {
                        popUpTo(Main) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<SearchScreen>{
            val browseScreenViewModel : browseMovieViewModel = hiltViewModel()
            if (searchQuery.value != null){
                browseScreenViewModel.updateSearchQuery(searchQuery.value ?: "")
                browseScreenViewModel.searchMovies(searchQuery.value ?: "")
                searchQuery.value = null
            }

            BrowseScreen (
                browseScreenVM = browseScreenViewModel,
                modifier = modifier
            ){ movieId ->
                controller.navigate(MovieDetailsDestination(movieId))
            }


        }
        composable<MovieDetailsDestination> {
            val args = it.toRoute<MovieDetailsDestination>()
            MovieDetailRoute(
                movieId = args.movieId,
                modifier = modifier,
                movieListViewModel = movieVM,
                onBack = { controller.popBackStack() }
            )
        }

        composable<Profile> {
            LaunchedEffect(
                key1 = accountSelected
            ) {
                if (accountSelected == null) {
                    controller.navigate(
                        AccountScreen
                    ){
                        popUpTo(controller.graph.id)
                        { inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
            ProfileScreen(
                modifier = modifier,
                onMyWatchlist = { controller.navigate(Watchlist)},
                onLogout = {
                    accountVM.logout()
                    rootVM.setLoggedIn(false)
                }
            )
        }

        composable<Watchlist> {
            WatchlistScreen(
                onBack = { controller.popBackStack() },
                onMovieClick = { movieId ->
                    controller.navigate(MovieDetailsDestination(movieId))
                }
            )
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailRoute(
    movieId: Long,
    modifier: Modifier = Modifier,
    movieListViewModel: MovieListViewModel,
    onBack: () -> Unit
) {
    val uiState by movieListViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        movieListViewModel.loadMovieDetails(movieId)
        movieListViewModel.loadMovieReviews(movieId)
        movieListViewModel.loadMovieWatchProviders(movieId)
        movieListViewModel.loadMovieVideos(movieId)
        movieListViewModel.observeLocalReviews(movieId)
    }

    val isScreenLoading = uiState.selectedMovieDetails?.id != movieId && uiState.isLoading
    val combinedReviews = uiState.selectedMovieLocalReviews + uiState.selectedMovieReviews
    MovieDetailScreen(
        modifier = modifier,
        movieVM = movieListViewModel,
        movie = uiState.selectedMovieDetails,
        reviews = combinedReviews,
        videos = uiState.selectedMovieVideos,
        watchProviders = uiState.selectedMovieWatchProviders,
        isLoading = isScreenLoading,
        errorMessage = uiState.errorMessage,
        onBack = onBack,
        onSubmitReview = { author, rating, content, photoPath ->
            movieListViewModel.addLocalReview(movieId, author, rating, content, photoPath)
        }
    )
}

@Serializable
data object P2

@Serializable
data class MovieDetailsDestination(val movieId: Long)
@Composable
fun BottomBar(navController: NavHostController) {


    data class NavItem<T: Any>(
        val label: String,
        val route: T,
        val icon: ImageVector
    )
    val items = listOf(
        NavItem("Home", Main, Icons.Default.Home),
        NavItem("Search", SearchScreen, Icons.Default.Search),
        NavItem("Profile", Profile, Icons.Default.AccountCircle)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 10.dp
    ) {
        items.forEach { item ->

            val selected = currentRoute == item.route::class.qualifiedName

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },

                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },

                label = {
                    Text(item.label)
                },

                alwaysShowLabel = true,

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                )
            )
        }
    }
}

