package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.espanol.data.TextPair

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel
) {
    val textPairs: List<TextPair> by viewModel.textPairs.collectAsStateWithLifecycle(emptyList())
    val editingItemId: Int? by viewModel.editingItemId.collectAsStateWithLifecycle()
    val searchQuery: String by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Translation Catalog",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search translations...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (textPairs.isEmpty()) {
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "No translations found matching \"$searchQuery\""
                } else {
                    "No translations saved yet.\n\nAdd some translations first!"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
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