package app.espanol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.espanol.data.TextPairMetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextPairMetadataViewModel @Inject constructor(
    private val metadataRepository: TextPairMetadataRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val categoriesCache = mutableMapOf<Int, MutableStateFlow<List<String>>>()

    init {
        viewModelScope.launch {
            metadataRepository.getAllCategories().collect { allCategories ->
                _categories.value = allCategories
            }
        }
    }

    fun getCategoriesForTextPair(textPairId: Int): StateFlow<List<String>> {
        return categoriesCache.getOrPut(textPairId) {
            MutableStateFlow<List<String>>(emptyList()).also { flow ->
                viewModelScope.launch {
                    val metadata = metadataRepository.getMetadataForTextPair(textPairId)
                    flow.value = metadata.map { it.category }
                }
            }
        }
    }

    fun loadCategoriesForTextPair(textPairId: Int) {
        viewModelScope.launch {
            val metadata = metadataRepository.getMetadataForTextPair(textPairId)
            _selectedCategories.value = metadata.map { it.category }

            categoriesCache[textPairId]?.value = metadata.map { it.category }
        }
    }

    fun updateSelectedCategories(categories: List<String>) {
        _selectedCategories.value = categories
    }

    fun saveMetadata(textPairId: Int): Boolean {
        var success = false
        viewModelScope.launch {
            metadataRepository.updateMetadataForTextPair(textPairId, _selectedCategories.value)
                .onSuccess {
                    success = true
                    categoriesCache[textPairId]?.value = _selectedCategories.value
                }
        }
        return success
    }
}