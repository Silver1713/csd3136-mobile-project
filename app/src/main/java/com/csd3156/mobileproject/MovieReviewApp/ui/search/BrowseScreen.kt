package com.csd3156.mobileproject.MovieReviewApp.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csd3156.mobileproject.MovieReviewApp.ui.movies.list.MovieListViewModel
import kotlinx.serialization.Serializable

@Serializable
data object SearchScreen


@Composable
fun BrowseScreen(
    viewmodel: MovieListViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onMovieClick: (Long) -> Unit
){

}