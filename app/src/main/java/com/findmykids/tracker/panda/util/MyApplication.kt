package com.findmykids.tracker.panda.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.lifecycle.LifecycleObserver
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.service.notification.LocationNotification
import com.findmykids.tracker.panda.sockets.MainSocket
import com.findmykids.tracker.panda.waves.VoicePlayerView
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider
import dagger.hilt.android.HiltAndroidApp
import io.socket.client.Socket
import javax.inject.Inject
import kotlin.jvm.internal.Intrinsics

@HiltAndroidApp
class MyApplication : Application(),LifecycleObserver {
    @JvmField
    @Inject
    var notification: LocationNotification? = null

    var mainSocket: Socket? = null
    public override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        Intrinsics.checkNotNullParameter(configuration, "newConfig")
        super.onConfigurationChanged(configuration)
    }

    override fun onCreate() {
        super.onCreate()
        notification!!.createChannel()
        initSocket()
        EmojiManager.install(
            GoogleCompatEmojiProvider(
                EmojiCompat.init(
                    FontRequestEmojiCompatConfig(
                        this, FontRequest(
                            "com.google.android.gms.fonts",
                            "com.google.android.gms",
                            "Noto Color Emoji Compat",
                            R.array.com_google_android_gms_fonts_certs,
                        )
                    ).setReplaceAll(true)
                )
            )
        )
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {
         val instance: MyApplication by lazy { getMainInstance() }
        val progressBar: ProgressLoading = ProgressLoading()

        private fun getMainInstance(): MyApplication {
                return MyApplication()
        }

        var mediaPlayer: com.findmykids.tracker.panda.waves.VoicePlayerView.CustomMediaPlayer? = null

        var mediaPath: String? = null
        fun setMedia(player: com.findmykids.tracker.panda.waves.VoicePlayerView.CustomMediaPlayer) {
            mediaPlayer = player
        }

        fun setMediaPaths(path: String?) {
            mediaPath = path
        }

        fun getMedia(): com.findmykids.tracker.panda.waves.VoicePlayerView.CustomMediaPlayer? {
            return mediaPlayer
        }

        fun getMediaPaths(): String? {
            return mediaPath
        }
    }

    fun initSocket() {
        mainSocket = MainSocket.getInstance(this)!!
    }
}