package com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import java.util.Date
import java.util.UUID

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
        @NonNull
    val id : Long,
    val uid : String,
    val email : String,
    val username : String,
    val name : String?,
    val profileUrl : String? = null,
    val bio : String?,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
){
    fun toDomain() : AccountDomain {
        return AccountDomain(
            uid = uid,
            email = email,
            username = username,
            name = name,
            bio = bio,
            profileUrl = profileUrl,
            createdAt = createdAt.time,
            updatedAt = updatedAt.time
        )
    }
}