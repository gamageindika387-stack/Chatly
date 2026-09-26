package com.example.data.model

enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER
}

data class GroupMember(
    val userId: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val joinedTimestamp: Long = System.currentTimeMillis()
)

data class GroupPermissions(
    val sendMessages: Boolean = true,
    val sendMedia: Boolean = true,
    val addOtherMembers: Boolean = false,
    val pinMessages: Boolean = false,
    val changeChatInfo: Boolean = false
)

data class GroupInfo(
    val id: String,
    val name: String,
    val description: String,
    val avatarUrl: String = "",
    val inviteCode: String = "pulse.me/join/grp_",
    val members: List<GroupMember> = emptyList(),
    val permissions: GroupPermissions = GroupPermissions(),
    val isMuted: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
