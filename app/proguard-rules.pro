# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
-keep class app.spanish.data.** { *; }

# Hilt
-dontwarn dagger.hilt.processor.internal.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }
-keep class **_HiltModules* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# TTS
-keep class android.speech.tts.** { *; }
-dontwarn android.speech.tts.**
-keep class javax.inject.** { *; }
-keepclassmembers class * {
    @javax.inject.* <fields>;
    @javax.inject.* <methods>;
}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
