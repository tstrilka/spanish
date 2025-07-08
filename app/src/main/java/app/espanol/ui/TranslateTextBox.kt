package app.espanol.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.espanol.speech.SpeechRecognitionManager
import app.espanol.translation.TranslationService

@Composable
fun TranslateTextBox(
    modifier: Modifier = Modifier,
    onSpeak: (String) -> Unit,
    onSave: (String, String) -> Unit,
    isLoading: Boolean = false,
    ttsReady: Boolean = true,
    translationService: TranslationService = TranslationService(),
    speechRecognitionManager: SpeechRecognitionManager? = null
) {
    var czechText by remember { mutableStateOf("") }
    var spanishText by remember { mutableStateOf("") }
    var isSpanishToCzech by remember { mutableStateOf(false) }
    var userModifiedTranslation by remember { mutableStateOf(false) }

    val isListening by speechRecognitionManager?.isListening?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) }
    val recognizedText by speechRecognitionManager?.recognizedText?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf("") }
    val speechError by speechRecognitionManager?.error?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf<String?>(null) }

    // Handle recognized speech
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank() && isSpanishToCzech) {
            spanishText = recognizedText
            userModifiedTranslation = false
            speechRecognitionManager?.clearRecognizedText()
        }
    }

    // Auto-translate
    LaunchedEffect(if (isSpanishToCzech) spanishText else czechText) {
        if (!userModifiedTranslation) {
            val inputText = if (isSpanishToCzech) spanishText else czechText
            if (inputText.isNotBlank()) {
                val suggestedTranslation = translationService.translate(inputText, isSpanishToCzech)

                if (suggestedTranslation.isNotBlank() && suggestedTranslation != inputText.trim()) {
                    if (isSpanishToCzech) {
                        czechText = suggestedTranslation
                    } else {
                        spanishText = suggestedTranslation
                    }
                }
            }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Mode switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Czech → Spanish")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isSpanishToCzech,
                onCheckedChange = {
                    isSpanishToCzech = it
                    czechText = ""
                    spanishText = ""
                    userModifiedTranslation = false
                },
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    checkedTrackColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    },
                    uncheckedThumbColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    uncheckedTrackColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Spanish → Czech")
        }

        // Input field
        TextField(
            value = if (isSpanishToCzech) spanishText else czechText,
            onValueChange = { newValue ->
                if (newValue.length <= 200 && !newValue.contains('\n')) {
                    if (isSpanishToCzech) {
                        spanishText = newValue
                    } else {
                        czechText = newValue
                    }
                    userModifiedTranslation = false
                }
            },
            label = { Text(if (isSpanishToCzech) "Enter Spanish text" else "Enter Czech text") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        // Voice input for Spanish
        if (isSpanishToCzech && speechRecognitionManager != null) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isListening) {
                            speechRecognitionManager.stopListening()
                        } else {
                            speechRecognitionManager.startListening()
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        },
                        contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                ) {
                    if (isListening) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🎙️ Listening...")
                    } else {
                        Text("🎤 Voice Input")
                    }
                }

                speechError?.let { error ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Translation field
        TextField(
            value = if (isSpanishToCzech) czechText else spanishText,
            onValueChange = { newValue ->
                if (newValue.length <= 200 && !newValue.contains('\n')) {
                    if (isSpanishToCzech) {
                        czechText = newValue
                    } else {
                        spanishText = newValue
                    }
                    userModifiedTranslation = true
                }
            },
            label = { Text(if (isSpanishToCzech) "Czech translation" else "Spanish translation") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        val inputText = if (isSpanishToCzech) spanishText else czechText
        val translatedText = if (isSpanishToCzech) czechText else spanishText

        if (inputText.trim().isNotBlank() && translatedText.trim().isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSpeak(translatedText) },
                    enabled = ttsReady && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                ) {
                    Text("🔊 Speak")
                }

                Button(
                    onClick = {
                        // Always save as Czech -> Spanish
                        val czech = if (isSpanishToCzech) czechText else czechText
                        val spanish = if (isSpanishToCzech) spanishText else spanishText
                        onSave(czech.trim(), spanish.trim())
                        // Clear after saving
                        czechText = ""
                        spanishText = ""
                        userModifiedTranslation = false
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("💾 Save")
                    }
                }
            }
        }
    }
}