package app.spanish.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "text_pairs",
    indices = [Index(value = ["original"]), Index(value = ["translated"]), Index(value = ["createdAt"])]
)
data class TextPair(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val original: String,
    val translated: String,
    val createdAt: Long = System.currentTimeMillis()
)