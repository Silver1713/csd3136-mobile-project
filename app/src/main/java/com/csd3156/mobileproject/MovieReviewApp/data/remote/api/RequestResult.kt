package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

sealed class RequestResult<out T> {
    data class Success <T>(val message : String? = null,val data: T) : RequestResult<T>()
    data class Error(val message : String? = null, val exception: Exception? = null) : RequestResult<Nothing>()

}