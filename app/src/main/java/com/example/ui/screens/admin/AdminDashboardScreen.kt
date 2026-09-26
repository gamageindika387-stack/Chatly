package com.example.ui.screens.admin

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportStatus
import com.example.data.model.SystemAnalytics
import com.example.ui.PulseViewModel
import com.example.ui.theme.PulseCyanAccent
import com.example.ui.theme.PulseEmeraldGreen
import com.example.ui.theme.PulseRosePink
import com.example.ui.theme.PulseVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: PulseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = remember { SystemAnalytics() }
    val reports by viewModel.reportsList.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val channels by viewModel.allChannels.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0 = Overview/Analytics, 1 = Reports, 2 = Users, 3 = Channels

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Owner Admin Dashboard", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("admin_dashboard_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PulseVioletPrimary,
                edgePadding = 16.dp
            ) {
                Tab(selected = selectedAdminTab == 0, onClick = { selectedAdminTab = 0 }, text = { Text("Analytics") })
                Tab(selected = selectedAdminTab == 1, onClick = { selectedAdminTab = 1 }, text = { Text("Reports (${reports.count { it.status == ReportStatus.PENDING }})") })
                Tab(selected = selectedAdminTab == 2, onClick = { selectedAdminTab = 2 }, text = { Text("Users (${users.size})") })
                Tab(selected = selectedAdminTab == 3, onClick = { selectedAdminTab = 3 }, text = { Text("Channels (${channels.size})") })
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedAdminTab) {
                    0 -> {
                        // Analytics Tab
                        item {
                            Text("REAL-TIME SYSTEM METRICS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricCard(title = "Active Users Today", value = "${analytics.activeUsersToday}", color = PulseVioletPrimary, modifier = Modifier.weight(1f))
                                MetricCard(title = "Messages Sent", value = "${analytics.messagesSentToday}", color = PulseCyanAccent, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricCard(title = "WebRTC Calls", value = "${analytics.callsCompletedToday}", color = PulseEmeraldGreen, modifier = Modifier.weight(1f))
                                MetricCard(title = "Storage Used", value = "${analytics.totalStorageUsedGb} GB", color = PulseRosePink, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Cluster Health & Status", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PulseEmeraldGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text("Operational", color = PulseEmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Active WebSocket Connections: ${analytics.activeWebSocketConnections}", fontSize = 13.sp)
                                    Text("Global Server Uptime: 99.98%", fontSize = 13.sp)
                                    Text("Distributed Media Nodes: 12 Active (Global CDN)", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    1 -> {
                        // Moderation / Reports Tab
                        item {
                            Text("USER REPORTS & MODERATION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        items(reports) { report ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(report.reason, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PulseRosePink)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (report.status) {
                                                ReportStatus.PENDING -> PulseRosePink.copy(alpha = 0.15f)
                                                ReportStatus.RESOLVED -> PulseEmeraldGreen.copy(alpha = 0.15f)
                                                ReportStatus.DISMISSED -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ) {
                                            Text(
                                                report.status.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (report.status) {
                                                    ReportStatus.PENDING -> PulseRosePink
                                                    ReportStatus.RESOLVED -> PulseEmeraldGreen
                                                    ReportStatus.DISMISSED -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Reported: ${report.reportedUserName} by ${report.reporterName}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Details: ${report.details}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    if (report.status == ReportStatus.PENDING) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.resolveReport(report.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = PulseEmeraldGreen),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("Ban & Resolve", fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.dismissReport(report.id) },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("Dismiss", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Users Management Tab
                        item {
                            Text("USER DIRECTORY & SUSPENSION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        items(users) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("@${user.username} • ${user.phoneNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Button(
                                        onClick = { viewModel.toggleBlockUser(user) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (user.isBlocked) PulseEmeraldGreen else PulseRosePink
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (user.isBlocked) "Unsuspend" else "Suspend", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Channels Moderation Tab
                        item {
                            Text("PUBLIC BROADCAST CHANNELS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        items(channels) { channel ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${channel.followerCount} followers", fontSize = 12.sp, color = PulseVioletPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(channel.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
