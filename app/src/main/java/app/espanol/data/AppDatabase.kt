package app.espanol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TextPair::class], version = 1, exportSchema = true // Enable for production
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun textPairDao(): TextPairDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add favorite column with proper default
                database.execSQL("ALTER TABLE text_pairs ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "espanol-db"
            ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        app.espanol.util.Logger.i("Database created")
                    }
                }).apply {
                    if (app.espanol.BuildConfig.DEBUG) {
                        fallbackToDestructiveMigration()
                    }
                }.build()
        }
    }
}