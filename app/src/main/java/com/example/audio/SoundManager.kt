package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var isSoundEnabled: Boolean = true
    private var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        isVibrationEnabled = enabled
    }

    fun playCardSound() {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
            } catch (_: Exception) {}
        }
        vibrate(30)
    }

    fun playDrawSound() {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 90)
            } catch (_: Exception) {}
        }
        vibrate(40)
    }

    fun playStackAlertSound() {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            } catch (_: Exception) {}
        }
        vibratePattern(longArrayOf(0, 50, 40, 90))
    }

    fun playUnoCallSound() {
        if (isSoundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 150)
                    kotlinx.coroutines.delay(120)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 250)
                } catch (_: Exception) {}
            }
        }
        vibratePattern(longArrayOf(0, 80, 50, 150))
    }

    fun playPenaltyBuzz() {
        if (isSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
            } catch (_: Exception) {}
        }
        vibratePattern(longArrayOf(0, 120, 60, 120))
    }

    fun playVictorySound() {
        if (isSoundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
                    kotlinx.coroutines.delay(150)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                    kotlinx.coroutines.delay(180)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_9, 350)
                } catch (_: Exception) {}
            }
        }
        vibratePattern(longArrayOf(0, 80, 50, 100, 50, 200))
    }

    private fun vibrate(millis: Long) {
        if (!isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(millis)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        if (!isVibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
