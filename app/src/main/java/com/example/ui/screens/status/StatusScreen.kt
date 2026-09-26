package com.example.ui.screens.status

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StatusItem
import com.example.data.model.StatusType
import com.example.ui.PulseViewModel
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulseCyanAccent
import com.example.ui.theme.PulseVioletPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val statuses by viewModel.allStatuses.collectAsState()
    val activeStory by viewModel.activeStatusStory.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    val myStatuses = remember(statuses) { statuses.filter { it.isMyStatus } }
    val recentStatuses = remember(statuses) { statuses.filter { !it.isMyStatus && !it.isViewedByMe } }
    val viewedStatuses = remember(statuses) { statuses.filter { !it.isMyStatus && it.isViewedByMe } }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("status_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PulseVioletPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_status")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "New Status")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // My Status Section
            item {
                Surface(
                    onClick = {
                        if (myStatuses.isNotEmpty()) {
                            viewModel.viewStatus(myStatuses.first())
                        } else {
                            showCreateDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("my_status_item")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            PulseAvatar(
                                name = "Me",
                                size = 56.dp,
                                hasStory = myStatuses.isNotEmpty(),
                                isStoryViewed = false
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(PulseVioletPrimary)
                                    .clickable { showCreateDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("My Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = if (myStatuses.isNotEmpty()) "${myStatuses.size} active status • Tap to view" else "Tap to add status update",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (myStatuses.isNotEmpty()) {
                            Icon(Icons.Default.Visibility, contentDescription = "Views", tint = PulseVioletPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${myStatuses.sumOf { it.viewers.size }}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PulseVioletPrimary)
                        }
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Recent Updates
            if (recentStatuses.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENT UPDATES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(recentStatuses) { status ->
                    StatusRowItem(status = status, onClick = { viewModel.viewStatus(status) })
                }
            }

            // Viewed Updates
            if (viewedStatuses.isNotEmpty()) {
                item {
                    Text(
                        text = "VIEWED UPDATES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(viewedStatuses) { status ->
                    StatusRowItem(status = status, onClick = { viewModel.viewStatus(status) })
                }
            }
        }
    }

    // Active Story Viewer Dialog
    activeStory?.let { status ->
        StoryViewerModal(
            status = status,
            onClose = { viewModel.closeStatusStory() },
            onReply = { reply ->
                viewModel.closeStatusStory()
            }
        )
    }

    // Create Status Dialog
    if (showCreateDialog) {
        CreateStatusDialog(
            onDismiss = { showCreateDialog = false },
            onPostText = { text, bgHex ->
                viewModel.postTextStatus(text, bgHex)
                showCreateDialog = false
            },
            onPostVoice = { sec ->
                viewModel.postVoiceStatus(sec)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun StatusRowItem(status: StatusItem, onClick: () -> Unit) {
    val timeFormatted = remember(status.timestamp) {
        val diffHours = ((System.currentTimeMillis() - status.timestamp) / (1000 * 3600)).toInt()
        if (diffHours == 0) "Just now" else "$diffHours hours ago"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("status_row_${status.userName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulseAvatar(
                name = status.userName,
                size = 52.dp,
                hasStory = true,
                isStoryViewed = status.isViewedByMe
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(status.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(timeFormatted, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (status.type == StatusType.VOICE) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Status", tint = PulseVioletPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun StoryViewerModal(
    status: StatusItem,
    onClose: () -> Unit,
    onReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(status.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        onClose()
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    try {
                        Color(android.graphics.Color.parseColor(status.backgroundColorHex))
                    } catch (e: Exception) {
                        Color(0xFF0F172A)
                    }
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
                .testTag("story_viewer_modal")
        ) {
            // Story Progress Bar
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .align(Alignment.TopCenter),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            // Header User Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PulseAvatar(name = status.userName, size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(status.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Today • Expires in 24h", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Story Main Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (status.type == StatusType.VOICE) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Voice Status Note (${status.voiceDurationSec}s)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(status.caption, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    Text(
                        text = status.caption,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Reply or Viewers Bar
            if (status.isMyStatus) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (status.viewers.isEmpty()) "No views yet" else "Viewed by ${status.viewers.joinToString { it.userName }}",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Reply to status...", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) onReply(replyText)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = PulseVioletPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateStatusDialog(
    onDismiss: () -> Unit,
    onPostText: (String, String) -> Unit,
    onPostVoice: (Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6366F1") }
    val colors = listOf("#6366F1", "#0284C7", "#D946EF", "#10B981", "#F59E0B", "#F43F5E")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post a 24-Hour Status") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Select Theme Color:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { selectedColor = hex }
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onPostText(text, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PulseVioletPrimary)
            ) {
                Text("Post Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
