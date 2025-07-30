package app.spanish.data

import app.spanish.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextPairMetadataRepository @Inject constructor(
    private val metadataDao: TextPairMetadataDao
) {
    fun getAllCategories(): Flow<List<String>> =
        metadataDao.getAllCategories().catch { emit(emptyList()) }

    suspend fun getMetadataForTextPair(textPairId: Int): List<TextPairMetadata> {
        return try {
            metadataDao.getMetadataForTextPair(textPairId)
        } catch (e: Exception) {
            Logger.e("Failed to get metadata for text pair", e)
            emptyList()
        }
    }

    suspend fun updateMetadataForTextPair(textPairId: Int, categories: List<String>): Result<Unit> {
        return try {
            metadataDao.deleteAllForTextPair(textPairId)

            for (category in categories) {
                if (category.isNotBlank()) {
                    metadataDao.insertMetadata(
                        TextPairMetadata(
                            textPairId = textPairId,
                            category = category.trim()
                        )
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to update metadata for text pair", e)
            Result.failure(e)
        }
    }
}