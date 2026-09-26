package com.example.data.model

data class MetaverseParticipant(
    val id: String,
    val username: String,
    val isMe: Boolean = false,
    val isDeveloper: Boolean = false,
    val isCryaAdmin: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var facingAngle: Float = 0f,
    var isWalking: Boolean = false,
    var isSpeaking: Boolean = false,
    var voiceLevel: Float = 0f, // 0f..1f for visualizer
    var isMutedByAdmin: Boolean = false,
    var isLocallyMuted: Boolean = false,
    var localVolume: Float = 1.0f,
    val avatarColorHex: String = "#00F5D4",
    val avatarHat: String = "None",
    val avatarOutfit: String = "Cyber Hoodie",
    val avatarAura: String = "None",
    var currentEmote: String? = null,
    var recentSpeech: String? = null,
    var speechExpiry: Long = 0L
)

data class RoomObject3D(
    val id: String,
    val type: String, // "DJ_BOOTH", "DISCO_BALL", "NEON_SIGN", "LOUNGE_SOFA", "ARCADE_CABINET", "FOUNTAIN", "CRYSTAL_SPIRE"
    val x: Float,
    val y: Float,
    val label: String,
    val iconEmoji: String
)
