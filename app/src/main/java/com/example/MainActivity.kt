package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatFeedScreen
import com.example.ui.screens.CreateRoomDialog
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.Room3DScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidDark
import com.example.ui.theme.VoidSurface
import com.example.viewmodel.CryaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CryaManiaApp()
            }
        }
    }
}

@Composable
fun CryaManiaApp(
    viewModel: CryaViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRoom by viewModel.activeRoom.collectAsState()
    val publicRooms by viewModel.publicRooms.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val roomObjects by viewModel.roomObjects.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val isMicEnabled by viewModel.isMicEnabled.collectAsState()
    val isDeafened by viewModel.isDeafened.collectAsState()
    val voiceLevel by viewModel.myVoiceLevel.collectAsState()
    val multiplayerStatus by viewModel.multiplayerStatus.collectAsState()
    val networkMode by viewModel.networkMode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Shop, 2: Menu
    var showCreateRoomDialogFromMenu by remember { mutableStateOf(false) }

    // Audio record permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleMic()
        } else {
            Toast.makeText(context, "Microphone permission needed for Voice Chat", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { eventMsg ->
            snackbarHostState.showSnackbar(eventMsg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = VoidDark,
        bottomBar = {
            // Show bottom bar only when logged in and NOT inside a 3D room
            if (currentUser != null && currentRoom == null) {
                NavigationBar(
                    containerColor = VoidSurface,
                    contentColor = NeonCyan
                ) {
                    NavigationBarItem(
                        selected = selectedNavTab == 0,
                        onClick = { selectedNavTab = 0 },
                        icon = {
                            Icon(
                                Icons.Default.Forum,
                                contentDescription = "Chats",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Chats", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_chats_tab")
                    )

                    NavigationBarItem(
                        selected = selectedNavTab == 1,
                        onClick = { selectedNavTab = 1 },
                        icon = {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = "Shop",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Shop", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_shop_tab")
                    )

                    NavigationBarItem(
                        selected = selectedNavTab == 2,
                        onClick = { selectedNavTab = 2 },
                        icon = {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Menu", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_menu_tab")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 1. Initial State: Account Registration or Login
                currentUser == null -> {
                    AuthScreen(
                        allUsers = allUsers,
                        onRegister = { username, password, avatarColor ->
                            viewModel.registerAccount(username, password, avatarColor)
                        },
                        onLogin = { username, password ->
                            viewModel.login(username, password)
                        }
                    )
                }

                // 2. In-Room 3D Experience (Metaverse Space)
                currentRoom != null -> {
                    Room3DScreen(
                        room = currentRoom!!,
                        currentUser = currentUser,
                        participants = participants,
                        roomObjects = roomObjects,
                        messages = messages,
                        isMicEnabled = isMicEnabled,
                        isDeafened = isDeafened,
                        voiceLevel = voiceLevel,
                        multiplayerStatus = multiplayerStatus,
                        networkMode = networkMode,
                        onMoveAvatar = { dx, dy ->
                            viewModel.moveMyAvatar(dx, dy)
                        },
                        onTapFloor = { wx, wy ->
                            viewModel.tapToMove(wx, wy)
                        },
                        onToggleMic = {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                viewModel.toggleMic()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onToggleDeaf = {
                            viewModel.toggleDeaf()
                        },
                        onSendMessage = { text ->
                            viewModel.sendChatMessage(text)
                        },
                        onTriggerEmote = { emote ->
                            viewModel.triggerEmote(emote)
                        },
                        onChangeTrack = { track ->
                            viewModel.changeRoomTrack(track)
                        },
                        onLeaveRoom = {
                            viewModel.leaveRoom()
                        },
                        onKickParticipant = { username, reason ->
                            viewModel.kickParticipant(username, reason)
                        },
                        onBanParticipant = { username, reason ->
                            viewModel.banParticipant(username, reason)
                        },
                        onServerMuteParticipant = { username, mute ->
                            viewModel.serverMuteParticipant(username, mute)
                        },
                        onTeleportParticipant = { username ->
                            viewModel.teleportToMe(username)
                        },
                        onTipCrin = { username, amount ->
                            viewModel.tipCrin(username, amount)
                        },
                        onGrantHammerParticipant = { username, grant ->
                            viewModel.grantOrRevokeHammerBadge(username, grant)
                        }
                    )
                }

                // 3. Lobby Navigation: Chats, Shop, or Menu
                else -> {
                    when (selectedNavTab) {
                        0 -> {
                            ChatFeedScreen(
                                currentUser = currentUser,
                                publicRooms = publicRooms,
                                onJoinRoom = { room ->
                                    viewModel.joinRoom(room)
                                },
                                onFindAndJoin = { query ->
                                    viewModel.findAndJoinChat(query)
                                },
                                onCreateRoom = { name, desc, isPub, theme, voiceMode ->
                                    viewModel.createRoom(name, desc, isPub, theme, voiceMode)
                                }
                            )
                        }
                        1 -> {
                            ShopScreen(
                                currentUser = currentUser,
                                shopItems = shopItems,
                                onPurchaseItem = { item ->
                                    viewModel.purchaseShopItem(item)
                                },
                                onClaimAllowance = {
                                    viewModel.claimDailyAllowance()
                                },
                                onUpdateAvatarColor = { colorHex ->
                                    viewModel.updateAvatarColor(colorHex)
                                }
                            )
                        }
                        2 -> {
                            MenuScreen(
                                currentUser = currentUser,
                                onSwitchAccount = {
                                    viewModel.logout()
                                },
                                onClaimAllowance = {
                                    viewModel.claimDailyAllowance()
                                },
                                onOpenCreateRoom = {
                                    showCreateRoomDialogFromMenu = true
                                }
                            )
                        }
                    }
                }
            }

            if (showCreateRoomDialogFromMenu) {
                CreateRoomDialog(
                    onDismiss = { showCreateRoomDialogFromMenu = false },
                    onCreate = { name, desc, isPub, theme, voiceMode ->
                        viewModel.createRoom(name, desc, isPub, theme, voiceMode)
                        showCreateRoomDialogFromMenu = false
                    }
                )
            }
        }
    }
}
