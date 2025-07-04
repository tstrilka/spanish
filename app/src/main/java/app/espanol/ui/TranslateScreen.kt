package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.espanol.data.TextPair
import app.espanol.translation.TranslationService
import app.espanol.tts.TTSManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    modifier: Modifier = Modifier,
    viewModel: TranslateViewModel,
    onSpeak: (String) -> Unit,
    ttsManager: TTSManager,
    translationService: TranslationService
) {
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val recentPairs by viewModel.recentPairs.collectAsStateWithLifecycle(emptyList())
    val ttsReady by ttsManager.isReady.collectAsStateWithLifecycle()
    val ttsError by ttsManager.error.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle(emptyList())

    ttsError?.let { errorMessage ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                text = "Speech Error: $errorMessage",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    val currentSaveState = saveState
    when (currentSaveState) {
        is SaveState.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "Save Error: ${currentSaveState.message}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        else -> {}
    }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                viewModel.resetSaveState()
            }

            is SaveState.Error -> {
                // Error is already displayed in UI
            }

            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TranslateTextBox(
            onSpeak = onSpeak,
            onSave = { original, translated ->
                viewModel.saveTextPair(original, translated)
            },
            isLoading = saveState is SaveState.Loading,
            ttsReady = ttsReady,
            translationService = translationService
        )

        // Add search functionality
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchTranslations(it)
            },
            label = { Text("Search translations...") },
            modifier = Modifier.fillMaxWidth()
        )

        // Show search results or recent pairs
        val displayPairs = if (searchQuery.isNotBlank()) searchResults else recentPairs

        if (displayPairs.isNotEmpty()) {
            Text(
                text = if (searchQuery.isNotBlank()) "Search Results" else "Recent Translations",
                style = MaterialTheme.typography.headlineSmall
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayPairs) { pair ->
                    TranslationItem(
                        pair = pair,
                        onSpeak = onSpeak
                    )
                }
            }
        }
    }
}

@Composable
fun TranslationItem(
    pair: TextPair,
    onSpeak: (String) -> Unit,
    onDelete: (TextPair) -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "English: ${pair.original}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spanish: ${pair.translated}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { onSpeak(pair.translated) }) {
                        Text("🔊")
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Text("🗑️")
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Translation") },
            text = { Text("Are you sure you want to delete this translation?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(pair)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}