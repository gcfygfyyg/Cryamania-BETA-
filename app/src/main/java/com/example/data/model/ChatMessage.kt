package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String,
    val roomId: String,
    val senderUsername: String,
    val senderIsDev: Boolean = false,
    val senderIsAdmin: Boolean = false,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemNotice: Boolean = false
)
