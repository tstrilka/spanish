package app.espanol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.espanol.data.TextPair
import app.espanol.data.TextPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: TextPairRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editingItemId = MutableStateFlow<Int?>(null)
    val editingItemId: StateFlow<Int?> = _editingItemId.asStateFlow()

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
}