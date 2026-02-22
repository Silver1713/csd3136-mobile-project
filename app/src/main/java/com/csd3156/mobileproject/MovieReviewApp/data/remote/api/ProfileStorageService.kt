package com.csd3156.mobileproject.MovieReviewApp.data.remote.api

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileStorageService @Inject constructor(
    private val cloudStorage : FirebaseStorage
) {

    private val rootPath = "images/profiles/"

    suspend fun uploadImage(
        uId: String,
        imageDataBytes : ByteArray
    ) : RequestResult<String?> {

        return try {
            val fullPath : String = rootPath + "${uId}.jpg"
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
        imageUri : Uri
    ) : RequestResult<String?> {

        return try {
            val fullPath : String = rootPath + "${uId}.jpg"
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
    ) : RequestResult<Unit>{
        return try {
            val fullPath : String = rootPath + "${uId}.jpg"
            cloudStorage.reference.child(fullPath).delete().await()
            RequestResult.Success(null, Unit)
        } catch (error : Exception){
            RequestResult.Error(error.message, error)
        }
    }
}