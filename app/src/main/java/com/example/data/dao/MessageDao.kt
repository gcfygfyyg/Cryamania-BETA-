package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC LIMIT 100")
    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId")
    suspend fun clearRoomMessages(roomId: String)
}
