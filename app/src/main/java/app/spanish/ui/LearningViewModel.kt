package app.spanish.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.spanish.data.LearningProgress
import app.spanish.data.LearningProgressDao
import app.spanish.data.TextPair
import app.spanish.data.TextPairMetadataDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val learningProgressDao: LearningProgressDao,
    private val textPairMetadataDao: TextPairMetadataDao
) : ViewModel() {

    private val _currentPair = MutableStateFlow<TextPair?>(null)
    val currentPair: StateFlow<TextPair?> = _currentPair

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showTranslation = MutableStateFlow(false)
    val showTranslation: StateFlow<Boolean> = _showTranslation

    private val _hasPlayedAudio = MutableStateFlow(false)
    val hasPlayedAudio: StateFlow<Boolean> = _hasPlayedAudio

    private val _learningMode = MutableStateFlow(LearningMode.VISUAL)
    val learningMode: StateFlow<LearningMode> = _learningMode

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private var lastPairId: Int? = null

    private val _categoryTotalCount = MutableStateFlow(0)
    val categoryTotalCount: StateFlow<Int> = _categoryTotalCount

    private val _categorySuccessCount = MutableStateFlow(0)
    val categorySuccessCount: StateFlow<Int> = _categorySuccessCount

    init {
        loadCategories()

        // Observe changes in metadata/categories and recompute counts when metadata changes.
        viewModelScope.launch {
            textPairMetadataDao.getAllCategories().collect {
                // Recompute only if a category is selected (keeps zero when "All" selected)
                val category = _selectedCategory.value
                if (category != null) {
                    try {
                        val allPairs = textPairMetadataDao.getPairsForCategory(category)
                        _categoryTotalCount.value = allPairs.size

                        val ids = allPairs.map { it.id }
                        var successCount = 0
                        for (id in ids) {
                            val progress = learningProgressDao.getProgress(id)
                            if ((progress?.successCount ?: 0) > 0) successCount++
                        }
                        _categorySuccessCount.value = successCount
                    } catch (e: Exception) {
                        app.spanish.util.Logger.e("Failed to update category progress on metadata change", e)
                        _categoryTotalCount.value = 0
                        _categorySuccessCount.value = 0
                    }
                } else {
                    _categoryTotalCount.value = 0
                    _categorySuccessCount.value = 0
                }
            }
        }
    }

    fun updateCategoryProgress() {
        viewModelScope.launch {
            val category = _selectedCategory.value
            if (category != null) {
                val allPairs = textPairMetadataDao.getPairsForCategory(category)
                _categoryTotalCount.value = allPairs.size

                val ids = allPairs.map { it.id }
                val successCount = ids.count { id ->
                    val progress = learningProgressDao.getProgress(id)
                    progress?.successCount ?: 0 > 0
                }
                _categorySuccessCount.value = successCount
            } else {
                _categoryTotalCount.value = 0
                _categorySuccessCount.value = 0
            }
        }
    }

    fun resetCategoryProgress() {
        viewModelScope.launch {
            val category = _selectedCategory.value
            if (category != null) {
                val pairs = textPairMetadataDao.getPairsForCategory(category)
                for (pair in pairs) {
                    learningProgressDao.deleteProgress(pair.id)
                }
                updateCategoryProgress()
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                textPairMetadataDao.getAllCategories().collect { dbCategories ->
                    val allCategories = dbCategories
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .sorted()
                    _availableCategories.value = allCategories
                }
            } catch (e: Exception) {
                app.spanish.util.Logger.e("Failed to load categories", e)
                _availableCategories.value = emptyList()
            }
        }
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
        loadNextPair()
        updateCategoryProgress()
    }

    fun setLearningMode(mode: LearningMode) {
        _learningMode.value = mode
        _hasPlayedAudio.value = false
        _showTranslation.value = false
    }

    fun loadNextPair() {
        viewModelScope.launch {
            _isLoading.value = true
            _showTranslation.value = false
            _hasPlayedAudio.value = false

            var pair: TextPair? = null

            try {
                val category = _selectedCategory.value

                if (category != null) {
                    val freshPair = learningProgressDao.getRandomPairForLearningWithCategory(category)
                    if (freshPair != null && freshPair.id != lastPairId) {
                        pair = freshPair
                    } else {
                        val allPairs = textPairMetadataDao.getPairsForCategory(category)
                        val filtered = allPairs.filter { it.id != lastPairId }
                        pair = when {
                            filtered.isNotEmpty() -> filtered.shuffled().first()
                            allPairs.isNotEmpty() -> allPairs.first()
                            else -> null
                        }
                    }
                } else {
                    val freshPair = learningProgressDao.getRandomPairForLearning()
                    if (freshPair != null && freshPair.id != lastPairId) {
                        pair = freshPair
                    } else {
                        val allPairs = mutableListOf<TextPair>()
                        val randomPair = learningProgressDao.getRandomPair()
                        if (randomPair != null) allPairs.add(randomPair)
                        val filtered = allPairs.filter { it.id != lastPairId }
                        pair = when {
                            filtered.isNotEmpty() -> filtered.shuffled().first()
                            allPairs.isNotEmpty() -> allPairs.first()
                            else -> null
                        }
                    }
                }

                _currentPair.value = pair
                lastPairId = pair?.id
            } catch (e: Exception) {
                app.spanish.util.Logger.e("Failed to load learning pair", e)
                _currentPair.value = null
            } finally {
                _isLoading.value = false
            }

            if (_currentPair.value == null && _selectedCategory.value != null) {
                _message.value = "No items available in category '${_selectedCategory.value}'. Please select another category."
            } else if (_currentPair.value == null) {
                _message.value = "No items available for learning. Please add some content first."
            } else {
                _message.value = null
            }
        }
    }

    fun showTranslation() {
        _showTranslation.value = true
    }

    fun markAudioPlayed() {
        _hasPlayedAudio.value = true
    }

    fun markResult(success: Boolean) {
        viewModelScope.launch {
            _currentPair.value?.let { pair ->
                try {
                    val existingProgress = learningProgressDao.getProgress(pair.id)
                    val newProgress = if (existingProgress != null) {
                        existingProgress.copy(
                            successCount = if (success) existingProgress.successCount + 1 else existingProgress.successCount,
                            failureCount = if (!success) existingProgress.failureCount + 1 else existingProgress.failureCount,
                            lastAttempt = System.currentTimeMillis()
                        )
                    } else {
                        LearningProgress(
                            textPairId = pair.id,
                            successCount = if (success) 1 else 0,
                            failureCount = if (!success) 1 else 0,
                            lastAttempt = System.currentTimeMillis()
                        )
                    }
                    learningProgressDao.insertOrUpdate(newProgress)
                    updateCategoryProgress()
                } catch (e: Exception) {
                    app.spanish.util.Logger.e("Failed to save learning progress", e)
                }
            }
        }
    }
}

enum class LearningMode {
    VISUAL,
    LISTENING
}
