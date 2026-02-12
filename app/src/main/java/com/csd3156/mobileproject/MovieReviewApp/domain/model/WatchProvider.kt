package com.csd3156.mobileproject.MovieReviewApp.domain.model

data class WatchProvider(
    val id: Long,
    val name: String,
    val logoUrl: String,
    val type: String,
    val countryCode: String,
    val link: String
)
