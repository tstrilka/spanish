package app.spanish.data

import app.spanish.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextPairRepository @Inject constructor(
    private val dao: TextPairDao
) {
    fun getAllTextPairs(): Flow<List<TextPair>> = dao.getAll()
        .catch { emit(emptyList()) }

    fun searchTextPairs(query: String): Flow<List<TextPair>> = dao.searchTextPairs(query)
        .catch { emit(emptyList()) }

    fun getRecentPairs(limit: Int = 10): Flow<List<TextPair>> = dao.getRecentPairs(limit)
        .catch { emit(emptyList()) }

    suspend fun insertTextPair(pair: TextPair): Result<Long> {
        return try {
            if (pair.original.isBlank() || pair.translated.isBlank()) {
                Result.failure(IllegalArgumentException("Text pairs cannot be empty"))
            } else {
                val existing = dao.findByOriginalAndTranslated(
                    pair.original.trim(),
                    pair.translated.trim()
                )
                if (existing != null) {
                    Result.failure(IllegalArgumentException("This translation already exists"))
                } else {
                    Result.success(dao.insert(pair))
                }
            }
        } catch (e: Exception) {
            Logger.e("Failed to insert text pair", e)
            Result.failure(e)
        }
    }

    suspend fun updateTextPair(pair: TextPair): Result<Unit> {
        return try {
            if (pair.original.isBlank() || pair.translated.isBlank()) {
                Result.failure(IllegalArgumentException("Text pairs cannot be empty"))
            } else {
                // Check for duplicate with other pairs (excluding this one)
                val existingPair = dao.findDuplicateForUpdate(
                    pair.original.trim(),
                    pair.translated.trim(),
                    pair.id
                )
                if (existingPair != null) {
                    Result.failure(IllegalArgumentException("This translation already exists"))
                } else {
                    dao.update(pair)
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Logger.e("Failed to update text pair", e)
            Result.failure(e)
        }
    }

    suspend fun getCount(): Int {
        return try {
            dao.getCount()
        } catch (e: Exception) {
            Logger.e("Failed to get count", e)
            0
        }
    }

    suspend fun deleteTextPair(id: Int): Result<Unit> {
        return try {
            dao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to delete text pair", e)
            Result.failure(e)
        }
    }
}