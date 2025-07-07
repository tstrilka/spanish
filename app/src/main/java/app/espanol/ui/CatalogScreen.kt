package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel
) {
    val textPairs by viewModel.textPairs.collectAsStateWithLifecycle(emptyList())
    val editingItemId by viewModel.editingItemId.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Translation Catalog",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (textPairs.isEmpty()) {
            Text(
                text = "No translations saved yet.\nAdd some translations first!",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(textPairs) { textPair ->
                    CatalogItem(
                        textPair = textPair,
                        isEditing = editingItemId == textPair.id,
                        onEdit = { viewModel.startEditing(textPair.id) },
                        onSave = { updatedPair ->
                            viewModel.saveTextPair(
                                id = updatedPair.id,
                                original = updatedPair.original,
                                translated = updatedPair.translated
                            )
                        },
                        onCancel = { viewModel.cancelEditing() },
                        onDelete = { viewModel.deleteTextPair(textPair.id) }
                    )
                }
            }
        }
    }
}