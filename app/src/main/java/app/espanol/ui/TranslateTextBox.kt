package app.espanol.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.espanol.translation.TranslationService

@Composable
fun TranslateTextBox(
    modifier: Modifier = Modifier,
    onSpeak: (String) -> Unit,
    onSave: (String, String) -> Unit,
    isLoading: Boolean = false,
    ttsReady: Boolean = true,
    translationService: TranslationService = TranslationService()
) {
    var input by remember { mutableStateOf("") }
    val translated = translationService.translate(input)
    val hasTranslation = translated.isNotBlank() && translated != input.trim()

    Column(modifier = modifier.padding(16.dp)) {
        TextField(
            value = input,
            onValueChange = { newValue ->
                // Improved validation - prevent newlines and excessive length
                if (newValue.length <= 200 && !newValue.contains('\n') && !newValue.contains('\r')) {
                    input = newValue
                }
            },
            label = { Text("Enter English text (max 200 chars)") },
            modifier = Modifier.semantics {
                contentDescription = "Text input field for English translation"
            },
            enabled = !isLoading,
            singleLine = false,
            maxLines = 3,
            supportingText = {
                Text("${input.length}/200 characters")
            },
            isError = input.length > 190 // Show warning when approaching limit
        )
        if (hasTranslation) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Spanish: $translated",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSpeak(translated) },
                    enabled = !isLoading && ttsReady
                ) {
                    Text(if (ttsReady) "🔊" else "❌")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(input.trim(), translated) },
                    enabled = !isLoading && input.trim().isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}
