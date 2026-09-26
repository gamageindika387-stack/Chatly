package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.model.CallType
import com.example.ui.MainNavigationTab
import com.example.ui.PulseViewModel
import com.example.ui.components.IncomingCallDialog
import com.example.ui.components.PulseBottomBar
import com.example.ui.components.PulseTopBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.calls.ActiveCallScreen
import com.example.ui.screens.calls.CallsScreen
import com.example.ui.screens.channels.ChannelsScreen
import com.example.ui.screens.chats.ChatDetailScreen
import com.example.ui.screens.chats.ChatsListScreen
import com.example.ui.screens.contacts.ContactsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.status.StatusScreen

@Composable
fun PulseApp(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val activeChat by viewModel.activeChat.collectAsState()
    val incomingCall by viewModel.realtimeService.incomingCall.collectAsState()
    val activeCall by viewModel.realtimeService.activeCall.collectAsState()
    val isAdminOpen by viewModel.isAdminOpen.collectAsState()
    var isContactsOpen by remember { mutableStateOf(false) }

    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.globalSearchQuery.collectAsState()
    val chats by viewModel.allChats.collectAsState()
    val statuses by viewModel.allStatuses.collectAsState()

    val totalUnread = remember(chats) { chats.sumOf { it.unreadCount } }
    val hasUnseenStatus = remember(statuses) { statuses.any { !it.isMyStatus && !it.isViewedByMe } }

    // 1. If not authenticated, show Auth
    if (!authState.isAuthenticated) {
        AuthScreen(viewModel = viewModel)
        return
    }

    // 2. If Active Call is ongoing, show full-screen Call Interface
    if (activeCall != null) {
        ActiveCallScreen(
            callSession = activeCall!!,
            onToggleMic = { viewModel.realtimeService.toggleMic() },
            onToggleSpeaker = { viewModel.realtimeService.toggleSpeaker() },
            onToggleCamera = { viewModel.realtimeService.toggleVideoCamera() },
            onSwitchCamera = { viewModel.realtimeService.switchCameraFacing() },
            onEndCall = { viewModel.realtimeService.endActiveCall() }
        )
        return
    }

    // 3. If Admin Dashboard is open, show it
    if (isAdminOpen) {
        AdminDashboardScreen(
            viewModel = viewModel,
            onBack = { viewModel.closeAdminDashboard() }
        )
        return
    }

    // 4. If Contacts Screen is open
    if (isContactsOpen) {
        ContactsScreen(
            viewModel = viewModel,
            onBack = { isContactsOpen = false },
            onStartChat = { contact ->
                isContactsOpen = false
                viewModel.startChatWithContact(contact)
            }
        )
        return
    }

    // 5. If Chat Detail is open
    if (activeChat != null) {
        ChatDetailScreen(
            chat = activeChat!!,
            viewModel = viewModel,
            onBack = { viewModel.closeChat() }
        )
        return
    }

    // 6. Main App Container with Bottom Navigation
    Scaffold(
        topBar = {
            PulseTopBar(
                title = when (currentTab) {
                    MainNavigationTab.CHATS -> "Pulse"
                    MainNavigationTab.STATUS -> "Status"
                    MainNavigationTab.CHANNELS -> "Channels"
                    MainNavigationTab.CALLS -> "Calls"
                    MainNavigationTab.SETTINGS -> "Settings"
                },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setGlobalSearchQuery(it) },
                onOpenAdmin = { viewModel.openAdminDashboard() },
                onOpenContacts = { isContactsOpen = true }
            )
        },
        bottomBar = {
            PulseBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) },
                unreadMessagesTotal = totalUnread,
                hasUnseenStatus = hasUnseenStatus
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainNavigationTab.CHATS -> {
                    ChatsListScreen(
                        viewModel = viewModel,
                        onOpenChat = { chat -> viewModel.openChat(chat) }
                    )
                }
                MainNavigationTab.STATUS -> {
                    StatusScreen(viewModel = viewModel)
                }
                MainNavigationTab.CHANNELS -> {
                    ChannelsScreen(viewModel = viewModel)
                }
                MainNavigationTab.CALLS -> {
                    CallsScreen(viewModel = viewModel)
                }
                MainNavigationTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onOpenAdminDashboard = { viewModel.openAdminDashboard() }
                    )
                }
            }
        }
    }

    // Incoming Call Ringing Dialog
    incomingCall?.let { call ->
        IncomingCallDialog(
            callSession = call,
            onAccept = { viewModel.realtimeService.acceptIncomingCall() },
            onDecline = { viewModel.realtimeService.declineIncomingCall() }
        )
    }
}
