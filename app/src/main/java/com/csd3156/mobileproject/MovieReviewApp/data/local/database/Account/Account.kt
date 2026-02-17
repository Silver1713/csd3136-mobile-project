package com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id : Int = 0,
    val uuid : UUID,
    val username : String,
    val hashed_password : String? = null,
    val name : String?,
    val bio : String?,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)