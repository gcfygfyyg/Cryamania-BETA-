package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MetaverseParticipant
import com.example.data.model.UserAccount
import com.example.ui.theme.AdminCrownYellow
import com.example.ui.theme.CrinGold
import com.example.ui.theme.HammerBuilderOrange
import com.example.ui.theme.MicRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.VoidSurfaceHigh
import com.example.ui.theme.VoidSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerActionSheet(
    target: MetaverseParticipant,
    currentUser: UserAccount?,
    onDismiss: () -> Unit,
    onKick: (username: String, reason: String) -> Unit,
    onBan: (username: String, reason: String) -> Unit,
    onServerMute: (username: String, mute: Boolean) -> Unit,
    onTeleport: (username: String) -> Unit,
    onTipCrin: (username: String, amount: Int) -> Unit,
    onGrantHammer: ((username: String, grant: Boolean) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isMe = target.isMe
    val canModerate = currentUser?.isCryaAdmin == true || currentUser?.isDeveloper == true

    var showKickDialog by remember { mutableStateOf(false) }
    var showBanDialog by remember { mutableStateOf(false) }
    var reasonText by remember { mutableStateOf("") }
    var volumeSlider by remember { mutableFloatStateOf(1.0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VoidSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with avatar & nameplate
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceHigh)
                            .border(2.dp, if (target.isDeveloper) HammerBuilderOrange else if (target.isCryaAdmin) AdminCrownYellow else NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (target.isCryaAdmin) "👑" else if (target.isDeveloper) "🔨" else "👤",
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = target.username,
                                color = TextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (target.isDeveloper) {
                                Spacer(modifier = Modifier.width(6.dp))
                                DevYellowHammerBadge(sizeDp = 20f)
                            }
                            if (target.isCryaAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AdminCrownYellow.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminCrownYellow)
                                ) {
                                    Text(
                                        text = "CRYA DEV",
                                        color = AdminCrownYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isMe) "You (Active Session)" else "Hat: ${target.avatarHat} • Outfit: ${target.avatarOutfit}",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = VoidBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Non-moderation actions for all players
            if (!isMe) {
                // Voice Chat Volume Slider
                Text(
                    text = "Player Voice Volume (${(volumeSlider * 100).toInt()}%)",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (volumeSlider == 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volumeSlider,
                        onValueChange = { volumeSlider = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = VoidBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // DEV SELF-GRANT CRIN (Visible only if inspecting yourself AND you are a Dev account)
            if (canModerate && isMe) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DevYellowHammerBadge(sizeDp = 18f)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEV CRIN GENERATOR",
                        color = CrinGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onTipCrin(target.username, 250)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrinGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dev_self_crin_250_button")
                    ) {
                        Icon(Icons.Default.Paid, contentDescription = "Give Self CRIN", tint = CrinGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Give Self +250 CRIN",
                            color = CrinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = {
                            onTipCrin(target.username, 1000)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrinGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dev_self_crin_1000_button")
                    ) {
                        Icon(Icons.Default.Paid, contentDescription = "Give Self CRIN", tint = CrinGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Give Self +1000 CRIN",
                            color = CrinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // MODERATION TOOLS SECTION (Visible if user has CRYA Admin or Developer Hammer badge)
            if (canModerate && !isMe) {
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = VoidBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    DevYellowHammerBadge(sizeDp = 18f)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEV MODERATION & CRIN GRANT",
                        color = HammerBuilderOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dev-Only Give CRIN to Player in Room
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onTipCrin(target.username, 100)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrinGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tip_crin_button")
                    ) {
                        Icon(Icons.Default.Paid, contentDescription = "Give CRIN", tint = CrinGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Give 100 CRIN",
                            color = CrinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = {
                            onTipCrin(target.username, 500)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrinGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("give_500_crin_button")
                    ) {
                        Icon(Icons.Default.Paid, contentDescription = "Give 500 CRIN", tint = CrinGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Give 500 CRIN",
                            color = CrinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Voice Server-Mute Toggle
                Button(
                    onClick = {
                        onServerMute(target.username, !target.isMutedByAdmin)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target.isMutedByAdmin) VoidSurfaceHigh else VoidSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (target.isMutedByAdmin) NeonCyan else MicRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("server_mute_button")
                ) {
                    Icon(
                        imageVector = if (target.isMutedByAdmin) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Mute",
                        tint = if (target.isMutedByAdmin) NeonCyan else MicRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (target.isMutedByAdmin) "Unmute Player in Voice Chat" else "Server-Mute Voice Microphone",
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Teleport to Me
                Button(
                    onClick = {
                        onTeleport(target.username)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teleport_button")
                ) {
                    Icon(Icons.Default.NearMe, contentDescription = "Teleport", tint = NeonMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Teleport ${target.username} to Me", color = TextPrimary)
                }

                // Grant / Revoke Hammer Badge (CRYA Admin Exclusive)
                if (currentUser?.isCryaAdmin == true && onGrantHammer != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onGrantHammer(target.username, !target.isDeveloper)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (target.isDeveloper) Color(0xFF332010) else VoidSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HammerBuilderOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_hammer_button")
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = "Hammer", tint = HammerBuilderOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (target.isDeveloper) "Revoke Hammer Badge from ${target.username}"
                            else "Grant Hammer Badge 🔨 to ${target.username}",
                            color = HammerBuilderOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Kick and Ban Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Kick Button
                    Button(
                        onClick = { showKickDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1A1A)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HammerBuilderOrange),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kick_player_button")
                    ) {
                        Icon(Icons.Default.PersonRemove, contentDescription = "Kick", tint = HammerBuilderOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Kick", color = TextPrimary)
                    }

                    // Ban Button
                    Button(
                        onClick = { showBanDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF660708)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MicRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ban_player_button")
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = "Ban", tint = MicRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Ban", color = Color.White)
                    }
                }
            }
        }
    }

    // Kick Confirmation Dialog
    if (showKickDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showKickDialog = false },
            containerColor = VoidSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = HammerBuilderOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kick ${target.username}?", color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        "This will immediately disconnect ${target.username} from this room.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason (optional)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HammerBuilderOrange,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onKick(target.username, reasonText)
                        showKickDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HammerBuilderOrange)
                ) {
                    Text("Confirm Kick", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showKickDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Ban Confirmation Dialog
    if (showBanDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBanDialog = false },
            containerColor = VoidSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = MicRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ban ${target.username}?", color = Color.White)
                }
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to permanently ban ${target.username}? They will not be able to rejoin.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Ban Reason", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MicRed,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBan(target.username, reasonText)
                        showBanDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MicRed)
                ) {
                    Text("Execute Ban", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showBanDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
