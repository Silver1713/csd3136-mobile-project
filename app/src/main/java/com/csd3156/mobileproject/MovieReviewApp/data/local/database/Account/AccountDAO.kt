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
    @Query("SELECT * FROM accounts WHERE accounts.id = :id LIMIT 1")
    suspend fun getById(id : Int) : Account?
    @Query("SELECT * FROM accounts WHERE accounts.uuid = :uuid LIMIT 1")
    suspend fun getByUUID(uuid: UUID) : Account?
    @Query ("SELECT * FROM accounts WHERE accounts.id = (:id)")
    suspend fun getAccountsByIds(id: Int) :  List<Account>
    @Query("SELECT * FROM accounts WHERE accounts.uuid = :uuid LIMIT 1")
    fun observeAccountByUUID(uuid: UUID): Flow<Account?>
    @Query("SELECT * FROM accounts WHERE accounts.id = :id LIMIT 1")
    fun observeAccountById(id: Int): Flow<Account?>
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
    suspend fun updateInfo(id: Int, name: String?, bio: String?, updatedAt: Date = Date()) : Int
    @Query("UPDATE accounts SET name = :name, bio = :bio, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun updateInfoByUUID(uuid: UUID, name: String?, bio: String?, updatedAt: Date = Date()) : Int

    // Deletion
    @Delete
    suspend fun delete(account: Account) : Int
    @Delete
    suspend fun deleteAccounts(accounts: List<Account>) : Int

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Int) : Int

    @Query("DELETE FROM accounts WHERE uuid = :uuid")
    suspend fun deleteByUUID(uuid: UUID) : Int

    @Query("DELETE FROM accounts WHERE id = (:ids)")
    suspend fun  deleteByIds(ids: List<Int>) : Int
    @Query("DELETE FROM accounts WHERE uuid = (:uuids)")
    suspend fun  deleteByUUIDs(uuids: List<UUID>) : Int

    @Query("DELETE FROM accounts")
    suspend fun deleteAll() : Int



}


