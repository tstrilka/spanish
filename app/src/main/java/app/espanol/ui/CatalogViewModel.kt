package app.espanol.ui

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.espanol.data.TextPair
import app.espanol.data.TextPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExportResult {
    data class Success(val filePath: String) : ExportResult()
    object Failure : ExportResult()
}

sealed class ImportResult {
    data class Success(val imported: Int, val skipped: Int) : ImportResult()
    object Failure : ImportResult()
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: TextPairRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editingItemId = MutableStateFlow<Int?>(null)
    val editingItemId: StateFlow<Int?> = _editingItemId.asStateFlow()

    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult

    private val _importResult = MutableSharedFlow<ImportResult>()
    val importResult: SharedFlow<ImportResult> = _importResult

    val textPairs = repository.getAllTextPairs()
        .combine(searchQuery) { pairs, query ->
            if (query.isBlank()) {
                pairs
            } else {
                pairs.filter { pair ->
                    pair.original.contains(query, ignoreCase = true) ||
                            pair.translated.contains(query, ignoreCase = true)
                }
            }
        }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startEditing(id: Int) {
        _editingItemId.value = id
    }

    fun cancelEditing() {
        _editingItemId.value = null
    }

    fun saveTextPair(id: Int, original: String, translated: String) {
        viewModelScope.launch {
            val textPair = TextPair(id = id, original = original, translated = translated)
            repository.updateTextPair(textPair).fold(
                onSuccess = {
                    _editingItemId.value = null
                    app.espanol.util.Logger.i("Updated text pair: $id")
                },
                onFailure = { error ->
                    app.espanol.util.Logger.e("Failed to update text pair", error)
                }
            )
        }
    }

    fun deleteTextPair(id: Int) {
        viewModelScope.launch {
            repository.deleteTextPair(id).fold(
                onSuccess = {
                    _editingItemId.value = null
                    app.espanol.util.Logger.i("Deleted text pair: $id")
                },
                onFailure = { error ->
                    app.espanol.util.Logger.e("Failed to delete text pair", error)
                }
            )
        }
    }

    fun exportCatalogToCsv() {
        viewModelScope.launch {
            try {
                val pairs = textPairs.first()
                val csvBuilder = StringBuilder()
                csvBuilder.append("Original,Translated\n")
                for (pair in pairs) {
                    val original = pair.original.replace("\"", "\"\"")
                    val translated = pair.translated.replace("\"", "\"\"")
                    csvBuilder.append("\"$original\",\"$translated\"\n")
                }
                // Save to Downloads folder
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "catalog_export_$timestamp.csv"
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = downloadsDir?.resolve(fileName)
                file?.writeText(csvBuilder.toString())
                if (file != null) {
                    _exportResult.emit(ExportResult.Success(file.absolutePath))
                } else {
                    _exportResult.emit(ExportResult.Failure)
                }
            } catch (e: Exception) {
                _exportResult.emit(ExportResult.Failure)
            }
        }
    }

    fun importCatalogFromCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _importResult.emit(ImportResult.Failure)
                    return@launch
                }
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                reader.close()
                if (lines.isEmpty()) {
                    _importResult.emit(ImportResult.Failure)
                    return@launch
                }
                val pairsToImport = mutableListOf<TextPair>()
                for (line in lines.drop(1)) { // skip header
                    val regex = "^\"(.*)\",\"(.*)\"$".toRegex()
                    val match = regex.find(line)
                    if (match != null) {
                        val (original, translated) = match.destructured
                        pairsToImport.add(TextPair(original = original, translated = translated))
                    }
                }
                val existingPairs = textPairs.first()
                val existingSet = existingPairs.map { it.original to it.translated }.toSet()
                var imported = 0
                var skipped = 0
                for (pair in pairsToImport) {
                    if ((pair.original to pair.translated) in existingSet) {
                        skipped++
                    } else {
                        repository.insertTextPair(pair)
                        imported++
                    }
                }
                _importResult.emit(ImportResult.Success(imported, skipped))
            } catch (e: Exception) {
                _importResult.emit(ImportResult.Failure)
            }
        }
    }
}