package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val about: String,
    val isOnline: Boolean,
    val lastSeenTimestamp: Long,
    val isBlocked: Boolean
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: String, // DIRECT, GROUP
    val title: String,
    val avatarUrl: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isMuted: Boolean,
    val unreadCount: Int,
    val lastMessagePreview: String,
    val lastMessageTimestamp: Long,
    val isOnline: Boolean,
    val lastSeenText: String,
    val participantIds: String // comma separated
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val type: String, // TEXT, EMOJI, GIF, IMAGE, VIDEO, DOCUMENT, CONTACT, LOCATION, VOICE
    val content: String,
    val mediaUrl: String,
    val mediaFileName: String,
    val mediaFileSize: String,
    val voiceDurationSec: Int,
    val waveformStr: String, // comma separated floats
    val locationLatitude: Double,
    val locationLongitude: Double,
    val locationTitle: String,
    val contactName: String,
    val contactPhone: String,
    val replyToId: String?,
    val replyToSender: String?,
    val replyToContent: String?,
    val status: String, // SENDING, SENT, DELIVERED, READ
    val timestamp: Long,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val reactionsStr: String // serialized emoji|userId|userName separated by ;;
)

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val type: String,
    val caption: String,
    val mediaUrl: String,
    val backgroundColorHex: String,
    val voiceDurationSec: Int,
    val timestamp: Long,
    val expiresAtTimestamp: Long,
    val isViewedByMe: Boolean,
    val viewersStr: String,
    val isMyStatus: Boolean
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val followerCount: Int,
    val isFollowedByMe: Boolean,
    val isOwnerOrAdmin: Boolean,
    val category: String
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val peerId: String,
    val peerName: String,
    val peerAvatar: String,
    val type: String, // VOICE, VIDEO
    val direction: String, // INCOMING, OUTGOING
    val status: String, // MISSED, COMPLETED, REJECTED
    val durationSec: Int,
    val timestamp: Long
)
