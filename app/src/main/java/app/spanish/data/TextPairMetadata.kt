package app.spanish.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "text_pair_metadata",
    primaryKeys = ["textPairId", "category"],
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
data class TextPairMetadata(
    val textPairId: Int,
    val category: String
)