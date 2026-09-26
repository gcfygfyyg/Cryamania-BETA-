package com.example.audio

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundEffects {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.w("SoundEffects", "ToneGenerator init failed", e)
        }
    }

    fun playMicToggle(enabled: Boolean) {
        try {
            if (enabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } else {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playCoinJingle() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playHammerStrike() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playEmoteSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONFIRM, 120)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playChatPop() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 60)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
