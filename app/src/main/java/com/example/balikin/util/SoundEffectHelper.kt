package com.example.balikin.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.example.R

/**
 * Utility for playing transaction action sound effects.
 */
object SoundEffectHelper {
    private const val PREFS_NAME = "balikin_preferences"
    private const val KEY_SFX_ENABLED = "pref_sfx_enabled"

    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isLoaded: Boolean = false
    private var cachedSoundEnabled: Boolean? = null

    /**
     * Initializes SoundPool with the action sound resource.
     */
    fun init(context: Context) {
        if (cachedSoundEnabled == null) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            cachedSoundEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true)
        }

        if (soundPool != null) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()
                .apply {
                    setOnLoadCompleteListener { _, _, status ->
                        if (status == 0) {
                            isLoaded = true
                        }
                    }
                    soundId = load(context.applicationContext, R.raw.sound_action, 1)
                }
        } catch (e: Exception) {
            Log.e("SoundEffectHelper", "SoundPool initialization failed: ${e.message}")
        }
    }

    /**
     * Checks if sound effects are enabled.
     */
    fun isSoundEnabled(context: Context): Boolean {
        if (cachedSoundEnabled == null) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            cachedSoundEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true)
        }
        return cachedSoundEnabled ?: true
    }

    /**
     * Updates the sound effects preference.
     */
    fun setSoundEnabled(context: Context, enabled: Boolean) {
        cachedSoundEnabled = enabled
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SFX_ENABLED, enabled).apply()
    }

    /**
     * Plays the transaction action sound effect if enabled.
     */
    fun play(context: Context) {
        if (!isSoundEnabled(context)) return

        try {
            if (soundPool == null) {
                init(context)
            }

            if (isLoaded && soundPool != null && soundId != 0) {
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            } else {
                MediaPlayer.create(context.applicationContext, R.raw.sound_action)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e("SoundEffectHelper", "Failed to play sound effect: ${e.message}")
        }
    }
}
