package app.espanol

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.espanol.tts.TTSManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EspanolApplication : Application(), DefaultLifecycleObserver {

    @Inject
    lateinit var ttsManager: TTSManager

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        ttsManager.shutdown()
    }
}