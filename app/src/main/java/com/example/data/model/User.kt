package com.example.data.model

data class User(
    val id: String,
    val phoneNumber: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val about: String = "Hey there! I am using Pulse.",
    val isOnline: Boolean = false,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false
)

data class AuthSession(
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val authToken: String = ""
)

data class UserPrivacySettings(
    val lastSeenPrivacy: PrivacyAudience = PrivacyAudience.EVERYONE,
    val profilePhotoPrivacy: PrivacyAudience = PrivacyAudience.EVERYONE,
    val statusPrivacy: PrivacyAudience = PrivacyAudience.MY_CONTACTS,
    val readReceipts: Boolean = true,
    val onlineStatusPrivacy: PrivacyAudience = PrivacyAudience.EVERYONE
)

enum class PrivacyAudience {
    EVERYONE,
    MY_CONTACTS,
    NOBODY
}
