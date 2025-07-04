package app.espanol.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        try {
            tts = TextToSpeech(context) { status ->
                when (status) {
                    TextToSpeech.SUCCESS -> {
                        val result = tts?.setLanguage(Locale("es", "ES"))
                        when (result) {
                            TextToSpeech.LANG_MISSING_DATA -> {
                                app.espanol.util.Logger.e("Spanish language data missing")
                                _error.value = "Spanish language data missing"
                                _isReady.value = false
                            }

                            TextToSpeech.LANG_NOT_SUPPORTED -> {
                                app.espanol.util.Logger.e("Spanish language not supported")
                                _error.value = "Spanish language not supported"
                                _isReady.value = false
                            }

                            else -> {
                                app.espanol.util.Logger.i("TTS initialized successfully")
                                _isReady.value = true
                                _error.value = null
                            }
                        }
                    }

                    else -> {
                        app.espanol.util.Logger.e("TTS initialization failed with status: $status")
                        _error.value = "TTS initialization failed"
                        _isReady.value = false
                    }
                }
            }
        } catch (e: Exception) {
            app.espanol.util.Logger.e("TTS initialization error", e)
            _error.value = "TTS initialization error: ${e.message}"
            _isReady.value = false
        }
    }

    fun speak(text: String) {
        if (_isReady.value && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
    }

    fun stop() {
        tts?.stop()
    }
}