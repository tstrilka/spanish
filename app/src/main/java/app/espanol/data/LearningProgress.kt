package app.espanol.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_progress",
    foreignKeys = [
        ForeignKey(
            entity = TextPair::class,
            parentColumns = ["id"],
            childColumns = ["textPairId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("textPairId")]
)
data class LearningProgress(
    @PrimaryKey val textPairId: Int,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val lastAttempt: Long = 0
)