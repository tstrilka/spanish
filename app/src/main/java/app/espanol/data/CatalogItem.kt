package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    var editedOriginal by remember(textPair.original) { mutableStateOf(textPair.original) }
    var editedTranslated by remember(textPair.translated) { mutableStateOf(textPair.translated) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isEditing) {
                // Edit mode
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
                        enabled = editedOriginal.isNotBlank() && editedTranslated.isNotBlank()
                    ) {
                        Text("Save")
                    }

                    OutlinedButton(
                        onClick = {
                            // Reset the edited values
                            editedOriginal = textPair.original
                            editedTranslated = textPair.translated
                            onCancel()
                        }
                    ) {
                        Text("Cancel")
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
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Text("✏️")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDelete) {
                        Text("🗑️")
                    }
                }
            }
        }
    }
}