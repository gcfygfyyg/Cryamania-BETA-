package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShopItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_items ORDER BY category ASC, priceCrin ASC")
    fun getAllItems(): Flow<List<ShopItem>>

    @Query("SELECT COUNT(*) FROM shop_items")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultItems(items: List<ShopItem>)

    @Update
    suspend fun updateItem(item: ShopItem)

    @Query("UPDATE shop_items SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockItem(id: String)
}
