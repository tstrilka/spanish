package app.espanol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TextPairMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: TextPairMetadata): Long

    @Query("DELETE FROM text_pair_metadata WHERE textPairId = :textPairId")
    suspend fun deleteAllForTextPair(textPairId: Int)

    @Query("SELECT * FROM text_pair_metadata WHERE textPairId = :textPairId")
    suspend fun getMetadataForTextPair(textPairId: Int): List<TextPairMetadata>

    @Query("SELECT DISTINCT category FROM text_pair_metadata ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM text_pair_metadata WHERE category = :category")
    suspend fun getCountForCategory(category: String): Int

    @Query("SELECT textPairId FROM text_pair_metadata WHERE category = :category")
    fun getTextPairIdsForCategory(category: String): List<Int>

    @Query("""
        SELECT tp.* FROM text_pairs tp
        JOIN text_pair_metadata tpm ON tp.id = tpm.textPairId
        WHERE LOWER(tpm.category) = LOWER(:category)
    """)
    suspend fun getPairsForCategory(category: String): List<TextPair>
}