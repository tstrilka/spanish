package app.spanish.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.spanish.data.AppDatabase
import app.spanish.data.TextPairMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
        metadataDao.getMetadataForTextPairFlow(id)
            .map { it.map { meta -> meta.category } }

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

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            db.textPairMetadataDao().renameCategory(oldName, newName)
        }
    }

    fun removeCategory(category: String) {
        viewModelScope.launch {
            db.textPairMetadataDao().removeCategory(category)
        }
    }

    fun joinCategories(catA: String, catB: String, mergedName: String) {
        viewModelScope.launch {
            db.textPairMetadataDao().joinCategories(catA, catB, mergedName)
        }
    }

    companion object {
        val defaultCategories = listOf("Food", "Travel", "Work", "Family")
    }
}