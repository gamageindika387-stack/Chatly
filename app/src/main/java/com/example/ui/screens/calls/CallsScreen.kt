package com.example.ui.screens.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.PulseViewModel
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.PulseEmeraldGreen
import com.example.ui.theme.PulseRosePink
import com.example.ui.theme.PulseVioletPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallsScreen(
    viewModel: PulseViewModel,
    modifier: Modifier = Modifier
) {
    val calls by viewModel.allCalls.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("calls_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.realtimeService.triggerIncomingCall("u_elena", "Elena Rostova", CallType.VIDEO) },
                icon = { Icon(Icons.Default.PhoneCallback, contentDescription = null) },
                text = { Text("Test Incoming Call") },
                containerColor = PulseVioletPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_test_incoming_call")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT CALLS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "WebRTC Powered",
                        fontSize = 11.sp,
                        color = PulseVioletPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (calls.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No call history", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(calls, key = { it.id }) { call ->
                    CallHistoryRowItem(
                        call = call,
                        onRedial = {
                            viewModel.startCall(call.peerId, call.peerName, call.peerAvatar, call.type)
                        },
                        onDelete = { viewModel.deleteCallRecord(call.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CallHistoryRowItem(
    call: CallRecord,
    onRedial: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = remember(call.timestamp) {
        SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date(call.timestamp))
    }

    Surface(
        onClick = onRedial,
        modifier = Modifier.fillMaxWidth().testTag("call_item_${call.peerName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulseAvatar(name = call.peerName, size = 48.dp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.peerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (call.status == CallStatus.MISSED) PulseRosePink else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, color) = when {
                        call.status == CallStatus.MISSED -> Icons.AutoMirrored.Filled.CallMissed to PulseRosePink
                        call.direction == CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to PulseEmeraldGreen
                        else -> Icons.AutoMirrored.Filled.CallMade to PulseVioletPrimary
                    }

                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$timeFormatted • ${if (call.durationSec > 0) "${call.durationSec}s" else "Missed"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onRedial, modifier = Modifier.testTag("redial_button_${call.peerName}")) {
                Icon(
                    imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call Again",
                    tint = PulseVioletPrimary
                )
            }
        }
    }
}
