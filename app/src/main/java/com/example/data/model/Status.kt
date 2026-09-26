package com.example.data.model

enum class StatusType {
    TEXT,
    PHOTO,
    VIDEO,
    VOICE
}

data class StatusViewer(
    val userId: String,
    val userName: String,
    val avatarUrl: String = "",
    val viewedAtTimestamp: Long = System.currentTimeMillis()
)

data class StatusItem(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val type: StatusType = StatusType.TEXT,
    val caption: String = "",
    val mediaUrl: String = "",
    val backgroundColorHex: String = "#6366F1",
    val voiceDurationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAtTimestamp: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val isViewedByMe: Boolean = false,
    val viewers: List<StatusViewer> = emptyList(),
    val isMyStatus: Boolean = false
)

data class UserStatusGroup(
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val isMyStatus: Boolean = false,
    val statuses: List<StatusItem> = emptyList(),
    val hasUnseen: Boolean = false
)
