package app.espanol.tts

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val ttsLock = Mutex()

    private var utteranceId = 0
    private val spanishLocale = Locale("es", "ES")

    init {
        if (!isTTSAvailable()) {
            _error.value = "TTS not available on this device"
            app.espanol.util.Logger.e("TTS not available")
        } else {
            initializeTTS()
        }
    }

    private fun isTTSAvailable(): Boolean {
        return try {
            val intent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
            val activities = context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            activities.isNotEmpty()
        } catch (e: Exception) {
            app.espanol.util.Logger.e("TTS availability check failed", e)
            false
        }
    }

    private fun initializeTTS() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    configureSpanishTTS()
                } else {
                    _error.value = "TTS initialization failed"
                    app.espanol.util.Logger.e("TTS initialization failed with status: $status")
                }
            }
        } catch (e: Exception) {
            _error.value = "Failed to create TTS: ${e.message}"
            app.espanol.util.Logger.e("TTS creation failed", e)
        }
    }

    private fun configureSpanishTTS() {
        tts?.let { ttsEngine ->
            // Check if Spanish is available
            val result = ttsEngine.isLanguageAvailable(spanishLocale)

            when (result) {
                TextToSpeech.LANG_AVAILABLE,
                TextToSpeech.LANG_COUNTRY_AVAILABLE,
                TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                    // Spanish is available, set it
                    val setResult = ttsEngine.setLanguage(spanishLocale)
                    if (setResult == TextToSpeech.LANG_MISSING_DATA || setResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        _error.value = "Spanish language not supported"
                        app.espanol.util.Logger.e("Spanish language not supported")
                    } else {
                        _isReady.value = true
                        _error.value = null
                        app.espanol.util.Logger.i("Spanish TTS ready")
                    }
                }

                else -> {
                    // Try fallback Spanish locales
                    val fallbackLocales = listOf(
                        Locale("es", "MX"), // Mexican Spanish
                        Locale("es", "US"), // US Spanish
                        Locale("es")        // Generic Spanish
                    )

                    var configured = false
                    for (locale in fallbackLocales) {
                        if (ttsEngine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                            val setResult = ttsEngine.setLanguage(locale)
                            if (setResult != TextToSpeech.LANG_MISSING_DATA && setResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                                _isReady.value = true
                                _error.value = null
                                configured = true
                                app.espanol.util.Logger.i("Fallback Spanish TTS ready: $locale")
                                break
                            }
                        }
                    }

                    if (!configured) {
                        _error.value =
                            "Spanish language not available. Please install Spanish TTS data."
                        app.espanol.util.Logger.e("No Spanish TTS available")
                    }
                }
            }

            // Set speech rate and pitch
            ttsEngine.setSpeechRate(0.8f) // Slightly slower for learning
            ttsEngine.setPitch(1.0f)

            // Set up progress listener
            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    app.espanol.util.Logger.d("TTS started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    app.espanol.util.Logger.d("TTS completed: $utteranceId")
                }

                override fun onError(utteranceId: String?) {
                    app.espanol.util.Logger.e("TTS error: $utteranceId")
                    _error.value = "Speech playback failed"
                }
            })
        }
    }

    suspend fun speak(text: String) {
        withContext(Dispatchers.Main) {
            ttsLock.withLock {
                if (text.isBlank()) return@withLock

                try {
                    if (!isTTSHealthy()) {
                        app.espanol.util.Logger.w("TTS not healthy, reinitializing")
                        reinitializeTTS()
                        return@withLock
                    }

                    val currentUtteranceId = "utterance_${++utteranceId}"
                    val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, currentUtteranceId)

                    if (result == TextToSpeech.ERROR) {
                        _error.value = "Failed to speak text"
                        app.espanol.util.Logger.e("TTS speak failed")
                    } else {
                        _error.value = null
                        app.espanol.util.Logger.i("Speaking: $text")
                    }
                } catch (e: Exception) {
                    _error.value = "TTS error: ${e.message}"
                    app.espanol.util.Logger.e("TTS speak exception", e)
                }
            }
        }
    }

    private fun isTTSHealthy(): Boolean {
        return _isReady.value && tts != null
    }

    private fun reinitializeTTS() {
        app.espanol.util.Logger.i("Reinitializing TTS")
        stop()
        shutdown()
        _isReady.value = false
        initializeTTS()
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            app.espanol.util.Logger.e("TTS stop failed", e)
        }
    }

    private var isShutdown = false

    fun shutdown() {
        if (isShutdown) return

        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            _isReady.value = false
            isShutdown = true
        } catch (e: Exception) {
            app.espanol.util.Logger.e("TTS shutdown failed", e)
        }
    }
}