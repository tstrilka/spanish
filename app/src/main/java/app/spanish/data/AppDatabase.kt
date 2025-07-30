package app.spanish.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TextPair::class, TextPairMetadata::class, LearningProgress::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun textPairDao(): TextPairDao
    abstract fun textPairMetadataDao(): TextPairMetadataDao
    abstract fun learningProgressDao(): LearningProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spanish-db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table with composite primary key
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `text_pair_metadata` (
                `textPairId` INTEGER NOT NULL,
                `category` TEXT NOT NULL,
                PRIMARY KEY(`textPairId`, `category`),
                FOREIGN KEY(`textPairId`) REFERENCES `text_pairs`(`id`) ON DELETE CASCADE
            )
        """
                )

                // Create index
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_text_pair_metadata_textPairId` ON `text_pair_metadata` (`textPairId`)")

                // Assign a default category to all existing text pairs
                database.execSQL(
                    """
            INSERT INTO text_pair_metadata (textPairId, category)
            SELECT id, 'Uncategorized' FROM text_pairs
        """
                )
            }
        }
    }
}