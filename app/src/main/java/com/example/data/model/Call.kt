package com.example.data.model

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING
}

enum class CallStatus {
    MISSED,
    COMPLETED,
    REJECTED,
    UNANSWERED
}

data class CallRecord(
    val id: String,
    val peerId: String,
    val peerName: String,
    val peerAvatar: String = "",
    val type: CallType = CallType.VOICE,
    val direction: CallDirection = CallDirection.OUTGOING,
    val status: CallStatus = CallStatus.COMPLETED,
    val durationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActiveCallStage {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ENDED
}

enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    POOR
}

data class ActiveCallSession(
    val callId: String = "",
    val peerId: String = "",
    val peerName: String = "",
    val peerAvatar: String = "",
    val type: CallType = CallType.VOICE,
    val stage: ActiveCallStage = ActiveCallStage.IDLE,
    val isMicMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoCameraOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val networkQuality: NetworkQuality = NetworkQuality.EXCELLENT,
    val durationSec: Int = 0,
    val isIncoming: Boolean = false
)
