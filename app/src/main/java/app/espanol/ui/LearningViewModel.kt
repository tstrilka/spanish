package app.espanol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.espanol.data.LearningProgress
import app.espanol.data.LearningProgressDao
import app.espanol.data.TextPair
import app.espanol.data.TextPairMetadataDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val learningProgressDao: LearningProgressDao,
    private val textPairMetadataDao: TextPairMetadataDao  // Add this dependency
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

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                textPairMetadataDao.getAllCategories().collect { allCategories ->
                    _availableCategories.value = allCategories
                }
            } catch (e: Exception) {
                app.espanol.util.Logger.e("Failed to load categories", e)
            }
        }
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
        loadNextPair()
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
                    pair = learningProgressDao.getRandomPairForLearningWithCategory(category)

                    if (pair == null) {
                        app.espanol.util.Logger.w("No text pairs available for learning in category: $category")
                    }
                } else {
                    pair = learningProgressDao.getRandomPairForLearning()
                        ?: learningProgressDao.getRandomPair()
                }

                _currentPair.value = pair
            } catch (e: Exception) {
                app.espanol.util.Logger.e("Failed to load learning pair", e)
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
                } catch (e: Exception) {
                    app.espanol.util.Logger.e("Failed to save learning progress", e)
                }
            }
        }
    }
}

enum class LearningMode {
    VISUAL,
    LISTENING
}