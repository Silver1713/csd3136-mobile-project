package com.csd3156.mobileproject.MovieReviewApp.data.repository

import android.content.Context
import com.csd3156.mobileproject.MovieReviewApp.data.local.LocalReviewRepositoryImpl
import com.csd3156.mobileproject.MovieReviewApp.data.local.MovieReviewDatabase
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.Account
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountDAO
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.AccountFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.FirebaseAuthService
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.AccountDto
import com.csd3156.mobileproject.MovieReviewApp.data.remote.dto.CreateAccountDto
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val accountFirestoreService: AccountFirestoreService,
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

            val createRequestAccount: CreateAccountDto = CreateAccountDto(
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

        } catch (error: FirebaseException) {
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

    companion object {
        @Deprecated(
            message = "Legacy constructor, use Hilt injection instead"
        )
        fun create(context: Context): AccountRepository {
            val database = MovieReviewDatabase.getInstance(context)
            return AccountRepository(
                firebaseAuthService = FirebaseAuthService(FirebaseAuth.getInstance()),
                accountFirestoreService = AccountFirestoreService(FirebaseFirestore.getInstance()),
                accountDao = database.accountDao()
            )
        }
    }


}

