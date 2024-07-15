package com.findmykids.tracker.panda.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivitySplashBinding
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.MyApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : BaseActivity() {
    val b: ActivitySplashBinding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    //media player
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
//        SocketEvents.registerAllEvents()
        //bottomActionBar
        window.navigationBarColor = ContextCompat.getColor(this, R.color.gradiant2);
        //status bar
        val windowInsetController = ViewCompat.getWindowInsetsController(window.decorView)
        windowInsetController?.isAppearanceLightStatusBars = false // or false
        window.statusBarColor =
            ActivityCompat.getColor(this, R.color.gradiant1);// set status background white

        setAlphaAnimation(b.imgSplash)

           mediaPlayer = MediaPlayer.create(activity, R.raw.splash_sound);

        Handler(Looper.getMainLooper()).postDelayed({
            checkProfileStatus()
        }, 2500)
        Handler(Looper.getMainLooper()).postDelayed({
            //textAnimation
            b.txtText.typeWrite(this, getString(R.string.app_name), 300L)
            mediaPlayer.start()
        }, 500)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
    }

    private fun setAlphaAnimation(v: View?) {
        b.imgSplash.animate()
            .rotationY(360f)
            .setDuration(2000)
            .setStartDelay(500)
            .setInterpolator(AccelerateDecelerateInterpolator())

    }

    fun TextView.typeWrite(lifecycleOwner: LifecycleOwner, text: String, intervalMs: Long) {
        this@typeWrite.text = ""
        lifecycleOwner.lifecycleScope.launch {
            repeat(text.length) {
                delay(intervalMs)
                this@typeWrite.text = text.take(it + 1)
            }
        }
    }
}