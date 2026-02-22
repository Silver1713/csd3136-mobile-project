package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewStorageService @Inject constructor(
    private val cloudStorage : FirebaseStorage
) {

    private val rootPath = "images/reviews/"

    suspend fun uploadImage(
        uId: String,
        reviewId: String,
        imageDataBytes : ByteArray
    ) : RequestResult<String?> {

        return try {
            val fullPath : String = rootPath + "${uId}/${reviewId}.jpg"
            val ref = cloudStorage.reference.child(fullPath)
             ref.putBytes(imageDataBytes)
                .await()
            val downloadUrl = ref.downloadUrl.await()
            RequestResult.Success(null, downloadUrl.toString())



        } catch (error : Exception){
            RequestResult.Error(error.message, error)
        }
    }

    suspend fun uploadImage(
        uId: String,
        reviewId: String,
        imageUri : Uri
    ) : RequestResult<String?> {

        return try {
            val fullPath : String = rootPath + "${uId}/${reviewId}.jpg"
            val ref = cloudStorage.reference.child(fullPath)
            ref.putFile(imageUri)
                .await()
            val downloadUrl = ref.downloadUrl.await()
            RequestResult.Success(null, downloadUrl.toString())



        } catch (error : Exception){
            RequestResult.Error(error.message, error)
        }
    }


    suspend fun deleteImage(
        uId: String,
        reviewId: String
    ) : RequestResult<Unit>{
        return try {
            val fullPath : String = rootPath + "${uId}/${reviewId}.jpg"
            cloudStorage.reference.child(fullPath).delete().await()
            RequestResult.Success(null, Unit)
        } catch (error : Exception){
            RequestResult.Error(error.message, error)
        }
    }
}