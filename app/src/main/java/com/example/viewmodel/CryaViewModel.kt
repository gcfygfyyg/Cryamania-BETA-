package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffects
import com.example.data.CryaDatabase
import com.example.data.DefaultData
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.MetaverseParticipant
import com.example.data.model.RoomObject3D
import com.example.data.model.ShopItem
import com.example.data.model.UserAccount
import com.example.network.CryaMultiplayerManager
import com.example.network.MultiplayerConnectionState
import com.example.network.MultiplayerNetworkMode
import com.example.network.MultiplayerPacket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class CryaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CryaDatabase.getInstance(application)
    private val userDao = db.userDao()
    private val roomDao = db.roomDao()
    private val messageDao = db.messageDao()
    private val shopDao = db.shopDao()

    // Authentication & Account State
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    val allUsers: StateFlow<List<UserAccount>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Toast / Alert Events
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    // Public Rooms Feed
    val publicRooms: StateFlow<List<ChatRoom>> = roomDao.getPublicRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shop Catalog
    val shopItems: StateFlow<List<ShopItem>> = shopDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Room State
    private val _activeRoom = MutableStateFlow<ChatRoom?>(null)
    val activeRoom: StateFlow<ChatRoom?> = _activeRoom.asStateFlow()

    private val _participants = MutableStateFlow<List<MetaverseParticipant>>(emptyList())
    val participants: StateFlow<List<MetaverseParticipant>> = _participants.asStateFlow()

    private val _roomObjects = MutableStateFlow<List<RoomObject3D>>(emptyList())
    val roomObjects: StateFlow<List<RoomObject3D>> = _roomObjects.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessage>> = _activeMessages.asStateFlow()

    // Voice Chat State
    private val _isMicEnabled = MutableStateFlow(false)
    val isMicEnabled: StateFlow<Boolean> = _isMicEnabled.asStateFlow()

    private val _isDeafened = MutableStateFlow(false)
    val isDeafened: StateFlow<Boolean> = _isDeafened.asStateFlow()

    private val _myVoiceLevel = MutableStateFlow(0f)
    val myVoiceLevel: StateFlow<Float> = _myVoiceLevel.asStateFlow()

    // Real Multiplayer Engine
    val multiplayerManager = CryaMultiplayerManager(viewModelScope)
    val multiplayerStatus: StateFlow<MultiplayerConnectionState> = multiplayerManager.connectionState
    val peerCount: StateFlow<Int> = multiplayerManager.peerCount
    val networkMode: StateFlow<MultiplayerNetworkMode> = multiplayerManager.currentMode

    fun getLocalIpAddress(): String = multiplayerManager.getLocalIpAddress()

    // Active room lifecycle jobs
    private var roomSimJob: Job? = null
    private var hangoutRewardJob: Job? = null

    init {
        // Wire up real-time packet receiver & status notices
        multiplayerManager.onPacketReceived = { packet ->
            handleIncomingMultiplayerPacket(packet)
        }
        multiplayerManager.onLobbyPacketReceived = { packet ->
            handleIncomingLobbyPacket(packet)
        }
        multiplayerManager.onStatusNotice = { notice ->
            viewModelScope.launch { _events.emit(notice) }
        }

        viewModelScope.launch {
            // Clear any unhosted/empty rooms on startup so rooms with 0 players are never shown
            roomDao.clearAllRooms()

            // Start live public lobby discovery across network
            multiplayerManager.startLobbyDiscovery()

            // Seed shop items if empty
            if (shopDao.getCount() == 0) {
                shopDao.insertDefaultItems(DefaultData.defaultShopItems)
            }
            // Auto login first user if available
            val firstUser = userDao.getFirstUser()
            if (firstUser != null) {
                _currentUser.value = firstUser
            }
        }
    }

    private fun handleIncomingLobbyPacket(packet: MultiplayerPacket) {
        viewModelScope.launch {
            when (packet.type) {
                MultiplayerPacket.TYPE_LOBBY_ANNOUNCE -> {
                    val count = packet.roomOnlineCount ?: 0
                    val isPub = packet.roomIsPublic ?: true
                    if (!isPub || count <= 0) {
                        roomDao.deleteRoom(packet.roomId)
                        return@launch
                    }
                    val announcedRoom = ChatRoom(
                        id = packet.roomId,
                        name = packet.roomName ?: "Public Lobby",
                        description = packet.roomDesc ?: "Live hosted 3D lobby",
                        isPublic = true,
                        roomCode = packet.roomCode ?: "CRY-LIVE",
                        theme = packet.roomTheme ?: "Iron Cafe",
                        voiceMode = packet.roomVoiceMode ?: "SPATIAL",
                        creatorUsername = packet.sender,
                        onlineCount = count,
                        decorations = packet.roomDecorations ?: "DJ_BOOTH,DISCO_BALL,NEON_SIGN"
                    )
                    roomDao.insertRoom(announcedRoom)
                }
                MultiplayerPacket.TYPE_LOBBY_CLOSE -> {
                    roomDao.deleteRoom(packet.roomId)
                }
            }
        }
    }

    private fun handleIncomingMultiplayerPacket(packet: MultiplayerPacket) {
        val me = _currentUser.value ?: return
        if (packet.sender.equals(me.username, ignoreCase = true)) return
        val currentRoom = _activeRoom.value ?: return
        if (packet.roomId != currentRoom.id) return

        when (packet.type) {
            MultiplayerPacket.TYPE_JOIN, MultiplayerPacket.TYPE_PRESENCE -> {
                val list = _participants.value.toMutableList()
                val existingIdx = list.indexOfFirst { it.username.equals(packet.sender, ignoreCase = true) }
                val participant = MetaverseParticipant(
                    id = "peer_" + packet.sender,
                    username = packet.sender,
                    isMe = false,
                    isDeveloper = packet.isDeveloper ?: false,
                    isCryaAdmin = packet.isCryaAdmin ?: false,
                    x = packet.x ?: 0f,
                    y = packet.y ?: 3f,
                    facingAngle = packet.facingAngle ?: 0f,
                    isWalking = packet.isWalking ?: false,
                    avatarColorHex = packet.avatarColorHex ?: "#00F5D4",
                    avatarHat = packet.avatarHat ?: "None",
                    avatarOutfit = packet.avatarOutfit ?: "Default Avatar",
                    avatarAura = packet.avatarAura ?: "None"
                )

                if (existingIdx >= 0) {
                    list[existingIdx] = participant
                } else {
                    list.add(participant)
                    // If this was a new JOIN, respond with our own presence so the newcomer knows about us
                    if (packet.type == MultiplayerPacket.TYPE_JOIN) {
                        val myPart = list.firstOrNull { it.isMe }
                        if (myPart != null) {
                            multiplayerManager.sendPacket(
                                MultiplayerPacket(
                                    type = MultiplayerPacket.TYPE_PRESENCE,
                                    roomId = currentRoom.id,
                                    sender = me.username,
                                    x = myPart.x,
                                    y = myPart.y,
                                    facingAngle = myPart.facingAngle,
                                    isWalking = myPart.isWalking,
                                    avatarColorHex = me.avatarColorHex,
                                    avatarHat = me.avatarHat,
                                    avatarOutfit = me.avatarOutfit,
                                    avatarAura = me.avatarAura,
                                    isDeveloper = me.isDeveloper,
                                    isCryaAdmin = me.isCryaAdmin
                                )
                            )
                        }
                    }
                    viewModelScope.launch {
                        _events.emit("👤 ${packet.sender} connected to the chat room!")
                    }
                }
                _participants.value = list
            }

            MultiplayerPacket.TYPE_MOVE -> {
                _participants.value = _participants.value.map {
                    if (it.username.equals(packet.sender, ignoreCase = true)) {
                        it.copy(
                            x = packet.x ?: it.x,
                            y = packet.y ?: it.y,
                            facingAngle = packet.facingAngle ?: it.facingAngle,
                            isWalking = packet.isWalking ?: false
                        )
                    } else it
                }
            }

            MultiplayerPacket.TYPE_CHAT -> {
                val text = packet.chatText ?: return
                SoundEffects.playChatPop()
                val msg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    roomId = currentRoom.id,
                    senderUsername = packet.sender,
                    senderIsDev = packet.isDeveloper ?: false,
                    senderIsAdmin = packet.isCryaAdmin ?: false,
                    messageText = text
                )
                viewModelScope.launch {
                    messageDao.insertMessage(msg)
                }
                _participants.value = _participants.value.map {
                    if (it.username.equals(packet.sender, ignoreCase = true)) {
                        it.copy(
                            recentSpeech = text,
                            speechExpiry = System.currentTimeMillis() + 5000L
                        )
                    } else it
                }
            }

            MultiplayerPacket.TYPE_EMOTE -> {
                val emote = packet.emote ?: return
                SoundEffects.playEmoteSound()
                _participants.value = _participants.value.map {
                    if (it.username.equals(packet.sender, ignoreCase = true)) {
                        it.copy(currentEmote = emote)
                    } else it
                }
                viewModelScope.launch {
                    delay(4000L)
                    _participants.value = _participants.value.map {
                        if (it.username.equals(packet.sender, ignoreCase = true) && it.currentEmote == emote) {
                            it.copy(currentEmote = null)
                        } else it
                    }
                }
            }

            MultiplayerPacket.TYPE_VOICE -> {
                val speaking = packet.isSpeaking == true
                val level = packet.voiceLevel ?: if (speaking) 0.7f else 0f
                _participants.value = _participants.value.map {
                    if (it.username.equals(packet.sender, ignoreCase = true)) {
                        it.copy(isSpeaking = speaking, voiceLevel = level)
                    } else it
                }
            }

            MultiplayerPacket.TYPE_MOD_ACTION -> {
                val action = packet.modAction ?: return
                val target = packet.targetUser ?: return
                SoundEffects.playHammerStrike()

                when (action) {
                    "GRANT_HAMMER" -> {
                        if (target.equals(me.username, ignoreCase = true)) {
                            val updatedMe = me.copy(isDeveloper = true)
                            _currentUser.value = updatedMe
                            viewModelScope.launch {
                                userDao.updateUser(updatedMe)
                                _events.emit("🔨 CRYA has granted you the Developer Hammer Badge!")
                            }
                        }
                        _participants.value = _participants.value.map {
                            if (it.username.equals(target, ignoreCase = true)) it.copy(isDeveloper = true) else it
                        }
                    }

                    "REVOKE_HAMMER" -> {
                        if (target.equals(me.username, ignoreCase = true)) {
                            val updatedMe = me.copy(isDeveloper = false)
                            _currentUser.value = updatedMe
                            viewModelScope.launch {
                                userDao.updateUser(updatedMe)
                                _events.emit("🔨 Your Developer Hammer badge was revoked by CRYA.")
                            }
                        }
                        _participants.value = _participants.value.map {
                            if (it.username.equals(target, ignoreCase = true)) it.copy(isDeveloper = false) else it
                        }
                    }

                    "KICK" -> {
                        if (target.equals(me.username, ignoreCase = true)) {
                            leaveRoom()
                            viewModelScope.launch {
                                _events.emit("🔨 You were KICKED by ${packet.sender}! Reason: ${packet.reason ?: "Moderation action"}")
                            }
                        } else {
                            _participants.value = _participants.value.filterNot { it.username.equals(target, ignoreCase = true) }
                        }
                    }

                    "BAN" -> {
                        if (target.equals(me.username, ignoreCase = true)) {
                            leaveRoom()
                            viewModelScope.launch {
                                _events.emit("⚖️ You were PERMANENTLY BANNED by ${packet.sender}! Reason: ${packet.reason ?: "Violation"}")
                            }
                        } else {
                            _participants.value = _participants.value.filterNot { it.username.equals(target, ignoreCase = true) }
                        }
                    }

                    "SERVER_MUTE" -> {
                        if (target.equals(me.username, ignoreCase = true)) {
                            _isMicEnabled.value = false
                            viewModelScope.launch {
                                _events.emit("🎙️ You were voice-muted by ${packet.sender}.")
                            }
                        }
                        _participants.value = _participants.value.map {
                            if (it.username.equals(target, ignoreCase = true)) {
                                it.copy(isMutedByAdmin = true, isSpeaking = false, voiceLevel = 0f)
                            } else it
                        }
                    }

                    "TELEPORT" -> {
                        if (target.equals(me.username, ignoreCase = true)) {
                            val destX = packet.x ?: 0f
                            val destY = packet.y ?: 0f
                            _participants.value = _participants.value.map {
                                if (it.isMe) it.copy(x = destX, y = destY) else it
                            }
                            viewModelScope.launch {
                                _events.emit("✨ You were summoned by ${packet.sender}!")
                            }
                        }
                    }
                }
            }

            MultiplayerPacket.TYPE_TIP -> {
                if (packet.targetUser.equals(me.username, ignoreCase = true)) {
                    val amt = packet.crinAmount ?: 50
                    viewModelScope.launch {
                        userDao.addCrin(me.username, amt)
                        val updated = userDao.getUserByUsername(me.username)
                        if (updated != null) _currentUser.value = updated
                        SoundEffects.playCoinJingle()
                        _events.emit("🪙 ${packet.sender} tipped you $amt CRIN!")
                    }
                }
            }

            MultiplayerPacket.TYPE_TRACK -> {
                val track = packet.trackName ?: return
                _activeRoom.value = currentRoom.copy(currentTrack = track)
            }

            MultiplayerPacket.TYPE_LEAVE -> {
                _participants.value = _participants.value.filterNot { it.username.equals(packet.sender, ignoreCase = true) }
                viewModelScope.launch {
                    _events.emit("${packet.sender} left the chat room.")
                }
            }
        }
    }

    // --- Authentication & User Creation ---
    fun registerAccount(
        username: String,
        password: String,
        avatarColor: String = "#FFFFFF"
    ) {
        val trimmed = username.trim()
        if (trimmed.length < 3) {
            viewModelScope.launch { _events.emit("Username must be at least 3 characters.") }
            return
        }
        if (password.length < 4) {
            viewModelScope.launch { _events.emit("Password must be at least 4 characters.") }
            return
        }

        viewModelScope.launch {
            val existing = userDao.getUserByUsername(trimmed)
            if (existing != null) {
                _events.emit("Username '$trimmed' is already taken! Please choose another.")
                return@launch
            }

            val totalUsers = userDao.getUserCount()
            // The account named "CRYA" (or very first account) is the ONLY one that receives the Hammer badge and Admin powers!
            val isCrya = trimmed.equals("CRYA", ignoreCase = true) || totalUsers == 0

            val newUser = UserAccount(
                username = trimmed,
                password = password,
                isCryaAdmin = isCrya,
                isDeveloper = isCrya, // The account named CRYA is the ONLY one that gets the Hammer badge, till it gives it out to more people
                crinBalance = if (isCrya) 5000 else 1000,
                avatarColorHex = avatarColor,
                avatarHat = if (isCrya) "Dominos Crown" else "None",
                avatarOutfit = if (isCrya) "VIP Black Tie" else "Classic 2009 T-Shirt",
                avatarAura = if (isCrya) "Silver Sparkles" else "None"
            )

            val id = userDao.insertUser(newUser)
            _currentUser.value = newUser.copy(id = id)
            SoundEffects.playCoinJingle()
            _events.emit("Welcome to 2009 CryaMania, ${newUser.username}! Password set securely.")
        }
    }

    fun login(username: String, password: String) {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) {
            viewModelScope.launch { _events.emit("Please enter your username.") }
            return
        }
        if (password.isEmpty()) {
            viewModelScope.launch { _events.emit("Please enter your password.") }
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserByUsername(trimmed)
            if (user == null) {
                _events.emit("Account '$trimmed' not found. Please create an account.")
                return@launch
            }

            // Verify password
            if (user.password.isNotEmpty() && user.password != password) {
                _events.emit("❌ Incorrect password for '$trimmed'. Please try again.")
                return@launch
            }

            _currentUser.value = user
            _events.emit("Logged in as ${user.username}")
        }
    }

    fun switchAccount(user: UserAccount) {
        _currentUser.value = user
        viewModelScope.launch { _events.emit("Switched to account ${user.username}") }
    }

    fun logout() {
        leaveRoom()
        _currentUser.value = null
        viewModelScope.launch { _events.emit("Logged out.") }
    }

    fun grantOrRevokeHammerBadge(targetUsername: String, grant: Boolean) {
        val me = _currentUser.value ?: return
        if (!me.isCryaAdmin) {
            viewModelScope.launch {
                _events.emit("Only the account named CRYA can grant the Hammer badge!")
            }
            return
        }

        viewModelScope.launch {
            val targetUser = userDao.getUserByUsername(targetUsername)
            if (targetUser != null) {
                val updated = targetUser.copy(isDeveloper = grant)
                userDao.updateUser(updated)
            }
            // Update active room participants
            _participants.value = _participants.value.map {
                if (it.username.equals(targetUsername, ignoreCase = true)) {
                    it.copy(isDeveloper = grant)
                } else it
            }

            SoundEffects.playHammerStrike()
            val room = _activeRoom.value
            if (room != null) {
                multiplayerManager.sendPacket(
                    MultiplayerPacket(
                        type = MultiplayerPacket.TYPE_MOD_ACTION,
                        roomId = room.id,
                        sender = me.username,
                        modAction = if (grant) "GRANT_HAMMER" else "REVOKE_HAMMER",
                        targetUser = targetUsername
                    )
                )
                val notice = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    roomId = room.id,
                    senderUsername = "CRYA",
                    senderIsDev = true,
                    senderIsAdmin = true,
                    messageText = if (grant) "🔨 CRYA granted the Developer Hammer badge to $targetUsername!"
                    else "🔨 CRYA revoked the Developer Hammer badge from $targetUsername.",
                    isSystemNotice = true
                )
                messageDao.insertMessage(notice)
            }
            _events.emit(if (grant) "Granted Hammer badge to $targetUsername!" else "Revoked Hammer badge from $targetUsername.")
        }
    }

    fun claimDailyAllowance() {
        val user = _currentUser.value ?: return
        if (!user.isDeveloper && !user.isCryaAdmin) {
            viewModelScope.launch {
                _events.emit("Access Denied: Only Dev accounts can give themselves CRIN!")
            }
            return
        }
        viewModelScope.launch {
            userDao.addCrin(user.username, 250)
            val updated = userDao.getUserByUsername(user.username)
            if (updated != null) _currentUser.value = updated
            SoundEffects.playCoinJingle()
            _events.emit("Dev Grant: Added +250 CRIN to your account!")
        }
    }

    // --- Chat Rooms ---
    fun createRoom(
        name: String,
        description: String,
        isPublic: Boolean,
        theme: String,
        voiceMode: String
    ) {
        val user = _currentUser.value ?: return
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            viewModelScope.launch { _events.emit("Please enter a room name.") }
            return
        }

        val randomCode = "CRY-" + Random.nextInt(1000, 9999)
        val roomId = "room_" + UUID.randomUUID().toString().take(8)

        val newRoom = ChatRoom(
            id = roomId,
            name = trimmedName,
            description = description.ifBlank { "Custom 3D chat room by ${user.username}" },
            isPublic = isPublic,
            roomCode = randomCode,
            theme = theme,
            voiceMode = voiceMode,
            creatorUsername = user.username,
            onlineCount = 1,
            decorations = "DJ_BOOTH,DISCO_BALL,NEON_SIGN"
        )

        viewModelScope.launch {
            roomDao.insertRoom(newRoom)
            joinRoom(newRoom)
            _events.emit(if (isPublic) "Public Chat created!" else "Private Chat created! Join Code: $randomCode")
        }
    }

    fun findAndJoinChat(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            // Check for direct LAN Host IP connect (e.g. 192.168.1.15:9050 or 10.0.2.15 or localhost)
            if (trimmed.contains(":") || trimmed.startsWith("192.") || trimmed.startsWith("10.") || trimmed.startsWith("127.") || trimmed.equals("localhost", ignoreCase = true)) {
                val hostIp = if (trimmed.contains(":")) trimmed.substringBefore(":") else trimmed
                val roomName = "LAN Direct: $hostIp"
                val lanRoom = ChatRoom(
                    id = "lan_" + hostIp.replace(".", "_"),
                    name = roomName,
                    description = "Direct LAN connection to host at $trimmed",
                    isPublic = false,
                    roomCode = "LAN-HOST",
                    theme = "NEON_CYBER",
                    voiceMode = "SPATIAL",
                    creatorUsername = "LAN_HOST",
                    onlineCount = 1,
                    decorations = "DJ_BOOTH,DISCO_BALL,NEON_SIGN"
                )
                joinRoom(lanRoom, lanHostIp = hostIp)
                _events.emit("Connecting directly to LAN host: $trimmed...")
                return@launch
            }

            val upperQuery = trimmed.uppercase()
            val byCode = roomDao.getRoomByCode(upperQuery)
            if (byCode != null) {
                joinRoom(byCode)
                _events.emit("Joined chat '${byCode.name}' via code!")
                return@launch
            }

            // Fallback search by ID or name
            val allPublic = publicRooms.value
            val match = allPublic.firstOrNull {
                it.roomCode.equals(upperQuery, ignoreCase = true) ||
                it.name.contains(trimmed, ignoreCase = true) ||
                it.id.equals(trimmed, ignoreCase = true)
            }

            if (match != null) {
                joinRoom(match)
                _events.emit("Joined chat '${match.name}'!")
            } else {
                _events.emit("No chat found with ID or code '$query'")
            }
        }
    }

    fun joinRoom(room: ChatRoom, lanHostIp: String? = null) {
        val me = _currentUser.value ?: return
        _activeRoom.value = room
        _isMicEnabled.value = false
        _isDeafened.value = false

        // Parse room objects from decorations
        _roomObjects.value = buildRoomObjects(room.decorations, room.theme)

        // Real multiplayer: start with local player only!
        val meParticipant = MetaverseParticipant(
            id = "me_" + me.username,
            username = me.username,
            isMe = true,
            isDeveloper = me.isDeveloper,
            isCryaAdmin = me.isCryaAdmin,
            x = 0f,
            y = 3f,
            avatarColorHex = me.avatarColorHex,
            avatarHat = me.avatarHat,
            avatarOutfit = me.avatarOutfit,
            avatarAura = me.avatarAura
        )
        _participants.value = listOf(meParticipant)

        // Connect real multiplayer network
        multiplayerManager.joinRoom(
            roomId = room.id,
            username = me.username,
            isHost = room.creatorUsername == me.username,
            lanHostIp = lanHostIp
        )

        // Broadcast our join packet to real network peers
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_JOIN,
                roomId = room.id,
                sender = me.username,
                x = 0f,
                y = 3f,
                facingAngle = 0f,
                isWalking = false,
                avatarColorHex = me.avatarColorHex,
                avatarHat = me.avatarHat,
                avatarOutfit = me.avatarOutfit,
                avatarAura = me.avatarAura,
                isDeveloper = me.isDeveloper,
                isCryaAdmin = me.isCryaAdmin
            )
        )

        // If public lobby, announce it to live lobby discovery
        if (room.isPublic) {
            announceActiveLobby(room, 1)
        }

        startRoomSimulation(room)
        startHangoutRewardLoop()

        // Post welcome notice to chat
        viewModelScope.launch {
            roomDao.insertRoom(room.copy(onlineCount = 1))
            val notice = ChatMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderUsername = "CRYA SYSTEM",
                senderIsDev = true,
                senderIsAdmin = true,
                messageText = "Welcome to ${room.name}! Live Multiplayer active. Voice mode: ${room.voiceMode}.",
                isSystemNotice = true
            )
            messageDao.insertMessage(notice)
            loadRoomMessages(room.id)
        }
    }

    private fun announceActiveLobby(room: ChatRoom, count: Int) {
        if (!room.isPublic || count <= 0) return
        multiplayerManager.broadcastLobbyPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_LOBBY_ANNOUNCE,
                roomId = room.id,
                sender = room.creatorUsername,
                roomName = room.name,
                roomDesc = room.description,
                roomCode = room.roomCode,
                roomTheme = room.theme,
                roomVoiceMode = room.voiceMode,
                roomOnlineCount = count,
                roomDecorations = room.decorations,
                roomIsPublic = true
            )
        )
    }

    fun leaveRoom() {
        val me = _currentUser.value
        val room = _activeRoom.value
        val remainingPeers = _participants.value.filterNot { it.isMe }.size
        if (me != null && room != null) {
            multiplayerManager.sendPacket(
                MultiplayerPacket(
                    type = MultiplayerPacket.TYPE_LEAVE,
                    roomId = room.id,
                    sender = me.username
                )
            )
            // If no one else is in the room (or host closes it), remove it so empty lobbies cannot be seen
            if (remainingPeers <= 0 || room.creatorUsername.equals(me.username, ignoreCase = true)) {
                multiplayerManager.broadcastLobbyPacket(
                    MultiplayerPacket(
                        type = MultiplayerPacket.TYPE_LOBBY_CLOSE,
                        roomId = room.id,
                        sender = me.username
                    )
                )
                viewModelScope.launch {
                    roomDao.deleteRoom(room.id)
                }
            } else {
                viewModelScope.launch {
                    roomDao.updateRoom(room.copy(onlineCount = remainingPeers))
                }
            }
        }
        multiplayerManager.disconnectRoomOnly()
        roomSimJob?.cancel()
        hangoutRewardJob?.cancel()
        _activeRoom.value = null
        _participants.value = emptyList()
        _activeMessages.value = emptyList()
        _isMicEnabled.value = false
    }

    private fun loadRoomMessages(roomId: String) {
        viewModelScope.launch {
            messageDao.getMessagesForRoom(roomId).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    private fun buildRoomObjects(decorationsCsv: String, theme: String): List<RoomObject3D> {
        val objects = mutableListOf<RoomObject3D>()
        val items = decorationsCsv.split(",").map { it.trim() }

        // Always put DJ booth in center north
        if (items.contains("DJ_BOOTH")) {
            objects.add(RoomObject3D("dj_1", "DJ_BOOTH", 0f, -5f, "Jukebox Deck", "🎛️"))
        }
        if (items.contains("DISCO_BALL")) {
            objects.add(RoomObject3D("disco_1", "DISCO_BALL", 0f, 0f, "Mirror Disco Sphere", "🪩"))
        }
        if (items.contains("NEON_SIGN")) {
            objects.add(RoomObject3D("sign_1", "NEON_SIGN", 0f, -7.5f, "CryaMania Neon", "💡"))
        }
        if (items.contains("LOUNGE_SOFA")) {
            objects.add(RoomObject3D("sofa_1", "LOUNGE_SOFA", -5f, 3f, "VIP Lounge", "🛋️"))
            objects.add(RoomObject3D("sofa_2", "LOUNGE_SOFA", 5f, 3f, "VIP Lounge", "🛋️"))
        }
        if (items.contains("ARCADE_CABINET")) {
            objects.add(RoomObject3D("arcade_1", "ARCADE_CABINET", -6f, -3f, "Retro Arcade", "🕹️"))
        }
        if (items.contains("FOUNTAIN")) {
            objects.add(RoomObject3D("fountain_1", "FOUNTAIN", 0f, 1f, "Crya Fountain", "⛲"))
        }
        if (items.contains("CRYSTAL_SPIRE")) {
            objects.add(RoomObject3D("crystal_1", "CRYSTAL_SPIRE", -4f, -4f, "Cyan Void Crystal", "🔮"))
        }

        return objects
    }

    // --- Avatar Movement & Interaction ---
    fun moveMyAvatar(dx: Float, dy: Float) {
        val currentList = _participants.value.toMutableList()
        val myIdx = currentList.indexOfFirst { it.isMe }
        if (myIdx >= 0) {
            val me = currentList[myIdx]
            val speed = 0.28f
            val newX = (me.x + dx * speed).coerceIn(-8f, 8f)
            val newY = (me.y + dy * speed).coerceIn(-8f, 8f)
            val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            val isWalking = hypot(dx, dy) > 0.05f

            currentList[myIdx] = me.copy(
                x = newX,
                y = newY,
                facingAngle = if (isWalking) angle else me.facingAngle,
                isWalking = isWalking
            )
            _participants.value = currentList

            // Broadcast real-time movement to peers
            val room = _activeRoom.value
            if (room != null) {
                multiplayerManager.sendMovement(
                    roomId = room.id,
                    x = newX,
                    y = newY,
                    facingAngle = angle,
                    isWalking = isWalking
                )
            }
        }
    }

    fun tapToMove(targetWorldX: Float, targetWorldY: Float) {
        val currentList = _participants.value.toMutableList()
        val myIdx = currentList.indexOfFirst { it.isMe }
        if (myIdx >= 0) {
            val me = currentList[myIdx]
            val clampedX = targetWorldX.coerceIn(-8f, 8f)
            val clampedY = targetWorldY.coerceIn(-8f, 8f)
            val dx = clampedX - me.x
            val dy = clampedY - me.y
            val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

            currentList[myIdx] = me.copy(
                x = clampedX,
                y = clampedY,
                facingAngle = angle,
                isWalking = false
            )
            _participants.value = currentList

            // Broadcast immediate tap-move to peers
            val room = _activeRoom.value
            if (room != null) {
                multiplayerManager.sendMovement(
                    roomId = room.id,
                    x = clampedX,
                    y = clampedY,
                    facingAngle = angle,
                    isWalking = false,
                    force = true
                )
            }
        }
    }

    // --- Voice Chat & Audio System ---
    fun toggleMic() {
        val current = _isMicEnabled.value
        val me = _participants.value.firstOrNull { it.isMe }
        if (me?.isMutedByAdmin == true) {
            viewModelScope.launch { _events.emit("You were muted by an Admin/Developer!") }
            return
        }

        _isMicEnabled.value = !current
        SoundEffects.playMicToggle(!current)
        viewModelScope.launch {
            _events.emit(if (!current) "Microphone ON (Broadcasting Voice)" else "Microphone Muted")
        }

        // Update local participant voice state
        val updated = _participants.value.map {
            if (it.isMe) it.copy(isSpeaking = !current, voiceLevel = if (!current) 0.8f else 0f) else it
        }
        _participants.value = updated

        // Broadcast voice activity packet to peers
        val room = _activeRoom.value
        val user = _currentUser.value
        if (room != null && user != null) {
            multiplayerManager.sendPacket(
                MultiplayerPacket(
                    type = MultiplayerPacket.TYPE_VOICE,
                    roomId = room.id,
                    sender = user.username,
                    isSpeaking = !current,
                    voiceLevel = if (!current) 0.8f else 0f
                )
            )
        }
    }

    fun toggleDeaf() {
        val current = _isDeafened.value
        _isDeafened.value = !current
        viewModelScope.launch {
            _events.emit(if (!current) "Deafened (Incoming voice muted)" else "Undeafened (Incoming voice active)")
        }
    }

    // --- Chat & Emotes ---
    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val me = _currentUser.value ?: return
        val room = _activeRoom.value ?: return

        SoundEffects.playChatPop()

        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = room.id,
            senderUsername = me.username,
            senderIsDev = me.isDeveloper,
            senderIsAdmin = me.isCryaAdmin,
            messageText = trimmed
        )

        viewModelScope.launch {
            messageDao.insertMessage(msg)
        }

        // Display speech bubble over player avatar
        val updated = _participants.value.map {
            if (it.isMe) {
                it.copy(
                    recentSpeech = trimmed,
                    speechExpiry = System.currentTimeMillis() + 5000L
                )
            } else it
        }
        _participants.value = updated

        // Broadcast chat to real network peers
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_CHAT,
                roomId = room.id,
                sender = me.username,
                chatText = trimmed,
                isDeveloper = me.isDeveloper,
                isCryaAdmin = me.isCryaAdmin
            )
        )
    }

    fun triggerEmote(emote: String) {
        SoundEffects.playEmoteSound()
        val updated = _participants.value.map {
            if (it.isMe) it.copy(currentEmote = emote) else it
        }
        _participants.value = updated

        // Send emote to chat log as notice
        val me = _currentUser.value ?: return
        val room = _activeRoom.value ?: return
        val emoji = when (emote.lowercase()) {
            "dance" -> "🕺"
            "wave" -> "👋"
            "cheer" -> "🎉"
            "laugh" -> "😂"
            "backflip" -> "🤸"
            "sit" -> "🛋️"
            else -> "✨"
        }

        // Broadcast emote to peers
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_EMOTE,
                roomId = room.id,
                sender = me.username,
                emote = emote
            )
        )

        viewModelScope.launch {
            val notice = ChatMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderUsername = me.username,
                senderIsDev = me.isDeveloper,
                senderIsAdmin = me.isCryaAdmin,
                messageText = "$emoji performs $emote!",
                isSystemNotice = true
            )
            messageDao.insertMessage(notice)

            delay(4000)
            // Reset emote
            _participants.value = _participants.value.map {
                if (it.isMe && it.currentEmote == emote) it.copy(currentEmote = null) else it
            }
        }
    }

    // --- Jukebox & Music ---
    fun changeRoomTrack(trackName: String) {
        val room = _activeRoom.value ?: return
        val me = _currentUser.value
        val updatedRoom = room.copy(currentTrack = trackName)
        _activeRoom.value = updatedRoom
        SoundEffects.playEmoteSound()

        // Broadcast music track update to peers
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_TRACK,
                roomId = room.id,
                sender = me?.username ?: "JUKEBOX",
                trackName = trackName
            )
        )

        viewModelScope.launch {
            roomDao.updateRoom(updatedRoom)
            val msg = ChatMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderUsername = "JUKEBOX",
                senderIsDev = true,
                senderIsAdmin = false,
                messageText = "🎵 Now playing: $trackName",
                isSystemNotice = true
            )
            messageDao.insertMessage(msg)
            _events.emit("Switched room track to: $trackName")
        }
    }

    // --- Moderation Commands (Hammer Badge & CRYA Admin Powers) ---
    fun kickParticipant(targetUsername: String, reason: String) {
        val me = _currentUser.value ?: return
        val canModerate = me.isCryaAdmin || me.isDeveloper
        if (!canModerate) {
            viewModelScope.launch { _events.emit("Access Denied: Requires CRYA or Hammer Developer badge.") }
            return
        }

        SoundEffects.playHammerStrike()
        _participants.value = _participants.value.filterNot { it.username.equals(targetUsername, ignoreCase = true) }

        val room = _activeRoom.value ?: return
        // Broadcast kick mod action to network
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_MOD_ACTION,
                roomId = room.id,
                sender = me.username,
                modAction = "KICK",
                targetUser = targetUsername,
                reason = reason
            )
        )

        viewModelScope.launch {
            val notice = ChatMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderUsername = "MODERATION",
                senderIsDev = true,
                senderIsAdmin = true,
                messageText = "🔨 $targetUsername was KICKED by ${me.username}. Reason: ${reason.ifBlank { "Violation of CryaMania guidelines" }}",
                isSystemNotice = true
            )
            messageDao.insertMessage(notice)
            _events.emit("🔨 Kicked $targetUsername from chat room.")
        }
    }

    fun banParticipant(targetUsername: String, reason: String) {
        val me = _currentUser.value ?: return
        val canModerate = me.isCryaAdmin || me.isDeveloper
        if (!canModerate) {
            viewModelScope.launch { _events.emit("Access Denied: Requires CRYA or Hammer Developer badge.") }
            return
        }

        SoundEffects.playHammerStrike()
        _participants.value = _participants.value.filterNot { it.username.equals(targetUsername, ignoreCase = true) }

        val room = _activeRoom.value ?: return
        // Broadcast ban mod action to network
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_MOD_ACTION,
                roomId = room.id,
                sender = me.username,
                modAction = "BAN",
                targetUser = targetUsername,
                reason = reason
            )
        )

        viewModelScope.launch {
            val notice = ChatMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderUsername = "BAN HAMMER",
                senderIsDev = true,
                senderIsAdmin = true,
                messageText = "⚖️ $targetUsername was PERMANENTLY BANNED by ${me.username}. Reason: ${reason.ifBlank { "Disruptive behavior" }}",
                isSystemNotice = true
            )
            messageDao.insertMessage(notice)
            _events.emit("⚖️ Banned $targetUsername from room.")
        }
    }

    fun serverMuteParticipant(targetUsername: String, mute: Boolean) {
        val me = _currentUser.value ?: return
        val canModerate = me.isCryaAdmin || me.isDeveloper
        if (!canModerate) {
            viewModelScope.launch { _events.emit("Access Denied: Requires CRYA or Hammer Developer badge.") }
            return
        }

        SoundEffects.playHammerStrike()
        _participants.value = _participants.value.map {
            if (it.username.equals(targetUsername, ignoreCase = true)) {
                it.copy(isMutedByAdmin = mute, isSpeaking = false, voiceLevel = 0f)
            } else it
        }

        val room = _activeRoom.value ?: return
        // Broadcast server mute mod action to network
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_MOD_ACTION,
                roomId = room.id,
                sender = me.username,
                modAction = if (mute) "SERVER_MUTE" else "SERVER_UNMUTE",
                targetUser = targetUsername
            )
        )

        viewModelScope.launch {
            val action = if (mute) "MUTED" else "UNMUTED"
            val notice = ChatMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderUsername = "MODERATION",
                senderIsDev = true,
                senderIsAdmin = true,
                messageText = "🎙️ $targetUsername was voice $action by ${me.username}.",
                isSystemNotice = true
            )
            messageDao.insertMessage(notice)
            _events.emit("🎙️ $targetUsername voice $action.")
        }
    }

    fun teleportToMe(targetUsername: String) {
        val me = _currentUser.value ?: return
        val canModerate = me.isCryaAdmin || me.isDeveloper
        if (!canModerate) return

        val myPart = _participants.value.firstOrNull { it.isMe } ?: return
        _participants.value = _participants.value.map {
            if (it.username.equals(targetUsername, ignoreCase = true)) {
                it.copy(x = myPart.x + 1f, y = myPart.y)
            } else it
        }

        val room = _activeRoom.value
        if (room != null) {
            multiplayerManager.sendPacket(
                MultiplayerPacket(
                    type = MultiplayerPacket.TYPE_MOD_ACTION,
                    roomId = room.id,
                    sender = me.username,
                    modAction = "TELEPORT",
                    targetUser = targetUsername,
                    x = myPart.x + 1f,
                    y = myPart.y
                )
            )
        }

        viewModelScope.launch {
            _events.emit("Teleported $targetUsername to your position.")
        }
    }

    fun tipCrin(targetUsername: String, amount: Int = 50) {
        val me = _currentUser.value ?: return
        if (!me.isDeveloper && !me.isCryaAdmin) {
            viewModelScope.launch {
                _events.emit("Access Denied: Only Dev accounts can give CRIN in rooms!")
            }
            return
        }

        val room = _activeRoom.value ?: return

        // If Dev is giving CRIN to themselves in a room
        if (targetUsername.equals(me.username, ignoreCase = true)) {
            viewModelScope.launch {
                userDao.addCrin(me.username, amount)
                val updated = userDao.getUserByUsername(me.username)
                if (updated != null) _currentUser.value = updated
                SoundEffects.playCoinJingle()

                val notice = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    roomId = room.id,
                    senderUsername = "DEV CRIN",
                    senderIsDev = true,
                    senderIsAdmin = me.isCryaAdmin,
                    messageText = "🪙 Dev ${me.username} gave themselves +$amount CRIN!",
                    isSystemNotice = true
                )
                messageDao.insertMessage(notice)
                _events.emit("Dev Grant: Added +$amount CRIN to your account!")
            }
            return
        }

        // Dev is giving CRIN to another player in the room
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_TIP,
                roomId = room.id,
                sender = me.username,
                targetUser = targetUsername,
                crinAmount = amount
            )
        )

        viewModelScope.launch {
            userDao.addCrin(targetUsername, amount)
            SoundEffects.playCoinJingle()

            val notice = ChatMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderUsername = "DEV CRIN",
                senderIsDev = true,
                senderIsAdmin = me.isCryaAdmin,
                messageText = "🪙 Dev ${me.username} gave $amount CRIN to $targetUsername!",
                isSystemNotice = true
            )
            messageDao.insertMessage(notice)
            _events.emit("Gave $amount CRIN to $targetUsername!")
        }
    }

    // --- Shop & Cosmetics ---
    fun purchaseShopItem(item: ShopItem) {
        val me = _currentUser.value ?: return
        if (item.isUnlocked) {
            // Already unlocked, equip/unequip
            equipShopItem(item)
            return
        }

        if (me.crinBalance < item.priceCrin) {
            viewModelScope.launch {
                _events.emit("Not enough CRIN! Needs ${item.priceCrin} CRIN (You have ${me.crinBalance})")
            }
            return
        }

        viewModelScope.launch {
            userDao.addCrin(me.username, -item.priceCrin)
            shopDao.unlockItem(item.id)
            val updatedUser = userDao.getUserByUsername(me.username)
            if (updatedUser != null) _currentUser.value = updatedUser
            SoundEffects.playCoinJingle()
            _events.emit("Unlocked ${item.name}! Equipping now.")
            equipShopItem(item.copy(isUnlocked = true))
        }
    }

    fun updateAvatarColor(colorHex: String) {
        val me = _currentUser.value ?: return
        val updated = me.copy(avatarColorHex = colorHex)
        _currentUser.value = updated
        viewModelScope.launch {
            userDao.updateUser(updated)
            _participants.value = _participants.value.map {
                if (it.isMe) it.copy(avatarColorHex = colorHex) else it
            }
            broadcastMyAvatarPresence(updated)
            _events.emit("Updated 3D Avatar color tone")
        }
    }

    private fun broadcastMyAvatarPresence(user: UserAccount) {
        val room = _activeRoom.value ?: return
        val myPart = _participants.value.firstOrNull { it.isMe } ?: return
        multiplayerManager.sendPacket(
            MultiplayerPacket(
                type = MultiplayerPacket.TYPE_PRESENCE,
                roomId = room.id,
                sender = user.username,
                x = myPart.x,
                y = myPart.y,
                facingAngle = myPart.facingAngle,
                isWalking = myPart.isWalking,
                avatarColorHex = user.avatarColorHex,
                avatarHat = user.avatarHat,
                avatarOutfit = user.avatarOutfit,
                avatarAura = user.avatarAura,
                isDeveloper = user.isDeveloper,
                isCryaAdmin = user.isCryaAdmin
            )
        )
    }

    private fun equipShopItem(item: ShopItem) {
        val me = _currentUser.value ?: return
        val updated = when (item.category) {
            "SHIRT" -> {
                me.copy(avatarOutfit = item.name)
            }
            "COSMETIC" -> {
                if (item.id.startsWith("hat_")) {
                    me.copy(avatarHat = if (me.avatarHat == item.name) "None" else item.name)
                } else if (item.id.startsWith("outfit_")) {
                    me.copy(avatarOutfit = item.name)
                } else me
            }
            "EFFECT" -> {
                me.copy(avatarAura = if (me.avatarAura == item.name) "None" else item.name)
            }
            "DECORATION" -> {
                // Add decoration to active room if host
                val room = _activeRoom.value
                if (room != null && (room.creatorUsername == me.username || me.isDeveloper || me.isCryaAdmin)) {
                    val decoKey = item.id.removePrefix("deco_").uppercase()
                    val currentDecos = room.decorations.split(",").map { it.trim() }.toMutableList()
                    if (!currentDecos.contains(decoKey)) {
                        currentDecos.add(decoKey)
                        val newDecos = currentDecos.joinToString(",")
                        val updatedRoom = room.copy(decorations = newDecos)
                        _activeRoom.value = updatedRoom
                        _roomObjects.value = buildRoomObjects(newDecos, room.theme)
                        viewModelScope.launch { roomDao.updateRoom(updatedRoom) }
                    }
                }
                me
            }
            else -> me
        }

        _currentUser.value = updated
        viewModelScope.launch {
            userDao.updateUser(updated)
            // Update active participant avatar
            _participants.value = _participants.value.map {
                if (it.isMe) {
                    it.copy(
                        avatarHat = updated.avatarHat,
                        avatarOutfit = updated.avatarOutfit,
                        avatarAura = updated.avatarAura
                    )
                } else it
            }
            broadcastMyAvatarPresence(updated)
            _events.emit("Equipped ${item.name}")
        }
    }

    // --- Active Room Multiplayer & Hangout Loops ---
    private fun startRoomSimulation(room: ChatRoom) {
        roomSimJob?.cancel()
        var heartbeatTick = 0
        roomSimJob = viewModelScope.launch {
            while (isActive) {
                delay(300)
                val now = System.currentTimeMillis()
                val current = _participants.value.toMutableList()
                val myPart = current.firstOrNull { it.isMe }
                var changed = false

                // Periodically broadcast active public lobby heartbeat (~every 4.5s)
                heartbeatTick++
                if (heartbeatTick % 15 == 0 && room.isPublic && current.isNotEmpty()) {
                    announceActiveLobby(room, current.size)
                    roomDao.updateRoom(room.copy(onlineCount = current.size))
                }

                for (i in current.indices) {
                    val p = current[i]
                    if (p.isMe) {
                        // Expire local speech bubble if needed
                        if (p.recentSpeech != null && now > p.speechExpiry) {
                            current[i] = p.copy(recentSpeech = null)
                            changed = true
                        }
                        // Update local voice visualizer if mic is on
                        if (_isMicEnabled.value) {
                            val wave = Random.nextFloat() * 0.6f + 0.4f
                            _myVoiceLevel.value = wave
                            current[i] = current[i].copy(voiceLevel = wave, isSpeaking = true)
                            changed = true
                        } else {
                            if (_myVoiceLevel.value != 0f) {
                                _myVoiceLevel.value = 0f
                                current[i] = current[i].copy(voiceLevel = 0f, isSpeaking = false)
                                changed = true
                            }
                        }
                        continue
                    }

                    // Expire peer speech bubble
                    if (p.recentSpeech != null && now > p.speechExpiry) {
                        current[i] = p.copy(recentSpeech = null)
                        changed = true
                    }

                    // Spatial distance audio attenuation for real peers
                    if (p.isSpeaking && room.voiceMode == "SPATIAL" && myPart != null) {
                        val distanceToMe = hypot(myPart.x - p.x, myPart.y - p.y)
                        val falloff = (1f - (distanceToMe / 10f)).coerceIn(0f, 1f)
                        val effectiveVoice = p.voiceLevel * falloff
                        if (effectiveVoice != p.voiceLevel) {
                            current[i] = p.copy(voiceLevel = effectiveVoice)
                            changed = true
                        }
                    }
                }

                if (changed) {
                    _participants.value = current
                }
            }
        }
    }

    private fun startHangoutRewardLoop() {
        hangoutRewardJob?.cancel()
        hangoutRewardJob = viewModelScope.launch {
            while (isActive) {
                // Every 30 seconds hanging out in a chat gives +25 CRIN
                delay(30_000L)
                val me = _currentUser.value ?: continue
                userDao.addCrin(me.username, 25)
                val updated = userDao.getUserByUsername(me.username)
                if (updated != null) _currentUser.value = updated
                SoundEffects.playCoinJingle()
                _events.emit("+25 CRIN Hangout Bonus earned!")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        multiplayerManager.disconnect()
        roomSimJob?.cancel()
        hangoutRewardJob?.cancel()
    }
}
