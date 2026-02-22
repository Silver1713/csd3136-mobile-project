package com.csd3156.mobileproject.MovieReviewApp.recommender

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.java

/*
    Used to store data used in the Recommender Machine Learning model.
    Database consists of movie information like id, rating, vote count used in recommending, as well as
    vectors of movie features.
*/

@Entity(tableName = "movie_features")
data class MovieEntity(
    @PrimaryKey val id: Long,
    val rating: Double,
    val voteCount: Int,
    val popularity: Double,
    // Represents the movie features as a vector
    val overviewTagVector: List<Float>,
    val categoryVector: List<Float>
)

class listConverter {
    @TypeConverter
    fun fromFloatList(value: List<Float>): String = value.joinToString(",")

    @TypeConverter
    fun toFloatList(value: String): List<Float> =
        if (value.isEmpty()) emptyList() else value.split(",").map { it.toFloat() }
}

@Dao
interface RecommenderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    /**
     * Clears the entire trained model/feature set.
     * Useful at the start of TrainModel() to prevent stale data.
     */
    @Query("DELETE FROM movie_features")
    suspend fun deleteAll()

    @Query("SELECT * FROM movie_features WHERE id = :id")
    suspend fun getMovieById(id: Long): MovieEntity?

    @Query("SELECT * FROM movie_features")
    fun getAllMovies(): Flow<List<MovieEntity>>
}

@Database(entities = [MovieEntity::class], version = 1)
@TypeConverters(listConverter::class)
abstract class RecommenderDatabase : RoomDatabase() {
    abstract fun recommenderDao(): RecommenderDao

    companion object {
        @Volatile
        private var INSTANCE: RecommenderDatabase? = null

        fun getDatabase(context: Context): RecommenderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecommenderDatabase::class.java,
                    "recommender_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}