package com.csd3156.mobileproject.MovieReviewApp.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovieReviewApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}