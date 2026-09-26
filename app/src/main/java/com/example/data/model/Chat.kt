package com.example.data.model

enum class ChatType {
    DIRECT,
    GROUP
}

enum class MessageType {
    TEXT,
    EMOJI,
    GIF,
    IMAGE,
    VIDEO,
    DOCUMENT,
    CONTACT,
    LOCATION,
    VOICE
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class MessageReaction(
    val emoji: String,
    val userId: String,
    val userName: String
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String = "",
    val type: MessageType = MessageType.TEXT,
    val content: String,
    val mediaUrl: String = "",
    val mediaFileName: String = "",
    val mediaFileSize: String = "",
    val voiceDurationSec: Int = 0,
    val voiceWaveform: List<Float> = emptyList(),
    val locationLatitude: Double = 0.0,
    val locationLongitude: Double = 0.0,
    val locationTitle: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val replyToMessageId: String? = null,
    val replyToSenderName: String? = null,
    val replyToContent: String? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val reactions: List<MessageReaction> = emptyList()
)

data class Chat(
    val id: String,
    val type: ChatType = ChatType.DIRECT,
    val title: String,
    val avatarUrl: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessagePreview: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeenText: String = "online",
    val isTyping: Boolean = false,
    val participantIds: List<String> = emptyList()
)
