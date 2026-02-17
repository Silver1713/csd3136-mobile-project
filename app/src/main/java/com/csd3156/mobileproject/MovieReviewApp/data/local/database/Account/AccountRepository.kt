package com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account

import at.favre.lib.crypto.bcrypt.BCrypt
import java.util.UUID
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private  val accountDao: AccountDAO
) {
    suspend fun createAccount(account: Account): Long {
        return accountDao.insert(account)
    }

    suspend fun insertAccount(account: Account) : Long {
        return accountDao.insert(account)
    }
    suspend fun getAccountByUUID(uuid: UUID) : Account? {
        return accountDao.getByUUID(uuid)
    }

    suspend fun deleteAccount(account: Account) : Int {
        return accountDao.delete(account)

    }

    suspend fun findAccountByUser(username: String) : Account? {
        return accountDao.getAccountByUsername(username)
    }

    fun verifyPassword(account: Account, plainPwd : String): Boolean {
        val pwd : String = account.hashed_password ?: return false
        val result = BCrypt.verifyer().verify(plainPwd.toCharArray(), pwd)
        return result.verified
    }
}





