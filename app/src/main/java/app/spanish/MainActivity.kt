package app.spanish

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.spanish.speech.SpeechRecognitionManager
import app.spanish.translation.TranslationService
import app.spanish.tts.TTSManager
import app.spanish.ui.CatalogScreen
import app.spanish.ui.LearningScreen
import app.spanish.ui.TranslateScreen
import app.spanish.ui.theme.SpanishTheme
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
            app.spanish.util.Logger.w("Audio recording permission denied")
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
            SpanishTheme {
                var currentMode by remember { mutableStateOf(0) } // 0=translate, 1=learn, 2=catalog
                val coroutineScope = rememberCoroutineScope()

                remember {
                    coroutineScope.launch {
                        translationService.initializeTranslators()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = when (currentMode) {
                                    0 -> "Translation Mode"
                                    1 -> "Learning Mode"
                                    2 -> "Catalog Mode"
                                    else -> "Translation Mode"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            ModeSwitcher(
                                currentMode = currentMode,
                                onModeChange = { currentMode = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentMode) {
                        0 -> TranslateScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = hiltViewModel(),
                            onSpeak = { text -> coroutineScope.launch { ttsManager.speak(text) } },
                            ttsManager = ttsManager,
                            translationService = translationService,
                            speechRecognitionManager = speechRecognitionManager
                        )

                        1 -> LearningScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = hiltViewModel(),
                            onSpeak = { text -> coroutineScope.launch { ttsManager.speak(text) } }
                        )

                        2 -> CatalogScreen(
                            modifier = Modifier.padding(innerPadding),
                            catalogViewModel = hiltViewModel(),
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

@Composable
fun ModeSwitcher(
    currentMode: Int,
    onModeChange: (Int) -> Unit
) {
    val modes = listOf("Translate", "Learn", "Catalog")
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        modes.forEachIndexed { idx, label ->
            SegmentedButton(
                selected = currentMode == idx,
                onClick = { onModeChange(idx) },
                shape = SegmentedButtonDefaults.itemShape(idx, modes.size),
            ) {
                Text(label)
            }
        }
    }
}
