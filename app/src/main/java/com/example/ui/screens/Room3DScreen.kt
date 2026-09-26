package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.MetaverseParticipant
import com.example.data.model.RoomObject3D
import com.example.data.model.UserAccount
import com.example.network.MultiplayerConnectionState
import com.example.network.MultiplayerNetworkMode
import com.example.ui.components.ChatOverlay
import com.example.ui.components.EmoteBar
import com.example.ui.components.JukeboxDialog
import com.example.ui.components.PlayerActionSheet
import com.example.ui.components.Room3DCanvas
import com.example.ui.components.VirtualJoystick
import com.example.ui.components.VoiceChatBar
import com.example.ui.theme.VoidDark

@Composable
fun Room3DScreen(
    room: ChatRoom,
    currentUser: UserAccount?,
    participants: List<MetaverseParticipant>,
    roomObjects: List<RoomObject3D>,
    messages: List<ChatMessage>,
    isMicEnabled: Boolean,
    isDeafened: Boolean,
    voiceLevel: Float,
    multiplayerStatus: MultiplayerConnectionState? = null,
    networkMode: MultiplayerNetworkMode? = null,
    onMoveAvatar: (dx: Float, dy: Float) -> Unit,
    onTapFloor: (worldX: Float, worldY: Float) -> Unit,
    onToggleMic: () -> Unit,
    onToggleDeaf: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTriggerEmote: (String) -> Unit,
    onChangeTrack: (String) -> Unit,
    onLeaveRoom: () -> Unit,
    onKickParticipant: (username: String, reason: String) -> Unit,
    onBanParticipant: (username: String, reason: String) -> Unit,
    onServerMuteParticipant: (username: String, mute: Boolean) -> Unit,
    onTeleportParticipant: (username: String) -> Unit,
    onTipCrin: (username: String, amount: Int) -> Unit,
    onGrantHammerParticipant: ((username: String, grant: Boolean) -> Unit)? = null
) {
    var selectedParticipant by remember { mutableStateOf<MetaverseParticipant?>(null) }
    var showJukeboxDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
    ) {
        // 1. 3D Isometric / Perspective Room Canvas
        Room3DCanvas(
            modifier = Modifier.fillMaxSize(),
            theme = room.theme,
            participants = participants,
            roomObjects = roomObjects,
            onTapFloor = onTapFloor,
            onSelectParticipant = { p -> selectedParticipant = p },
            onTapObject = { obj ->
                if (obj.type == "DJ_BOOTH") {
                    showJukeboxDialog = true
                }
            }
        )

        // 2. Top Voice Chat Bar HUD
        VoiceChatBar(
            roomName = room.name,
            roomCode = room.roomCode,
            voiceMode = room.voiceMode,
            currentTrack = room.currentTrack,
            isMicEnabled = isMicEnabled,
            isDeafened = isDeafened,
            voiceLevel = voiceLevel,
            onlineCount = participants.size,
            multiplayerStatus = multiplayerStatus,
            networkMode = networkMode,
            peerCount = participants.filterNot { it.isMe }.size,
            onToggleMic = onToggleMic,
            onToggleDeaf = onToggleDeaf,
            onOpenJukebox = { showJukeboxDialog = true },
            onLeaveRoom = onLeaveRoom,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Bottom Layer: Joystick on Left, Emote Bar, Chat Overlay on Right/Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Virtual Joystick & Quick Emote Bar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // On-screen Virtual Joystick
                VirtualJoystick(
                    radiusDp = 50f,
                    thumbRadiusDp = 20f,
                    onMove = onMoveAvatar
                )

                Spacer(modifier = Modifier.weight(1f))

                // Emote Bar
                EmoteBar(
                    onSelectEmote = onTriggerEmote
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Chat Overlay Input & Stream
            ChatOverlay(
                messages = messages,
                participants = participants,
                onSendMessage = onSendMessage,
                onSelectParticipant = { p -> selectedParticipant = p }
            )
        }

        // 4. Player Action Sheet (Moderation tools & player commands via 3-dots)
        if (selectedParticipant != null) {
            PlayerActionSheet(
                target = selectedParticipant!!,
                currentUser = currentUser,
                onDismiss = { selectedParticipant = null },
                onKick = onKickParticipant,
                onBan = onBanParticipant,
                onServerMute = onServerMuteParticipant,
                onTeleport = onTeleportParticipant,
                onTipCrin = onTipCrin,
                onGrantHammer = onGrantHammerParticipant
            )
        }

        // 5. Jukebox Track Dialog
        if (showJukeboxDialog) {
            JukeboxDialog(
                currentTrack = room.currentTrack,
                onSelectTrack = onChangeTrack,
                onDismiss = { showJukeboxDialog = false }
            )
        }
    }
}
