package app.espanol.di

import android.content.Context
import app.espanol.speech.SpeechRecognitionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SpeechModule {

    @Provides
    @Singleton
    fun provideSpeechRecognitionManager(@ApplicationContext context: Context): SpeechRecognitionManager {
        return SpeechRecognitionManager(context)
    }
}