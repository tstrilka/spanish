package app.espanol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.espanol.data.LearningProgress
import app.espanol.data.LearningProgressDao
import app.espanol.data.TextPair
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val learningProgressDao: LearningProgressDao
) : ViewModel() {

    private val _currentPair = MutableStateFlow<TextPair?>(null)
    val currentPair: StateFlow<TextPair?> = _currentPair.asStateFlow()

    private val _showTranslation = MutableStateFlow(false)
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadNextPair() {
        viewModelScope.launch {
            _isLoading.value = true
            _showTranslation.value = false

            try {
                val pair = learningProgressDao.getRandomPairForLearning()
                    ?: learningProgressDao.getRandomPair()

                if (pair == null) {
                    app.espanol.util.Logger.w("No text pairs available for learning")
                }

                _currentPair.value = pair
            } catch (e: Exception) {
                app.espanol.util.Logger.e("Failed to load learning pair", e)
                _currentPair.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showTranslation() {
        _showTranslation.value = true
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