package com.csd3156.mobileproject.MovieReviewApp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ReviewEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MovieReviewDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao

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
