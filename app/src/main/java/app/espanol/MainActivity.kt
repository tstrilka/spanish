package app.espanol

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.espanol.speech.SpeechRecognitionManager
import app.espanol.translation.TranslationService
import app.espanol.tts.TTSManager
import app.espanol.ui.LearningScreen
import app.espanol.ui.TranslateScreen
import app.espanol.ui.theme.EspanolTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ttsManager: TTSManager

    @Inject
    lateinit var translationService: TranslationService

    @Inject
    lateinit var speechRecognitionManager: SpeechRecognitionManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            app.espanol.util.Logger.w("Audio recording permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        enableEdgeToEdge()

        setContent {
            EspanolTheme {
                var isLearningMode by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                // Initialize translation service
                remember {
                    coroutineScope.launch {
                        translationService.initializeTranslators()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isLearningMode) "Learning Mode" else "Translation Mode",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Button(
                                onClick = { isLearningMode = !isLearningMode },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    },
                                    contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSecondary
                                    }
                                )
                            ) {
                                Text(if (isLearningMode) "Translate" else "Learn")
                            }                        }
                    }
                ) { innerPadding ->
                    if (isLearningMode) {
                        LearningScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = hiltViewModel(),
                            onSpeak = { text ->
                                coroutineScope.launch {
                                    ttsManager.speak(text)
                                }
                            }
                        )
                    } else {
                        TranslateScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = hiltViewModel(),
                            onSpeak = { text ->
                                coroutineScope.launch {
                                    ttsManager.speak(text)
                                }
                            },
                            ttsManager = ttsManager,
                            translationService = translationService,
                            speechRecognitionManager = speechRecognitionManager
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        ttsManager.stop()
        speechRecognitionManager.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        speechRecognitionManager.destroy()
    }
}