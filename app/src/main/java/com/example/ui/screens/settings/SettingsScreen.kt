package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrivacyAudience
import com.example.ui.PulseViewModel
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulseCyanAccent
import com.example.ui.theme.PulseRosePink
import com.example.ui.theme.PulseVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PulseViewModel,
    onOpenAdminDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val privacySettings by viewModel.privacySettings.collectAsState()
    val blockedUsers by viewModel.blockedUsers.collectAsState()
    val isDarkOverride by viewModel.isDarkThemeOverride.collectAsState()

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().testTag("settings_profile_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulseAvatar(name = authState.displayName, size = 68.dp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(authState.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("@${authState.username}", fontSize = 13.sp, color = PulseVioletPrimary, fontWeight = FontWeight.SemiBold)
                        Text(authState.phoneNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(authState.bio, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Admin Dashboard Special Banner
            Card(
                onClick = onOpenAdminDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("settings_admin_dashboard_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PulseCyanAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Owner & Admin Panel", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Active metrics, user reports & content moderation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("PREFERENCES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))

            // Privacy & Security
            ListItem(
                headlineContent = { Text("Privacy & Security") },
                supportingContent = { Text("Last seen, profile photo, read receipts") },
                leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = PulseVioletPrimary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { showPrivacyDialog = true }.testTag("settings_privacy_item")
            )

            // Blocked Users
            ListItem(
                headlineContent = { Text("Blocked Users") },
                supportingContent = { Text("${blockedUsers.size} contacts blocked") },
                leadingContent = { Icon(Icons.Default.Block, contentDescription = null, tint = PulseRosePink) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { showBlockedUsersDialog = true }.testTag("settings_blocked_item")
            )

            // Appearance & Themes
            ListItem(
                headlineContent = { Text("Appearance & Theme") },
                supportingContent = {
                    Text(
                        when (isDarkOverride) {
                            true -> "Dark Theme"
                            false -> "Light Theme"
                            null -> "System Default"
                        }
                    )
                },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null, tint = PulseCyanAccent) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { showThemeDialog = true }.testTag("settings_appearance_item")
            )

            // Notifications
            var notificationsEnabled by remember { mutableStateOf(true) }
            ListItem(
                headlineContent = { Text("Notifications & Sounds") },
                supportingContent = { Text("Message previews, call ringtones, badges") },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = PulseVioletPrimary) },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            )

            // Data & Storage
            ListItem(
                headlineContent = { Text("Storage and Data") },
                supportingContent = { Text("Network usage, auto-download media") },
                leadingContent = { Icon(Icons.Default.Storage, contentDescription = null, tint = PulseVioletPrimary) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))

            // Logout
            ListItem(
                headlineContent = { Text("Log Out", color = MaterialTheme.colorScheme.onSurface) },
                leadingContent = { Icon(Icons.Default.Logout, contentDescription = null) },
                modifier = Modifier.clickable { viewModel.logout() }.testTag("settings_logout_button")
            )

            // Delete Account
            ListItem(
                headlineContent = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                leadingContent = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { showDeleteAccountConfirm = true }.testTag("settings_delete_account_button")
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Controls") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Read Receipts", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show checkmarks when messages are read", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Switch(
                            checked = privacySettings.readReceipts,
                            onCheckedChange = { viewModel.toggleReadReceipts() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Who can see my Last Seen:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    PrivacyAudience.values().forEach { audience ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateLastSeenPrivacy(audience) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = privacySettings.lastSeenPrivacy == audience,
                                onClick = { viewModel.updateLastSeenPrivacy(audience) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(audience.name.replace("_", " "), fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("Done") }
            }
        )
    }

    // Blocked Users Dialog
    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            title = { Text("Blocked Users") },
            text = {
                if (blockedUsers.isEmpty()) {
                    Text("No blocked users.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(blockedUsers) { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user.displayName, fontSize = 14.sp)
                                TextButton(onClick = { viewModel.toggleBlockUser(user) }) {
                                    Text("Unblock", color = PulseVioletPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBlockedUsersDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Appearance") },
            text = {
                Column {
                    listOf("System Default" to null, "Light Mode" to false, "Dark Mode" to true).forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(value)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = isDarkOverride == value, onClick = {
                                viewModel.setThemeMode(value)
                                showThemeDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Delete Account Confirmation
    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = { Text("Delete Pulse Account?") },
            text = { Text("This will permanently delete your messages, media, and profile. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirm = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
