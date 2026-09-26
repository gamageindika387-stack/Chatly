package com.example.data.model

data class UserReport(
    val id: String,
    val reporterId: String,
    val reporterName: String,
    val reportedUserId: String,
    val reportedUserName: String,
    val reason: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.PENDING
)

enum class ReportStatus {
    PENDING,
    RESOLVED,
    DISMISSED
}

data class SystemAnalytics(
    val activeUsersToday: Int = 18450,
    val messagesSentToday: Int = 328900,
    val callsCompletedToday: Int = 14200,
    val totalStorageUsedGb: Double = 642.8,
    val activeWebSocketConnections: Int = 12940,
    val serverHealthPercent: Int = 99
)
