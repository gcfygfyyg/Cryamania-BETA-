package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_accounts WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccount?

    @Query("SELECT * FROM user_accounts ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUser(): UserAccount?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM user_accounts ORDER BY username ASC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE user_accounts SET crinBalance = crinBalance + :amount WHERE username = :username")
    suspend fun addCrin(username: String, amount: Int)
}
