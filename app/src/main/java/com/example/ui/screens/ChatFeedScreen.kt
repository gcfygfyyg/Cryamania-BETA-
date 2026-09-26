package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatRoom
import com.example.data.model.UserAccount
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
fun ChatFeedScreen(
    currentUser: UserAccount?,
    publicRooms: List<ChatRoom>,
    onJoinRoom: (ChatRoom) -> Unit,
    onFindAndJoin: (String) -> Unit,
    onCreateRoom: (name: String, desc: String, isPublic: Boolean, theme: String, voiceMode: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Only rooms that are public AND have at least 1 active player are visible
    val activePublicRooms = remember(publicRooms) {
        publicRooms.filter { it.isPublic && it.onlineCount > 0 }
    }

    Scaffold(
        containerColor = CyberBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AccentYellow,
                contentColor = CyberBlack,
                modifier = Modifier.testTag("create_chat_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Host Public Lobby")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Host Lobby", fontWeight = FontWeight.Black)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Minimalistic Black & Yellow Top Header with App Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_by_1790444477261),
                        contentDescription = "CryaMania Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, AccentYellow, RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.username ?: "Guest",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (currentUser?.isDeveloper == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                DevYellowHammerBadge(sizeDp = 18f)
                            }
                        }
                        Text(
                            text = "CRYAMANIA • LIVE 3D",
                            color = AccentYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Minimalist Black & Yellow CRIN Balance Pill
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Paid,
                            contentDescription = "CRIN",
                            tint = AccentYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentUser?.crinBalance ?: 0} CRIN",
                            color = AccentYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Minimalist "Join by Code or Direct IP" Bar
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "JOIN LOBBY BY CODE OR IP",
                        color = AccentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Enter code (CRY-1234) or LAN IP...",
                                    color = TextTertiary,
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        onFindAndJoin(searchQuery)
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentYellow,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("find_chat_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    onFindAndJoin(searchQuery)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("find_chat_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Join", tint = CyberBlack)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Join", color = CyberBlack, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Public Lobbies Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE PUBLIC LOBBIES",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = "${activePublicRooms.size} Hosted",
                    color = AccentYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activePublicRooms.isEmpty()) {
                // Empty Lobbies Hidden Notice (Minimalistic Black & Yellow)
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CyberBlack)
                                .border(1.5.dp, AccentYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = "No Active Lobbies",
                                tint = AccentYellow,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "NO ACTIVE PUBLIC LOBBIES",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Lobbies with 0 players (such as an unhosted Iron Cafe) are hidden and cannot be seen until someone actively hosts a public lobby.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onCreateRoom(
                                        "Iron Cafe",
                                        "Live hosted 3D Iron Cafe lobby with jukebox & spatial voice.",
                                        true,
                                        "Iron Cafe",
                                        "SPATIAL"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("quick_host_iron_cafe_button")
                            ) {
                                Text(
                                    text = "Host Iron Cafe",
                                    color = CyberBlack,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { showCreateDialog = true },
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Custom Lobby",
                                    color = AccentYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activePublicRooms) { room ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentYellow.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJoinRoom(room) }
                                .testTag("room_card_${room.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = room.name,
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "HOST: ${room.creatorUsername}",
                                            color = AccentYellow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        color = CyberBlack,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentYellow)
                                    ) {
                                        Text(
                                            text = room.roomCode,
                                            color = AccentYellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = room.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            color = CyberBlack,
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (room.voiceMode == "SPATIAL") Icons.Default.VolumeUp else Icons.Default.GraphicEq,
                                                    contentDescription = null,
                                                    tint = AccentYellow,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (room.voiceMode == "SPATIAL") "Spatial 3D" else "Group Voice",
                                                    color = AccentYellow,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            color = CyberSurfaceHigh,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = room.theme,
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${room.onlineCount} active",
                                            color = AccentYellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onJoinRoom(room) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Join", color = CyberBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, isPub, theme, voiceMode ->
                onCreateRoom(name, desc, isPub, theme, voiceMode)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, isPublic: Boolean, theme: String, voiceMode: String) -> Unit
) {
    var name by remember { mutableStateOf("Iron Cafe") }
    var description by remember { mutableStateOf("Live hosted 3D hangout with spatial voice & jukebox") }
    var isPublic by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("Iron Cafe") }
    var selectedVoiceMode by remember { mutableStateOf("SPATIAL") }

    val themes = listOf("Iron Cafe", "Classic Baseplate", "Crossroads", "B&Y Studio")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = AccentYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Host a 3D Lobby", color = TextPrimary, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Lobby Name", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentYellow,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentYellow,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Lobby Visibility:", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPublic) AccentYellow.copy(alpha = 0.15f) else CyberSurfaceHigh)
                            .border(1.dp, if (isPublic) AccentYellow else CyberBorder, RoundedCornerShape(8.dp))
                            .clickable { isPublic = true }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isPublic,
                            onClick = { isPublic = true },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentYellow)
                        )
                        Column {
                            Text("Public", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Visible while active", color = TextTertiary, fontSize = 9.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isPublic) AccentYellow.copy(alpha = 0.15f) else CyberSurfaceHigh)
                            .border(1.dp, if (!isPublic) AccentYellow else CyberBorder, RoundedCornerShape(8.dp))
                            .clickable { isPublic = false }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isPublic,
                            onClick = { isPublic = false },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentYellow)
                        )
                        Column {
                            Text("Private", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Hidden (Code only)", color = TextTertiary, fontSize = 9.sp)
                        }
                    }
                }

                Text("Voice Chat Mode:", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (selectedVoiceMode == "SPATIAL") AccentYellow.copy(alpha = 0.18f) else CyberSurfaceHigh,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedVoiceMode == "SPATIAL") AccentYellow else CyberBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedVoiceMode = "SPATIAL" }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("🔊 Spatial 3D", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Proximity audio", color = TextTertiary, fontSize = 10.sp)
                        }
                    }

                    Surface(
                        color = if (selectedVoiceMode == "GROUP") AccentYellow.copy(alpha = 0.18f) else CyberSurfaceHigh,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedVoiceMode == "GROUP") AccentYellow else CyberBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedVoiceMode = "GROUP" }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("🎙️ Group Voice", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Global room audio", color = TextTertiary, fontSize = 10.sp)
                        }
                    }
                }

                Text("3D Map Preset:", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    themes.forEach { theme ->
                        val isSelected = selectedTheme == theme
                        Surface(
                            color = if (isSelected) AccentYellow else CyberSurfaceHigh,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentYellow else CyberBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedTheme = theme
                                    if (name.isBlank() || themes.contains(name)) {
                                        name = theme
                                    }
                                }
                        ) {
                            Text(
                                text = theme.substringBefore(" "),
                                color = if (isSelected) CyberBlack else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, description, isPublic, selectedTheme, selectedVoiceMode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)
            ) {
                Text("Host Lobby", color = CyberBlack, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHigh)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
