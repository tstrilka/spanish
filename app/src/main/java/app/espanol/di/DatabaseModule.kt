package app.espanol.di

import android.content.Context
import app.espanol.data.AppDatabase
import app.espanol.data.LearningProgressDao
import app.espanol.data.TextPairDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideTextPairDao(database: AppDatabase): TextPairDao {
        return database.textPairDao()
    }

    @Provides
    fun provideLearningProgressDao(database: AppDatabase): LearningProgressDao {
        return database.learningProgressDao()
    }
}