package app.espanol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LearningProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LearningProgress)

    @Query("SELECT * FROM learning_progress WHERE textPairId = :textPairId")
    suspend fun getProgress(textPairId: Int): LearningProgress?

    @Query(
        """
        SELECT tp.* FROM text_pairs tp 
        LEFT JOIN learning_progress lp ON tp.id = lp.textPairId 
        WHERE (lp.successCount IS NULL OR lp.successCount < lp.failureCount OR lp.successCount < 3)
        ORDER BY RANDOM() LIMIT 1
    """
    )
    suspend fun getRandomPairForLearning(): TextPair?

    @Query("SELECT tp.* FROM text_pairs tp ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPair(): TextPair?
}