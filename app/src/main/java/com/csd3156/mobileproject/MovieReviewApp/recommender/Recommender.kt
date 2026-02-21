package com.csd3156.mobileproject.MovieReviewApp.recommender
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.SortedMap
import javax.inject.Singleton

//Datastore for count vectorization
val Context.dataStore by preferencesDataStore(name = "recommenderCategoryCount")
/*
Recommends a list of movies similar to what the user has watched.
Content-based filtering (since TMDB doesn't allow to see what users watched what movies, so it's harder to integrate collaborative. Also idk how).

 Note:
 - Call TrainModel before usage
 - Should be placed in a view model so it lasts through recomposition etc.

 */
class Recommender private constructor(context : Context){

    /*
        Note: Use Dispatchers.Default when executing coroutines for training, as it splits workload among
        background threads
     */

    /* Errors in Implementation, don't use.
    - Londogard cannot build, as function name conflicts with android rules
    - Smile cannot run due to regex error when init
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
    Vector 1 --> Combine Overview, Tagline into a single string before passing through a sentence embedder (pre-trained model from Google's Tensorflow Lite
    --> Multiply Tagline by a higher weight to prevent it from being drowned out by the higher overview length.
    Vector 2 --> Genres, Original Language, Adult, Release Decade(one-hot-encoded)
    - Clean categorical features like "Sci Fi" to "scifi" to remove unnecessary spaces and make the term be treated the same.
    - Can multiply language and adult feature by a higher weight in the vector created
    --> Since adult and original language is a big deal, repeat many times.
    - Vectorize Genres (multi-hot encoding), Original Language (one-hot encoding), Adult (with weightage), Release Decade(one-hot encoding)
    --> Have to find all possible unique categorical words for all categories, so make a dict<String, Int>. Set every unique string to 0 before storing.
    When reading the map, it can be copied; for each categoryString, increment respective words in the dictionary (only if there)
    Finally, sort dictionary and read each value before setting it in the vector.
    Note: New words should be ignored.

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

        // Get and save all potential category unique words
        SaveCategoryUniqueWords(movies);
        for(movie in movies)
        {
            //TODO: Multithreading, where each thread takes multiple movies?
            val overviewCategoryVectors = ComputeVectors(movie);
        }


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
            - Use pre-trained sentence embedders that are suitable for short paragraphs.
            - May need to reduce number of features, but if so ensure the vector structure is kept the same for user watchlist.
         */
        val overviewTagVector = textEmbedder.GetVectorFromText(GetPreProcessedOverviewTagWords(movieDetails));
        /*
            Vector 2: Genres, Original Language, Adult, Release Decade(one-hot-encoded)
            Using count-vectorize on a concatenated string (repeating feature words based on their weights)

            Run .fit() on the entire training dataset first to capture all potential categories.
            Afterwards just keep and reuse the vectorizer for all training and usage purposes, to ensure vector structure is the same.
         */
        val categoryList = GetPreProcessedCategoryWords(movieDetails);
        //Get the set of unique words from datastore, then form a map before running through the list.
        //If the map is already formed then reuse it.
        if(categoryWordsMap.isEmpty())
        {
            val set = CategoryWordsDatastore.getSet(appContext);
            categoryWordsMap = set.associateWith { 0 };
        }
        //Used to ensure vector always has the same structure (alphabetical order).
        val mapCopy = categoryWordsMap.toSortedMap();

        //Only increment, do not add any new words.
        for(word in categoryList)
        {
            if(!mapCopy.containsKey(word)) continue;
            mapCopy[word] ;
        }

        val categoryVector = FloatArray(mapCopy.size);
        for(pair in mapCopy)
        {

        }


        return emptyList()
    }

    //Writes to datastore a Set<String> representing unique words in the categorical strings
    private suspend fun SaveCategoryUniqueWords(movies : List<MovieDetails>)
    {
        //Checks all category words and joins them into a set.
        var uniqueWords = setOf<String>();
        for(movie in movies)
        {
            uniqueWords = uniqueWords + GetPreProcessedCategoryWords(movie).toSet();
        }
        CategoryWordsDatastore.saveSet(appContext, uniqueWords);
    }

    //Concatenates Overview, Tagline.
    //Returns a string
    //Used: Overview, Tagline following weight 1:X specified in class variables.
    private suspend fun GetPreProcessedOverviewTagWords(movieDetails: MovieDetails) : String
    {
        //Basic concatenation
        val overviewTaglineTags = mutableListOf<String>();
        overviewTaglineTags.add(movieDetails.overview);
        repeat(taglineRepeat) {overviewTaglineTags.add(movieDetails.tagline);}
        return overviewTaglineTags.joinToString(" ").lowercase();
    }

    //Returns a list of important categorical information
    //Used: Genres, Original Language, Adult, Release Decade(one-hot-encoded), following the weights 1:X:Y:Z specified in class variables.
    private suspend fun GetPreProcessedCategoryWords(movieDetails : MovieDetails) : List<String>
    {
        //Basic concatenation
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





    class CategoryWordsDatastore {
        companion object {
            suspend fun saveSet(context: Context, set: Set<String>) {
                context.dataStore.edit { prefs ->
                    prefs[stringSetPreferencesKey("vocabulary")] = set
                }
            }

            suspend fun getSet(context: Context): Set<String> {
                return context.dataStore.data
                    .first()[stringSetPreferencesKey("vocabulary")]
                    ?: emptySet()
            }
        }
    }

    lateinit var recommenderInstance: Recommender;

    private val appContext = context.applicationContext
    //Sentence Embedder for vectorization of movie summary paragraph.
    private val textEmbedder = TextEmbedder(appContext);
    private var categoryWordsMap = mapOf<String, Int>();

    companion object{
        //Weightage modifiers for vectorization of movie
        public var taglineRepeat = 2;
        public var decadeRepeat = 1;
        public var adultRepeat = 3;
        public var languageRepeat = 2;

        //Singleton pattern
        @Volatile
        private var INSTANCE: Recommender? = null

        fun getInstance(context: Context): Recommender {
            return INSTANCE ?: synchronized(this) {
                val instance = Recommender(context)
                INSTANCE = instance
                instance
            }
        }
    }

    //Private local_database_instance

}