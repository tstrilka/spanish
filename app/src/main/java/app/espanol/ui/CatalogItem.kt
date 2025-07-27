package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.espanol.data.TextPair

@Composable
fun CatalogItem(
    textPair: TextPair,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onSave: (TextPair) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    metadataViewModel: TextPairMetadataViewModel? = null,
    modifier: Modifier = Modifier
) {
    var editedOriginal by remember(textPair.original) { mutableStateOf(textPair.original) }
    var editedTranslated by remember(textPair.translated) { mutableStateOf(textPair.translated) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    var categories by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(textPair.id) {
        metadataViewModel?.getCategoriesForTextPair(textPair.id)?.collect { newCategories ->
            categories = newCategories
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isEditing) {
                // Edit mode remains unchanged
                Text(
                    text = "Edit Translation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = editedOriginal,
                    onValueChange = { editedOriginal = it },
                    label = { Text("Czech") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = editedTranslated,
                    onValueChange = { editedTranslated = it },
                    label = { Text("Spanish") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSave(textPair.copy(
                                original = editedOriginal.trim(),
                                translated = editedTranslated.trim()
                            ))
                        },
                        enabled = editedOriginal.isNotBlank() && editedTranslated.isNotBlank(),
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
                        Text("Save")
                    }

                    OutlinedButton(
                        onClick = {
                            editedOriginal = textPair.original
                            editedTranslated = textPair.translated
                            onCancel()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    ) {
                        Text("Cancel")
                    }
                }

                // Add category editing in edit mode as well
                if (metadataViewModel != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Display current categories
                    if (categories.isNotEmpty()) {
                        Text(
                            text = "Categories:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.forEach { category ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(category) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showCategoryDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Edit Categories")
                    }
                }
            } else {
                // Display mode
                Text(
                    text = "Czech: ${textPair.original}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Spanish: ${textPair.translated}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )

                // Add categories display
                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { category ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

// In display mode (when not editing), make the category button more visible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),  // Add spacing between items
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Make a standalone button for categories that's clearly visible
                    if (metadataViewModel != null) {
                        Button(
                            onClick = { showCategoryDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Categories")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))  // Push the following buttons to the right

                    // Your existing edit/delete buttons
                    IconButton(onClick = { onEdit() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = { onDelete() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // Show the category dialog as an overlay
    if (showCategoryDialog && metadataViewModel != null) {
        // The dialog overlay
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
        ) {
            // Make sure TextPairEditMetadata loads and refreshes the data for this text pair
            LaunchedEffect(textPair.id) {
                metadataViewModel.loadCategoriesForTextPair(textPair.id)
            }

            TextPairEditMetadata(
                textPairId = textPair.id,
                viewModel = metadataViewModel,
                onDismiss = {
                    showCategoryDialog = false
                    // Reload categories after editing
                    metadataViewModel.saveMetadata(textPair.id)
                }
            )
        }
    }
}