package app.espanol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import app.espanol.translation.TranslationService
import app.espanol.tts.TTSManager
import app.espanol.ui.TranslateScreen
import app.espanol.ui.theme.EspanolTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ttsManager: TTSManager

    @Inject
    lateinit var translationService: TranslationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EspanolTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TranslateScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = hiltViewModel(),
                        onSpeak = { text ->
                            ttsManager.speak(text)
                        },
                        ttsManager = ttsManager,
                        translationService = translationService
                    )
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
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}