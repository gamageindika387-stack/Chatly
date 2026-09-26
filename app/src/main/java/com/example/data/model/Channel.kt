package com.example.data.model

enum class ChannelPostType {
    TEXT,
    IMAGE,
    VIDEO,
    VOICE,
    POLL
}

data class PollOption(
    val id: String,
    val text: String,
    val voteCount: Int = 0,
    val isVotedByMe: Boolean = false
)

data class ChannelPost(
    val id: String,
    val channelId: String,
    val type: ChannelPostType = ChannelPostType.TEXT,
    val content: String,
    val mediaUrl: String = "",
    val mediaDescription: String = "",
    val voiceDurationSec: Int = 0,
    val pollQuestion: String = "",
    val pollOptions: List<PollOption> = emptyList(),
    val totalVotes: Int = 0,
    val viewCount: Int = 120,
    val timestamp: Long = System.currentTimeMillis(),
    val reactionsCount: Map<String, Int> = mapOf("❤️" to 42, "🔥" to 28, "👏" to 15),
    val myReaction: String? = null
)

data class Channel(
    val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val avatarUrl: String = "",
    val bannerUrl: String = "",
    val followerCount: Int = 1420,
    val isFollowedByMe: Boolean = false,
    val isOwnerOrAdmin: Boolean = false,
    val category: String = "Technology",
    val posts: List<ChannelPost> = emptyList()
)
