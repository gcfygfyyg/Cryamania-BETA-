package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopItem
import com.example.data.model.UserAccount
import com.example.ui.components.Avatar3DPreviewCard
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShopScreen(
    currentUser: UserAccount?,
    shopItems: List<ShopItem>,
    onPurchaseItem: (ShopItem) -> Unit,
    onClaimAllowance: () -> Unit,
    onUpdateAvatarColor: ((String) -> Unit)? = null
) {
    val categories = listOf("3D Shirts", "3D Hats", "Effects", "3D Props", "All")
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val filteredItems = when (selectedCategoryIndex) {
        0 -> shopItems.filter { it.category == "SHIRT" || it.id.startsWith("outfit_") }
        1 -> shopItems.filter { it.category == "COSMETIC" && it.id.startsWith("hat_") }
        2 -> shopItems.filter { it.category == "EFFECT" }
        3 -> shopItems.filter { it.category == "DECORATION" }
        else -> shopItems
    }

    val avatarTones = listOf(
        "#FFD600" to "Yellow",
        "#FFFFFF" to "White",
        "#D6D6D6" to "Silver",
        "#757575" to "Slate",
        "#262626" to "Obsidian"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header & CRIN Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SHOP",
                    color = AccentYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Balance: ${currentUser?.crinBalance ?: 0} CRIN",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (currentUser?.isDeveloper == true || currentUser?.isCryaAdmin == true) {
                Button(
                    onClick = onClaimAllowance,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("claim_daily_crin_button")
                ) {
                    Text(
                        text = "+250 CRIN (Dev)",
                        color = CyberBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive 3D Avatar & Shirt Rig Preview
        Avatar3DPreviewCard(
            username = currentUser?.username ?: "Player",
            avatarColorHex = currentUser?.avatarColorHex ?: "#FFD600",
            avatarOutfit = currentUser?.avatarOutfit ?: "Black & Yellow Cyber Shirt",
            avatarHat = currentUser?.avatarHat ?: "None",
            avatarAura = currentUser?.avatarAura ?: "None",
            isDeveloper = currentUser?.isDeveloper == true,
            isCryaAdmin = currentUser?.isCryaAdmin == true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3D Avatar Skin Tone Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "3D BODY TONE:",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                avatarTones.forEach { (hex, _) ->
                    val isSelected = currentUser?.avatarColorHex.equals(hex, ignoreCase = true)
                    val parsedColor = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        AccentYellow
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) AccentYellow else CyberBorder,
                                shape = CircleShape
                            )
                            .clickable { onUpdateAvatarColor?.invoke(hex) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = CyberSurface,
            contentColor = AccentYellow,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                    color = AccentYellow
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Black else FontWeight.Medium,
                            color = if (selectedCategoryIndex == index) AccentYellow else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3D Shirts & Cosmetics Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems) { item ->
                val isEquipped = when (item.category) {
                    "SHIRT" -> currentUser?.avatarOutfit == item.name
                    "COSMETIC" -> currentUser?.avatarHat == item.name || currentUser?.avatarOutfit == item.name
                    "EFFECT" -> currentUser?.avatarAura == item.name
                    else -> false
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (isEquipped) AccentYellow else CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberBlack)
                                .border(1.dp, if (isEquipped) AccentYellow else CyberBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.iconEmoji, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = item.description,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 13.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (item.isUnlocked) {
                            Button(
                                onClick = { onPurchaseItem(item) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) AccentYellow else CyberSurfaceHigh
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                            ) {
                                if (isEquipped) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Equipped", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                } else {
                                    Text("Equip 3D", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onPurchaseItem(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Default.Paid, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${item.priceCrin} CRIN",
                                    color = CyberBlack,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
