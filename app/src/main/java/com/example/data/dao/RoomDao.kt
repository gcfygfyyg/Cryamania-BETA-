package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM chat_rooms WHERE isPublic = 1 AND onlineCount > 0 ORDER BY onlineCount DESC, createdTimestamp DESC")
    fun getPublicRooms(): Flow<List<ChatRoom>>

    @Query("SELECT * FROM chat_rooms WHERE id = :id LIMIT 1")
    suspend fun getRoomById(id: String): ChatRoom?

    @Query("SELECT * FROM chat_rooms WHERE roomCode = :code COLLATE NOCASE LIMIT 1")
    suspend fun getRoomByCode(code: String): ChatRoom?

    @Query("SELECT * FROM chat_rooms WHERE creatorUsername = :username AND onlineCount > 0 ORDER BY createdTimestamp DESC")
    fun getRoomsByCreator(username: String): Flow<List<ChatRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: ChatRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<ChatRoom>)

    @Update
    suspend fun updateRoom(room: ChatRoom)

    @Query("DELETE FROM chat_rooms WHERE id = :id")
    suspend fun deleteRoom(id: String)

    @Query("DELETE FROM chat_rooms WHERE onlineCount <= 0")
    suspend fun deleteEmptyRooms()

    @Query("DELETE FROM chat_rooms")
    suspend fun clearAllRooms()
}
