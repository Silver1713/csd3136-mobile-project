package com.csd3156.mobileproject.MovieReviewApp.recommender
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.io.Read
import smile.regression.OLS
/*
Recommends a list of movies similar to what the user has watched.
Content-based filtering (since TMDB doesn't allow to see what users watched what movies, so it's harder to integrate collaborative. Also idk how).
 */
class Recommender{
    /*
        Note: Use Dispatchers.Default when executing coroutines for training, as it splits workload among
        background threads
     */


    /* Implementation
    Use MovieDetails class as the data features.
    Vector Features:
    - Overview
    - Tagline
    - Release Date
    - Genres (one-hot-encoding?)
    - original language
    - adult

    Other Important Features:
    - Rating, voteCount, popularity to give a weighted rating to the recommendations

    Training Steps  (done in a coroutine ran by dispatcher.default to not hang the thread):
    Theory: Vectorize important movie features, and put each vector into a matrix.
    1. Get the list of the top 10000 (?) movies based on weighted rating. This is the training dataset.
    2. Vectorize each movie's features into multiple vectors
    Vector 1 --> Combine Overview, Tagline into a single string before passing through a word embedder or TF-IDF to get the vector.
    - Use pre-trained word embedders that are suitable for short paragraphs like SBERT/FastText.
    Vector 2 --> Genres, Original Language, Adult, Release Decade(one-hot-encoded)
    - Clean categorical features like "Sci Fi" to "scifi" to remove unnecessary spaces and make the term be treated the same.
    - Can multiply language and adult feature by a higher weight in the vector created
    - Vectorize Genres (multi-hot encoding), Original Language (one-hot encoding), Adult (with weightage), Release Decade(one-hot encoding)
    --> It's easier to combine them all into a single string (separated by spaces) and use count-vectorize. repeat words to give higher weights.
    --> Since adult and original language is a big deal, repeat many times.
    Normalize each vector type separately.
    3. Store each vector type in independent matrixes
    4. Store the matrixes in a local database or such together with other items like id, rating, popularity, vote count.

    Usage Steps (done in a coroutine ran by dispatcher.default to not hang the thread):
    1. Take all user movies and form vectors based on the Training Steps or use existing vectors in the matrix if id is present.
    Give weightage based on user's rating + how recent they watched it, before taking the mean of those vectors to form a single vector (representing the user's preference).
    2a. Use that vector and find cosine similarity with each movie in the matrix, assigning the calculated value to each movie
    - Rating, voteCount, popularity(?) of that movie to give a weighted increase/decrease to the value calculated
    - Do for each matrix.
    2b. Combine scores for each matrix using weights.
    - Ensure scores are normalized within each matrix in 2a.
    3. Save that list of movie ids in a local datastore/database for easy retrieval (no need to check every time)
    4. Repeat steps (update vector, find movies, update list) when user reviews a movie.

    Note: For weightage like weighted rating, weighted user rating etc. make them easily adjustable.
     */


    /*
        Brief: Trains the model on movie data. Must be called before using the recommender system.
        Pass in a list of movies, where list length is around 5-8k.
        Higher list lengths mean a longer training time and higher ram usage, with possible crashes due to ram usage.
     */
    suspend fun TrainModel(movies : List<MovieDetails>) {
        /*
            Note: Make each matrix separately (clear mem after storing in local database) to reduce memory usage.
         */


    }

    /*
        Brief: Returns a list of movie ids as recommendations, based on the user's watchlist of movies.
        Must train the model before calling this.
        A higher number of movies in the watchlist will give more accurate results.
     */
    suspend fun GetRecommendations(userWatchlist : List<MovieDetails>, numRecommendations : Int) : Flow<Int> {
        return emptyFlow()
    }

    /*
        Brief: Helper to compute a list of vectors based on movie features.
    */
    private suspend fun ComputeVectors(movieDetails : MovieDetails) : List<Array<Float>> {
        /*
            Vector 1: Overview + Tagline using wordembedding or TF-IDF
            - Use pre-trained word embedders that are suitable for short paragraphs like SBERT/FastText.
            - May need to reduce number of features, but if so ensure the vector structure is kept the same for user watchlist.
         */

        /*
            Vector 2: Genres, Original Language, Adult, Release Decade(one-hot-encoded)
            Using count-vectorize on a concatenated string (repeating feature words based on their weights)

            Run .fit() on the entire dataset of 10000? first to capture all potential categories.
            Afterwards just keep and reuse the vectorizer for all training and usage purposes, to ensure vector structure is the same.
         */


        return emptyList()
    }
    //Private local_database_instance
    //Private datastore/file count_vectorizer (store in mem so the .fit() can be kept, reset when train model is reran)
}