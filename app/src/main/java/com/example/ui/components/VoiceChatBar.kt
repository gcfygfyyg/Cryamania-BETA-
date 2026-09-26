package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrinGold
import com.example.ui.theme.MicRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorder
import com.example.network.MultiplayerConnectionState
import com.example.network.MultiplayerNetworkMode
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.VoidSurfaceHigh

@Composable
fun VoiceChatBar(
    roomName: String,
    roomCode: String,
    voiceMode: String,
    currentTrack: String,
    isMicEnabled: Boolean,
    isDeafened: Boolean,
    voiceLevel: Float,
    onlineCount: Int,
    multiplayerStatus: MultiplayerConnectionState? = null,
    networkMode: MultiplayerNetworkMode? = null,
    peerCount: Int = 1,
    onToggleMic: () -> Unit,
    onToggleDeaf: () -> Unit,
    onOpenJukebox: () -> Unit,
    onLeaveRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val micBgColor by animateColorAsState(
        targetValue = if (isMicEnabled) OnlineGreen else MicRed.copy(alpha = 0.8f),
        label = "mic_bg"
    )

    Surface(
        color = VoidSurface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Top Row: Room title, Voice Mode Badge, Leave Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = roomName,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // Join Code Pill
                        Surface(
                            color = VoidSurfaceHigh,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder)
                        ) {
                            Text(
                                text = roomCode,
                                color = CrinGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Voice mode badge
                        Surface(
                            color = if (voiceMode == "SPATIAL") NeonCyan.copy(alpha = 0.15f) else NeonMagenta.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (voiceMode == "SPATIAL") "🔊 Spatial Voice (3D Proximity)" else "🎙️ Group Voice (Studio)",
                                color = if (voiceMode == "SPATIAL") NeonCyan else NeonMagenta,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• $onlineCount online",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (multiplayerStatus != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dotColor = when (multiplayerStatus) {
                                MultiplayerConnectionState.CONNECTED -> OnlineGreen
                                MultiplayerConnectionState.CONNECTING -> CrinGold
                                MultiplayerConnectionState.ERROR -> MicRed
                                else -> TextSecondary
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val modeStr = if (networkMode == MultiplayerNetworkMode.LAN_DIRECT) "LAN Direct" else "Cloud P2P"
                            val statusText = when (multiplayerStatus) {
                                MultiplayerConnectionState.CONNECTED -> "Live ($modeStr) • $peerCount peer(s)"
                                MultiplayerConnectionState.CONNECTING -> "Connecting to Multiplayer..."
                                MultiplayerConnectionState.ERROR -> "Offline (Error)"
                                else -> "Multiplayer Ready"
                            }
                            Text(
                                text = statusText,
                                color = dotColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onLeaveRoom,
                    modifier = Modifier.testTag("leave_room_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Leave Chat",
                        tint = MicRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Audio Controls & Jukebox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Jukebox Track Pill (clickable)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VoidSurfaceHigh)
                        .border(1.dp, NeonMagenta.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onOpenJukebox() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("jukebox_track_pill"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = NeonMagenta,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentTrack,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Deafen Toggle
                IconButton(
                    onClick = onToggleDeaf,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDeafened) MicRed.copy(alpha = 0.2f) else VoidSurfaceHigh)
                        .border(1.dp, if (isDeafened) MicRed else VoidBorder, CircleShape)
                        .testTag("deafen_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                        contentDescription = "Deafen Audio",
                        tint = if (isDeafened) MicRed else TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Microphone Toggle with Speaking Visualizer
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(micBgColor)
                        .clickable { onToggleMic() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("mic_toggle_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Microphone",
                        tint = if (isMicEnabled) Color(0xFF00382E) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMicEnabled) "TALKING" else "MUTED",
                        color = if (isMicEnabled) Color(0xFF00382E) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isMicEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Waves",
                            tint = Color(0xFF00382E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
