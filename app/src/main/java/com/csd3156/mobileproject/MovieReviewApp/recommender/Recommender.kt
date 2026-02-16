package com.csd3156.mobileproject.MovieReviewApp.recommender
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.io.Read
import smile.regression.OLS
/*
Recommends a list of movies similar to what the user has watched.
Content-based filtering (since TMDB doesn't allow to see what users watched what movies, so it's harder to integrate collaborative. Also idk how).
 */
class Recommender {
    /*
        Note: Use Dispatchers.Default when executing coroutines for training, as it splits workload among
        background threads
     */
}