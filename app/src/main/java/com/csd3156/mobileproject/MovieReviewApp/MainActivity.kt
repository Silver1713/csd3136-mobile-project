package com.csd3156.mobileproject.MovieReviewApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.csd3156.mobileproject.MovieReviewApp.ui.main.Main
import com.csd3156.mobileproject.MovieReviewApp.ui.main.HomeScreen
import com.csd3156.mobileproject.MovieReviewApp.ui.theme.MovieReviewAppTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieReviewAppTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
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

    NavHost(navController = controller, startDestination = startDestination) {
        composable<Main>{
            HomeScreen(modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailRoute(movieId: Long, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Movie #$movieId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_media_previous),
                            contentDescription = stringResource(android.R.string.cancel)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Text(
            text = "Detail screen placeholder for movie id=$movieId.",
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
}

@Composable
fun Greeting(name: String){
    Text(text = "Hello $name!")
}