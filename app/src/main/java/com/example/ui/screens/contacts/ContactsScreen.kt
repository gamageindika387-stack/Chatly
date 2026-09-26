package com.example.ui.screens.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallType
import com.example.data.model.User
import com.example.ui.PulseViewModel
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulseRosePink
import com.example.ui.theme.PulseVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: PulseViewModel,
    onBack: () -> Unit,
    onStartChat: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForProfile by remember { mutableStateOf<User?>(null) }
    var showReportDialogForUser by remember { mutableStateOf<User?>(null) }

    val filtered = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts & People", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("contacts_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize().testTag("contacts_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, @username, or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PulseVioletPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("contacts_search_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filtered, key = { it.id }) { user ->
                    Surface(
                        onClick = { selectedUserForProfile = user },
                        modifier = Modifier.fillMaxWidth().testTag("contact_item_${user.username}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PulseAvatar(name = user.displayName, size = 50.dp, isOnline = user.isOnline)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("@${user.username} • ${user.phoneNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(user.about, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 1)
                            }
                            IconButton(onClick = { onStartChat(user) }) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = PulseVioletPrimary)
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }

    // Contact Profile Modal Sheet
    selectedUserForProfile?.let { user ->
        ModalBottomSheet(
            onDismissRequest = { selectedUserForProfile = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PulseAvatar(name = user.displayName, size = 80.dp, isOnline = user.isOnline)
                Spacer(modifier = Modifier.height(12.dp))
                Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("@${user.username} • ${user.phoneNumber}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(user.about, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(20.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(onClick = {
                        selectedUserForProfile = null
                        onStartChat(user)
                    }) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Message")
                    }

                    FilledTonalButton(onClick = {
                        selectedUserForProfile = null
                        viewModel.startCall(user.id, user.displayName, user.avatarUrl, CallType.VOICE)
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice")
                    }

                    FilledTonalButton(onClick = {
                        selectedUserForProfile = null
                        viewModel.startCall(user.id, user.displayName, user.avatarUrl, CallType.VIDEO)
                    }) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Video")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()

                // Block & Report options
                ListItem(
                    headlineContent = { Text(if (user.isBlocked) "Unblock User" else "Block User", color = PulseRosePink) },
                    leadingContent = { Icon(Icons.Default.Block, contentDescription = null, tint = PulseRosePink) },
                    modifier = Modifier.clickable {
                        viewModel.toggleBlockUser(user)
                        selectedUserForProfile = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Report User", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Report, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        showReportDialogForUser = user
                        selectedUserForProfile = null
                    }
                )
            }
        }
    }

    // Report user dialog
    showReportDialogForUser?.let { target ->
        var reportReason by remember { mutableStateOf("Spam or fraudulent messages") }
        AlertDialog(
            onDismissRequest = { showReportDialogForUser = null },
            title = { Text("Report @${target.username}") },
            text = {
                Column {
                    Text("Select a violation reason:")
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("Spam or fraudulent messages", "Harassment or hate speech", "Impersonation", "Inappropriate content").forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = reportReason == reason, onClick = { reportReason = reason })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reason, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialogForUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialogForUser = null }) { Text("Cancel") }
            }
        )
    }
}
