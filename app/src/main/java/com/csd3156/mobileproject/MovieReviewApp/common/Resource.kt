package com.csd3156.mobileproject.MovieReviewApp.common

// Sealed class to represent loading, success and error UI states
sealed interface Resource<out T>{
    data object Loading: Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>
}