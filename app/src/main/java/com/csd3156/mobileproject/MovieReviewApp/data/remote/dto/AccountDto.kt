package com.csd3156.mobileproject.MovieReviewApp.data.remote.dto

import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class AccountDto(
    val uid: String = "",
    val email: String = "" ,
    val username: String = "",
    val name: String? =  null,
    val bio: String? = null,
    val profileUrl: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,

){
    fun toDomain() : AccountDomain {
        return AccountDomain(
            uid = uid,
            email = email,
            username = username,
            name = name,
            bio = bio,
            profileUrl = profileUrl,
            createdAt = createdAt?.toDate()?.time ?: 0,
            updatedAt = updatedAt?.toDate()?.time ?: 0
        )
    }
}

data class CreateAccountDto(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val name: String? = null,
    val bio: String? = null,
    val profileUrl: String? = null,


){
    fun toMap() : Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["uid"] = uid
        email.let { map["email"] = it }
        username.let { map["username"] = it.trim().lowercase() }
        name?.let { map["name"] = it }
        bio?.let { map["bio"] = it }
        profileUrl?.let { map["profileUrl"] = it }
        map["createdAt"] = FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map
    }
}

data class UpdateAccountDto(
    val email: String? = null,
    val username: String? = null,
    val name: String? = null,
    val bio: String? = null,
    val profileUrl: String? =  null,
){
    fun toMap() : Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        email?.let { map["email"] = it }
        username?.let { map["username"] = it.trim().lowercase() }
        name?.let { map["name"] = it }
        bio?.let { map["bio"] = it }
        profileUrl?.let { map["profileUrl"] = it }
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map
    }
}

data class UserMetadataDto (
    val uid: String = "",
    val email: String = ""

){
    fun toMap() : Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["uid"] = uid
        map["email"] = email
        return map

    }
}