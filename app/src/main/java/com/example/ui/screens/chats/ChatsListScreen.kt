package com.example.ui.screens.chats

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.StatusItem
import com.example.ui.PulseViewModel
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulseCyanAccent
import com.example.ui.theme.PulseVioletPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    viewModel: PulseViewModel,
    onOpenChat: (Chat) -> Unit,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.filteredChats.collectAsState()
    val statuses by viewModel.allStatuses.collectAsState()
    val typingMap by viewModel.realtimeService.isTypingMap.collectAsState()

    var showNewChatDialog by remember { mutableStateOf(false) }
    var showArchivedOnly by remember { mutableStateOf(false) }
    var selectedChatForActions by remember { mutableStateOf<Chat?>(null) }

    val displayedChats = remember(chats, showArchivedOnly) {
        if (showArchivedOnly) chats.filter { it.isArchived }
        else chats.filter { !it.isArchived }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("chats_list_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = PulseVioletPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_chat")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New Chat")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Stories / Status bar
            if (!showArchivedOnly) {
                item {
                    StatusStoriesRow(
                        statuses = statuses,
                        onViewStatus = { viewModel.viewStatus(it) },
                        onAddStatus = { viewModel.postTextStatus("Hello Pulse!", "#6366F1") }
                    )
                }
            }

            // 2. Archived chats header toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showArchivedOnly) "Archived Conversations" else "Recent Conversations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = { showArchivedOnly = !showArchivedOnly },
                        modifier = Modifier.testTag("toggle_archive_button")
                    ) {
                        Text(
                            text = if (showArchivedOnly) "Show All Chats" else "Archived (${chats.count { it.isArchived }})",
                            fontSize = 12.sp,
                            color = PulseVioletPrimary
                        )
                    }
                }
            }

            // 3. Chats List
            if (displayedChats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (showArchivedOnly) "No archived chats" else "No messages found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the write icon to start a new chat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(displayedChats, key = { it.id }) { chat ->
                    val isTyping = typingMap[chat.id] == true

                    ChatItemRow(
                        chat = chat,
                        isTyping = isTyping,
                        onClick = { onOpenChat(chat) },
                        onLongClick = { selectedChatForActions = chat }
                    )
                }
            }
        }
    }

    // Long press Chat Actions Bottom Sheet
    selectedChatForActions?.let { chat ->
        ModalBottomSheet(
            onDismissRequest = { selectedChatForActions = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = chat.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ListItem(
                    headlineContent = { Text(if (chat.isPinned) "Unpin Chat" else "Pin Chat") },
                    leadingContent = {
                        Icon(if (chat.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        viewModel.togglePinChat(chat)
                        selectedChatForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text(if (chat.isMuted) "Unmute Notifications" else "Mute Notifications") },
                    leadingContent = {
                        Icon(if (chat.isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeOff, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        viewModel.toggleMuteChat(chat)
                        selectedChatForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text(if (chat.isArchived) "Unarchive Chat" else "Archive Chat") },
                    leadingContent = {
                        Icon(Icons.Outlined.Archive, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        viewModel.toggleArchiveChat(chat)
                        selectedChatForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Delete Chat", color = MaterialTheme.colorScheme.error) },
                    leadingContent = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.clickable {
                        viewModel.deleteChat(chat)
                        selectedChatForActions = null
                    }
                )
            }
        }
    }

    // New Chat / New Group Dialog
    if (showNewChatDialog) {
        NewConversationDialog(
            viewModel = viewModel,
            onDismiss = { showNewChatDialog = false },
            onChatStarted = { chat ->
                showNewChatDialog = false
                onOpenChat(chat)
            }
        )
    }
}

@Composable
fun StatusStoriesRow(
    statuses: List<StatusItem>,
    onViewStatus: (StatusItem) -> Unit,
    onAddStatus: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "Add My Story" circle
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onAddStatus() }
                    .testTag("add_my_status_button")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    PulseAvatar(name = "Me", size = 56.dp)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PulseVioletPrimary)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Status",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "My Status",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Friends statuses
        items(statuses) { status ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onViewStatus(status) }
                    .testTag("status_story_${status.userName}")
            ) {
                PulseAvatar(
                    name = status.userName,
                    size = 56.dp,
                    hasStory = true,
                    isStoryViewed = status.isViewedByMe
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = status.userName.split(" ").firstOrNull() ?: status.userName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChatItemRow(
    chat: Chat,
    isTyping: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val timeFormatted = remember(chat.lastMessageTimestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_item_${chat.title}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            PulseAvatar(
                name = chat.title,
                size = 52.dp,
                isOnline = chat.isOnline
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = if (chat.unreadCount > 0) PulseVioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTyping) {
                        Text(
                            text = "typing...",
                            fontSize = 13.sp,
                            color = PulseCyanAccent,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = chat.lastMessagePreview,
                            fontSize = 13.sp,
                            color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (chat.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (chat.isMuted) {
                            Icon(
                                imageVector = Icons.Default.VolumeOff,
                                contentDescription = "Muted",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PulseVioletPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${chat.unreadCount}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewConversationDialog(
    viewModel: PulseViewModel,
    onDismiss: () -> Unit,
    onChatStarted: (Chat) -> Unit
) {
    val users by viewModel.allUsers.collectAsState()
    var isCreateGroupMode by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    val selectedMemberIds = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isCreateGroupMode) "Create New Group" else "Start Conversation")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isCreateGroupMode) {
                    Button(
                        onClick = { isCreateGroupMode = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PulseVioletPrimary)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Group")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Or select a contact:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(users) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.startChatWithContact(user)
                                        onDismiss()
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PulseAvatar(name = user.displayName, size = 36.dp, isOnline = user.isOnline)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(user.displayName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(user.phoneNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupDescription,
                        onValueChange = { groupDescription = it },
                        label = { Text("Group Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Members:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                        items(users) { user ->
                            val isSelected = selectedMemberIds.contains(user.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedMemberIds.remove(user.id)
                                        else selectedMemberIds.add(user.id)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedMemberIds.add(user.id)
                                        else selectedMemberIds.remove(user.id)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(user.displayName, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isCreateGroupMode) {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.createGroup(groupName, groupDescription, selectedMemberIds.toList())
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulseVioletPrimary)
                ) {
                    Text("Create Group")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
