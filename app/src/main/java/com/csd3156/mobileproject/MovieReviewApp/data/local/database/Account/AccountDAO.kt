package com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

@Dao
interface AccountDAO {
    @Query("SELECT * FROM accounts")
    fun getAll() : Flow<List<Account>>
    @Query("SELECT * FROM accounts LIMIT 1")
    fun getOne() : Flow<Account?>
    @Query("SELECT * FROM accounts WHERE accounts.id = :id LIMIT 1")
    suspend fun getById(id : Long) : Account?
    @Query("SELECT * FROM accounts WHERE accounts.uid = :uid LIMIT 1")
    suspend fun getByUID(uid: String) : Account?
    @Query ("SELECT * FROM accounts WHERE accounts.id = (:ids)")
    suspend fun getAccountsByIds(ids: List<Long>) :  List<Account>
    @Query("SELECT * FROM accounts WHERE accounts.uid = :uid LIMIT 1")
    fun observeAccountByUUID(uid: String): Flow<Account?>
    @Query("SELECT * FROM accounts WHERE accounts.id = :id LIMIT 1")
    fun observeAccountById(id: String): Flow<Account?>
    @Query("SELECT * FROM accounts WHERE accounts.username = :username LIMIT 1")
    suspend fun getAccountByUsername(username: String): Account?

    // Insert
    @Insert
    suspend fun insert(account: Account) : Long
    @Insert
    suspend fun insertAll(accounts: List<Account>) : List<Long>


    //Update ALL details by ID
    @Update
    suspend fun UpdateAccount(account: Account) : Int
    @Update
    suspend fun UpdateAccounts(accounts: List<Account>) : Int
    //Update name and bio
    @Query("UPDATE accounts SET name = :name, bio = :bio, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateInfo(id: String, name: String?, bio: String?, updatedAt: Date = Date()) : Int
    @Query("UPDATE accounts SET name = :name, bio = :bio, updatedAt = :updatedAt WHERE uid = :uid")
    suspend fun updateInfoByUUID(uid: String, name: String?, bio: String?, updatedAt: Date = Date()) : Int

    // Deletion
    @Delete
    suspend fun delete(account: Account) : Int
    @Delete
    suspend fun deleteAccounts(accounts: List<Account>) : Int

    @Query("DELETE FROM accounts WHERE uid = :id")
    suspend fun deleteById(id: String) : Int

    @Query("DELETE FROM accounts WHERE uid = :uuid")
    suspend fun deleteByUID(uuid: UUID) : Int

    @Query("DELETE FROM accounts WHERE id = (:ids)")
    suspend fun  deleteByIds(ids: List<String>) : Int
    @Query("DELETE FROM accounts WHERE uid = (:uuids)")
    suspend fun  deleteByUIDs(uuids: List<UUID>) : Int

    @Query("DELETE FROM accounts")
    suspend fun deleteAll() : Int



}


