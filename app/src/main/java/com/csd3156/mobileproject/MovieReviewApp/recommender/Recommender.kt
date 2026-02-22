/*
    Recommender System using cosine similarity matrix to predict similar movies to the input movie.
    Content-based system.
    Steps:  To be runned in a coroutine
    1. Call TrainModel with a large list of (popular) movies. Only done once.
    - ~5000-7000 is a good number, more will increase accuracy of model but take a longer time to train and use.
    - The trained model will be stored and reused from a database, so it lasts through app lifetimes.
    - Thus, only call TrainModel ONCE ever, unless updating model.
    2. Call GetRecommendations with a list of movies (user's watchlist, movie to recommend for)
    to get list of movie recommendations based on the movie(s) passed in.
    - TrainModel must be called first, ensure it completes before running this.
    3. Call TrainModel again only when the model needs to be retrained.
    - If user's watchlist changes, just call GetRecommendations with a new list.

    Not thread-safe.
    Recommender is a singleton, lasting for the entire program once created.
    Weight class variables can be adjusted to change influence of specific movie details on the vectorization, i.e. how impactful they are on similarity scores.
        public var taglineRepeat;
        public var decadeRepeat;
        public var adultRepeat;
        public var languageRepeat;
 */

/*
    Errors in Implementation, don't use these libraries.
    - Londogard cannot build, as function name conflicts with android rules
    - Smile cannot run due to regex error when init


    Implementation details:
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

    Overview: Content-based filtering (since TMDB doesn't allow to see what users watched what movies, so it's harder to integrate collaborative. Also idk how).
    - Create 2 vectors to store movie features
    - Store them in separate matrixes, together with vectors from other movies.
    - Perform cosine similarity on each matrix using input movie(s).
    - Combine the two scores for each movie using weighted rating + weighted features (e.g. giving 2nd vector more weight than 1st)
    - Choose top X movies with highest scores.

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
    3. Store the two vectors into a database together with those from other movies.

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.SortedMap
import javax.inject.Singleton
import org.ejml.simple.SimpleMatrix
import java.util.PriorityQueue
import kotlin.collections.toDoubleArray
import kotlin.math.absoluteValue


//Datastore for category unique words set.
val Context.dataStore by preferencesDataStore(name = "recommenderCategoryCount")
/*
Recommends a list of movies similar to what the user has watched.
Content-based filtering (since TMDB doesn't allow to see what users watched what movies, so it's harder to integrate collaborative. Also idk how).

 Note:
 - Call TrainModel before usage
 - Lasts for entire lifetime of program.
 */
class Recommender private constructor(context : Context){
    /*
        Brief: Trains the model on movie data. Must be called before using the recommender system.
        Pass in a list of movies, where list length is around 5-8k.
        Higher list lengths mean a longer training time.
     */
    suspend fun TrainModel(movies : List<MovieDetails>) {

        //Fool-proof, ensure cpu-heavy task doesn't block caller thread which may have called this on the wrong thread.
        withContext(Dispatchers.Default) {
            // Get and save all potential category unique words into a datastore for vectorizer to use.
            SaveCategoryUniqueWords(movies)
            //Used to store "trained" model.
            val recommenderDatabase = RecommenderDatabase.getDatabase(appContext)
            val recommenderDao = recommenderDatabase.recommenderDao()
            //Clean up all existing vectors, as they may not follow the same vector feature order.
            //Abit dangerous if the user closes the app halfway... use isTrained to indicate if model requires retraining.
            isTrained = false; //Model is deleted so set to false.
            recommenderDao.deleteAll()


            // Insert in batches of 100 to save RAM instead of all at once.
            //TODO: Multithreading, where each thread takes multiple movies? Need to be thread-safe.
            //TODO: For multi-threading, categoryWordsMap needs to be thread-safe.
            movies.chunked(100).forEach { movieBatch ->
                val movieEntityList = mutableListOf<MovieEntity>()

                // 3. Process each movie one-by-one (Synchronous)
                for (movie in movieBatch) {
                    val (overviewTagVector, categoryVector) = ComputeVectors(movie)
                    movieEntityList.add(
                        MovieEntity(
                            movie.id,
                            movie.rating,
                            movie.voteCount,
                            movie.popularity,
                            overviewTagVector.toList(),
                            categoryVector.toList()
                        )
                    )
                }

                // Save the batch of 100 to the Database
                recommenderDao.insertAll(movieEntityList)
            }
            isTrained = true;
            val testMovie = recommenderDao.getMovieById(movies[0].id);
            print(testMovie?.categoryVector);
        }
    }

    /*
        Brief: Returns a list of movie ids as recommendations, based on the user's watchlist of movies.
        Must train the model before calling this.
        A higher number of movies in the watchlist will give more accurate results.

        Returns a list where
     */
    suspend fun GetRecommendations(userWatchlist : List<MovieDetails>, numRecommendations : Int) : List<ScoreResult> {
        if (userWatchlist.isEmpty()) return emptyList();
        //Prevent recommending what the user already watched.
        val watchlistIds = userWatchlist.map { it.id }.toSet()

        //Database for movie features
        val recommenderDatabase = RecommenderDatabase.getDatabase(appContext);
        val recommenderDao = recommenderDatabase.recommenderDao();

        //Ensure weights add to 1.
        val weightSum = overviewTagWeight + categoryWeight;
        overviewTagWeight = overviewTagWeight / weightSum;
        categoryWeight = categoryWeight / weightSum;


        //Fool-proof, ensure cpu-heavy task doesn't block caller thread which may have called this on the wrong thread.
        val recommendationList = withContext(Dispatchers.Default) {
            /*
                1. Get the vectors of user's preferences, by taking a weighted average of all movie vectors in their watchlist.
                2. Get the 2 matrixes from database, and perform cosine similarity with the user's vector
                --> Stream it in batches so it doesn't eat too much ram.
                2b. For each movie add scores together using feature weight and weighted rating.
                3. Find top X movies with highest scores.
             */
            //Get user's preferences.
            var (userOverviewTagVector, userCategoryVector) = CalculateUserPreferences(userWatchlist, recommenderDao);
            if(userOverviewTagVector == null || userCategoryVector == null) return@withContext emptyList<ScoreResult>();

            //Stores score results for each movie, where movies with lower scores are at the head.
            val topScores = PriorityQueue<ScoreResult>(compareBy { it.score })

            //Perform cosine similarity, multiply by feature weight and weighted rating, before summing score.
            //Cosine Similarity: A.B / |A||B|  so normalize A and B first.
            userOverviewTagVector = userOverviewTagVector.divide(userOverviewTagVector.normF());
            userCategoryVector = userCategoryVector.divide(userCategoryVector.normF());

            //Pagination, calculate cosine similarity with batches of rows.
            val pageSize = 50;
            var offset = 0;
            //Compute in batches of 50 or less at a time.
            while (true) {
                val batch = recommenderDao.getMoviesPaged(pageSize, offset)
                if (batch.isEmpty()) break //Stop when end of database.

                // Convert batch to Matrices. Ensure user preference vector size and stored feature vector size is the same for each type.
                val overviewMatrix = SimpleMatrix(batch.size, userOverviewTagVector.numRows())
                val categoryMatrix = SimpleMatrix(batch.size, userCategoryVector.numRows())
                batch.forEachIndexed { i, movie ->
                    overviewMatrix.setRow(i, 0, *movie.overviewTagVector.map { it.toDouble() }.toDoubleArray())
                    categoryMatrix.setRow(i, 0, *movie.categoryVector.map { it.toDouble() }.toDoubleArray())
                }

                // Batch Cosine Similarity: (Matrix * Vector) / (MatrixRowNorms)
                val overviewScores = calculateBatchCosineSim(overviewMatrix, userOverviewTagVector)
                val categoryScores = calculateBatchCosineSim(categoryMatrix, userCategoryVector)

                /*
                    Scores are in a vector row x 1, where each row is the similarity score for that type for that movie.
                    Add both scores together in weight X:Y, based on how valuable each score is.
                    For example, category scores may have higher weight as they have valuable information like genres or language.
                 */
                for (i in batch.indices) {
                    //Don't recommend what the user has already watched.
                    if(watchlistIds.contains(batch[i].id)) continue;
                    //No need to normalize each cause cosine similarity already did it.
                    val finalScore = (overviewScores[i] * overviewTagWeight) + (categoryScores[i] * categoryWeight);
                    topScores.add(ScoreResult(batch[i].id, finalScore))
                    //Only keep top X movies.
                    if (topScores.size > numRecommendations) topScores.poll()
                }

                offset += pageSize
            }

            //Return top X movies.
            return@withContext topScores.toList().sortedByDescending { it.score };
        }
        return recommendationList;
    }

    //Getter, check if model has been trained.
    fun IsTrained() : Boolean{
        return isTrained;
    }



    //=== Helpers ===//

    //Writes to datastore a Set<String> representing unique words in the categorical strings
    private suspend fun SaveCategoryUniqueWords(movies : List<MovieDetails>)
    {
        //Checks all category words and joins them into a set.
        var uniqueWords = mutableSetOf<String>();
        for(movie in movies)
        {
            uniqueWords.addAll(GetPreProcessedCategoryWords(movie));
        }
        CategoryWordsDatastore.saveSet(appContext, uniqueWords);
    }

    /*
        Brief: Helper to compute a list of vectors based on movie features.
    */
    private suspend fun ComputeVectors(movieDetails : MovieDetails) : List<FloatArray> {
        /*
            Vector 1: Overview + Tagline using wordembedding or TF-IDF
            - Use pre-trained sentence embedders that are suitable for short paragraphs.
            - May need to reduce number of features, but if so ensure the vector structure is kept the same for user watchlist.
         */
        val overviewTagVector = textEmbedder.GetVectorFromText(GetPreProcessedOverviewTagWords(movieDetails));
        /*
            Vector 2: Genres, Original Language, Adult, Release Decade(one-hot-encoded)
            Using count-vectorize on a concatenated string (repeating feature words based on their weights)
            This automatically "hot encodes" the category values into separate columns in the vector.

            Run .fit() on the entire training dataset first to capture all potential categories.
            Afterwards just keep and reuse the vectorizer for all training and usage purposes, to ensure vector structure is the same.
         */
        val categoryList = GetPreProcessedCategoryWords(movieDetails);
        //Get the set of unique words from datastore, then form a map before running through the list.
        //If the map is already formed then reuse it.
        if(categoryWordsSet.isEmpty())
        {
            //Expensive operation, only do this once.
            categoryWordsSet = CategoryWordsDatastore.getSet(appContext);
        }
        //Sorted map to ensure vector always has the same structure (alphabetical order).
        val mapCopy = categoryWordsSet.associateWith{0}.toSortedMap();
        for(word in categoryList)
        {
            //Only increment, do not add any new words to the map to prevent different size vectors.
            if(!mapCopy.containsKey(word)) continue;
            mapCopy[word] = (mapCopy[word] ?: 0) + 1;
        }

        //Iterate over the map to see which ones have values in them. This turns the map into a vector.
        val categoryVector = FloatArray(mapCopy.size);
        var currIdx = 0; //each element in the map corresponds to its own index in the vector.
        for(pair in mapCopy)
        {
            categoryVector[currIdx++] = pair.value.toFloat();
        }

        return listOf(overviewTagVector, categoryVector);
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

    //Returns unnormalized weighted (TODO) average of user's watchlist movie feature vectors
    private suspend fun CalculateUserPreferences(userWatchlist : List<MovieDetails>, recommenderDao : RecommenderDao) : List<SimpleMatrix?>
    {
        var overviewTagVectorSum : SimpleMatrix? = null;
        var categoryVectorSum : SimpleMatrix? = null;

        //Get vectors of user's preferences.
        for(movie in userWatchlist)
        {
            val overviewTagVector : FloatArray?;
            val categoryVector : FloatArray?;
            //Get from database (existing movie vectors) if available, no need for recalculations.
            if(recommenderDao.exists(movie.id))
            {
                val storedResult = recommenderDao.getMovieById(movie.id);
                overviewTagVector = storedResult?.overviewTagVector?.toFloatArray();
                categoryVector = storedResult?.categoryVector?.toFloatArray();
            }
            else
            {
                val calculatedResult = ComputeVectors(movie);
                overviewTagVector = calculatedResult[0];
                categoryVector = calculatedResult[1];
            };
            //Something went wrong, ignore movie.
            if(overviewTagVector == null || categoryVector == null)
            {
                continue;
            }
            //Init or add to sum.
            //TODO: Currently giving all user's movies equal weights. It should be weighted based off their ratings instead.
            if(overviewTagVectorSum == null) overviewTagVectorSum = SimpleMatrix(overviewTagVector.size, 1, true, overviewTagVector);
            else overviewTagVectorSum += SimpleMatrix(overviewTagVector.size, 1, true, overviewTagVector);
            if(categoryVectorSum == null) categoryVectorSum = SimpleMatrix(categoryVector.size, 1, true, categoryVector);
            else categoryVectorSum += SimpleMatrix(categoryVector.size, 1, true, categoryVector);
        }
        if(overviewTagVectorSum == null || categoryVectorSum == null) return listOf(null, null);
        return listOf(overviewTagVectorSum, categoryVectorSum);
    }

    //Computes Matrix - Vector cosine similarity.
    //Normalize the vector before passing it in.
    private suspend fun calculateBatchCosineSim(matrix: SimpleMatrix, normUserVec: SimpleMatrix): DoubleArray {
        // Matrix Multiplication: [Rows x Features] * [Features x 1] = [Rows x 1]
        val dotProducts = matrix.mult(normUserVec)
        //Each row represents the similarity score for that movie for that specific type.
        val results = DoubleArray(matrix.numRows())

        //Normalize the results for each row.
        for (i in 0 until matrix.numRows()) {
            val rowNorm = matrix.extractVector(true, i).normF()
            //When normalizing avoid division by 0
            results[i] = if (rowNorm.absoluteValue > 0.0001) dotProducts.get(i) / rowNorm else 0.0
        }
        return results
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

    //Keeps track of combined weighted similarity score for a movie.
    data class ScoreResult(val movieId: Long, val score: Double)

    //Context of the current app, making it last as long as the recommender and its databases so it's safer than local context.
    private val appContext = context.applicationContext
    //Sentence Embedder for vectorization of movie summary paragraph.
    private val textEmbedder = TextEmbedder(appContext);
    //Used for count vectorization of categories. Need to turn it into a SORTED map.
    private var categoryWordsSet = setOf<String>();
    //Indicates whether the model has been trained fully.
    private var isTrained = false;

    companion object{
        //Weightage modifiers for vectorization of movie
        public var taglineRepeat = 2;
        public var decadeRepeat = 1;
        public var adultRepeat = 3;
        public var languageRepeat = 2;

        //Weightage modifiers for similarity scoring, i.e. how important X should be relative to Y.
        public var categoryWeight = 0.7;
        public var overviewTagWeight = 0.3;

        //Singleton pattern
        @Volatile
        private var INSTANCE: Recommender? = null

        fun getInstance(context: Context): Recommender {
            //Return existing instance, or create a new one if it doesn't exist
            //Only one instance will be created for the entire program.
            return INSTANCE ?: synchronized(this) {
                val instance = Recommender(context)
                INSTANCE = instance
                instance
            }
        }
    }
}