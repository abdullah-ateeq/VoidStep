package com.example.voidstep.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural synthesizer for crisp, low-latency arcade sounds.
 * Fully offline, zero audio asset overhead.
 */
class SoundController(
    private val scope: CoroutineScope,
    var isEnabled: Boolean = true
) {
    private val sampleRate = 22050

    fun playStepLand() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            // Bright crystal chime (ascending dual tone)
            generateAndPlayTone(
                startFreq = 540f,
                endFreq = 720f,
                durationMs = 90,
                decayRate = 22f,
                volume = 0.55f
            )
        }
    }

    fun playStreakBonus() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            // Harmonious ascending triplet
            generateAndPlayTone(startFreq = 650f, endFreq = 980f, durationMs = 140, decayRate = 14f, volume = 0.7f)
        }
    }

    fun playDissolve() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            // Soft deep whoosh
            generateAndPlayTone(
                startFreq = 260f,
                endFreq = 90f,
                durationMs = 180,
                decayRate = 12f,
                volume = 0.45f
            )
        }
    }

    fun playFall() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            // Deep descending resonant boom
            generateAndPlayTone(
                startFreq = 340f,
                endFreq = 50f,
                durationMs = 450,
                decayRate = 7f,
                volume = 0.85f
            )
        }
    }

    fun playButtonClick() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            generateAndPlayTone(
                startFreq = 880f,
                endFreq = 880f,
                durationMs = 30,
                decayRate = 50f,
                volume = 0.35f
            )
        }
    }

    private fun generateAndPlayTone(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        decayRate: Float,
        volume: Float
    ) {
        try {
            val totalSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(100)
            val buffer = ShortArray(totalSamples)

            var phase = 0.0
            for (i in 0 until totalSamples) {
                val progress = i.toFloat() / totalSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val phaseIncrement = 2.0 * PI * currentFreq / sampleRate
                phase += phaseIncrement

                // Natural exponential envelope decay
                val envelope = exp((-decayRate * progress).toDouble()).toFloat()
                val sampleValue = (sin(phase) * envelope * volume * Short.MAX_VALUE).toInt()
                buffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playing
            Thread.sleep(durationMs.toLong() + 30)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio glitches or device audio limitations safely
        }
    }
}
