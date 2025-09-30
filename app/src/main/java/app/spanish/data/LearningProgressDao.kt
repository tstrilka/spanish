package app.spanish.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LearningProgress)

    @Query("SELECT * FROM learning_progress WHERE textPairId = :textPairId")
    suspend fun getProgress(textPairId: Int): LearningProgress?

    @Query("""
        SELECT tp.* FROM text_pairs tp
        LEFT JOIN learning_progress lp ON tp.id = lp.textPairId
        WHERE (lp.textPairId IS NULL OR lp.lastAttempt < :cutoffTime)
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomPairForLearning(cutoffTime: Long = System.currentTimeMillis() - 24 * 60 * 60 * 1000): TextPair?

    @Query("""
    SELECT DISTINCT tp.* FROM text_pairs tp
    JOIN text_pair_metadata tpm ON tp.id = tpm.textPairId
    LEFT JOIN learning_progress lp ON tp.id = lp.textPairId
    WHERE LOWER(tpm.category) = LOWER(:category) 
    AND (lp.textPairId IS NULL OR lp.lastAttempt < :cutoffTime)
    ORDER BY RANDOM()
    LIMIT 1
""")
    suspend fun getRandomPairForLearningWithCategory(
        category: String,
        cutoffTime: Long = System.currentTimeMillis() - 24 * 60 * 60 * 1000
    ): TextPair?

    @Query("SELECT tp.* FROM text_pairs tp ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPair(): TextPair?

    @Query("""
        SELECT tp.* FROM text_pairs tp
        JOIN learning_progress lp ON tp.id = lp.textPairId
        ORDER BY lp.successCount DESC
        LIMIT :limit
    """)
    fun getMostSuccessfulPairs(limit: Int): Flow<List<TextPair>>

    @Query("""
        SELECT tp.* FROM text_pairs tp
        JOIN learning_progress lp ON tp.id = lp.textPairId
        ORDER BY lp.failureCount DESC
        LIMIT :limit
    """)
    fun getMostDifficultPairs(limit: Int): Flow<List<TextPair>>

    @Query("SELECT * FROM text_pairs WHERE id = :id LIMIT 1")
    suspend fun getPairById(id: Int): TextPair?

    @Query("DELETE FROM learning_progress WHERE textPairId = :textPairId")
    suspend fun deleteProgress(textPairId: Int)
}