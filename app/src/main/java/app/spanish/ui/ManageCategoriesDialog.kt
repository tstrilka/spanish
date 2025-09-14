package app.spanish.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ManageCategoriesDialog(
    viewModel: TextPairMetadataViewModel,
    onDismiss: () -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Categories") },
        text = {
            Column {
                categories.forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Allow multi-select (up to 2)
                        androidx.compose.material3.Checkbox(
                            checked = selectedCategories.contains(cat),
                            onCheckedChange = {
                                selectedCategories = if (it) {
                                    (selectedCategories + cat).take(2).toSet()
                                } else {
                                    selectedCategories - cat
                                }
                            },
                            enabled = selectedCategories.size < 2 || selectedCategories.contains(cat)
                        )
                        Text(cat, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                viewModel.removeCategory(cat); selectedCategories =
                                selectedCategories - cat
                            }
                        ) { Text("Remove") }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Rename/Join to") },
                    enabled = selectedCategories.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row {
                Button(
                    onClick = {
                        if (selectedCategories.size == 1 && newName.isNotBlank()) {
                            viewModel.renameCategory(selectedCategories.first(), newName)
                            newName = ""
                            selectedCategories = emptySet()
                            onDismiss()
                        }
                    },
                    enabled = selectedCategories.size == 1 && newName.isNotBlank()
                ) { Text("Rename") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (selectedCategories.size == 2 && newName.isNotBlank()) {
                            val (catA, catB) = selectedCategories.toList()
                            viewModel.joinCategories(catA, catB, newName)
                            newName = ""
                            selectedCategories = emptySet()
                            onDismiss()
                        }
                    },
                    enabled = selectedCategories.size == 2 && newName.isNotBlank()
                ) { Text("Join") }            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}