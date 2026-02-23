package com.csd3156.mobileproject.MovieReviewApp.data.di

import android.app.Application
import androidx.room.Room
import com.csd3156.mobileproject.MovieReviewApp.data.local.MovieReviewDatabase
import com.csd3156.mobileproject.MovieReviewApp.data.local.ReviewDao
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountDAO
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistDao
import com.csd3156.mobileproject.MovieReviewApp.recommender.RecommenderDao
import com.csd3156.mobileproject.MovieReviewApp.recommender.RecommenderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): MovieReviewDatabase {
        return Room.databaseBuilder(
            app,
            MovieReviewDatabase::class.java,
            "movie_reviews.db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideAccountDao(database: MovieReviewDatabase): AccountDAO {
        return database.accountDao()
    }
    @Provides
    fun provideReviewDao(database: MovieReviewDatabase): ReviewDao {
        return database.reviewDao()
    }

    @Provides
    fun provideWatchlistDao(database: MovieReviewDatabase): WatchlistDao {
        return database.watchlistDao()
    }

    @Provides
    @Singleton
    fun provideReccomenderDatabase(app : Application) : RecommenderDatabase {
        return Room.databaseBuilder(
                app,
                RecommenderDatabase::class.java,
                "recommender_database"
            ).build()
    }

    @Provides
    fun provideRecommenderDao(database: RecommenderDatabase): RecommenderDao {
        return database.recommenderDao()
    }
}


