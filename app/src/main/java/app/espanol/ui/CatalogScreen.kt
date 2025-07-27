package app.espanol.ui

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.espanol.data.TextPair
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    catalogViewModel: CatalogViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Move metadataViewModel creation here so it is available for import/export
    val context = LocalContext.current
    val metadataViewModel: TextPairMetadataViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TextPairMetadataViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            catalogViewModel.importCatalogFromCsv(uri, metadataViewModel)
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            catalogViewModel.exportResult.collect { result ->
                when (result) {
                    is ExportResult.Success -> snackbarHostState.showSnackbar("Exported to ${result.filePath}")
                    is ExportResult.Failure -> snackbarHostState.showSnackbar("Export failed")
                }
            }
        }
        coroutineScope.launch {
            catalogViewModel.importResult.collect { result ->
                when (result) {
                    is ImportResult.Success -> snackbarHostState.showSnackbar("Imported: ${result.imported}, Skipped: ${result.skipped}")
                    is ImportResult.Failure -> snackbarHostState.showSnackbar("Import failed")
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Translation Catalog",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { catalogViewModel.exportCatalogToCsv(metadataViewModel) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export Catalog"
                )
            }
            IconButton(
                onClick = { importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/csv", "application/vnd.ms-excel", "*/*")) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Import Catalog"
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState)

        CatalogScreenContent(viewModel = catalogViewModel, metadataViewModel = metadataViewModel)
    }
}

// Update CatalogScreenContent to accept metadataViewModel as a parameter
@Composable
fun CatalogScreenContent(viewModel: CatalogViewModel, metadataViewModel: TextPairMetadataViewModel) {
    val textPairs: List<TextPair> by viewModel.textPairs.collectAsStateWithLifecycle(emptyList())
    val editingItemId: Int? by viewModel.editingItemId.collectAsStateWithLifecycle()
    val searchQuery: String by viewModel.searchQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importCatalogFromCsv(uri, metadataViewModel)
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            viewModel.exportResult.collect { result ->
                when (result) {
                    is ExportResult.Success -> snackbarHostState.showSnackbar("Exported to ${result.filePath}")
                    is ExportResult.Failure -> snackbarHostState.showSnackbar("Export failed")
                }
            }
        }
        coroutineScope.launch {
            viewModel.importResult.collect { result ->
                when (result) {
                    is ImportResult.Success -> snackbarHostState.showSnackbar("Imported: ${result.imported}, Skipped: ${result.skipped}")
                    is ImportResult.Failure -> snackbarHostState.showSnackbar("Import failed")
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

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
                        onDelete = { viewModel.deleteTextPair(textPair.id) },
                        metadataViewModel = metadataViewModel // <-- Pass the viewmodel here!
                    )
                }
            }
        }
    }
}