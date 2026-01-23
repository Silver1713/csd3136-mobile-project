package com.csd3156.mobileproject.MovieReviewApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
//import com.csd3156.mobileproject.MovieReviewApp.data.Account
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.csd3156.mobileproject.MovieReviewApp.ui.theme.MovieReviewAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieReviewAppTheme {
                MovieReviewNavHost()
            }
        }
    }
}

@Composable
fun MovieReviewNavHost(startDestination: String = "movieList") {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("movieList") {
            MovieListRoute(
                onMovieClick = { id -> navController.navigate("movieDetail/$id") }
            )
        }
        composable("movieDetail/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toLongOrNull()
                ?: return@composable
            MovieDetailRoute(
                movieId = movieId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}


@Composable
fun GreetingPreview() {
    MovieReviewAppTheme {
        Greeting("Android")
    }
}