package com.csd3156.mobileproject.MovieReviewApp.domain.model

import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.Account
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.AccountDto
import com.google.firebase.Timestamp
import java.util.Date

data class AccountDomain(
    val uid : String,
    val email : String,
    val username : String,
    val name : String?,
    val profileUrl : String? = null,
    val bio : String?,
    val createdAt: Long = 0,
    val updatedAt: Long = 0


){
    fun toRoomEntity() : Account {
        return Account(
            id = 0,
            uid = uid,
            username = username,
            name = name,
            email = email,
            profileUrl = profileUrl,
            bio = bio,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)

        )
    }

    fun toDTO() : AccountDto {
        return AccountDto(
            uid = uid,
            username = username,
            name = name,
            email = email,
            profileUrl = profileUrl,
            bio = bio,
            createdAt = Timestamp(Date(createdAt)),
            updatedAt = Timestamp(Date(updatedAt))
        )
    }
}
