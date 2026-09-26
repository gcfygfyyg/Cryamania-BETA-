package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_rooms",
    indices = [Index(value = ["roomCode"], unique = true)]
)
data class ChatRoom(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val roomCode: String, // Short ID like CRY-7392
    val theme: String = "Neon Cyber", // "Neon Cyber", "Sunset Rooftop", "Arcade Retro", "Crystal Void"
    val voiceMode: String = "SPATIAL", // "SPATIAL" or "GROUP"
    val creatorUsername: String,
    val onlineCount: Int = 1,
    val maxParticipants: Int = 50,
    val decorations: String = "DJ_BOOTH,DISCO_BALL,NEON_SIGN", // Comma-separated placed objects
    val currentTrack: String = "Metaverse Pulse",
    val createdTimestamp: Long = System.currentTimeMillis()
)
