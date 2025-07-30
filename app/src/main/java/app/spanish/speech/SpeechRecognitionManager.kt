package app.spanish.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognitionManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        try {
            // Check available languages for debugging
            checkAvailableLanguages()

            // Check if speech recognition is available
            if (!isSpeechRecognitionAvailable()) {
                _error.value = "Speech recognition not available on this device"
                app.spanish.util.Logger.e("Speech recognition not available")
                return
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            if (speechRecognizer == null) {
                _error.value = "Speech recognition service not available"
                app.spanish.util.Logger.e("Speech recognition service not available")
            }
        } catch (e: Exception) {
            _error.value = "Failed to initialize speech recognition: ${e.message}"
            app.spanish.util.Logger.e("Speech recognition initialization failed", e)
        }
    }

    private fun isSpeechRecognitionAvailable(): Boolean {
        val pm = context.packageManager
        val activities = pm.queryIntentActivities(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0
        )
        return activities.isNotEmpty()
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startListening() {
        try {
            // Check permissions first
            if (!hasAudioPermission()) {
                _error.value = "Microphone permission required"
                app.spanish.util.Logger.e("Audio permission not granted")
                return
            }

            if (speechRecognizer == null) {
                initializeSpeechRecognizer()
            }

            if (speechRecognizer == null) {
                _error.value = "Speech recognition not available"
                return
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                // Set primary Spanish locale
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")


                // Prompt for Spanish
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla en español...")
            }

            _isListening.value = true
            _error.value = null

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    app.spanish.util.Logger.i("Speech recognition ready for Spanish")
                }

                override fun onBeginningOfSpeech() {
                    app.spanish.util.Logger.i("Spanish speech recognition started")
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    app.spanish.util.Logger.i("Spanish speech recognition ended")
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error - check microphone"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error - try restarting"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error - check internet connection"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout - try again"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No Spanish speech detected - try speaking louder"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition busy - try again in a moment"
                        SpeechRecognizer.ERROR_SERVER -> "Server error - try again later"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected - try speaking"
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "Spanish not supported - install Spanish voice data"
                        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Spanish unavailable - check device settings"
                        else -> "Recognition error ($error) - try again"
                    }
                    _error.value = errorMessage
                    app.spanish.util.Logger.e("Spanish speech recognition error: $errorMessage (code: $error)")

                    // Auto-retry for certain errors
                    if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        reinitializeAfterError()
                    }
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val bestMatch = matches[0]
                        _recognizedText.value = bestMatch
                        _error.value = null
                        app.spanish.util.Logger.i("Spanish speech recognized: $bestMatch")

                        // Log all alternatives for debugging
                        matches.forEachIndexed { index, match ->
                            app.spanish.util.Logger.d("Spanish alternative $index: $match")
                        }
                    } else {
                        _error.value = "No Spanish speech recognized"
                        app.spanish.util.Logger.w("No Spanish speech matches found")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        app.spanish.util.Logger.d("Partial Spanish result: ${matches[0]}")
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _error.value = "Failed to start Spanish speech recognition: ${e.message}"
            app.spanish.util.Logger.e("Spanish speech recognition start failed", e)
        }
    }

    private fun reinitializeAfterError() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            initializeSpeechRecognizer()
        } catch (e: Exception) {
            app.spanish.util.Logger.e("Failed to reinitialize speech recognition", e)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
        } catch (e: Exception) {
            app.spanish.util.Logger.e("Failed to stop speech recognition", e)
        }
    }

    fun clearRecognizedText() {
        _recognizedText.value = ""
    }

    fun clearError() {
        _error.value = null
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isListening.value = false
        } catch (e: Exception) {
            app.spanish.util.Logger.e("Failed to destroy speech recognition", e)
        }
    }

    private fun checkAvailableLanguages() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
            context.sendBroadcast(intent)

            app.spanish.util.Logger.d("Checking available languages...")

            // Also check if Spanish is supported by attempting to create a recognition intent
            val testIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            }

            val pm = context.packageManager
            val activities = pm.queryIntentActivities(testIntent, 0)

            if (activities.isNotEmpty()) {
                app.spanish.util.Logger.d("Spanish language support detected")
            } else {
                app.spanish.util.Logger.w("No Spanish language support found")
            }

        } catch (e: Exception) {
            app.spanish.util.Logger.e("Language check failed", e)
        }
    }
}