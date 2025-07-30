package app.spanish

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.spanish.tts.TTSManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SpanishApplication : Application(), DefaultLifecycleObserver {

    @Inject
    lateinit var ttsManager: TTSManager

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("FATAL", "Uncaught exception on thread ${thread.name}", throwable)
            throwable.printStackTrace()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        ttsManager.shutdown()
    }
}