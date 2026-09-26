package com.example.service

import com.example.data.model.*
import com.example.data.repository.PulseRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class RealtimeService(
    private val repository: PulseRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val _incomingCall = MutableStateFlow<ActiveCallSession?>(null)
    val incomingCall: StateFlow<ActiveCallSession?> = _incomingCall.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCallSession?>(null)
    val activeCall: StateFlow<ActiveCallSession?> = _activeCall.asStateFlow()

    private val _isTypingMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isTypingMap: StateFlow<Map<String, Boolean>> = _isTypingMap.asStateFlow()

    private var callTimerJob: Job? = null

    fun onUserSendMessage(chatId: String, text: String) {
        // Trigger simulated live peer response to demonstrate real-time WebSocket protocol
        scope.launch {
            delay(1200)
            _isTypingMap.value = _isTypingMap.value + (chatId to true)
            delay(2000)
            _isTypingMap.value = _isTypingMap.value - chatId

            val replyText = generateContextualReply(text)
            val isElena = chatId.contains("elena")
            val senderId = if (isElena) "u_elena" else "u_liam"
            val senderName = if (isElena) "Elena Rostova" else "Liam Chen"

            repository.insertIncomingMessage(
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                content = replyText,
                type = MessageType.TEXT
            )
        }
    }

    private fun generateContextualReply(message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hey") || lower.contains("hi") ->
                "Hey Alex! Great to hear from you. How are things going?"
            lower.contains("call") || lower.contains("talk") ->
                "Sure thing, I'm free right now! Let me know if you want voice or video."
            lower.contains("design") || lower.contains("ui") || lower.contains("theme") ->
                "The deep blue and violet palette looks stunning with high contrast and smooth transitions!"
            lower.contains("audio") || lower.contains("voice") ->
                "Voice notes with interactive waveforms make asynchronous communication so much better 🎙️"
            lower.contains("channel") ->
                "Just read the latest broadcast on the Tech Radar channel, really insightful!"
            else ->
                "Got it! Thanks for the update. Let's sync more on this shortly 🚀"
        }
    }

    // --- WebRTC / Call Signaling ---
    fun startOutgoingCall(peerId: String, peerName: String, peerAvatar: String, type: CallType) {
        val session = ActiveCallSession(
            callId = "call_${System.currentTimeMillis()}",
            peerId = peerId,
            peerName = peerName,
            peerAvatar = peerAvatar,
            type = type,
            stage = ActiveCallStage.OUTGOING_RINGING,
            isMicMuted = false,
            isSpeakerOn = type == CallType.VIDEO,
            isVideoCameraOn = type == CallType.VIDEO,
            networkQuality = NetworkQuality.EXCELLENT,
            durationSec = 0,
            isIncoming = false
        )
        _activeCall.value = session

        // Simulate remote peer answering after 2.5 seconds
        scope.launch {
            delay(2500)
            if (_activeCall.value?.callId == session.callId && _activeCall.value?.stage == ActiveCallStage.OUTGOING_RINGING) {
                _activeCall.value = _activeCall.value?.copy(stage = ActiveCallStage.CONNECTED)
                startCallTimer()
            }
        }
    }

    fun triggerIncomingCall(peerId: String = "u_elena", peerName: String = "Elena Rostova", type: CallType = CallType.VIDEO) {
        val session = ActiveCallSession(
            callId = "call_in_${System.currentTimeMillis()}",
            peerId = peerId,
            peerName = peerName,
            peerAvatar = "",
            type = type,
            stage = ActiveCallStage.INCOMING_RINGING,
            isMicMuted = false,
            isSpeakerOn = type == CallType.VIDEO,
            isVideoCameraOn = type == CallType.VIDEO,
            networkQuality = NetworkQuality.EXCELLENT,
            durationSec = 0,
            isIncoming = true
        )
        _incomingCall.value = session
    }

    fun acceptIncomingCall() {
        val incoming = _incomingCall.value ?: return
        _incomingCall.value = null
        _activeCall.value = incoming.copy(
            stage = ActiveCallStage.CONNECTED,
            isIncoming = false
        )
        startCallTimer()
    }

    fun declineIncomingCall() {
        val incoming = _incomingCall.value ?: return
        _incomingCall.value = null
        scope.launch {
            repository.recordCall(
                peerId = incoming.peerId,
                peerName = incoming.peerName,
                peerAvatar = incoming.peerAvatar,
                type = incoming.type,
                direction = CallDirection.INCOMING,
                status = CallStatus.REJECTED,
                durationSec = 0
            )
        }
    }

    fun toggleMic() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMicMuted = !it.isMicMuted) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleVideoCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoCameraOn = !it.isVideoCameraOn) }
    }

    fun switchCameraFacing() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun endActiveCall() {
        val call = _activeCall.value ?: return
        callTimerJob?.cancel()
        _activeCall.value = call.copy(stage = ActiveCallStage.ENDED)

        scope.launch {
            repository.recordCall(
                peerId = call.peerId,
                peerName = call.peerName,
                peerAvatar = call.peerAvatar,
                type = call.type,
                direction = if (call.isIncoming) CallDirection.INCOMING else CallDirection.OUTGOING,
                status = CallStatus.COMPLETED,
                durationSec = call.durationSec
            )
            delay(500)
            _activeCall.value = null
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = scope.launch {
            while (isActive) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { current ->
                    if (current.stage == ActiveCallStage.CONNECTED) {
                        // Fluctuate network quality occasionally for realistic testing
                        val quality = if (current.durationSec % 30 == 25) NetworkQuality.GOOD else NetworkQuality.EXCELLENT
                        current.copy(durationSec = current.durationSec + 1, networkQuality = quality)
                    } else current
                }
            }
        }
    }
}
