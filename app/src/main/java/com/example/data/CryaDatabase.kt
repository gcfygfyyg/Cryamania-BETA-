package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.MessageDao
import com.example.data.dao.RoomDao
import com.example.data.dao.ShopDao
import com.example.data.dao.UserDao
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.ShopItem
import com.example.data.model.UserAccount

@Database(
    entities = [UserAccount::class, ChatRoom::class, ChatMessage::class, ShopItem::class],
    version = 3,
    exportSchema = false
)
abstract class CryaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun messageDao(): MessageDao
    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: CryaDatabase? = null

        fun getInstance(context: Context): CryaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CryaDatabase::class.java,
                    "cryamania_metaverse.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
