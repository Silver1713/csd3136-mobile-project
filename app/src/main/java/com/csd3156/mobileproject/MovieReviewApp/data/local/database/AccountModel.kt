package com.csd3156.mobileproject.MovieReviewApp.data.local.database

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accounts")
data class AccountModel(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id : Int,
    val uuid : UUID,
    val username : String,
    val name : String,
    val bio : String
)