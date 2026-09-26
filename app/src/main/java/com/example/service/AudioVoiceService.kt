package com.example.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class VoiceRecordState(
    val isRecording: Boolean = false,
    val isLocked: Boolean = false,
    val durationSec: Int = 0,
    val waveformPoints: List<Float> = emptyList()
)

data class VoicePlaybackState(
    val playingMessageId: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentDurationSec: Int = 0,
    val totalDurationSec: Int = 0,
    val speed: Float = 1.0f
)

class AudioVoiceService(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val _recordState = MutableStateFlow(VoiceRecordState())
    val recordState: StateFlow<VoiceRecordState> = _recordState.asStateFlow()

    private val _playbackState = MutableStateFlow(VoicePlaybackState())
    val playbackState: StateFlow<VoicePlaybackState> = _playbackState.asStateFlow()

    private var recordJob: Job? = null
    private var playbackJob: Job? = null

    fun startRecording(isLocked: Boolean = false) {
        if (_recordState.value.isRecording) return
        _recordState.value = VoiceRecordState(isRecording = true, isLocked = isLocked, durationSec = 0, waveformPoints = emptyList())
        recordJob?.cancel()
        recordJob = scope.launch {
            while (isActive) {
                delay(200)
                val newAmplitude = Random.nextFloat().coerceIn(0.15f, 1.0f)
                _recordState.value = _recordState.value.let { curr ->
                    val updatedWaveform = (curr.waveformPoints + newAmplitude).takeLast(40)
                    val newDuration = curr.waveformPoints.size / 5
                    curr.copy(
                        durationSec = newDuration,
                        waveformPoints = updatedWaveform
                    )
                }
            }
        }
    }

    fun lockRecording() {
        _recordState.value = _recordState.value.copy(isLocked = true)
    }

    fun cancelRecording() {
        recordJob?.cancel()
        _recordState.value = VoiceRecordState()
    }

    fun stopAndGetRecording(): Pair<Int, List<Float>>? {
        recordJob?.cancel()
        val state = _recordState.value
        if (!state.isRecording || state.durationSec < 1) {
            _recordState.value = VoiceRecordState()
            return null
        }
        val result = Pair(state.durationSec.coerceAtLeast(1), state.waveformPoints)
        _recordState.value = VoiceRecordState()
        return result
    }

    // --- Playback controls ---
    fun playVoiceMessage(messageId: String, totalSec: Int) {
        val curr = _playbackState.value
        if (curr.playingMessageId == messageId && curr.isPlaying) {
            pausePlayback()
            return
        }

        playbackJob?.cancel()
        val startProgress = if (curr.playingMessageId == messageId) curr.progress else 0f
        _playbackState.value = VoicePlaybackState(
            playingMessageId = messageId,
            isPlaying = true,
            progress = startProgress,
            currentDurationSec = (startProgress * totalSec).toInt(),
            totalDurationSec = totalSec,
            speed = curr.speed
        )

        val speedFactor = _playbackState.value.speed
        val stepMs = (100 / speedFactor).toLong()

        playbackJob = scope.launch {
            val totalSteps = (totalSec * 1000L / stepMs).coerceAtLeast(10)
            var currentStep = (startProgress * totalSteps).toInt()

            while (isActive && currentStep < totalSteps) {
                delay(stepMs)
                currentStep++
                val p = currentStep.toFloat() / totalSteps
                _playbackState.value = _playbackState.value.copy(
                    progress = p,
                    currentDurationSec = (p * totalSec).toInt()
                )
            }
            // Finished
            _playbackState.value = _playbackState.value.copy(isPlaying = false, progress = 0f, currentDurationSec = 0)
        }
    }

    fun pausePlayback() {
        playbackJob?.cancel()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    fun togglePlaybackSpeed() {
        val nextSpeed = when (_playbackState.value.speed) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        _playbackState.value = _playbackState.value.copy(speed = nextSpeed)
    }

    fun seekPlayback(messageId: String, progress: Float, totalSec: Int) {
        _playbackState.value = _playbackState.value.copy(
            playingMessageId = messageId,
            progress = progress.coerceIn(0f, 1f),
            currentDurationSec = (progress * totalSec).toInt(),
            totalDurationSec = totalSec
        )
    }
}
