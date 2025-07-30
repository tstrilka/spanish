package app.spanish.di

import android.content.Context
import app.spanish.data.AppDatabase
import app.spanish.data.LearningProgressDao
import app.spanish.data.TextPairDao
import app.spanish.data.TextPairMetadataDao
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