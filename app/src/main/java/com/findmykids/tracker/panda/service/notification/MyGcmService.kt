package com.findmykids.tracker.panda.service.notification

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.ChatActivity
import com.findmykids.tracker.panda.activity.DashBoardActivity
import com.findmykids.tracker.panda.activity.SplashActivity
import com.findmykids.tracker.panda.model.NotificationModel
import com.findmykids.tracker.panda.sockets.MainSocket
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import java.util.Random
import java.util.concurrent.ExecutionException

class MyGcmService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private var foreGround: Boolean? = null
    private val TAG = "MyGcmListenerService"

    var notificationModel: NotificationModel? = null
    var modelType = ""
    lateinit var pref: PreferenceManager
//    handleIntent: >>>>>>>>>>>> Bundle[{google.delivered_priority=high, google.sent_time=1706605999465, google.ttl=2419200, google.original_priority=high, gcm.notification.e=1, gcm.n.dnp=1, gcm.notification.title=nena parent has sent you message, google.product_id=111881503, data={"connectionId":"65a8f2869b3b225a4f5c129c","notificationType":"MESSAGES"}, from=331131518146, google.message_id=0:1706605999489112%2e3b4fff2e3b4fff, gcm.notification.body=hi, google.c.a.e=1, google.c.sender.id=331131518146, collapse_key=com.app.childapp}]

    override fun handleIntent(intent: Intent) {
        Log.e(TAG, "handleIntent: >>>>>>>>>>>> ${intent.extras}")
        pref = PreferenceManager(this)
        Const.tokenForApi = pref.getString(Const.token).toString()
        val gson = Gson()
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey("data")) {
                val test: String = bundle.getString("data")!!
                if (test.isNotEmpty()) {
                    Log.e(TAG, "handleIntent:>>>>>>>>> " + bundle.getString("title"))
                    Log.e("test-->>>>>>>>>>>>>>>>", "onMessageReceived: $test")
                    try {
                        notificationModel = gson.fromJson(test, NotificationModel::class.java)
                        modelType = notificationModel?.notificationType!!
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                try {
                    foreGround = ForegroundCheckTask().execute(this).get()
                    Log.e(TAG, "foreGround: $foreGround")
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                pushNotification(
                    bundle.get("gcm.notification.title").toString(),
                    bundle.get("gcm.notification.body").toString(),
                    notificationModel
                )
            }
        }

    }

    private fun pushNotification(
        title: String, description: String, notificationModel: NotificationModel?
    ) {
        val activity = ArrayList<Intent>()
        var pIntent: PendingIntent? = null
        val intent: Intent

        if (SocketConnectionCall.mSocket == null || !SocketConnectionCall.isConnected) {
            Log.e("TAG", "onResume: >>>>>>>>>>> not connected ")
            val mainSocket = MainSocket.getInstance(this@MyGcmService)
            Log.e(TAG, "pushNotification: >>>>>>>>>>> " + mainSocket)
            SocketConnectionCall.setSocket(mainSocket!!)
            SocketConnectionCall.socketConnect()
        }
        if (!foreGround!!) {
            Log.e(TAG, "pushNotification: >>>>>>>. home added 11 ")
            val intent1 = Intent(this@MyGcmService, DashBoardActivity::class.java)
            intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.add(intent1)
        }
        when (modelType) {
            Const.MESSAGES -> {
                intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(ChatActivity.ConnectionId, notificationModel?.connectionId)
                intent.putExtra(ChatActivity.ChatGroupIcon, notificationModel?.senderProfile)
                intent.putExtra(ChatActivity.ChatGroupName, notificationModel?.senderName)
                activity.add(intent)
            }

            else -> {
                intent = Intent(
                    this@MyGcmService, SplashActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.add(intent)
            }
        }
        if (modelType == Const.MESSAGES) {
            val intents = activity.toTypedArray()
            try {
                pIntent = PendingIntent.getActivities(
                    this,
                    getNotificationId(),
                    intents,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            val path =
                Uri.parse("android.resource://" + packageName + "/" + +R.raw.message_incoming)

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            val notificationManager =
                this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // If android version is greater than 8.0 then create notification channel
            val audioAttributes: AudioAttributes? =
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a notification channel
                val notificationChannel = NotificationChannel(
                    CHANNEL_ID,
                    resources.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
                // Set properties to notification channel
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.setSound(path, audioAttributes);
                notificationChannel.enableVibration(true)
                notificationChannel.vibrationPattern = longArrayOf(100, 200, 300)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val color = ContextCompat.getColor(this, R.color.gradiant1)


            builder.setContentTitle(title).setContentText(description)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(path).setColor(color).setContentIntent(pIntent).setAutoCancel(true)
            notificationManager.notify(
                Integer.parseInt(
                    notificationModel?.connectionId!!.replace(
                        ("[^0-9]").toRegex(),
                        "1"
                    ).substring(0, 9)
                ), builder.build()
            )
        }
        else {
            val intents = activity.toTypedArray()
            try {
                pIntent = PendingIntent.getActivities(
                    this,
                    getNotificationId(),
                    intents,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = getString(R.string.general_channel_title)
                val channelDescription = getString(R.string.general_channel_description)
                val importance = NotificationManagerCompat.IMPORTANCE_HIGH
                val channel = NotificationChannelCompat.Builder(CHANNEL_ID, importance).apply {
                    setName(channelName)
                    setDescription(channelDescription)
                    setSound(
                        Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${packageName}/raw/message_incoming"),
                        Notification.AUDIO_ATTRIBUTES_DEFAULT
                    )
                }
                NotificationManagerCompat.from(this).createNotificationChannel(channel.build())
            }
            val color = ContextCompat.getColor(this, R.color.gradiant1)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)

                .setContentTitle(title).setSmallIcon(R.drawable.ic_notification).setColor(R.color.gradiant1)
                .setContentText(description).setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(false)
                .setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${packageName}/${R.raw.message_incoming}"))
            val notificationManager = NotificationManagerCompat.from(this)
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(
                    getNotificationId(), builder.build()
                )
            }
        }

    }

    val CHANNEL_ID: String = "general_channel_new"
    private fun getNotificationId(): Int {
//        return Const.NOTIFICATION_ID
        return 100 + Random().nextInt(9000)
    }

    class ForegroundCheckTask : AsyncTask<Context?, Void?, Boolean>() {
        override fun doInBackground(vararg p0: Context?): Boolean {
            val context = p0[0]?.applicationContext
            return isAppOnForeground(context!!)
        }

        private fun isAppOnForeground(context: Context): Boolean {
            val activityManager =
                context.getSystemService(FirebaseMessagingService.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: return false
            val packageName = context.packageName
            for (appProcess in appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                    return true
                }
            }
            return false
        }
    }
}