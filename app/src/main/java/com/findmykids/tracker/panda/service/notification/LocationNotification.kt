package com.findmykids.tracker.panda.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.findmykids.tracker.panda.R


class LocationNotification(
    private val context: Context,
    private val manager: NotificationManager,
) {

    companion object {
        const val ID = 1
    }

    //    var contentIntent = PendingIntent.getActivity(context, 0,
    //    Intent(context, SplashActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
    val color = ContextCompat.getColor(context, R.color.gradiant1)
    private val builder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(
            context,
            context.getString(R.string.default_notification_channel_id)
        )
            .setColor(ContextCompat.getColor(context, R.color.white))
            .setColor(color)
            .setContentTitle(context.getString(R.string.app_name))
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText("Background Service is Running")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
//            .setContentIntent(contentIntent)
    }

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                context.getString(R.string.default_notification_channel_id),
                context.getString(R.string.child_app),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_des)
                manager.createNotificationChannel(this)
            }
        }
    }

    fun createNotification(): Notification {
        return builder.build()
    }
}