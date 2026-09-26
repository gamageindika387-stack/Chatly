package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class PulseRepository(
    private val dao: PulseDao,
    private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    init {
        appScope.launch {
            seedInitialDataIfEmpty()
        }
    }

    // --- Current user session ---
    var currentUser: User = User(
        id = "me_user_001",
        phoneNumber = "+1 (555) 948-2026",
        username = "alex_vance",
        displayName = "Alex Vance",
        avatarUrl = "",
        about = "Exploring digital frontiers ✨ Pulse active"
    )

    // --- Users ---
    val allUsers: Flow<List<User>> = dao.getAllUsers().map { entities ->
        entities.map { it.toDomain() }
    }

    val blockedUsers: Flow<List<User>> = dao.getBlockedUsers().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun toggleBlockUser(userId: String, isBlocked: Boolean) {
        dao.updateUserBlock(userId, isBlocked)
    }

    // --- Chats ---
    val allChats: Flow<List<Chat>> = dao.getAllChats().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun togglePinChat(chatId: String, current: Boolean) {
        dao.updateChatPin(chatId, !current)
    }

    suspend fun toggleArchiveChat(chatId: String, current: Boolean) {
        dao.updateChatArchive(chatId, !current)
    }

    suspend fun toggleMuteChat(chatId: String, current: Boolean) {
        dao.updateChatMute(chatId, !current)
    }

    suspend fun deleteChat(chatId: String) {
        dao.deleteChatById(chatId)
    }

    suspend fun createDirectChat(contact: User): String {
        val chatId = "chat_${contact.id}"
        val existing = dao.getChatById(chatId)
        if (existing == null) {
            val newChat = ChatEntity(
                id = chatId,
                type = ChatType.DIRECT.name,
                title = contact.displayName,
                avatarUrl = contact.avatarUrl,
                isPinned = false,
                isArchived = false,
                isMuted = false,
                unreadCount = 0,
                lastMessagePreview = "Chat started",
                lastMessageTimestamp = System.currentTimeMillis(),
                isOnline = contact.isOnline,
                lastSeenText = if (contact.isOnline) "online" else "last seen recently",
                participantIds = "${currentUser.id},${contact.id}"
            )
            dao.insertChat(newChat)
        }
        return chatId
    }

    suspend fun createGroupChat(name: String, description: String, memberIds: List<String>): String {
        val groupId = "group_${UUID.randomUUID().toString().take(8)}"
        val membersStr = (listOf(currentUser.id) + memberIds).joinToString(",")
        val groupEntity = ChatEntity(
            id = groupId,
            type = ChatType.GROUP.name,
            title = name,
            avatarUrl = "",
            isPinned = false,
            isArchived = false,
            isMuted = false,
            unreadCount = 0,
            lastMessagePreview = "$name group created",
            lastMessageTimestamp = System.currentTimeMillis(),
            isOnline = false,
            lastSeenText = "${memberIds.size + 1} members",
            participantIds = membersStr
        )
        dao.insertChat(groupEntity)

        val welcomeMsg = MessageEntity(
            id = "msg_${UUID.randomUUID()}",
            chatId = groupId,
            senderId = currentUser.id,
            senderName = currentUser.displayName,
            senderAvatar = currentUser.avatarUrl,
            type = MessageType.TEXT.name,
            content = "Welcome everyone to $name! Description: $description",
            mediaUrl = "",
            mediaFileName = "",
            mediaFileSize = "",
            voiceDurationSec = 0,
            waveformStr = "",
            locationLatitude = 0.0,
            locationLongitude = 0.0,
            locationTitle = "",
            contactName = "",
            contactPhone = "",
            replyToId = null,
            replyToSender = null,
            replyToContent = null,
            status = MessageStatus.SENT.name,
            timestamp = System.currentTimeMillis(),
            isEdited = false,
            isDeleted = false,
            reactionsStr = ""
        )
        dao.insertMessage(welcomeMsg)
        return groupId
    }

    // --- Messages ---
    fun getMessagesForChat(chatId: String): Flow<List<Message>> {
        return dao.getMessagesForChat(chatId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun sendMessage(
        chatId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        mediaFileName: String = "",
        mediaFileSize: String = "",
        voiceDurationSec: Int = 0,
        voiceWaveform: List<Float> = emptyList(),
        replyToId: String? = null,
        replyToSender: String? = null,
        replyToContent: String? = null,
        contactName: String = "",
        contactPhone: String = "",
        locationTitle: String = "",
        lat: Double = 0.0,
        lon: Double = 0.0
    ): String {
        val msgId = "msg_${UUID.randomUUID()}"
        val waveformStr = voiceWaveform.joinToString(",")

        val preview = when (type) {
            MessageType.TEXT -> content
            MessageType.EMOJI -> content
            MessageType.GIF -> "GIF"
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.DOCUMENT -> "📄 $mediaFileName"
            MessageType.VOICE -> "🎤 Voice message ($voiceDurationSec s)"
            MessageType.CONTACT -> "👤 Contact: $contactName"
            MessageType.LOCATION -> "📍 Location: $locationTitle"
        }

        val entity = MessageEntity(
            id = msgId,
            chatId = chatId,
            senderId = currentUser.id,
            senderName = currentUser.displayName,
            senderAvatar = currentUser.avatarUrl,
            type = type.name,
            content = content,
            mediaUrl = mediaUrl,
            mediaFileName = mediaFileName,
            mediaFileSize = mediaFileSize,
            voiceDurationSec = voiceDurationSec,
            waveformStr = waveformStr,
            locationLatitude = lat,
            locationLongitude = lon,
            locationTitle = locationTitle,
            contactName = contactName,
            contactPhone = contactPhone,
            replyToId = replyToId,
            replyToSender = replyToSender,
            replyToContent = replyToContent,
            status = MessageStatus.SENT.name,
            timestamp = System.currentTimeMillis(),
            isEdited = false,
            isDeleted = false,
            reactionsStr = ""
        )
        dao.insertMessage(entity)
        dao.updateChatLastMessage(chatId, preview, System.currentTimeMillis())

        return msgId
    }

    suspend fun insertIncomingMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        voiceDurationSec: Int = 0,
        voiceWaveform: List<Float> = emptyList()
    ) {
        val msgId = "msg_${UUID.randomUUID()}"
        val preview = if (type == MessageType.VOICE) "🎤 Voice message ($voiceDurationSec s)" else content
        val entity = MessageEntity(
            id = msgId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderAvatar = "",
            type = type.name,
            content = content,
            mediaUrl = "",
            mediaFileName = "",
            mediaFileSize = "",
            voiceDurationSec = voiceDurationSec,
            waveformStr = voiceWaveform.joinToString(","),
            locationLatitude = 0.0,
            locationLongitude = 0.0,
            locationTitle = "",
            contactName = "",
            contactPhone = "",
            replyToId = null,
            replyToSender = null,
            replyToContent = null,
            status = MessageStatus.READ.name,
            timestamp = System.currentTimeMillis(),
            isEdited = false,
            isDeleted = false,
            reactionsStr = ""
        )
        dao.insertMessage(entity)
        dao.updateChatLastMessage(chatId, preview, System.currentTimeMillis(), unreadCount = 0)
    }

    suspend fun deleteMessage(messageId: String) {
        dao.deleteMessageById(messageId)
    }

    suspend fun editMessage(messageId: String, newContent: String) {
        dao.editMessageContent(messageId, newContent)
    }

    suspend fun toggleReaction(messageId: String, emoji: String, currentReactions: List<MessageReaction>) {
        val existingIndex = currentReactions.indexOfFirst { it.userId == currentUser.id && it.emoji == emoji }
        val updated = if (existingIndex >= 0) {
            currentReactions.filterIndexed { index, _ -> index != existingIndex }
        } else {
            currentReactions + MessageReaction(emoji, currentUser.id, currentUser.displayName)
        }
        val serialized = updated.joinToString(";;") { "${it.emoji}|${it.userId}|${it.userName}" }
        dao.updateMessageReactions(messageId, serialized)
    }

    // --- Statuses ---
    val allStatuses: Flow<List<StatusItem>> = dao.getAllStatuses().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun postStatus(caption: String, type: StatusType, bgHex: String = "#6366F1", voiceSec: Int = 0) {
        val statusId = "status_${UUID.randomUUID()}"
        val entity = StatusEntity(
            id = statusId,
            userId = currentUser.id,
            userName = currentUser.displayName,
            userAvatar = currentUser.avatarUrl,
            type = type.name,
            caption = caption,
            mediaUrl = "",
            backgroundColorHex = bgHex,
            voiceDurationSec = voiceSec,
            timestamp = System.currentTimeMillis(),
            expiresAtTimestamp = System.currentTimeMillis() + 24 * 3600 * 1000L,
            isViewedByMe = true,
            viewersStr = "",
            isMyStatus = true
        )
        dao.insertStatus(entity)
    }

    suspend fun markStatusViewed(statusId: String) {
        dao.markStatusViewed(statusId)
    }

    // --- Channels ---
    val allChannels: Flow<List<Channel>> = dao.getAllChannels().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun toggleFollowChannel(channelId: String, current: Boolean) {
        dao.updateChannelFollow(channelId, !current)
    }

    suspend fun createChannel(name: String, handle: String, description: String, category: String) {
        val channelId = "ch_${UUID.randomUUID().toString().take(8)}"
        val entity = ChannelEntity(
            id = channelId,
            name = name,
            handle = if (handle.startsWith("@")) handle else "@$handle",
            description = description,
            avatarUrl = "",
            bannerUrl = "",
            followerCount = 1,
            isFollowedByMe = true,
            isOwnerOrAdmin = true,
            category = category
        )
        dao.insertChannel(entity)
    }

    // --- Calls ---
    val allCalls: Flow<List<CallRecord>> = dao.getAllCalls().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun recordCall(
        peerId: String,
        peerName: String,
        peerAvatar: String,
        type: CallType,
        direction: CallDirection,
        status: CallStatus,
        durationSec: Int
    ) {
        val entity = CallEntity(
            id = "call_${UUID.randomUUID()}",
            peerId = peerId,
            peerName = peerName,
            peerAvatar = peerAvatar,
            type = type.name,
            direction = direction.name,
            status = status.name,
            durationSec = durationSec,
            timestamp = System.currentTimeMillis()
        )
        dao.insertCall(entity)
    }

    suspend fun deleteCall(callId: String) {
        dao.deleteCall(callId)
    }

    // --- Data Seed ---
    private suspend fun seedInitialDataIfEmpty() {
        val existingChats = dao.getAllChats().first()
        if (existingChats.isNotEmpty()) return

        // 1. Seed Contacts
        val users = listOf(
            UserEntity("u_elena", "+1 555-0101", "elena_rostova", "Elena Rostova", "", "Capturing moments & architecture 🏛️", true, System.currentTimeMillis(), false),
            UserEntity("u_liam", "+1 555-0102", "liam_chen", "Liam Chen", "", "Building real-time distributed systems 🚀", true, System.currentTimeMillis(), false),
            UserEntity("u_maya", "+1 555-0103", "maya_patel", "Maya Patel", "", "Product designer & sound explorer 🎧", false, System.currentTimeMillis() - 15 * 60 * 1000L, false),
            UserEntity("u_sophia", "+1 555-0104", "sophia_rossi", "Sophia Rossi", "", "Coffee, code, and cinema ☕", true, System.currentTimeMillis(), false),
            UserEntity("u_marcus", "+1 555-0105", "marcus_j", "Marcus Johnson", "", "Never not creating 🎨", false, System.currentTimeMillis() - 3600 * 1000L, false),
            UserEntity("u_zara", "+1 555-0106", "zara_almansoor", "Zara Al-Mansoor", "", "Cybersecurity & digital rights 🔒", true, System.currentTimeMillis(), false)
        )
        dao.insertUsers(users)

        // 2. Seed Chats
        val chats = listOf(
            ChatEntity(
                id = "chat_u_elena",
                type = ChatType.DIRECT.name,
                title = "Elena Rostova",
                avatarUrl = "",
                isPinned = true,
                isArchived = false,
                isMuted = false,
                unreadCount = 2,
                lastMessagePreview = "Have you tested the new audio waveform visualizer yet?",
                lastMessageTimestamp = System.currentTimeMillis() - 2 * 60 * 1000L,
                isOnline = true,
                lastSeenText = "online",
                participantIds = "${currentUser.id},u_elena"
            ),
            ChatEntity(
                id = "chat_group_pulse_devs",
                type = ChatType.GROUP.name,
                title = "Pulse Core Engineering",
                avatarUrl = "",
                isPinned = true,
                isArchived = false,
                isMuted = false,
                unreadCount = 0,
                lastMessagePreview = "Liam: WebRTC call signaling latency is down to 42ms!",
                lastMessageTimestamp = System.currentTimeMillis() - 12 * 60 * 1000L,
                isOnline = false,
                lastSeenText = "5 members",
                participantIds = "${currentUser.id},u_elena,u_liam,u_maya,u_sophia"
            ),
            ChatEntity(
                id = "chat_u_liam",
                type = ChatType.DIRECT.name,
                title = "Liam Chen",
                avatarUrl = "",
                isPinned = false,
                isArchived = false,
                isMuted = false,
                unreadCount = 0,
                lastMessagePreview = "🎤 Voice message (14 s)",
                lastMessageTimestamp = System.currentTimeMillis() - 45 * 60 * 1000L,
                isOnline = true,
                lastSeenText = "online",
                participantIds = "${currentUser.id},u_liam"
            ),
            ChatEntity(
                id = "chat_u_maya",
                type = ChatType.DIRECT.name,
                title = "Maya Patel",
                avatarUrl = "",
                isPinned = false,
                isArchived = false,
                isMuted = true,
                unreadCount = 0,
                lastMessagePreview = "Sending you the new UI color tokens draft 🎨",
                lastMessageTimestamp = System.currentTimeMillis() - 3 * 3600 * 1000L,
                isOnline = false,
                lastSeenText = "last seen 3 hours ago",
                participantIds = "${currentUser.id},u_maya"
            ),
            ChatEntity(
                id = "chat_u_sophia",
                type = ChatType.DIRECT.name,
                title = "Sophia Rossi",
                avatarUrl = "",
                isPinned = false,
                isArchived = false,
                isMuted = false,
                unreadCount = 0,
                lastMessagePreview = "Let's hop on a quick video call later today.",
                lastMessageTimestamp = System.currentTimeMillis() - 6 * 3600 * 1000L,
                isOnline = true,
                lastSeenText = "online",
                participantIds = "${currentUser.id},u_sophia"
            )
        )
        dao.insertChats(chats)

        // 3. Seed Messages for Elena
        val dummyWaveform = listOf(0.2f, 0.4f, 0.8f, 0.5f, 0.9f, 0.6f, 0.3f, 0.7f, 1.0f, 0.6f, 0.4f, 0.8f, 0.3f)
        val elenaMessages = listOf(
            MessageEntity(
                id = "msg_e_1",
                chatId = "chat_u_elena",
                senderId = "u_elena",
                senderName = "Elena Rostova",
                senderAvatar = "",
                type = MessageType.TEXT.name,
                content = "Hey Alex! Check out the modern deep blue and violet theme we deployed.",
                mediaUrl = "",
                mediaFileName = "",
                mediaFileSize = "",
                voiceDurationSec = 0,
                waveformStr = "",
                locationLatitude = 0.0,
                locationLongitude = 0.0,
                locationTitle = "",
                contactName = "",
                contactPhone = "",
                replyToId = null,
                replyToSender = null,
                replyToContent = null,
                status = MessageStatus.READ.name,
                timestamp = System.currentTimeMillis() - 30 * 60 * 1000L,
                isEdited = false,
                isDeleted = false,
                reactionsStr = "🔥|me_user_001|Alex Vance"
            ),
            MessageEntity(
                id = "msg_e_2",
                chatId = "chat_u_elena",
                senderId = currentUser.id,
                senderName = currentUser.displayName,
                senderAvatar = "",
                type = MessageType.TEXT.name,
                content = "It looks incredible! Smooth animations and clean rounded components.",
                mediaUrl = "",
                mediaFileName = "",
                mediaFileSize = "",
                voiceDurationSec = 0,
                waveformStr = "",
                locationLatitude = 0.0,
                locationLongitude = 0.0,
                locationTitle = "",
                contactName = "",
                contactPhone = "",
                replyToId = "msg_e_1",
                replyToSender = "Elena Rostova",
                replyToContent = "Hey Alex! Check out the modern deep blue and violet theme...",
                status = MessageStatus.READ.name,
                timestamp = System.currentTimeMillis() - 25 * 60 * 1000L,
                isEdited = false,
                isDeleted = false,
                reactionsStr = "❤️|u_elena|Elena Rostova"
            ),
            MessageEntity(
                id = "msg_e_3",
                chatId = "chat_u_elena",
                senderId = "u_elena",
                senderName = "Elena Rostova",
                senderAvatar = "",
                type = MessageType.VOICE.name,
                content = "Voice note from Elena",
                mediaUrl = "",
                mediaFileName = "voice_note.m4a",
                mediaFileSize = "142 KB",
                voiceDurationSec = 8,
                waveformStr = dummyWaveform.joinToString(","),
                locationLatitude = 0.0,
                locationLongitude = 0.0,
                locationTitle = "",
                contactName = "",
                contactPhone = "",
                replyToId = null,
                replyToSender = null,
                replyToContent = null,
                status = MessageStatus.READ.name,
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000L,
                isEdited = false,
                isDeleted = false,
                reactionsStr = ""
            ),
            MessageEntity(
                id = "msg_e_4",
                chatId = "chat_u_elena",
                senderId = "u_elena",
                senderName = "Elena Rostova",
                senderAvatar = "",
                type = MessageType.TEXT.name,
                content = "Have you tested the new audio waveform visualizer yet?",
                mediaUrl = "",
                mediaFileName = "",
                mediaFileSize = "",
                voiceDurationSec = 0,
                waveformStr = "",
                locationLatitude = 0.0,
                locationLongitude = 0.0,
                locationTitle = "",
                contactName = "",
                contactPhone = "",
                replyToId = null,
                replyToSender = null,
                replyToContent = null,
                status = MessageStatus.DELIVERED.name,
                timestamp = System.currentTimeMillis() - 2 * 60 * 1000L,
                isEdited = false,
                isDeleted = false,
                reactionsStr = ""
            )
        )
        dao.insertMessages(elenaMessages)

        // 4. Seed Statuses
        val statuses = listOf(
            StatusEntity(
                id = "st_elena_1",
                userId = "u_elena",
                userName = "Elena Rostova",
                userAvatar = "",
                type = StatusType.TEXT.name,
                caption = "Sunset coding session by the bay 🌅 💻",
                mediaUrl = "",
                backgroundColorHex = "#6366F1",
                voiceDurationSec = 0,
                timestamp = System.currentTimeMillis() - 2 * 3600 * 1000L,
                expiresAtTimestamp = System.currentTimeMillis() + 22 * 3600 * 1000L,
                isViewedByMe = false,
                viewersStr = "Sophia Rossi,Liam Chen",
                isMyStatus = false
            ),
            StatusEntity(
                id = "st_liam_1",
                userId = "u_liam",
                userName = "Liam Chen",
                userAvatar = "",
                type = StatusType.VOICE.name,
                caption = "Quick audio update on our 5G WebRTC test 🎙️",
                mediaUrl = "",
                backgroundColorHex = "#0284C7",
                voiceDurationSec = 12,
                timestamp = System.currentTimeMillis() - 5 * 3600 * 1000L,
                expiresAtTimestamp = System.currentTimeMillis() + 19 * 3600 * 1000L,
                isViewedByMe = false,
                viewersStr = "Elena Rostova",
                isMyStatus = false
            ),
            StatusEntity(
                id = "st_maya_1",
                userId = "u_maya",
                userName = "Maya Patel",
                userAvatar = "",
                type = StatusType.TEXT.name,
                caption = "New sound kit dropped for messaging notifications 🎵✨",
                mediaUrl = "",
                backgroundColorHex = "#D946EF",
                voiceDurationSec = 0,
                timestamp = System.currentTimeMillis() - 8 * 3600 * 1000L,
                expiresAtTimestamp = System.currentTimeMillis() + 16 * 3600 * 1000L,
                isViewedByMe = true,
                viewersStr = "Alex Vance,Elena Rostova,Liam Chen",
                isMyStatus = false
            )
        )
        dao.insertStatuses(statuses)

        // 5. Seed Channels
        val channels = listOf(
            ChannelEntity(
                id = "ch_tech_radar",
                name = "Pulse Tech Radar",
                handle = "@techradar",
                description = "Daily curated updates on AI, mobile software, real-time protocols, and distributed computing.",
                avatarUrl = "",
                bannerUrl = "",
                followerCount = 48200,
                isFollowedByMe = true,
                isOwnerOrAdmin = false,
                category = "Technology"
            ),
            ChannelEntity(
                id = "ch_sound_design",
                name = "Acoustic & Audio Labs",
                handle = "@soundlabs",
                description = "Exploring spatial audio, codecs, opus compression, and modern voice communications.",
                avatarUrl = "",
                bannerUrl = "",
                followerCount = 19400,
                isFollowedByMe = true,
                isOwnerOrAdmin = false,
                category = "Media"
            ),
            ChannelEntity(
                id = "ch_design_system",
                name = "Modern UI Systems",
                handle = "@modernui",
                description = "Inspiration, subtle micro-interactions, dark mode palettes, and design tokens.",
                avatarUrl = "",
                bannerUrl = "",
                followerCount = 67300,
                isFollowedByMe = false,
                isOwnerOrAdmin = false,
                category = "Design"
            ),
            ChannelEntity(
                id = "ch_my_creations",
                name = "Alex Vance Insights",
                handle = "@alexinsights",
                description = "Personal broadcasts, tech tips, open source projects, and deep dives.",
                avatarUrl = "",
                bannerUrl = "",
                followerCount = 1240,
                isFollowedByMe = true,
                isOwnerOrAdmin = true,
                category = "Personal"
            )
        )
        dao.insertChannels(channels)

        // 6. Seed Calls
        val calls = listOf(
            CallEntity(
                id = "call_1",
                peerId = "u_elena",
                peerName = "Elena Rostova",
                peerAvatar = "",
                type = CallType.VIDEO.name,
                direction = CallDirection.INCOMING.name,
                status = CallStatus.COMPLETED.name,
                durationSec = 432,
                timestamp = System.currentTimeMillis() - 40 * 60 * 1000L
            ),
            CallEntity(
                id = "call_2",
                peerId = "u_liam",
                peerName = "Liam Chen",
                peerAvatar = "",
                type = CallType.VOICE.name,
                direction = CallDirection.OUTGOING.name,
                status = CallStatus.COMPLETED.name,
                durationSec = 185,
                timestamp = System.currentTimeMillis() - 3 * 3600 * 1000L
            ),
            CallEntity(
                id = "call_3",
                peerId = "u_sophia",
                peerName = "Sophia Rossi",
                peerAvatar = "",
                type = CallType.VOICE.name,
                direction = CallDirection.INCOMING.name,
                status = CallStatus.MISSED.name,
                durationSec = 0,
                timestamp = System.currentTimeMillis() - 8 * 3600 * 1000L
            )
        )
        dao.insertCalls(calls)
    }
}

// Converters
private fun UserEntity.toDomain() = User(
    id = id,
    phoneNumber = phoneNumber,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    about = about,
    isOnline = isOnline,
    lastSeenTimestamp = lastSeenTimestamp,
    isBlocked = isBlocked
)

private fun ChatEntity.toDomain() = Chat(
    id = id,
    type = try { ChatType.valueOf(type) } catch (e: Exception) { ChatType.DIRECT },
    title = title,
    avatarUrl = avatarUrl,
    isPinned = isPinned,
    isArchived = isArchived,
    isMuted = isMuted,
    unreadCount = unreadCount,
    lastMessagePreview = lastMessagePreview,
    lastMessageTimestamp = lastMessageTimestamp,
    isOnline = isOnline,
    lastSeenText = lastSeenText,
    isTyping = false,
    participantIds = participantIds.split(",").filter { it.isNotBlank() }
)

private fun MessageEntity.toDomain(): Message {
    val parsedReactions = if (reactionsStr.isBlank()) emptyList() else {
        reactionsStr.split(";;").mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.size >= 3) {
                MessageReaction(tokens[0], tokens[1], tokens[2])
            } else null
        }
    }
    val waveform = if (waveformStr.isBlank()) emptyList() else {
        waveformStr.split(",").mapNotNull { it.toFloatOrNull() }
    }

    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        senderAvatar = senderAvatar,
        type = try { MessageType.valueOf(type) } catch (e: Exception) { MessageType.TEXT },
        content = content,
        mediaUrl = mediaUrl,
        mediaFileName = mediaFileName,
        mediaFileSize = mediaFileSize,
        voiceDurationSec = voiceDurationSec,
        voiceWaveform = waveform,
        locationLatitude = locationLatitude,
        locationLongitude = locationLongitude,
        locationTitle = locationTitle,
        contactName = contactName,
        contactPhone = contactPhone,
        replyToMessageId = replyToId,
        replyToSenderName = replyToSender,
        replyToContent = replyToContent,
        status = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.SENT },
        timestamp = timestamp,
        isEdited = isEdited,
        isDeleted = isDeleted,
        reactions = parsedReactions
    )
}

private fun StatusEntity.toDomain(): StatusItem {
    val viewers = if (viewersStr.isBlank()) emptyList() else {
        viewersStr.split(",").map { name ->
            StatusViewer(
                userId = "v_$name",
                userName = name.trim(),
                avatarUrl = "",
                viewedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 10
            )
        }
    }
    return StatusItem(
        id = id,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        type = try { StatusType.valueOf(type) } catch (e: Exception) { StatusType.TEXT },
        caption = caption,
        mediaUrl = mediaUrl,
        backgroundColorHex = backgroundColorHex,
        voiceDurationSec = voiceDurationSec,
        timestamp = timestamp,
        expiresAtTimestamp = expiresAtTimestamp,
        isViewedByMe = isViewedByMe,
        viewers = viewers,
        isMyStatus = isMyStatus
    )
}

private fun ChannelEntity.toDomain() = Channel(
    id = id,
    name = name,
    handle = handle,
    description = description,
    avatarUrl = avatarUrl,
    bannerUrl = bannerUrl,
    followerCount = followerCount,
    isFollowedByMe = isFollowedByMe,
    isOwnerOrAdmin = isOwnerOrAdmin,
    category = category,
    posts = listOf(
        ChannelPost(
            id = "post_${id}_1",
            channelId = id,
            type = ChannelPostType.TEXT,
            content = "Excited to announce our latest milestone! Real-time end-to-end messaging with sub-50ms latency across 12 worldwide edge nodes.",
            timestamp = System.currentTimeMillis() - 3600 * 1000L,
            viewCount = followerCount / 3
        ),
        ChannelPost(
            id = "post_${id}_2",
            channelId = id,
            type = ChannelPostType.POLL,
            content = "Community question: What is your favorite communication feature?",
            pollQuestion = "Which feature do you use most frequently?",
            pollOptions = listOf(
                PollOption("po1", "Voice Messages with waveforms", 68, true),
                PollOption("po2", "HD Video Calls", 42, false),
                PollOption("po3", "Broadcast Channels", 31, false),
                PollOption("po4", "24-Hour Stories / Status", 25, false)
            ),
            totalVotes = 166,
            viewCount = followerCount / 2,
            timestamp = System.currentTimeMillis() - 5 * 3600 * 1000L
        )
    )
)

private fun CallEntity.toDomain() = CallRecord(
    id = id,
    peerId = peerId,
    peerName = peerName,
    peerAvatar = peerAvatar,
    type = try { CallType.valueOf(type) } catch (e: Exception) { CallType.VOICE },
    direction = try { CallDirection.valueOf(direction) } catch (e: Exception) { CallDirection.OUTGOING },
    status = try { CallStatus.valueOf(status) } catch (e: Exception) { CallStatus.COMPLETED },
    durationSec = durationSec,
    timestamp = timestamp
)
