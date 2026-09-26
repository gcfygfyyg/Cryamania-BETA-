package com.example.network

import org.json.JSONObject

/**
 * Real-time network packet for CryaMania multiplayer synchronization.
 * Transmitted over MQTT / TCP to synchronize active public lobbies, 3D avatars, shirts, movements, chat, voice, and admin actions.
 */
data class MultiplayerPacket(
    val type: String,
    val roomId: String,
    val sender: String,
    val timestamp: Long = System.currentTimeMillis(),
    val x: Float? = null,
    val y: Float? = null,
    val facingAngle: Float? = null,
    val isWalking: Boolean? = null,
    val avatarColorHex: String? = null,
    val avatarHat: String? = null,
    val avatarOutfit: String? = null,
    val avatarAura: String? = null,
    val isDeveloper: Boolean? = null,
    val isCryaAdmin: Boolean? = null,
    val chatText: String? = null,
    val emote: String? = null,
    val isSpeaking: Boolean? = null,
    val voiceLevel: Float? = null,
    val modAction: String? = null,
    val targetUser: String? = null,
    val reason: String? = null,
    val trackName: String? = null,
    val crinAmount: Int? = null,
    // Public Lobby Discovery Fields
    val roomName: String? = null,
    val roomDesc: String? = null,
    val roomCode: String? = null,
    val roomTheme: String? = null,
    val roomVoiceMode: String? = null,
    val roomOnlineCount: Int? = null,
    val roomDecorations: String? = null,
    val roomIsPublic: Boolean? = null
) {
    fun toJsonString(): String {
        val json = JSONObject()
        json.put("type", type)
        json.put("roomId", roomId)
        json.put("sender", sender)
        json.put("timestamp", timestamp)
        x?.let { json.put("x", it.toDouble()) }
        y?.let { json.put("y", it.toDouble()) }
        facingAngle?.let { json.put("facingAngle", it.toDouble()) }
        isWalking?.let { json.put("isWalking", it) }
        avatarColorHex?.let { json.put("avatarColorHex", it) }
        avatarHat?.let { json.put("avatarHat", it) }
        avatarOutfit?.let { json.put("avatarOutfit", it) }
        avatarAura?.let { json.put("avatarAura", it) }
        isDeveloper?.let { json.put("isDeveloper", it) }
        isCryaAdmin?.let { json.put("isCryaAdmin", it) }
        chatText?.let { json.put("chatText", it) }
        emote?.let { json.put("emote", it) }
        isSpeaking?.let { json.put("isSpeaking", it) }
        voiceLevel?.let { json.put("voiceLevel", it.toDouble()) }
        modAction?.let { json.put("modAction", it) }
        targetUser?.let { json.put("targetUser", it) }
        reason?.let { json.put("reason", it) }
        trackName?.let { json.put("trackName", it) }
        crinAmount?.let { json.put("crinAmount", it) }
        roomName?.let { json.put("roomName", it) }
        roomDesc?.let { json.put("roomDesc", it) }
        roomCode?.let { json.put("roomCode", it) }
        roomTheme?.let { json.put("roomTheme", it) }
        roomVoiceMode?.let { json.put("roomVoiceMode", it) }
        roomOnlineCount?.let { json.put("roomOnlineCount", it) }
        roomDecorations?.let { json.put("roomDecorations", it) }
        roomIsPublic?.let { json.put("roomIsPublic", it) }
        return json.toString()
    }

    companion object {
        const val TYPE_JOIN = "JOIN"
        const val TYPE_PRESENCE = "PRESENCE"
        const val TYPE_MOVE = "MOVE"
        const val TYPE_CHAT = "CHAT"
        const val TYPE_EMOTE = "EMOTE"
        const val TYPE_VOICE = "VOICE"
        const val TYPE_MOD_ACTION = "MOD_ACTION"
        const val TYPE_TRACK = "TRACK"
        const val TYPE_TIP = "TIP"
        const val TYPE_LEAVE = "LEAVE"
        const val TYPE_PING = "PING"
        const val TYPE_LOBBY_ANNOUNCE = "LOBBY_ANNOUNCE"
        const val TYPE_LOBBY_CLOSE = "LOBBY_CLOSE"

        fun fromJsonString(jsonStr: String): MultiplayerPacket? {
            return try {
                val json = JSONObject(jsonStr)
                MultiplayerPacket(
                    type = json.optString("type", ""),
                    roomId = json.optString("roomId", ""),
                    sender = json.optString("sender", ""),
                    timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                    x = if (json.has("x")) json.optDouble("x").toFloat() else null,
                    y = if (json.has("y")) json.optDouble("y").toFloat() else null,
                    facingAngle = if (json.has("facingAngle")) json.optDouble("facingAngle").toFloat() else null,
                    isWalking = if (json.has("isWalking")) json.optBoolean("isWalking") else null,
                    avatarColorHex = if (json.has("avatarColorHex")) json.optString("avatarColorHex") else null,
                    avatarHat = if (json.has("avatarHat")) json.optString("avatarHat") else null,
                    avatarOutfit = if (json.has("avatarOutfit")) json.optString("avatarOutfit") else null,
                    avatarAura = if (json.has("avatarAura")) json.optString("avatarAura") else null,
                    isDeveloper = if (json.has("isDeveloper")) json.optBoolean("isDeveloper") else null,
                    isCryaAdmin = if (json.has("isCryaAdmin")) json.optBoolean("isCryaAdmin") else null,
                    chatText = if (json.has("chatText")) json.optString("chatText") else null,
                    emote = if (json.has("emote")) json.optString("emote") else null,
                    isSpeaking = if (json.has("isSpeaking")) json.optBoolean("isSpeaking") else null,
                    voiceLevel = if (json.has("voiceLevel")) json.optDouble("voiceLevel").toFloat() else null,
                    modAction = if (json.has("modAction")) json.optString("modAction") else null,
                    targetUser = if (json.has("targetUser")) json.optString("targetUser") else null,
                    reason = if (json.has("reason")) json.optString("reason") else null,
                    trackName = if (json.has("trackName")) json.optString("trackName") else null,
                    crinAmount = if (json.has("crinAmount")) json.optInt("crinAmount") else null,
                    roomName = if (json.has("roomName")) json.optString("roomName") else null,
                    roomDesc = if (json.has("roomDesc")) json.optString("roomDesc") else null,
                    roomCode = if (json.has("roomCode")) json.optString("roomCode") else null,
                    roomTheme = if (json.has("roomTheme")) json.optString("roomTheme") else null,
                    roomVoiceMode = if (json.has("roomVoiceMode")) json.optString("roomVoiceMode") else null,
                    roomOnlineCount = if (json.has("roomOnlineCount")) json.optInt("roomOnlineCount") else null,
                    roomDecorations = if (json.has("roomDecorations")) json.optString("roomDecorations") else null,
                    roomIsPublic = if (json.has("roomIsPublic")) json.optBoolean("roomIsPublic") else null
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
