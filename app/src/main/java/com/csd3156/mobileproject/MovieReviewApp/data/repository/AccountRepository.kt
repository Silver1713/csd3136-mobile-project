package com.csd3156.mobileproject.MovieReviewApp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.csd3156.mobileproject.MovieReviewApp.data.local.MovieReviewDatabase
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.Account
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountDAO
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.AccountFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.FirebaseAuthService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.ProfileStorageService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.AccountDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.CreateAccountDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.UpdateAccountDto
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

class AccountRepository @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val accountFirestoreService: AccountFirestoreService,
    private val profileStorageService: ProfileStorageService,
    private val accountDao: AccountDAO

) {
    //Private - Room
    private suspend fun findAccountByRoomUser(username: String): AccountDomain? {
        // Check Room
        val roomAccount = accountDao.getAccountByUsername(username)
        if (roomAccount != null) {
            return roomAccount.toDomain()
        }

        return null
    }

    //Private - Remote Service


    private suspend fun findAccountByRemoteUser(username: String): AccountDomain? {

        val remoteAccount = accountFirestoreService.getMetadataUser(username)
        if (remoteAccount != null) {
            val accountDto = accountFirestoreService.getAccount(remoteAccount.uid)
            if (accountDto != null) {
                return accountDto.toDomain()
            }
        }
        return null
    }


    //Dao
    suspend fun createAccount(account: Account): Long {
        return accountDao.insert(account)

    }

    suspend fun insertAccount(account: Account): Long {
        return accountDao.insert(account)
    }

    suspend fun getAccountByUUID(uid: String): Account? {
        return accountDao.getByUID(uid)
    }

    suspend fun deleteAccount(account: Account): Int {
        return accountDao.delete(account)

    }

    suspend fun clearRoom() {
        accountDao.deleteAll()
    }


    suspend fun logoutAccount(){
        accountDao.deleteAll()
        firebaseAuthService.signOut()
    }






    suspend fun registerAccount(email: String?, username: String, password: String, name: String? = null): RequestResult<String?> {
        try {
            // Clear Local Cache

            val remoteAccount = findAccountByRemoteUser(username)
            if (remoteAccount != null) {
                throw Exception("Username already exists")
            }
            accountDao.deleteAll()
            val fakeMail = "${username.trim().lowercase()}@moviereviewapp.default"
            val uid = firebaseAuthService.signUp(email ?: fakeMail, password) {
                accountFirestoreService
            }

            val createRequestAccount = CreateAccountDto(
                uid = uid,
                email = email ?: "${username}@moviereviewapp.default",
                username = username,
                name = name,
                bio = null,
                profileUrl = null,
            )

            when (val result = accountFirestoreService.createAccount(createRequestAccount)) {
                is RequestResult.Success -> {
                    val accountDto: AccountDto = accountFirestoreService.getAccount(uid) ?: throw Exception(
                        "Unable to create account"
                    )
                    accountDao.insert(accountDto.toDomain().toRoomEntity())
                    return RequestResult.Success(null, uid)
                }
                is RequestResult.Error -> {
                    throw Exception(result.message)
                }
            }

        } catch (error: Exception) {
            firebaseAuthService.deleteCurrentUser()
            return RequestResult.Error(error.message, error)
        }


    }

    suspend fun getAccountByUID(uid: String): AccountDomain? {
        val account = accountDao.getByUID(uid)
        if (account != null) {
            return account.toDomain()
        }
        val remoteAccount = accountFirestoreService.getAccount(uid)
        if (remoteAccount != null) {
            return remoteAccount.toDomain()
        }
        return null
    }


    suspend fun getLoginByUsername(username: String): AccountDomain? {
        val account = findAccountByRoomUser(username)
        return account
    }




    suspend fun validateLoginAccount(username: String, password: String): AccountDomain? {
        if (username.isEmpty() || password.isEmpty()) return null

        accountDao.deleteAll()
        val account = findAccountByRemoteUser(username)
        val emailAccount = account?.email
        if (emailAccount != null) {
            val req = firebaseAuthService.signIn(emailAccount, password)
            when (req){
                is RequestResult.Success -> {
                    val uid = req.data

                    if (uid != null) {
                        accountDao.insert(account.toRoomEntity())
                        return account
                    }

                }
                is RequestResult.Error -> {
                    return null
                }

            }
        }
        return null
    }

    // Get the current account from the local database
    fun getActiveAccountRoom()  : Flow<AccountDomain?> = accountDao.getOne()
        .map {
            it?.toDomain()
        }

    suspend fun refreshActiveAccountRemote(){
        val accountID: String = firebaseAuthService.GetActiveUserID() ?: return
        val accountDto = accountFirestoreService.getAccount(accountID) ?: return
        accountDao.deleteAll()
        accountDao.insert(accountDto.toDomain().toRoomEntity())

    }

    suspend fun changeAccountCredentials(fullName: String? = null, bio: String?=null, photoUrl: String?=null) : Boolean {
        val updateAccDto : UpdateAccountDto = UpdateAccountDto(
            name = fullName,
            bio = bio,
            profileUrl = photoUrl
        )
        val accountID: String = firebaseAuthService.GetActiveUserID() ?: return false
        val result = accountFirestoreService.updateAccount(accountID, updateAccDto)

        refreshActiveAccountRemote()
        return result
    }

    private fun applyExifOrientation(path: String, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        val orientation = runCatching {
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(90f)
            }
            else -> return bitmap
        }

        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrElse { bitmap }
    }

    fun localToCompressJPEG(
        path: String?,
        maxSizePx: Int = 1080,
        quality: Int = 80
    ): ByteArray? {
        val safePath = path ?: return null
        val original = BitmapFactory.decodeFile(safePath) ?: return null
        val oriented = applyExifOrientation(safePath, original)
        val resized = resizeKeepAspect(oriented, maxSizePx)

        return try {
            ByteArrayOutputStream().use { out ->
                resized.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.toByteArray()
            }
        } finally {
            if (resized !== oriented) resized.recycle()
            if (oriented !== original) oriented.recycle()
            original.recycle()
        }
    }



    private fun resizeKeepAspect(
        bitmap: Bitmap,
        maxSizePx : Int
    ) : Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxSizePx && h <= maxSizePx) {
            return bitmap
        }
        val scale = if (w >= h) maxSizePx.toFloat() / w else maxSizePx.toFloat() / h
        val scaleW = (scale * w).roundToInt().coerceAtLeast(1)
        val scaleH = (scale * h).roundToInt().coerceAtLeast(1)
        return bitmap.scale(scaleW, scaleH)
    }






}

