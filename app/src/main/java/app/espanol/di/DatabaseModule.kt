package app.espanol.di

import android.content.Context
import app.espanol.data.AppDatabase
import app.espanol.data.LearningProgressDao
import app.espanol.data.TextPairDao
import app.espanol.data.TextPairMetadataDao
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
    @Singleton
    fun provideTextPairDao(appDatabase: AppDatabase): TextPairDao {
        return appDatabase.textPairDao()
    }

    @Provides
    @Singleton
    fun provideLearningProgressDao(appDatabase: AppDatabase): LearningProgressDao {
        return appDatabase.learningProgressDao()
    }

    @Provides
    @Singleton
    fun provideTextPairMetadataDao(appDatabase: AppDatabase): TextPairMetadataDao {
        return appDatabase.textPairMetadataDao()
    }
}