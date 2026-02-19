package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.AccountDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.CreateAccountDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.UpdateAccountDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.UserMetadataDto
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private  val COLLECTION_NAME = "accounts"
    private val USERS_MAP_NAME = "users"

    private val collection get()   : CollectionReference = firestore.collection(COLLECTION_NAME)
    private val usersCollection get() : CollectionReference = firestore.collection(USERS_MAP_NAME)


    suspend fun getAccount(uid: String) : AccountDto? {
        return try {
            collection.document(uid)
                .get()
                .await()
                .toObject(AccountDto::class.java)
        } catch (e: FirebaseException){
            null
        }
    }

    suspend fun createAccount(account: CreateAccountDto) : RequestResult<Unit> {

        return try {
            val user = account.username.trim().lowercase()
           if (usersCollection.document(user).get().await().exists())
               return RequestResult.Error("Username already exists", null)

            collection.document(account.uid)
                .set(account.toMap())
                .await()

            usersCollection.document(user)
                .set(
                    UserMetadataDto(
                        uid = account.uid,
                        email = account.email
                    ).toMap()
                )
                .await()



            RequestResult.Success(null, Unit)
        } catch (e: FirebaseException){
            RequestResult.Error(e.message, e)
        }
    }

    suspend fun getMetadataUser(user: String) : UserMetadataDto? {
        return try {
            val username = user.trim().lowercase()
            usersCollection.document(username)
                .get()
                .await()
                .toObject(UserMetadataDto::class.java)
        } catch (e: FirebaseException){
            null
        }
    }


    suspend fun updateAccount(uid: String, account: UpdateAccountDto) : Boolean{
        val data = account.toMap()
        return try {
            collection.document(uid)
                .update(data)
                .await()
            true
        } catch (e: FirebaseException){
            false
        }
    }

    suspend fun deleteAccount(uid: String) : Boolean {
        return try {
            collection.document(uid)
                .delete()
                .await()
            true
        } catch (e: FirebaseException){
            false

        }

    }


}