package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RobloxBevelBottom
import com.example.ui.theme.RobloxBevelTop
import com.example.ui.theme.RobloxBlack
import com.example.ui.theme.RobloxButtonFace
import com.example.ui.theme.RobloxButtonPressed
import com.example.ui.theme.RobloxDarkGray
import com.example.ui.theme.RobloxLightGray
import com.example.ui.theme.RobloxPureWhite
import com.example.ui.theme.RobloxSilver

/**
 * Classic 2009 Roblox 3D Beveled Button:
 * Raised rectangle with top/left highlight border and bottom/right shadow border.
 * Inverts borders when pressed.
 */
@Composable
fun RobloxBeveledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = RobloxButtonFace,
    contentColor: Color = RobloxPureWhite,
    isPrimary: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topBorder = if (isPressed) RobloxBevelBottom else if (isPrimary) RobloxPureWhite else RobloxBevelTop
    val bottomBorder = if (isPressed) RobloxBevelTop else RobloxBevelBottom
    val bgColor = if (isPressed) RobloxButtonPressed else if (isPrimary) RobloxPureWhite else backgroundColor
    val textColor = if (isPrimary && !isPressed) RobloxBlack else contentColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bgColor)
            .drawBehind {
                val strokeW = 2.dp.toPx()
                // Top border
                drawLine(
                    color = topBorder,
                    start = Offset(0f, strokeW / 2f),
                    end = Offset(size.width, strokeW / 2f),
                    strokeWidth = strokeW
                )
                // Left border
                drawLine(
                    color = topBorder,
                    start = Offset(strokeW / 2f, 0f),
                    end = Offset(strokeW / 2f, size.height),
                    strokeWidth = strokeW
                )
                // Bottom border
                drawLine(
                    color = bottomBorder,
                    start = Offset(0f, size.height - strokeW / 2f),
                    end = Offset(size.width, size.height - strokeW / 2f),
                    strokeWidth = strokeW
                )
                // Right border
                drawLine(
                    color = bottomBorder,
                    start = Offset(size.width - strokeW / 2f, 0f),
                    end = Offset(size.width - strokeW / 2f, size.height),
                    strokeWidth = strokeW
                )
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Classic 2009 Roblox Beveled Surface/Card with retro sunken or raised border
 */
@Composable
fun RobloxBeveledCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RobloxDarkGray,
    isSunken: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val topBorder = if (isSunken) RobloxBevelBottom else RobloxBevelTop
    val bottomBorder = if (isSunken) RobloxBevelTop else RobloxBevelBottom

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(backgroundColor)
            .drawBehind {
                val strokeW = 2.dp.toPx()
                drawLine(topBorder, Offset(0f, strokeW / 2f), Offset(size.width, strokeW / 2f), strokeW)
                drawLine(topBorder, Offset(strokeW / 2f, 0f), Offset(strokeW / 2f, size.height), strokeW)
                drawLine(bottomBorder, Offset(0f, size.height - strokeW / 2f), Offset(size.width, size.height - strokeW / 2f), strokeW)
                drawLine(bottomBorder, Offset(size.width - strokeW / 2f, 0f), Offset(size.width - strokeW / 2f, size.height), strokeW)
            }
            .padding(12.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Classic 2009 Roblox Header Title Bar
 */
@Composable
fun RobloxTitleHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(RobloxDarkGray)
            .border(BorderStroke(1.5.dp, RobloxSilver), RoundedCornerShape(2.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = title,
                color = RobloxPureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = RobloxSilver,
                    fontSize = 11.sp
                )
            }
        }
    }
}
