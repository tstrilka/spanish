package app.espanol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.input.ImeAction

@Composable
fun TextPairEditMetadata(
    textPairId: Int,
    viewModel: TextPairMetadataViewModel,
    onDismiss: () -> Unit,
    showButtons: Boolean = true // Add flag
) {
    var newCategory by remember { mutableStateOf("") }
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val allCategories by viewModel.categories.collectAsStateWithLifecycle()

    LaunchedEffect(textPairId) {
        viewModel.loadCategoriesForTextPair(textPairId)
    }

    // Save categories immediately when changed
    LaunchedEffect(selectedCategories) {
        viewModel.saveMetadata(textPairId)
    }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .padding(16.dp) // Reduce padding for compactness
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Edit Categories",
            style = MaterialTheme.typography.titleMedium, // Use a less tall style
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display selected categories as chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp), // Less space between chips
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            selectedCategories.forEach { category ->
                InputChip(
                    selected = true,
                    onClick = { },
                    label = { Text(category, style = MaterialTheme.typography.bodySmall) }, // Smaller text
                    modifier = Modifier.height(32.dp), // Make chip less tall
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.updateSelectedCategories(
                                    selectedCategories.filter { it != category }
                                )
                            },
                            modifier = Modifier.size(20.dp) // Smaller icon button
                        ) {
                            Icon(
                                Icons.Default.Close,
                                "Remove category",
                                modifier = Modifier.size(16.dp) // Smaller icon
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add new category
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                placeholder = { Text("Add new category", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 20.dp), // Make the edit box less tall
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newCategory.isNotBlank() && !selectedCategories.contains(newCategory.trim())) {
                            viewModel.updateSelectedCategories(
                                selectedCategories + newCategory.trim()
                            )
                            newCategory = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    if (newCategory.isNotBlank() && !selectedCategories.contains(newCategory.trim())) {
                        viewModel.updateSelectedCategories(
                            selectedCategories + newCategory.trim()
                        )
                        newCategory = ""
                    }
                },
                modifier = Modifier.height(32.dp) // Match the edit box height
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add category", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Add", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Suggest existing categories
        Text(
            text = "Suggested Categories",
            style = MaterialTheme.typography.bodySmall, // Smaller label
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        label = { Text(category, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.height(32.dp)
                    )
                }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showButtons) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        viewModel.saveMetadata(textPairId)
                        onDismiss()
                    },
                    modifier = Modifier.padding(start = 8.dp).height(36.dp)
                ) {
                    Text("Save", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        // If showButtons is false, do not show Save/Cancel
    }
}