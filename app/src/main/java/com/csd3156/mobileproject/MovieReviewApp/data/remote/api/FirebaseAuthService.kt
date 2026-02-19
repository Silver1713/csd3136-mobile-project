package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
){
    suspend fun signIn(email: String, password: String) : RequestResult<String?> {
       return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password)
                .await()
            RequestResult.Success(null, result.user?.uid)
        } catch (e: Exception) {
            RequestResult.Error(e.message, e)
        }

    }

    suspend fun signUp(
        email: String,
        password: String,
        onSuccess: (AuthResult) -> Unit
    ): String{
        firebaseAuth.signOut()
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password)

            .await()


        return (result.user?.uid ?:
        throw Exception("Unable to create user"))

    }

    fun GetActiveUserID() : String? {
        return firebaseAuth.currentUser?.uid
    }

    fun signOut(){
        firebaseAuth.signOut()
    }


    suspend fun deleteCurrentUser(): Boolean {
        firebaseAuth.currentUser?.delete()?.await() ?: return false
        return true
    }
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }







}