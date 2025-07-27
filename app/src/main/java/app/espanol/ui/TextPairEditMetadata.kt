package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TextPairEditMetadata(
    textPairId: Int,
    viewModel: TextPairMetadataViewModel,
    onDismiss: () -> Unit
) {
    var newCategory by remember { mutableStateOf("") }
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val allCategories by viewModel.categories.collectAsStateWithLifecycle()

    LaunchedEffect(textPairId) {
        viewModel.loadCategoriesForTextPair(textPairId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Edit Categories",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display selected categories as chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedCategories.forEach { category ->
                InputChip(
                    selected = true,
                    onClick = { },
                    label = { Text(category) },
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.updateSelectedCategories(
                                selectedCategories.filter { it != category }
                            )
                        }) {
                            Icon(Icons.Default.Close, "Remove category")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add new category
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                label = { Text("Add category") },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (newCategory.isNotBlank() && !selectedCategories.contains(newCategory.trim())) {
                        viewModel.updateSelectedCategories(
                            selectedCategories + newCategory.trim()
                        )
                        newCategory = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, "Add category")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Suggest existing categories
        Text(
            text = "Suggested Categories",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allCategories
                .filter { it !in selectedCategories }
                .forEach { category ->
                    SuggestionChip(
                        onClick = {
                            viewModel.updateSelectedCategories(
                                selectedCategories + category
                            )
                        },
                        label = { Text(category) }
                    )
                }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    viewModel.saveMetadata(textPairId)
                    onDismiss()
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Save")
            }
        }
    }
}