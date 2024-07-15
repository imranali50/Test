package com.findmykids.tracker.panda.service.screenLockService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.MainActivity
import com.findmykids.tracker.panda.database.AnalysisDatabase
import com.findmykids.tracker.panda.database.InternetBlockDatabase
import com.findmykids.tracker.panda.database.LimitPackages
import com.findmykids.tracker.panda.database.RestrictPackages
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Objects
import java.util.SortedMap
import java.util.TreeMap

class ForegroundService : Service() {

    var mContext: Context? = null
    var db: RestrictPackages = RestrictPackages(this)
    var db1: LimitPackages =
        LimitPackages(this)
    var db2: AnalysisDatabase = AnalysisDatabase(this)
    var db3: InternetBlockDatabase =
        InternetBlockDatabase(this)

    override fun onCreate() {
        super.onCreate()
        mContext = this
        Log.d("Current App", "Current App in foreground is:++++++")
        check.start()
        if (db.isDbEmpty) {
            check.start()
        }
        if (db1.isDbEmpty) {
            check.start()
        }
        if (db3.isDbEmpty) {
            check.start()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val input: String? = intent.getStringExtra("inputExtra")
        createNotificationChannel()
        val sh: SharedPreferences = getSharedPreferences("PASS_CODE", MODE_PRIVATE)
        val i: Int = sh.getInt("pass", 0)
        val notificationIntent: Intent
        if (i > 0) {
            notificationIntent = Intent(this, MainActivity::class.java)
        } else {
            notificationIntent = Intent(this, MainActivity::class.java)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusOnMe")
            .setContentText(input)
            .setSmallIcon(R.drawable.card_background)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    protected var check: CountDownTimer = object : CountDownTimer(1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // do nothing
        }

        override fun onFinish() {
            var currentApp = ""
            val time = System.currentTimeMillis()

            // Get the current app package name
            var usm: UsageStatsManager =
                mContext!!.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

            // Get the UsageStats object for the currently running app.
            val currentTime = System.currentTimeMillis()
            val usageStats: List<UsageStats> = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                currentTime - 10000,
                currentTime
            )

            // Get the package name of the currently running app from the UsageStats object.
            if (usageStats != null && usageStats.size > 0) {
                val mySortedMap: SortedMap<Long, UsageStats> = TreeMap<Long, UsageStats>()
                for (usageStat in usageStats) {
                    mySortedMap[usageStat.getLastTimeUsed()] = usageStat
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp =
                        Objects.requireNonNull<UsageStats?>(mySortedMap[mySortedMap.lastKey()])
                            .packageName
                }
            }
            Log.d("Current App", "Current App in foreground is: $currentApp")
            val packs: ArrayList<String> = db.readRestrictPacks()
            val packs1: ArrayList<String> = db1.readLimitPacks()
            val packs2: ArrayList<String> = db3.readInternetPacks()
            for (i in packs.indices) {
                Log.e("TAG", "onFinish: "+packs[i] )
            }
            if (packs.contains(currentApp)) {
                this.cancel()
                val lockIntent = Intent(mContext, MainActivity::class.java)
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                lockIntent.putExtra("PACK_NAME", currentApp)
                val msg = "This App Is Blocked By FocusOnMe"
                lockIntent.putExtra("MSG", msg)
                db2.inAppBlocked(currentApp)
                startActivity(lockIntent)
            } else if (packs1.contains(currentApp)) {
                val calendar = Calendar.getInstance()
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0
                val startMillis: Long
                val endMillis: Long
                calendar[Calendar.HOUR_OF_DAY] = 1
                startMillis = calendar.timeInMillis
                endMillis = System.currentTimeMillis()
                val lUsageStatsMap: Map<String, UsageStats> =
                    usm.queryAndAggregateUsageStats(startMillis, endMillis)
                var total = ""
                if (lUsageStatsMap.containsKey(currentApp)) {
                    val m: Long = Objects.requireNonNull<UsageStats?>(
                        lUsageStatsMap[currentApp]
                    ).getTotalTimeInForeground()
                    total = m.toString()
                }
                val compare: String = db1.readDuration(currentApp)
                val m1 = total.toLong()
                val m2 = compare.toLong()
                if (m1 > m2) {
                    this.cancel()
                    val lockIntent = Intent(mContext, MainActivity::class.java)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    lockIntent.putExtra("PACK_NAME", currentApp)
                    val msg = "This App Is Blocked By FocusOnMe"
                    lockIntent.putExtra("MSG", msg)
                    db2.inAppBlocked(currentApp)
                    startActivity(lockIntent)
                }
            } else if (packs2.contains(currentApp)) {
                val temp: String = db3.readData(currentApp)
                val data = temp.toFloat()
                val calendar = Calendar.getInstance()
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0
                val startMillis: Long
                val endMillis: Long
                startMillis = calendar.timeInMillis
                endMillis = System.currentTimeMillis()
                val currentData = getPkgInfo(startMillis, endMillis, currentApp)
                if (data < currentData) {
                    this.cancel()
                    val lockIntent = Intent(mContext, MainActivity::class.java)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    lockIntent.putExtra("PACK_NAME", currentApp)
                    val msg = "This App Exceeds The Current Data Usage Limit"
                    lockIntent.putExtra("MSG", msg)
                    startActivity(lockIntent)
                }
            }
            this.start()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager = getSystemService<NotificationManager>(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    fun getPkgInfo(startMillis: Long, endMillis: Long, pkgName: String?): Float {
        val packageManager: PackageManager = this.packageManager
        var info: ApplicationInfo? = null
        val uid: Int
        try {
            info = pkgName?.let { packageManager.getApplicationInfo(it, 0) }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        uid = Objects.requireNonNull(info)!!.uid
        return fetchNetworkStatsInfo(startMillis, endMillis, uid)
    }

    fun fetchNetworkStatsInfo(startMillis: Long, endMillis: Long, uid: Int): Float {
        val networkStatsManager: NetworkStatsManager
        var total = 0.0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var receivedWifi = 0f
            var sentWifi = 0f
            var receivedMobData = 0f
            var sentMobData = 0f
            networkStatsManager =
                this.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager
            val nwStatsWifi: NetworkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI, null,
                startMillis, endMillis, uid
            )
            val bucketWifi: NetworkStats.Bucket = NetworkStats.Bucket()
            while (nwStatsWifi.hasNextBucket()) {
                nwStatsWifi.getNextBucket(bucketWifi)
                receivedWifi = receivedWifi + bucketWifi.getRxBytes()
                sentWifi = sentWifi + bucketWifi.getTxBytes()
            }
            val nwStatsMobData: NetworkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_MOBILE, null,
                startMillis, endMillis, uid
            )
            val bucketMobData: NetworkStats.Bucket = NetworkStats.Bucket()
            while (nwStatsMobData.hasNextBucket()) {
                nwStatsMobData.getNextBucket(bucketMobData)
                receivedMobData = receivedMobData + bucketMobData.getRxBytes()
                sentMobData = sentMobData + bucketMobData.getTxBytes()
            }
            total = (receivedWifi + sentWifi + receivedMobData + sentMobData) / (1024 * 1024)
        }
        val df = DecimalFormat("00000")
        df.roundingMode = RoundingMode.DOWN
        total = df.format(total.toDouble()).toFloat()
        return total
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
}