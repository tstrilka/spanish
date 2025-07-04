package app.espanol.data

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
                val id = dao.insert(pair)
                app.espanol.util.Logger.i("Saved text pair with ID: $id")
                Result.success(id)
            }
        } catch (e: Exception) {
            app.espanol.util.Logger.e("Failed to save text pair", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTextPair(id: Int): Result<Unit> {
        return try {
            dao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCount(): Int = try {
        dao.getCount()
    } catch (e: Exception) {
        0
    }
}