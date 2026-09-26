package com.example.ui.screens.channels

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.Channel
import com.example.data.model.ChannelPost
import com.example.data.model.ChannelPostType
import com.example.ui.PulseViewModel
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulseCyanAccent
import com.example.ui.theme.PulseGradient
import com.example.ui.theme.PulseVioletPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val channels by viewModel.allChannels.collectAsState()
    val activeChannel by viewModel.activeChannel.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Followed, 1 = Explore
    var showCreateDialog by remember { mutableStateOf(false) }

    val displayedChannels = remember(channels, selectedTab) {
        if (selectedTab == 0) channels.filter { it.isFollowedByMe }
        else channels
    }

    if (activeChannel != null) {
        ChannelFeedDetail(
            channel = activeChannel!!,
            onBack = { viewModel.closeChannel() },
            onToggleFollow = { viewModel.toggleFollowChannel(activeChannel!!) }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("channels_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PulseVioletPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_channel")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Channel")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: Followed vs Discover
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PulseVioletPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Channels (${channels.count { it.isFollowedByMe }})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Discover & Explore", fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedChannels, key = { it.id }) { channel ->
                    ChannelCardItem(
                        channel = channel,
                        onClick = { viewModel.openChannel(channel) },
                        onToggleFollow = { viewModel.toggleFollowChannel(channel) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateChannelDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, handle, desc, cat ->
                viewModel.createChannel(name, handle, desc, cat)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ChannelCardItem(
    channel: Channel,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("channel_item_${channel.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulseAvatar(name = channel.name, size = 52.dp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = PulseCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${channel.handle} • ${channel.followerCount} followers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = channel.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onToggleFollow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (channel.isFollowedByMe) MaterialTheme.colorScheme.surfaceVariant else PulseVioletPrimary,
                    contentColor = if (channel.isFollowedByMe) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (channel.isFollowedByMe) "Following" else "Follow",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelFeedDetail(
    channel: Channel,
    onBack: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulseAvatar(name = channel.name, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${channel.followerCount} followers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFollow) {
                        Icon(
                            imageVector = if (channel.isFollowedByMe) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                            contentDescription = "Mute/Unmute",
                            tint = PulseVioletPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Channel Banner & Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(channel.handle, fontSize = 13.sp, color = PulseVioletPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(channel.description, fontSize = 14.sp)
                    }
                }
            }

            // Channel Posts
            items(channel.posts) { post ->
                ChannelPostCard(post = post)
            }
        }
    }
}

@Composable
fun ChannelPostCard(post: ChannelPost) {
    var votedOptionId by remember { mutableStateOf<String?>(null) }
    var myReaction by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("channel_post_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.content,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            // If Poll
            if (post.type == ChannelPostType.POLL) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(post.pollQuestion, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.pollOptions.forEach { option ->
                        val isVoted = votedOptionId == option.id || (votedOptionId == null && option.isVotedByMe)
                        Surface(
                            onClick = { votedOptionId = option.id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isVoted) PulseVioletPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option.text,
                                    fontSize = 13.sp,
                                    fontWeight = if (isVoted) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${option.voteCount + if (votedOptionId == option.id) 1 else 0} votes",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Post Footer: Views & Reactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.viewCount} views", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("❤️", "🔥", "👏").forEach { emoji ->
                        val isReacted = myReaction == emoji
                        Surface(
                            onClick = { myReaction = if (isReacted) null else emoji },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isReacted) PulseVioletPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$emoji ${post.reactionsCount[emoji] ?: 0}",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateChannelDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, handle: String, desc: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technology") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Public Channel") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Channel Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Unique Handle (e.g. techpulse)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onCreate(name, handle, description, category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PulseVioletPrimary)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
