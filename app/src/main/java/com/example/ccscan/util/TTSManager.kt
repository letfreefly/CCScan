package com.example.ccscan.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * TextToSpeech管理器
 */
object TTSManager {

    private const val TAG = "TTSManager"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    /**
     * 初始化TTS引擎
     */
    fun init(context: Context) {
        if (tts != null) {
            return
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // 设置语言为中文
                val result = tts?.setLanguage(Locale.CHINA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Chinese language is not available")
                } else {
                    isInitialized = true
                    Log.d(TAG, "TTS initialized successfully")
                }
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
    }

    /**
     * 播放语音
     */
    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized, skipping speech")
            return
        }

        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to speak", e)
        }
    }

    /**
     * 释放资源
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}