package com.example.ui.screens.chats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.PulseViewModel
import com.example.ui.components.MediaViewerDialog
import com.example.ui.components.PulseAvatar
import com.example.ui.components.WaveformPlayer
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    viewModel: PulseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val messages by viewModel.activeChatMessages.collectAsState()
    val inputText by viewModel.messageInputText.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()
    val editingMsg by viewModel.editingMessage.collectAsState()
    val typingMap by viewModel.realtimeService.isTypingMap.collectAsState()
    val isPeerTyping = typingMap[chat.id] == true

    val recordState by viewModel.audioService.recordState.collectAsState()
    val playbackState by viewModel.audioService.playbackState.collectAsState()

    var showAttachSheet by remember { mutableStateOf(false) }
    var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }
    var mediaViewerData by remember { mutableStateOf<Pair<String, String>?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_detail_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulseAvatar(name = chat.title, size = 40.dp, isOnline = chat.isOnline)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chat.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isPeerTyping) "typing..." else if (chat.isOnline) "online" else chat.lastSeenText,
                                fontSize = 12.sp,
                                color = if (isPeerTyping) PulseCyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.startCall(chat.id, chat.title, chat.avatarUrl, CallType.VOICE) },
                        modifier = Modifier.testTag("chat_start_voice_call")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Voice Call", tint = PulseVioletPrimary)
                    }

                    IconButton(
                        onClick = { viewModel.startCall(chat.id, chat.title, chat.avatarUrl, CallType.VIDEO) },
                        modifier = Modifier.testTag("chat_start_video_call")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = PulseVioletPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Reply Preview Bar
                AnimatedVisibility(visible = replyingTo != null) {
                    replyingTo?.let { reply ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(32.dp)
                                    .background(PulseVioletPrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replying to ${reply.senderName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PulseVioletPrimary
                                )
                                Text(
                                    text = reply.content,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.setReplyingTo(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Edit Message Bar
                AnimatedVisibility(visible = editingMsg != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PulseCyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editing message",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.setEditing(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel edit", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Active Voice Recording Bar
                if (recordState.isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(PulseRosePink)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recording 0:${String.format("%02d", recordState.durationSec)}",
                            fontWeight = FontWeight.Bold,
                            color = PulseRosePink,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(onClick = { viewModel.audioService.cancelRecording() }) {
                            Text("Slide to Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                val rec = viewModel.audioService.stopAndGetRecording()
                                rec?.let { (duration, wave) ->
                                    viewModel.sendVoiceMessage(duration, wave)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PulseVioletPrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Voice", tint = Color.White)
                        }
                    }
                } else {
                    // Standard Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showAttachSheet = true },
                            modifier = Modifier.testTag("chat_attachment_button")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Attach", tint = PulseVioletPrimary)
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { viewModel.updateMessageInput(it) },
                            placeholder = { Text("Message...", fontSize = 15.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .testTag("chat_message_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.sendTextMessage() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PulseVioletPrimary)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                            }
                        } else {
                            // Voice Record Button (Tap or Hold)
                            IconButton(
                                onClick = {
                                    viewModel.audioService.startRecording(isLocked = true)
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("chat_voice_record_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record Voice", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Messages are protected with simulated end-to-end encryption",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                val isMe = message.senderId == viewModel.repository.currentUser.id

                MessageBubble(
                    message = message,
                    isMe = isMe,
                    playbackState = playbackState,
                    onPlayVoice = { viewModel.audioService.playVoiceMessage(message.id, message.voiceDurationSec) },
                    onSeekVoice = { viewModel.audioService.seekPlayback(message.id, it, message.voiceDurationSec) },
                    onToggleSpeed = { viewModel.audioService.togglePlaybackSpeed() },
                    onLongClick = { selectedMessageForMenu = message },
                    onOpenMedia = { title, sub -> mediaViewerData = Pair(title, sub) },
                    onReaction = { emoji -> viewModel.toggleMessageReaction(message, emoji) }
                )
            }
        }
    }

    // Attachment bottom sheet
    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Share Attachment", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(icon = Icons.Default.Image, label = "Photo", color = Color(0xFF6366F1)) {
                        viewModel.sendMediaAttachment(MessageType.IMAGE, "Sample Photo")
                        showAttachSheet = false
                    }
                    AttachmentOptionItem(icon = Icons.Default.Description, label = "Document", color = Color(0xFF06B6D4)) {
                        viewModel.sendMediaAttachment(MessageType.DOCUMENT, "Pulse_Spec.pdf")
                        showAttachSheet = false
                    }
                    AttachmentOptionItem(icon = Icons.Default.LocationOn, label = "Location", color = Color(0xFF10B981)) {
                        viewModel.sendMediaAttachment(MessageType.LOCATION, "Innovation District, 101 Blvd")
                        showAttachSheet = false
                    }
                    AttachmentOptionItem(icon = Icons.Default.Person, label = "Contact", color = Color(0xFFF59E0B)) {
                        viewModel.sendMediaAttachment(MessageType.CONTACT, "Liam Chen")
                        showAttachSheet = false
                    }
                }
            }
        }
    }

    // Message context menu
    selectedMessageForMenu?.let { msg ->
        val isMe = msg.senderId == viewModel.repository.currentUser.id

        ModalBottomSheet(
            onDismissRequest = { selectedMessageForMenu = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding()
            ) {
                // Quick emoji reaction bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("❤️", "🔥", "👍", "👏", "😂", "😮").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.toggleMessageReaction(msg, emoji)
                                    selectedMessageForMenu = null
                                }
                                .padding(6.dp)
                        )
                    }
                }

                Divider()

                ListItem(
                    headlineContent = { Text("Reply") },
                    leadingContent = { Icon(Icons.Default.Reply, contentDescription = null) },
                    modifier = Modifier.clickable {
                        viewModel.setReplyingTo(msg)
                        selectedMessageForMenu = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Copy Text") },
                    leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Pulse message", msg.content))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        selectedMessageForMenu = null
                    }
                )

                if (isMe && msg.type == MessageType.TEXT) {
                    ListItem(
                        headlineContent = { Text("Edit Message") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.setEditing(msg)
                            selectedMessageForMenu = null
                        }
                    )
                }

                ListItem(
                    headlineContent = { Text("Delete Message", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        viewModel.deleteMessage(msg.id)
                        selectedMessageForMenu = null
                    }
                )
            }
        }
    }

    // Media Viewer dialog
    mediaViewerData?.let { (title, sub) ->
        MediaViewerDialog(title = title, subtitle = sub, onDismiss = { mediaViewerData = null })
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    playbackState: com.example.service.VoicePlaybackState,
    onPlayVoice: () -> Unit,
    onSeekVoice: (Float) -> Unit,
    onToggleSpeed: () -> Unit,
    onLongClick: () -> Unit,
    onOpenMedia: (String, String) -> Unit,
    onReaction: (String) -> Unit
) {
    val bubbleBg = if (isMe) PulseVioletPrimary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleBg,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clickable { onLongClick() }
                .testTag("message_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Reply Header if any
                if (!message.replyToContent.isNullOrBlank()) {
                    Surface(
                        color = if (isMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = message.replyToSenderName ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White else PulseVioletPrimary
                            )
                            Text(
                                text = message.replyToContent ?: "",
                                fontSize = 11.sp,
                                color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Message Content by Type
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    }
                    MessageType.VOICE -> {
                        val isThisPlaying = playbackState.playingMessageId == message.id && playbackState.isPlaying
                        val progress = if (playbackState.playingMessageId == message.id) playbackState.progress else 0f

                        WaveformPlayer(
                            isPlaying = isThisPlaying,
                            progress = progress,
                            durationSec = message.voiceDurationSec,
                            waveform = message.voiceWaveform,
                            speed = playbackState.speed,
                            onPlayPause = onPlayVoice,
                            onSeek = onSeekVoice,
                            onToggleSpeed = onToggleSpeed,
                            isSentByMe = isMe
                        )
                    }
                    MessageType.IMAGE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PulseGradient)
                                .clickable { onOpenMedia("Photo", message.mediaFileSize) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("View Photo (${message.mediaFileSize})", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    MessageType.DOCUMENT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenMedia(message.mediaFileName, message.mediaFileSize) }
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = if (isMe) Color.White else PulseVioletPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(message.mediaFileName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                Text(message.mediaFileSize, fontSize = 11.sp, color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    MessageType.LOCATION -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isMe) Color.White else PulseRosePink)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(message.locationTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                            }
                            Text("37.7749° N, 122.4194° W", fontSize = 11.sp, color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    MessageType.CONTACT -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PulseAvatar(name = message.contactName, size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(message.contactName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                Text(message.contactPhone, fontSize = 11.sp, color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    else -> {
                        Text(text = message.content, fontSize = 15.sp, color = textColor)
                    }
                }

                // Timestamp and Status Row
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "edited",
                            fontSize = 10.sp,
                            color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = when (message.status) {
                                MessageStatus.READ -> Icons.Default.DoneAll
                                MessageStatus.DELIVERED -> Icons.Default.DoneAll
                                MessageStatus.SENT -> Icons.Default.Done
                                MessageStatus.SENDING -> Icons.Default.Schedule
                            },
                            contentDescription = message.status.name,
                            tint = if (message.status == MessageStatus.READ) PulseCyanAccent else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // Reactions Row
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.forEach { reaction ->
                    Text(
                        text = "${reaction.emoji} 1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
