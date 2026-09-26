package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.VoidSurfaceHigh

@Composable
fun EmoteBar(
    onSelectEmote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emotes = listOf(
        Pair("Dance", "🕺"),
        Pair("Wave", "👋"),
        Pair("Cheer", "🎉"),
        Pair("Laugh", "😂"),
        Pair("Flip", "🤸"),
        Pair("Sit", "🛋️")
    )

    Surface(
        color = VoidSurface.copy(alpha = 0.88f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            emotes.forEach { (name, emoji) ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VoidSurfaceHigh)
                        .border(1.dp, NeonMagenta.copy(alpha = 0.35f), CircleShape)
                        .clickable { onSelectEmote(name) }
                        .testTag("emote_btn_${name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}
