// utils/SoundManager.kt
package com.pip.cheeseroul.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager(context: Context) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playTick() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 20)
    }

    fun playJump() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
    }

    fun playWin() {
        toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 250)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}