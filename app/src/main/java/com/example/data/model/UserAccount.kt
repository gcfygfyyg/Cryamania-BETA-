package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_accounts",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String = "password",
    val isCryaAdmin: Boolean = false,
    val isDeveloper: Boolean = false,
    val crinBalance: Int = 1000,
    val avatarColorHex: String = "#FFFFFF",
    val avatarHat: String = "None",
    val avatarOutfit: String = "Classic 2009 T-Shirt",
    val avatarAura: String = "None",
    val createdTimestamp: Long = System.currentTimeMillis()
)
