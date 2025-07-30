package app.spanish.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import app.spanish.data.TextPair

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
    var categoryVersion by remember { mutableStateOf(0) } // Add this line

    var categories by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(textPair.id, categoryVersion, isEditing) { // Add isEditing as a key
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSave(textPair.copy(
                                original = editedOriginal.trim(),
                                translated = editedTranslated.trim()
                            ))
                            showCategoryDialog = false
                        },
                        enabled = editedOriginal.isNotBlank() && editedTranslated.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = androidx.compose.ui.graphics.Color.White // White text
                        )
                    ) {
                        Text("Save")
                    }

                    OutlinedButton(
                        onClick = {
                            editedOriginal = textPair.original
                            editedTranslated = textPair.translated
                            onCancel()
                            showCategoryDialog = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Show both edit and category icons for the category edit button
                    OutlinedButton(
                        onClick = { showCategoryDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Edit categories",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Remove the old Edit Categories button (and any related code)
            } else {
                // Display mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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

                        Spacer(modifier = Modifier.height(8.dp))
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
                            if (categories.isNotEmpty()) {
                                categories.forEach { category ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(category) }
                                    )
                                }
                            } else {
                                Text(
                                    text = "None",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // Place edit and delete buttons vertically at the right edge
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.End
                    ) {
                        IconButton(
                            onClick = { onEdit() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp)) // Add more space between buttons
                        IconButton(
                            onClick = { onDelete() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Show the category dialog as a full-screen overlay for visibility
    if (showCategoryDialog && metadataViewModel != null) {
        val editedTextPair = textPair.copy(
            original = editedOriginal.trim(),
            translated = editedTranslated.trim()
        )
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .heightIn(max = 500.dp)
            ) {
                // Save categories immediately when changed
                TextPairEditMetadata(
                    textPairId = editedTextPair.id,
                    viewModel = metadataViewModel,
                    onDismiss = {
                        showCategoryDialog = false
                        metadataViewModel.saveMetadata(editedTextPair.id)
                        categoryVersion++
                    },
                    showButtons = false
                )
            }
        }
    }
}