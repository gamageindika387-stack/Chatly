package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PulseDatabase
import com.example.data.model.*
import com.example.data.repository.PulseRepository
import com.example.service.AudioVoiceService
import com.example.service.RealtimeService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainNavigationTab {
    CHATS,
    STATUS,
    CHANNELS,
    CALLS,
    SETTINGS
}

data class AuthUiState(
    val isAuthenticated: Boolean = true, // default to logged in with seed account, user can log out / switch
    val phoneNumber: String = "+1 555-948-2026",
    val otpCode: String = "",
    val isOtpSent: Boolean = false,
    val username: String = "alex_vance",
    val displayName: String = "Alex Vance",
    val bio: String = "Exploring digital frontiers ✨ Pulse active",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PulseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PulseDatabase.getDatabase(application)
    val repository = PulseRepository(db.pulseDao(), viewModelScope)
    val realtimeService = RealtimeService(repository, viewModelScope)
    val audioService = AudioVoiceService(viewModelScope)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(MainNavigationTab.CHATS)
    val currentTab: StateFlow<MainNavigationTab> = _currentTab.asStateFlow()

    fun selectTab(tab: MainNavigationTab) {
        _currentTab.value = tab
    }

    // Auth State
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun updateAuthPhone(phone: String) { _authState.value = _authState.value.copy(phoneNumber = phone, errorMessage = null) }
    fun updateAuthOtp(otp: String) { _authState.value = _authState.value.copy(otpCode = otp, errorMessage = null) }
    fun updateAuthName(name: String) { _authState.value = _authState.value.copy(displayName = name) }
    fun updateAuthUsername(user: String) { _authState.value = _authState.value.copy(username = user) }
    fun updateAuthBio(bio: String) { _authState.value = _authState.value.copy(bio = bio) }

    fun sendOtp() {
        if (_authState.value.phoneNumber.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Please enter your phone number")
            return
        }
        _authState.value = _authState.value.copy(isOtpSent = true, errorMessage = null)
    }

    fun verifyOtp() {
        if (_authState.value.otpCode.length < 4 && _authState.value.otpCode != "1234") {
            _authState.value = _authState.value.copy(errorMessage = "Invalid verification code. Use 1234 or any 4 digits.")
            return
        }
        _authState.value = _authState.value.copy(isAuthenticated = true, isOtpSent = false, errorMessage = null)
    }

    fun logout() {
        _authState.value = _authState.value.copy(isAuthenticated = false, isOtpSent = false, otpCode = "")
    }

    fun deleteAccount() {
        viewModelScope.launch {
            // Clear or reset user
            _authState.value = AuthUiState(isAuthenticated = false)
        }
    }

    // Global Search & Filter
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    // Chats
    val allChats: StateFlow<List<Chat>> = repository.allChats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredChats: StateFlow<List<Chat>> = combine(allChats, _globalSearchQuery) { chats, query ->
        if (query.isBlank()) chats
        else chats.filter { it.title.contains(query, ignoreCase = true) || it.lastMessagePreview.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat State
    private val _activeChat = MutableStateFlow<Chat?>(null)
    val activeChat: StateFlow<Chat?> = _activeChat.asStateFlow()

    fun openChat(chat: Chat) {
        _activeChat.value = chat
    }

    fun closeChat() {
        _activeChat.value = null
    }

    val activeChatMessages: StateFlow<List<Message>> = _activeChat.flatMapLatest { chat ->
        if (chat == null) flowOf(emptyList())
        else repository.getMessagesForChat(chat.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message Input & Actions
    private val _messageInputText = MutableStateFlow("")
    val messageInputText: StateFlow<String> = _messageInputText.asStateFlow()

    fun updateMessageInput(text: String) {
        _messageInputText.value = text
    }

    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()

    fun setReplyingTo(msg: Message?) {
        _replyingToMessage.value = msg
    }

    private val _editingMessage = MutableStateFlow<Message?>(null)
    val editingMessage: StateFlow<Message?> = _editingMessage.asStateFlow()

    fun setEditing(msg: Message?) {
        _editingMessage.value = msg
        _messageInputText.value = msg?.content ?: ""
    }

    fun sendTextMessage() {
        val chat = _activeChat.value ?: return
        val text = _messageInputText.value.trim()
        if (text.isBlank()) return

        val editing = _editingMessage.value
        if (editing != null) {
            viewModelScope.launch {
                repository.editMessage(editing.id, text)
                _editingMessage.value = null
                _messageInputText.value = ""
            }
            return
        }

        val reply = _replyingToMessage.value
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chat.id,
                content = text,
                type = MessageType.TEXT,
                replyToId = reply?.id,
                replyToSender = reply?.senderName,
                replyToContent = reply?.content
            )
            _messageInputText.value = ""
            _replyingToMessage.value = null

            // Notify real-time service for simulated response
            realtimeService.onUserSendMessage(chat.id, text)
        }
    }

    fun sendVoiceMessage(durationSec: Int, waveform: List<Float>) {
        val chat = _activeChat.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chat.id,
                content = "Voice message",
                type = MessageType.VOICE,
                voiceDurationSec = durationSec,
                voiceWaveform = waveform
            )
            realtimeService.onUserSendMessage(chat.id, "audio message")
        }
    }

    fun sendMediaAttachment(type: MessageType, mediaTitle: String, extraInfo: String = "") {
        val chat = _activeChat.value ?: return
        viewModelScope.launch {
            when (type) {
                MessageType.IMAGE -> {
                    repository.sendMessage(
                        chatId = chat.id,
                        content = "Shared an image",
                        type = MessageType.IMAGE,
                        mediaUrl = "sample_photo",
                        mediaFileName = "photo_${System.currentTimeMillis()}.jpg",
                        mediaFileSize = "2.4 MB"
                    )
                }
                MessageType.LOCATION -> {
                    repository.sendMessage(
                        chatId = chat.id,
                        content = "Shared live location",
                        type = MessageType.LOCATION,
                        locationTitle = if (mediaTitle.isBlank()) "Downtown Tech Hub, 4th Ave" else mediaTitle,
                        lat = 37.7749,
                        lon = -122.4194
                    )
                }
                MessageType.CONTACT -> {
                    repository.sendMessage(
                        chatId = chat.id,
                        content = "Shared contact card",
                        type = MessageType.CONTACT,
                        contactName = if (mediaTitle.isBlank()) "Sarah Connor" else mediaTitle,
                        contactPhone = "+1 (555) 019-8392"
                    )
                }
                MessageType.DOCUMENT -> {
                    repository.sendMessage(
                        chatId = chat.id,
                        content = "Shared project documentation",
                        type = MessageType.DOCUMENT,
                        mediaFileName = if (mediaTitle.isBlank()) "Pulse_Architecture_Spec.pdf" else mediaTitle,
                        mediaFileSize = "4.1 MB"
                    )
                }
                else -> {}
            }
            realtimeService.onUserSendMessage(chat.id, "media")
        }
    }

    fun toggleMessageReaction(message: Message, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(message.id, emoji, message.reactions)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    // Chat management
    fun togglePinChat(chat: Chat) {
        viewModelScope.launch { repository.togglePinChat(chat.id, chat.isPinned) }
    }

    fun toggleArchiveChat(chat: Chat) {
        viewModelScope.launch { repository.toggleArchiveChat(chat.id, chat.isArchived) }
    }

    fun toggleMuteChat(chat: Chat) {
        viewModelScope.launch { repository.toggleMuteChat(chat.id, chat.isMuted) }
    }

    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            repository.deleteChat(chat.id)
            if (_activeChat.value?.id == chat.id) {
                _activeChat.value = null
            }
        }
    }

    fun createGroup(name: String, description: String, memberIds: List<String>) {
        viewModelScope.launch {
            val newGroupId = repository.createGroupChat(name, description, memberIds)
            val created = db.pulseDao().getChatById(newGroupId)
            created?.let {
                // open newly created group
            }
        }
    }

    // Statuses
    val allStatuses: StateFlow<List<StatusItem>> = repository.allStatuses.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _activeStatusStory = MutableStateFlow<StatusItem?>(null)
    val activeStatusStory: StateFlow<StatusItem?> = _activeStatusStory.asStateFlow()

    fun viewStatus(status: StatusItem) {
        _activeStatusStory.value = status
        viewModelScope.launch {
            repository.markStatusViewed(status.id)
        }
    }

    fun closeStatusStory() {
        _activeStatusStory.value = null
    }

    fun postTextStatus(text: String, bgHex: String) {
        viewModelScope.launch {
            repository.postStatus(text, StatusType.TEXT, bgHex)
        }
    }

    fun postVoiceStatus(voiceSec: Int) {
        viewModelScope.launch {
            repository.postStatus("Voice note story", StatusType.VOICE, "#0284C7", voiceSec)
        }
    }

    // Channels
    val allChannels: StateFlow<List<Channel>> = repository.allChannels.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _activeChannel = MutableStateFlow<Channel?>(null)
    val activeChannel: StateFlow<Channel?> = _activeChannel.asStateFlow()

    fun openChannel(channel: Channel) {
        _activeChannel.value = channel
    }

    fun closeChannel() {
        _activeChannel.value = null
    }

    fun toggleFollowChannel(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFollowChannel(channel.id, channel.isFollowedByMe)
        }
    }

    fun createChannel(name: String, handle: String, description: String, category: String) {
        viewModelScope.launch {
            repository.createChannel(name, handle, description, category)
        }
    }

    // Calls
    val allCalls: StateFlow<List<CallRecord>> = repository.allCalls.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun startCall(peerId: String, peerName: String, peerAvatar: String, type: CallType) {
        realtimeService.startOutgoingCall(peerId, peerName, peerAvatar, type)
    }

    fun deleteCallRecord(callId: String) {
        viewModelScope.launch { repository.deleteCall(callId) }
    }

    // Contacts / Users
    val allUsers: StateFlow<List<User>> = repository.allUsers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val blockedUsers: StateFlow<List<User>> = repository.blockedUsers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun startChatWithContact(contact: User) {
        viewModelScope.launch {
            val chatId = repository.createDirectChat(contact)
            val chat = db.pulseDao().getChatById(chatId)
            chat?.let {
                openChat(
                    Chat(
                        id = it.id,
                        type = ChatType.DIRECT,
                        title = it.title,
                        avatarUrl = it.avatarUrl,
                        isPinned = it.isPinned,
                        isArchived = it.isArchived,
                        isMuted = it.isMuted,
                        unreadCount = it.unreadCount,
                        lastMessagePreview = it.lastMessagePreview,
                        lastMessageTimestamp = it.lastMessageTimestamp,
                        isOnline = it.isOnline,
                        lastSeenText = it.lastSeenText
                    )
                )
            }
        }
    }

    fun toggleBlockUser(user: User) {
        viewModelScope.launch {
            repository.toggleBlockUser(user.id, !user.isBlocked)
        }
    }

    // Reports / Moderation
    private val _reportsList = MutableStateFlow<List<UserReport>>(
        listOf(
            UserReport(
                id = "rep_101",
                reporterId = "u_maya",
                reporterName = "Maya Patel",
                reportedUserId = "u_spammer_9",
                reportedUserName = "CryptoBots99",
                reason = "Spam / Automated Promotions",
                details = "Posting unsolicited crypto links in public channels"
            ),
            UserReport(
                id = "rep_102",
                reporterId = "u_sophia",
                reporterName = "Sophia Rossi",
                reportedUserId = "u_troll_4",
                reportedUserName = "NightCrawlerX",
                reason = "Harassment / Abusive language",
                details = "Continuous offensive messages in group chat"
            )
        )
    )
    val reportsList: StateFlow<List<UserReport>> = _reportsList.asStateFlow()

    fun resolveReport(reportId: String) {
        _reportsList.value = _reportsList.value.map {
            if (it.id == reportId) it.copy(status = ReportStatus.RESOLVED) else it
        }
    }

    fun dismissReport(reportId: String) {
        _reportsList.value = _reportsList.value.map {
            if (it.id == reportId) it.copy(status = ReportStatus.DISMISSED) else it
        }
    }

    // Admin Dashboard View State
    private val _isAdminOpen = MutableStateFlow(false)
    val isAdminOpen: StateFlow<Boolean> = _isAdminOpen.asStateFlow()

    fun openAdminDashboard() { _isAdminOpen.value = true }
    fun closeAdminDashboard() { _isAdminOpen.value = false }

    // Privacy Settings
    private val _privacySettings = MutableStateFlow(UserPrivacySettings())
    val privacySettings: StateFlow<UserPrivacySettings> = _privacySettings.asStateFlow()

    fun updateLastSeenPrivacy(audience: PrivacyAudience) {
        _privacySettings.value = _privacySettings.value.copy(lastSeenPrivacy = audience)
    }

    fun updateProfilePhotoPrivacy(audience: PrivacyAudience) {
        _privacySettings.value = _privacySettings.value.copy(profilePhotoPrivacy = audience)
    }

    fun toggleReadReceipts() {
        _privacySettings.value = _privacySettings.value.copy(readReceipts = !_privacySettings.value.readReceipts)
    }

    // Theme mode (SYSTEM, LIGHT, DARK)
    private val _isDarkThemeOverride = MutableStateFlow<Boolean?>(null) // null = system
    val isDarkThemeOverride: StateFlow<Boolean?> = _isDarkThemeOverride.asStateFlow()

    fun setThemeMode(dark: Boolean?) {
        _isDarkThemeOverride.value = dark
    }
}
