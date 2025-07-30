package app.spanish.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.spanish.data.TextPair
import app.spanish.data.TextPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val repository: TextPairRepository
) : ViewModel() {

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    val recentPairs = repository.getRecentPairs(10)

    private val _searchResults = MutableStateFlow<List<TextPair>>(emptyList())
    val searchResults: StateFlow<List<TextPair>> = _searchResults.asStateFlow()

    // Store current pairs for export
    private val _currentPairs = MutableStateFlow<List<TextPair>>(emptyList())

    init {
        // Collect recent pairs to use for export
        viewModelScope.launch {
            recentPairs.collect { pairs ->
                _currentPairs.value = pairs
            }
        }
    }

    fun saveTextPair(original: String, translated: String) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            repository.insertTextPair(
                TextPair(original = original, translated = translated)
            ).fold(
                onSuccess = { _saveState.value = SaveState.Success },
                onFailure = { _saveState.value = SaveState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    private var searchJob: Job? = null

    fun searchTranslations(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.searchTextPairs(query).collect { results ->
                _searchResults.value = results
            }
        }
    }

    fun exportTranslations(): String {
        return buildString {
            appendLine("Czech,Spanish,Date")
            _currentPairs.value.forEach { pair ->
                val date =
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(pair.createdAt))
                appendLine("\"${pair.original}\",\"${pair.translated}\",\"$date\"")
            }
        }
    }

    fun deleteTextPair(pair: TextPair) {
        viewModelScope.launch {
            repository.deleteTextPair(pair.id).fold(
                onSuccess = {
                    app.spanish.util.Logger.i("Deleted text pair: ${pair.id}")
                },
                onFailure = { error ->
                    app.spanish.util.Logger.e("Failed to delete text pair", error)
                }
            )
        }
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
