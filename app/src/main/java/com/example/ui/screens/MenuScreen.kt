package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserAccount
import com.example.ui.components.Avatar3DPreviewCard
import com.example.ui.components.DevYellowHammerBadge
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun MenuScreen(
    currentUser: UserAccount?,
    onSwitchAccount: () -> Unit,
    onClaimAllowance: () -> Unit,
    onOpenCreateRoom: () -> Unit,
    onToggleDeveloper: (() -> Unit)? = null
) {
    var spatialSensitivity by remember { mutableFloatStateOf(0.75f) }
    var micSensitivity by remember { mutableFloatStateOf(0.85f) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Minimalistic Black & Yellow Menu Brand Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_by_1790444477261),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.5.dp, AccentYellow, RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "CRYAMANIA",
                    color = AccentYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }

            OutlinedButton(
                onClick = onSwitchAccount,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("switch_account_button")
            ) {
                Text("Log Out", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive 3D Avatar & Shirt Inspector in Menu
        Avatar3DPreviewCard(
            username = currentUser?.username ?: "Guest",
            avatarColorHex = currentUser?.avatarColorHex ?: "#FFD600",
            avatarOutfit = currentUser?.avatarOutfit ?: "Black & Yellow Cyber Shirt",
            avatarHat = currentUser?.avatarHat ?: "None",
            avatarAura = currentUser?.avatarAura ?: "None",
            isDeveloper = currentUser?.isDeveloper == true,
            isCryaAdmin = currentUser?.isCryaAdmin == true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Minimalist Host Lobby Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = AccentYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PUBLIC & PRIVATE LOBBY HOSTING",
                        color = AccentYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rooms with 0 players (like an empty Iron Cafe) are invisible in the feed. Host a public lobby to make it live for everyone, or host a private code-only lobby.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenCreateRoom,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu_host_chat_button")
                ) {
                    Text(
                        text = "Host a 3D Lobby",
                        color = CyberBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Developer Hammer Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DevYellowHammerBadge(sizeDp = 20f)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEVELOPER HAMMER BADGE",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Surface(
                        color = if (currentUser?.isDeveloper == true) AccentYellow else CyberSurfaceHigh,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (currentUser?.isDeveloper == true) "ACTIVE" else "LOCKED",
                            color = if (currentUser?.isDeveloper == true) CyberBlack else TextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentUser?.isDeveloper == true)
                        "Dev tools unlocked: Give yourself CRIN, give CRIN to players in rooms, kick, ban, and mute."
                    else
                        "Only Dev accounts can give themselves CRIN or give CRIN to others in rooms.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                if (currentUser?.isDeveloper == true || currentUser?.isCryaAdmin == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onClaimAllowance,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("menu_dev_give_self_crin_button")
                    ) {
                        Text(
                            text = "Give Yourself +250 CRIN (Dev)",
                            color = CyberBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Minimalist Audio Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = AccentYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VOICE AUDIO SETTINGS",
                        color = AccentYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Spatial 3D Proximity Falloff (${(spatialSensitivity * 100).toInt()}%)",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Slider(
                    value = spatialSensitivity,
                    onValueChange = { spatialSensitivity = it },
                    colors = SliderDefaults.colors(
                        thumbColor = AccentYellow,
                        activeTrackColor = AccentYellow,
                        inactiveTrackColor = CyberBorder
                    )
                )

                HorizontalDivider(color = CyberBorder)
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Microphone Gain (${(micSensitivity * 100).toInt()}%)",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Slider(
                    value = micSensitivity,
                    onValueChange = { micSensitivity = it },
                    colors = SliderDefaults.colors(
                        thumbColor = AccentYellow,
                        activeTrackColor = AccentYellow,
                        inactiveTrackColor = CyberBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
