package com.csd3156.mobileproject.MovieReviewApp.recommender.di

import com.csd3156.mobileproject.MovieReviewApp.recommender.MLReccConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
       @Singleton
       @Provides
       fun provideReccomendedConfig() : MLReccConfig {
              return MLReccConfig()
       }
}