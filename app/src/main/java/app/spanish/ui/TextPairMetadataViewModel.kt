package app.spanish.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.spanish.data.AppDatabase
import app.spanish.data.TextPairMetadata
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TextPairMetadataViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val metadataDao = db.textPairMetadataDao()

    // All available categories (from Room)
    val categories: StateFlow<List<String>> = metadataDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected categories for the active text pair
    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    fun getCategoriesForTextPair(id: Int): Flow<List<String>> =
        flow {
            emit(metadataDao.getMetadataForTextPair(id).map { it.category })
        }

    fun getTextPairIdsForCategory(category: String): List<Int> =
        metadataDao.getTextPairIdsForCategory(category)

    fun loadCategoriesForTextPair(id: Int) {
        viewModelScope.launch {
            val categories = metadataDao.getMetadataForTextPair(id).map { it.category }
            _selectedCategories.value = categories
        }
    }

    fun updateSelectedCategories(categories: List<String>) {
        _selectedCategories.value = categories
    }

    fun saveMetadata(id: Int) {
        viewModelScope.launch {
            // Remove all previous categories for this text pair
            metadataDao.deleteAllForTextPair(id)
            // Save all selected categories as-is (do not auto-remove "Uncategorized")
            val categoriesToSave = _selectedCategories.value.ifEmpty { listOf("Uncategorized") }
            categoriesToSave.forEach { category ->
                metadataDao.insertMetadata(TextPairMetadata(textPairId = id, category = category))
            }
        }
    }

    companion object {
        val defaultCategories = listOf("Food", "Travel", "Work", "Family")
    }
}