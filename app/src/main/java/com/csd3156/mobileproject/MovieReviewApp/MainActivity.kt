package com.csd3156.mobileproject.MovieReviewApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.csd3156.mobileproject.MovieReviewApp.ui.theme.MovieReviewAppTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MovieReviewAppTheme(darkTheme = true) {
                val navController = rememberNavController()

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                    },
                    bottomBar = {
                        BottomBar(navController)
                    }
                ) {

                    MovieReviewNavHost(navController, modifier = Modifier.padding(it))
                }
            }
        }
    }
}

@Composable
fun MovieReviewNavHost(controller: NavHostController ,modifier: Modifier = Modifier, startDestination: Any = Main) {
    val movieVM : MovieListViewModel = viewModel(factory = MovieListViewModel.provideFactory())

    NavHost(navController = controller, startDestination = startDestination) {
        composable<Main>{
            HomeScreen(
                viewmodel = movieVM,
                modifier = modifier
            ) { movieId ->
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
    }

    val isScreenLoading = uiState.selectedMovieDetails?.id != movieId && uiState.isLoading
    MovieDetailScreen(
        modifier = modifier,
        movie = uiState.selectedMovieDetails,
        reviews = uiState.selectedMovieReviews,
        videos = uiState.selectedMovieVideos,
        watchProviders = uiState.selectedMovieWatchProviders,
        isLoading = isScreenLoading,
        errorMessage = uiState.errorMessage,
        onBack = onBack
    )
}

@Composable
fun Greeting(name: String){
    Text(text = "Hello $name!")
}

@Serializable
data object P1
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
        NavItem("Example1", P1, Icons.Default.Home),
        NavItem("Example2", P2, Icons.Default.Home),
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
                        popUpTo(Main) { saveState = true }
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

