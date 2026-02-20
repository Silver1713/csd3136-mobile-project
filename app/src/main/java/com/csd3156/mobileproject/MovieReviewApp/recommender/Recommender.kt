
package com.csd3156.mobileproject.MovieReviewApp.recommender
import android.R.attr.type
import android.content.Context
import android.text.TextUtils.split
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.londogard.nlp.embeddings.EmbeddingLoader
import com.londogard.nlp.embeddings.sentence.AverageSentenceEmbeddings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
//Sentence Embedding --> Sentence to Vector
import com.londogard.nlp.embeddings.sentence.*
import com.londogard.nlp.embeddings.*
import com.londogard.nlp.utils.LanguageSupport.*
import com.londogard.nlp.embeddings.sentence.USifSentenceEmbeddings
import com.londogard.nlp.meachinelearning.vectorizer.count.CountVectorizer
import com.londogard.nlp.utils.LanguageSupport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import smile.nlp.*;
import smile.util.SparseArray

val Context.dataStore by preferencesDataStore(name = "vectorizer_map_storage")
/*
Recommends a list of movies similar to what the user has watched.
Content-based filtering (since TMDB doesn't allow to see what users watched what movies, so it's harder to integrate collaborative. Also idk how).
 */
class Recommender (context: Context){
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
        //Fit count vectorizer, so it generates a fixed-size vector for any category string later.
        var categoryStrings = mutableListOf<List<String>>();
        for(movie in movies)
        {
            categoryStrings.add(GetPreProcessedCategoryWords(movie))
        }
        val countVectorizer = CountVectorizer<Float>();
        countVectorizer.fit(categoryStrings);
        CountVectorizerMapStore.saveMap(appContext, countVectorizer.vectorization);
        /*
            Note: Make each matrix separately (clear mem after storing in local database) to reduce memory usage.
            Use smile.SparseArray, SparseMatrix to save space.
         */
    }

    /*
        Brief: Returns a list of movie ids as recommendations, based on the user's watchlist of movies.
        Must train the model before calling this.
        A higher number of movies in the watchlist will give more accurate results.
        Note: If one movie is passed in, then recommendations will be based on that specific movie.
     */
    suspend fun GetRecommendations(userWatchlist : List<MovieDetails>, numRecommendations : Int) : Flow<Int> {
        return emptyFlow()
    }

    /*
        Brief: Helper to compute a list of vectors based on movie features.
        Vector 1: Overview + Tagline using wordembedding or TF-IDF
        Vector 2: Genres, Original Language, Adult, Release Decade(one-hot-encoded)
    */
    private suspend fun ComputeVectors(movieDetails : MovieDetails) : List<SparseArray> {
        //Return vectors.
        var vectorList = mutableListOf<Array<Float>>();

        /*  Pre-process data */
        val overviewTagWords = GetPreProcessedOverviewTagWords(movieDetails);
        val categoryWords = GetPreProcessedCategoryWords(movieDetails);

        /*
            Vector 1: Overview + Tagline using wordembedding or TF-IDF
            - Use pre-trained word embedders that are suitable for short paragraphs like SBERT/FastText.
            - May need to reduce number of features, but if so ensure the vector structure is kept the same for user watchlist.
         */
        //Using Londoguard sentence embedding to form a vector from the paragraph.
        val overviewTagVector = sentenceEmbedder.getSentenceEmbedding(overviewTagWords);

        /*
            Vector 2: Genres, Original Language, Adult, Release Decade(one-hot-encoded)
            Using count-vectorize on a concatenated string (repeating feature words based on their weights)

            Run .fit() on the entire dataset of 10000? first to capture all potential categories.
            Afterwards just keep and reuse the vectorizer for all training and usage purposes, to ensure vector structure is the same.
         */
        //Using trained count vectorizer
        val countVectorizer = CountVectorizer<Float>();
        countVectorizer.vectorization = CountVectorizerMapStore.getMap(appContext);
        val categoryVector = countVectorizer.transform(listOf<List<String>>(categoryWords)).data.toList();

        return listOf(SparseArray(overviewTagVector), SparseArray(categoryVector));
    }

    //Concatenates and cleans up (lowercase, remove unnecessary space etc) a string of important categorical information
    //Returns an array of words
    //Used: Overview, Tagline following weight 1:X specified in class variables.
    private suspend fun GetPreProcessedOverviewTagWords(movieDetails: MovieDetails) : List<String>
    {
        //Basic concatenation and cleaning
        val overviewTaglineTags = mutableListOf<String>();
        overviewTaglineTags.add(movieDetails.overview);
        repeat(taglineRepeat) {overviewTaglineTags.add(movieDetails.tagline);}
        val overviewTagString = overviewTaglineTags.joinToString(" ").lowercase();

        //Using smile to split into sentences, then words. Exclude common stop-words like "the"
        val overviewTagNormalizedSentences = overviewTagString.normalize().sentences();
        //Filter stop-words by adjusting filter strength here.
        return overviewTagNormalizedSentences.flatMap{it.words(filter = "default").asIterable()};
    }

    //Concatenates and cleans up (lowercase, remove unnecessary space) a string of important categorical information
    //Used: Genres, Original Language, Adult, Release Decade(one-hot-encoded), following the weights 1:X:Y:Z specified in class variables.
    private suspend fun GetPreProcessedCategoryWords(movieDetails : MovieDetails) : List<String>
    {
        //Basic concatenation and cleaning
        //2026-01-02, take the decade
        val year = movieDetails.releaseDate.split("-")[0].toInt()
        //Code it into a string so it doesn't clash with any numbers that may be in other categories.
        val decadeString = when (year) {
            in 0..1929 -> "silentera"
            in 1930..1949 -> "goldenage"
            in 1950..1969 -> "newhollywood"
            in 1970..1989 -> "blockbusterera"
            in 1990..2009 -> "modernera"
            in 2010..2029 -> "digitalera"
            else -> "futureera"
        }
        val adultString = if(movieDetails.adult) "adulttag" else "";
        val allTags = mutableListOf<String>();
        for(genre in movieDetails.genres)
        {
            allTags.add(genre.name.replace(" ", "").lowercase());
        }
        repeat(decadeRepeat) { allTags.add(decadeString.lowercase()) }
        repeat(adultRepeat) { if (adultString.isNotEmpty()) allTags.add(adultString.lowercase()) }
        repeat(languageRepeat) { allTags.add(movieDetails.originalLanguage.replace(" ", "").lowercase()) }

        return allTags;
    }

    //Private local_database_instance
    //Private datastore/file count_vectorizer (store in mem so the .fit() can be kept, reset when train model is reran)


    class CountVectorizerMapStore{
        companion object
        {
            suspend fun saveMap(context: Context, map: Map<String, Int>) {
                context.dataStore.edit { prefs ->
                    map.forEach { (key, value) ->
                        prefs[intPreferencesKey(key)] = value
                    }
                }
            }

            suspend fun getMap(context: Context): Map<String, Int> {
                return context.dataStore.data.first().asMap().mapKeys {
                    it.key.name
                }.filterValues { it is Int } as Map<String, Int>
            }
        }
    }
    private val appContext = context.applicationContext

    companion object{
        //Weightage modifiers for vectorization of movie
        public var taglineRepeat = 2;
        public var decadeRepeat = 1;
        public var adultRepeat = 3;
        public var languageRepeat = 2;

        //Sentence Embedder for vectorization of movie summary paragraph
        private val sentenceEmbedder = AverageSentenceEmbeddings(EmbeddingLoader.fromLanguageOrNull<BpeEmbeddings>(en)!!)
    }


}

