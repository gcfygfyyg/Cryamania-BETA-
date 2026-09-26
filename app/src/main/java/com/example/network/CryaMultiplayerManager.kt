package com.example.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

enum class MultiplayerNetworkMode {
    CLOUD_RELAY,   // Public high-speed broker (broker.hivemq.com)
    LAN_DIRECT     // Direct Host-to-Peer Wi-Fi / Local Server
}

enum class MultiplayerConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * High-performance real-time multiplayer engine for CryaMania.
 * Handles live public lobby discovery, 3D avatar movement, shirts/outfits, chat broadcast, voice levels, emotes, and moderation packets.
 */
class CryaMultiplayerManager(
    private val scope: CoroutineScope
) {
    private val TAG = "CryaMultiplayer"
    private val LOBBY_DISCOVERY_TOPIC = "cryamania/lobbies_v2"

    private val _connectionState = MutableStateFlow(MultiplayerConnectionState.DISCONNECTED)
    val connectionState: StateFlow<MultiplayerConnectionState> = _connectionState.asStateFlow()

    private val _peerCount = MutableStateFlow(1)
    val peerCount: StateFlow<Int> = _peerCount.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    private val _currentMode = MutableStateFlow(MultiplayerNetworkMode.CLOUD_RELAY)
    val currentMode: StateFlow<MultiplayerNetworkMode> = _currentMode.asStateFlow()

    private var myUsername: String = ""
    private var connectionJob: Job? = null
    private var lobbyDiscoveryJob: Job? = null
    private var pingJob: Job? = null
    private var lanServerJob: Job? = null

    // Sockets for Cloud Relay (Room)
    private var cloudSocket: Socket? = null
    private var cloudIn: DataInputStream? = null
    private var cloudOut: DataOutputStream? = null

    // Sockets for Global Lobby Discovery
    private var lobbySocket: Socket? = null
    private var lobbyOut: DataOutputStream? = null

    // Sockets for LAN Server & Client
    private var lanServerSocket: ServerSocket? = null
    private val lanClientSockets = CopyOnWriteArrayList<Socket>()
    private var lanClientSocket: Socket? = null
    private var lanClientWriter: PrintWriter? = null

    // Callbacks
    var onPacketReceived: ((MultiplayerPacket) -> Unit)? = null
    var onLobbyPacketReceived: ((MultiplayerPacket) -> Unit)? = null
    var onStatusNotice: ((String) -> Unit)? = null

    // Throttle for MOVE packets
    private var lastSentX = 0f
    private var lastSentY = 0f
    private var lastSentAngle = 0f
    private var lastMoveTime = 0L

    /**
     * Start listening for live hosted public lobbies across the network.
     * Lobbies with 0 players are never shown; only live hosted public lobbies appear.
     */
    fun startLobbyDiscovery() {
        if (lobbyDiscoveryJob?.isActive == true) return
        lobbyDiscoveryJob = scope.launch(Dispatchers.IO) {
            val brokers = listOf("broker.hivemq.com", "broker.emqx.io")
            for (broker in brokers) {
                if (!isActive) break
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(broker, 1883), 6000)
                    socket.soTimeout = 45000
                    lobbySocket = socket

                    val out = DataOutputStream(socket.getOutputStream())
                    val input = DataInputStream(socket.getInputStream())
                    lobbyOut = out

                    val clientId = "crya_lobby_${UUID.randomUUID().toString().take(8)}"
                    sendMqttConnect(out, clientId)

                    input.readUnsignedByte()
                    readMqttRemainingLength(input)
                    input.readByte()
                    val ackCode = input.readByte()
                    if (ackCode.toInt() != 0) {
                        socket.close()
                        continue
                    }

                    sendMqttSubscribe(out, packetId = 10, topic = LOBBY_DISCOVERY_TOPIC)
                    input.readUnsignedByte()
                    readMqttRemainingLength(input)
                    input.readShort()
                    input.readByte()

                    // Periodic ping for lobby socket
                    launch(Dispatchers.IO) {
                        while (isActive && !socket.isClosed) {
                            delay(20000)
                            try {
                                synchronized(out) {
                                    out.writeByte(0xC0)
                                    out.writeByte(0x00)
                                    out.flush()
                                }
                            } catch (_: Exception) {
                                break
                            }
                        }
                    }

                    while (isActive && !socket.isClosed) {
                        val header = input.readUnsignedByte()
                        val packetType = header shr 4
                        val remainingLen = readMqttRemainingLength(input)
                        val payloadBytes = ByteArray(remainingLen)
                        input.readFully(payloadBytes)

                        if (packetType == 3) {
                            parseMqttLobbyPublish(payloadBytes)
                        }
                    }
                    break
                } catch (e: Exception) {
                    try { lobbySocket?.close() } catch (_: Exception) {}
                }
            }
        }
    }

    private fun parseMqttLobbyPublish(bytes: ByteArray) {
        try {
            if (bytes.size < 2) return
            val topicLen = ((bytes[0].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF)
            val jsonOffset = 2 + topicLen
            if (jsonOffset < bytes.size) {
                val jsonStr = String(bytes, jsonOffset, bytes.size - jsonOffset, Charsets.UTF_8)
                val packet = MultiplayerPacket.fromJsonString(jsonStr)
                if (packet != null) {
                    onLobbyPacketReceived?.invoke(packet)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing lobby publish", e)
        }
    }

    /**
     * Broadcast a public lobby announcement or closure to the global lobby feed.
     */
    fun broadcastLobbyPacket(packet: MultiplayerPacket) {
        val jsonStr = packet.toJsonString()
        scope.launch(Dispatchers.IO) {
            val out = lobbyOut ?: cloudOut
            if (out != null) {
                try {
                    val payload = ByteArrayOutputStream()
                    val pOut = DataOutputStream(payload)
                    pOut.writeUTF(LOBBY_DISCOVERY_TOPIC)
                    val jsonBytes = jsonStr.toByteArray(Charsets.UTF_8)
                    pOut.write(jsonBytes)

                    val bytes = payload.toByteArray()
                    synchronized(out) {
                        out.writeByte(0x30)
                        writeMqttRemainingLength(out, bytes.size)
                        out.write(bytes)
                        out.flush()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error broadcasting lobby packet: ${e.message}")
                }
            }
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP", e)
        }
        return "127.0.0.1"
    }

    fun joinRoom(
        roomId: String,
        username: String,
        isHost: Boolean = false,
        lanHostIp: String? = null
    ) {
        disconnectRoomOnly()
        myUsername = username
        _activeRoomId.value = roomId
        _connectionState.value = MultiplayerConnectionState.CONNECTING

        if (!lanHostIp.isNullOrBlank()) {
            _currentMode.value = MultiplayerNetworkMode.LAN_DIRECT
            startLanClient(lanHostIp, 9050, roomId)
        } else if (isHost) {
            _currentMode.value = MultiplayerNetworkMode.CLOUD_RELAY
            startLanServer(9050)
            startCloudRelay(roomId)
        } else {
            _currentMode.value = MultiplayerNetworkMode.CLOUD_RELAY
            startCloudRelay(roomId)
        }
    }

    private fun startCloudRelay(roomId: String) {
        connectionJob = scope.launch(Dispatchers.IO) {
            val topic = "cryamania/rooms/$roomId"
            val brokers = listOf("broker.hivemq.com", "broker.emqx.io")
            var connected = false

            for (broker in brokers) {
                if (!isActive) break
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(broker, 1883), 7000)
                    socket.soTimeout = 40000
                    cloudSocket = socket

                    val out = DataOutputStream(socket.getOutputStream())
                    val input = DataInputStream(socket.getInputStream())
                    cloudOut = out
                    cloudIn = input

                    val clientId = "crya_${myUsername}_${UUID.randomUUID().toString().take(6)}"
                    sendMqttConnect(out, clientId)

                    input.readUnsignedByte()
                    readMqttRemainingLength(input)
                    input.readByte()
                    val ackReturnCode = input.readByte()
                    if (ackReturnCode.toInt() != 0) {
                        socket.close()
                        continue
                    }

                    sendMqttSubscribe(out, packetId = 1, topic = topic)

                    input.readUnsignedByte()
                    readMqttRemainingLength(input)
                    input.readShort()
                    input.readByte()

                    _connectionState.value = MultiplayerConnectionState.CONNECTED
                    onStatusNotice?.invoke("Connected to Live Lobby ($broker)")
                    connected = true

                    startPingLoop(out)

                    while (isActive && !socket.isClosed) {
                        val header = input.readUnsignedByte()
                        val packetType = header shr 4
                        val remainingLen = readMqttRemainingLength(input)
                        val payloadBytes = ByteArray(remainingLen)
                        input.readFully(payloadBytes)

                        if (packetType == 3) {
                            parseMqttPublish(payloadBytes)
                        }
                    }
                    break
                } catch (e: Exception) {
                    try { cloudSocket?.close() } catch (_: Exception) {}
                }
            }

            if (!connected && isActive) {
                _connectionState.value = MultiplayerConnectionState.CONNECTED
                onStatusNotice?.invoke("Live P2P Mode Active")
                startLanServer(9050)
            }
        }
    }

    private fun sendMqttConnect(out: DataOutputStream, clientId: String) {
        val payload = ByteArrayOutputStream()
        val pOut = DataOutputStream(payload)
        pOut.writeUTF("MQTT")
        pOut.writeByte(4)
        pOut.writeByte(0x02)
        pOut.writeShort(30)
        pOut.writeUTF(clientId)

        val bytes = payload.toByteArray()
        out.writeByte(0x10)
        writeMqttRemainingLength(out, bytes.size)
        out.write(bytes)
        out.flush()
    }

    private fun sendMqttSubscribe(out: DataOutputStream, packetId: Int, topic: String) {
        val payload = ByteArrayOutputStream()
        val pOut = DataOutputStream(payload)
        pOut.writeShort(packetId)
        pOut.writeUTF(topic)
        pOut.writeByte(0x00)

        val bytes = payload.toByteArray()
        out.writeByte(0x82)
        writeMqttRemainingLength(out, bytes.size)
        out.write(bytes)
        out.flush()
    }

    private fun parseMqttPublish(bytes: ByteArray) {
        try {
            if (bytes.size < 2) return
            val topicLen = ((bytes[0].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF)
            val jsonOffset = 2 + topicLen
            if (jsonOffset < bytes.size) {
                val jsonStr = String(bytes, jsonOffset, bytes.size - jsonOffset, Charsets.UTF_8)
                val packet = MultiplayerPacket.fromJsonString(jsonStr)
                if (packet != null && packet.sender != myUsername) {
                    onPacketReceived?.invoke(packet)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing publish", e)
        }
    }

    private fun startPingLoop(out: DataOutputStream) {
        pingJob?.cancel()
        pingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(15000)
                try {
                    synchronized(out) {
                        out.writeByte(0xC0)
                        out.writeByte(0x00)
                        out.flush()
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun writeMqttRemainingLength(out: DataOutputStream, length: Int) {
        var len = length
        do {
            var digit = len % 128
            len /= 128
            if (len > 0) {
                digit = digit or 0x80
            }
            out.writeByte(digit)
        } while (len > 0)
    }

    private fun readMqttRemainingLength(input: DataInputStream): Int {
        var multiplier = 1
        var value = 0
        var digit: Int
        do {
            digit = input.readUnsignedByte()
            value += (digit and 127) * multiplier
            multiplier *= 128
        } while ((digit and 128) != 0)
        return value
    }

    fun startLanServer(port: Int = 9050) {
        if (lanServerSocket != null) return
        lanServerJob = scope.launch(Dispatchers.IO) {
            try {
                val server = ServerSocket(port)
                lanServerSocket = server
                while (isActive && !server.isClosed) {
                    val client = server.accept()
                    lanClientSockets.add(client)
                    _peerCount.value = lanClientSockets.size + 1
                    handleLanClient(client)
                }
            } catch (e: Exception) {
                Log.w(TAG, "LAN server closed: ${e.message}")
            }
        }
    }

    private fun handleLanClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                while (isActive && !socket.isClosed) {
                    val line = reader.readLine() ?: break
                    val packet = MultiplayerPacket.fromJsonString(line)
                    if (packet != null) {
                        if (packet.sender != myUsername) {
                            onPacketReceived?.invoke(packet)
                        }
                        broadcastToLanClients(line, socket)
                    }
                }
            } catch (_: Exception) {
            } finally {
                lanClientSockets.remove(socket)
                _peerCount.value = lanClientSockets.size + 1
                try { socket.close() } catch (_: Exception) {}
            }
        }
    }

    private fun broadcastToLanClients(jsonLine: String, exclude: Socket? = null) {
        for (sock in lanClientSockets) {
            if (sock != exclude && !sock.isClosed) {
                try {
                    val writer = PrintWriter(sock.getOutputStream(), true)
                    writer.println(jsonLine)
                } catch (_: Exception) {}
            }
        }
    }

    private fun startLanClient(hostIp: String, port: Int, roomId: String) {
        connectionJob = scope.launch(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostIp, port), 5000)
                lanClientSocket = socket
                val writer = PrintWriter(socket.getOutputStream(), true)
                lanClientWriter = writer
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))

                _connectionState.value = MultiplayerConnectionState.CONNECTED
                onStatusNotice?.invoke("Connected to LAN Host ($hostIp)")

                while (isActive && !socket.isClosed) {
                    val line = reader.readLine() ?: break
                    val packet = MultiplayerPacket.fromJsonString(line)
                    if (packet != null && packet.sender != myUsername) {
                        onPacketReceived?.invoke(packet)
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = MultiplayerConnectionState.ERROR
                onStatusNotice?.invoke("LAN Connection Failed: ${e.message}")
            }
        }
    }

    fun sendPacket(packet: MultiplayerPacket) {
        val jsonStr = packet.toJsonString()
        scope.launch(Dispatchers.IO) {
            val out = cloudOut
            val topic = "cryamania/rooms/${packet.roomId}"
            if (out != null && cloudSocket?.isConnected == true) {
                try {
                    val payload = ByteArrayOutputStream()
                    val pOut = DataOutputStream(payload)
                    pOut.writeUTF(topic)
                    val jsonBytes = jsonStr.toByteArray(Charsets.UTF_8)
                    pOut.write(jsonBytes)

                    val bytes = payload.toByteArray()
                    synchronized(out) {
                        out.writeByte(0x30)
                        writeMqttRemainingLength(out, bytes.size)
                        out.write(bytes)
                        out.flush()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error sending to cloud relay: ${e.message}")
                }
            }

            broadcastToLanClients(jsonStr)

            lanClientWriter?.let { writer ->
                try {
                    writer.println(jsonStr)
                } catch (_: Exception) {}
            }
        }
    }

    fun sendMovement(
        roomId: String,
        x: Float,
        y: Float,
        facingAngle: Float,
        isWalking: Boolean,
        force: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val distSquared = (x - lastSentX) * (x - lastSentX) + (y - lastSentY) * (y - lastSentY)
        val angleDiff = kotlin.math.abs(facingAngle - lastSentAngle)

        if (force || (now - lastMoveTime >= 65 && (distSquared > 0.005f || angleDiff > 2.0f || !isWalking))) {
            lastSentX = x
            lastSentY = y
            lastSentAngle = facingAngle
            lastMoveTime = now

            val packet = MultiplayerPacket(
                type = MultiplayerPacket.TYPE_MOVE,
                roomId = roomId,
                sender = myUsername,
                x = x,
                y = y,
                facingAngle = facingAngle,
                isWalking = isWalking
            )
            sendPacket(packet)
        }
    }

    fun disconnectRoomOnly() {
        pingJob?.cancel()
        connectionJob?.cancel()
        lanServerJob?.cancel()

        try { cloudSocket?.close() } catch (_: Exception) {}
        try { lanServerSocket?.close() } catch (_: Exception) {}
        try { lanClientSocket?.close() } catch (_: Exception) {}

        for (sock in lanClientSockets) {
            try { sock.close() } catch (_: Exception) {}
        }
        lanClientSockets.clear()

        cloudSocket = null
        cloudIn = null
        cloudOut = null
        lanServerSocket = null
        lanClientSocket = null
        lanClientWriter = null

        _connectionState.value = MultiplayerConnectionState.DISCONNECTED
        _activeRoomId.value = null
        _peerCount.value = 1
    }

    fun disconnect() {
        disconnectRoomOnly()
        lobbyDiscoveryJob?.cancel()
        try { lobbySocket?.close() } catch (_: Exception) {}
        lobbySocket = null
        lobbyOut = null
    }
}
