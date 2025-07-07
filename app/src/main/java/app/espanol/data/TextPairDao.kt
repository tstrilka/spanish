package app.espanol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TextPairDao {
    @Insert
    suspend fun insert(pair: TextPair): Long

    @Query("SELECT * FROM text_pairs ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TextPair>>

    @Query("SELECT * FROM text_pairs WHERE original LIKE '%' || :searchQuery || '%' OR translated LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    fun searchTextPairs(searchQuery: String): Flow<List<TextPair>>

    @Query("SELECT COUNT(*) FROM text_pairs")
    suspend fun getCount(): Int

    @Query("DELETE FROM text_pairs WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM text_pairs ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPairs(limit: Int): Flow<List<TextPair>>

    @Query("SELECT * FROM text_pairs WHERE LOWER(TRIM(original)) = LOWER(TRIM(:original)) AND LOWER(TRIM(translated)) = LOWER(TRIM(:translated)) LIMIT 1")
    suspend fun findByOriginalAndTranslated(original: String, translated: String): TextPair?

    @Update
    suspend fun update(pair: TextPair)

    @Query("SELECT * FROM text_pairs WHERE LOWER(TRIM(original)) = LOWER(TRIM(:original)) AND LOWER(TRIM(translated)) = LOWER(TRIM(:translated)) AND id != :excludeId LIMIT 1")
    suspend fun findDuplicateForUpdate(original: String, translated: String, excludeId: Int): TextPair?
}