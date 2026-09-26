package com.example.ui.screens.calls

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.*

@Composable
fun ActiveCallScreen(
    callSession: ActiveCallSession,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationFormatted = String.format(
        "%02d:%02d",
        callSession.durationSec / 60,
        callSession.durationSec % 60
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0B0F19))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("active_call_screen")
    ) {
        // If Video Call: Remote video feed simulation
        if (callSession.type == CallType.VIDEO && callSession.isVideoCameraOn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PulseAvatar(name = callSession.peerName, size = 96.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Remote Video Connected (720p HD)", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }

                // Self Camera PIP (Picture-in-Picture)
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 150.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF334155))
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = "Me", tint = Color.White)
                        Text(if (callSession.isFrontCamera) "Front" else "Rear", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        // Top info header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .align(Alignment.TopCenter)
        ) {
            // Network quality pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (callSession.networkQuality) {
                    NetworkQuality.EXCELLENT -> PulseEmeraldGreen.copy(alpha = 0.2f)
                    NetworkQuality.GOOD -> PulseAmberOrange.copy(alpha = 0.2f)
                    NetworkQuality.POOR -> PulseRosePink.copy(alpha = 0.2f)
                }
            ) {
                Text(
                    text = "WebRTC • ${callSession.networkQuality.name}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (callSession.networkQuality) {
                        NetworkQuality.EXCELLENT -> PulseEmeraldGreen
                        NetworkQuality.GOOD -> PulseAmberOrange
                        NetworkQuality.POOR -> PulseRosePink
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = callSession.peerName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (callSession.stage) {
                    ActiveCallStage.OUTGOING_RINGING -> "Ringing..."
                    ActiveCallStage.INCOMING_RINGING -> "Incoming call..."
                    ActiveCallStage.CONNECTING -> "Connecting..."
                    ActiveCallStage.CONNECTED -> durationFormatted
                    ActiveCallStage.RECONNECTING -> "Reconnecting..."
                    ActiveCallStage.ENDED -> "Call Ended"
                    ActiveCallStage.IDLE -> ""
                },
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            if (callSession.type == CallType.VOICE) {
                Spacer(modifier = Modifier.height(48.dp))
                PulseAvatar(name = callSession.peerName, size = 120.dp)
            }
        }

        // Bottom Controls Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Mic
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (callSession.isMicMuted) PulseRosePink else Color.White.copy(alpha = 0.2f))
                        .testTag("call_toggle_mic")
                ) {
                    Icon(
                        imageVector = if (callSession.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }

                // Speaker
                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (callSession.isSpeakerOn) PulseVioletPrimary else Color.White.copy(alpha = 0.2f))
                        .testTag("call_toggle_speaker")
                ) {
                    Icon(
                        imageVector = if (callSession.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = Color.White
                    )
                }

                // Camera Toggle (for video)
                if (callSession.type == CallType.VIDEO) {
                    IconButton(
                        onClick = onToggleCamera,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (!callSession.isVideoCameraOn) PulseRosePink else Color.White.copy(alpha = 0.2f))
                            .testTag("call_toggle_camera")
                    ) {
                        Icon(
                            imageVector = if (callSession.isVideoCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Camera",
                            tint = Color.White
                        )
                    }

                    // Flip camera
                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("call_switch_camera")
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = Color.White)
                    }
                }

                // End Call Button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(PulseRosePink)
                        .testTag("call_end_button")
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
