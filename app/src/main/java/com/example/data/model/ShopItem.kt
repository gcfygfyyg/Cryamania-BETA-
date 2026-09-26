package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_items")
data class ShopItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String, // "COSMETIC", "DECORATION", "EFFECT", "EMOTE"
    val priceCrin: Int,
    val iconEmoji: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false
)
