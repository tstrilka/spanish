package app.espanol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.espanol.BuildConfig

@Database(
    entities = [TextPair::class, LearningProgress::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun textPairDao(): TextPairDao
    abstract fun learningProgressDao(): LearningProgressDao

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "espanol-db"
            ).apply {
                if (BuildConfig.DEBUG) {
                    fallbackToDestructiveMigration()
                }
            }.addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    app.espanol.util.Logger.i("Database created")
                }
            }).build()
        }
    }
}