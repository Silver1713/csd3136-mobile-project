package com.csd3156.mobileproject.MovieReviewApp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.Account
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountDAO
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Convertors.DateConvertor
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Convertors.UUIDConvertor
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistMovie
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistDao

@Database(
    entities = [Account::class, ReviewEntity::class,WatchlistMovie::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(
    DateConvertor::class,
    UUIDConvertor::class
)
abstract class MovieReviewDatabase : RoomDatabase() {
    abstract fun accountDao() : AccountDAO
    abstract fun reviewDao(): ReviewDao
    abstract fun watchlistDao(): WatchlistDao

    @Deprecated(
        message = "Use Hilt instead",
        ReplaceWith("@Hilt DatabaseModule")
    )
    companion object {
        @Volatile
        private var instance: MovieReviewDatabase? = null


        fun getInstance(context: Context): MovieReviewDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }
        }


        private fun buildDatabase(context: Context): MovieReviewDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MovieReviewDatabase::class.java,
                "movie_reviews.db"
            ).fallbackToDestructiveMigration(true).build()
        }
    }
}
