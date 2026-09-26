package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MetaverseParticipant
import com.example.ui.theme.AdminCrownYellow
import com.example.ui.theme.HammerBuilderOrange
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.VoidSurfaceHigh
import com.example.ui.theme.VoidSurfaceVariant

@Composable
fun ChatOverlay(
    messages: List<ChatMessage>,
    participants: List<MetaverseParticipant>,
    onSendMessage: (String) -> Unit,
    onSelectParticipant: (MetaverseParticipant) -> Unit,
    modifier: Modifier = Modifier
) {
    var chatText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var showPlayerList by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Player list modal / expander
        AnimatedVisibility(visible = showPlayerList) {
            Surface(
                color = VoidSurface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Room Participants (${participants.size})",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap ⋮ for moderation",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(participants) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VoidSurfaceHigh.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = p.username + if (p.isMe) " (You)" else "",
                                        color = if (p.isMe) NeonCyan else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (p.isDeveloper) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        DevYellowHammerBadge(sizeDp = 16f)
                                    }
                                    if (p.isSpeaking) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "🔊", fontSize = 11.sp)
                                    }
                                }

                                // Three-Dot Button for Player Actions & Moderation!
                                IconButton(
                                    onClick = { onSelectParticipant(p) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("player_three_dot_${p.username}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Player options",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent chat stream preview (shows last 3 messages when collapsed, or full when expanded)
        val displayMessages = if (isExpanded) messages else messages.takeLast(3)

        Surface(
            color = VoidSurface.copy(alpha = if (isExpanded) 0.92f else 0.72f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = if (isExpanded) 220.dp else 95.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(displayMessages) { msg ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (msg.isSystemNotice) {
                            Text(
                                text = "⚡ ${msg.messageText}",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            val senderColor = if (msg.senderIsAdmin) AdminCrownYellow else if (msg.senderIsDev) HammerBuilderOrange else NeonCyan

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = msg.senderUsername,
                                    color = senderColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (msg.senderIsDev) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    DevYellowHammerBadge(sizeDp = 14f)
                                }
                                Text(
                                    text = ": ",
                                    color = senderColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = msg.messageText,
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Chat Input Row with toggles for Player List and Expand Chat
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player list button
            IconButton(
                onClick = { showPlayerList = !showPlayerList },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (showPlayerList) NeonCyan.copy(alpha = 0.2f) else VoidSurfaceHigh)
                    .border(1.dp, if (showPlayerList) NeonCyan else VoidBorder, CircleShape)
                    .testTag("toggle_player_list_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Players",
                    tint = if (showPlayerList) NeonCyan else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Expand Chat Log button
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isExpanded) NeonCyan.copy(alpha = 0.2f) else VoidSurfaceHigh)
                    .border(1.dp, if (isExpanded) NeonCyan else VoidBorder, CircleShape)
                    .testTag("expand_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Expand Chat",
                    tint = if (isExpanded) NeonCyan else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Input field
            OutlinedTextField(
                value = chatText,
                onValueChange = { chatText = it },
                placeholder = { Text("Say something in 3D chat...", color = TextTertiary, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (chatText.isNotBlank()) {
                            onSendMessage(chatText)
                            chatText = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = VoidBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (chatText.isNotBlank()) {
                        onSendMessage(chatText)
                        chatText = ""
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color(0xFF00382E),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
