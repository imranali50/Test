package com.findmykids.tracker.panda.service

import android.app.ActivityManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
import android.location.Location
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.lifecycleScope
import com.findmykids.tracker.panda.activity.MainActivity
import com.findmykids.tracker.panda.database.AnalysisDatabase
import com.findmykids.tracker.panda.database.RestrictPackages
import com.findmykids.tracker.panda.roomDatabase.ApplicationUseDatabase
import com.findmykids.tracker.panda.roomDatabase.ApplicationUsesRepository
import com.findmykids.tracker.panda.roomDatabase.ApplicationUsesViewModel
import com.findmykids.tracker.panda.service.notification.LocationNotification
import com.findmykids.tracker.panda.service.util.LocationProvider
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.socketMvvm.SocketRepository
import com.findmykids.tracker.panda.sockets.MainSocket
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.DefaultExceptionHandler
import com.findmykids.tracker.panda.util.PreferenceManager
import com.findmykids.tracker.panda.util.Utils
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Objects
import java.util.SortedMap
import java.util.TreeMap
import javax.inject.Inject


@AndroidEntryPoint
class MainAppService : LifecycleService() {

    companion object {
        const val ACTION_START = "actionStart"
        const val ACTION_STOP = "actionStop"

        //const val INTERVAL = 10_000L
        const val INTERVAL = 10 * 1000L
        var minutes = 1
        val AppUpdateInterval = minutes * 60 * 1000L
        var mLocation: Location? = null

    }

    @Inject
    lateinit var notification: LocationNotification

    @Inject
    lateinit var location: LocationProvider

    private var locationJob: Job? = null
    var job: Job? = null

    //private var appList = ArrayList<AppInfoFilter>()
    private var jsonObjectList = JSONArray()

    private var speed = 0.0
    var currentSpeed = 0.0
    var kmphSpeed: Double = 0.0
    var utils: Utils = Utils(this)
    lateinit var pref: PreferenceManager
    private var foreGround: Boolean? = null

    //appBlockFeature
    var db: RestrictPackages = RestrictPackages(this)
    var db1: com.findmykids.tracker.panda.database.LimitPackages =
        com.findmykids.tracker.panda.database.LimitPackages(this)
    var db2: AnalysisDatabase = AnalysisDatabase(this)
    var db3: com.findmykids.tracker.panda.database.InternetBlockDatabase =
        com.findmykids.tracker.panda.database.InternetBlockDatabase(this)

    //appUses
    lateinit var applicationUsesRepository: ApplicationUsesRepository
    lateinit var applicationUsesViewModel: ApplicationUsesViewModel
    lateinit var downloadVideoDatabase: ApplicationUseDatabase


    val mViewModelStore = ViewModelStore()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler(this@MainAppService))
        Log.e("MainAppService", "start: ")
        startForeground()
        startUpdates()
        observe()
        check.start()
    }
    private fun startForeground() {

//        before
//        startForeground(LocationNotification.ID, notification.createNotification())

//        now
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(LocationNotification.ID, notification.createNotification())
        } else {
//            startForeground(LocationNotification.ID, notification.createNotification(),
//                FOREGROUND_SERVICE_TYPE_MICROPHONE)

            startForeground(
                LocationNotification.ID,
                notification.createNotification())

        }

    }

    private fun stop() {
        locationJob?.cancel()
        locationJob =null

        job?.cancel()
        job = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }




    private fun startUpdates() {
//        downloadVideoDatabase = ApplicationUseDatabase(this)
//        applicationUsesRepository = ApplicationUsesRepository(downloadVideoDatabase)
//        applicationUsesViewModel = ViewModelProvider(
//            mViewModelStore, ApplicationUsesModelFactory(applicationUsesRepository)
//        )[ApplicationUsesViewModel::class.java]
        job?.cancel()
        job = null
          job = lifecycleScope.launch {
            while (true) {

                delay(AppUpdateInterval)
                getData()
                Log.i("MainAppService", "appUpdate: $AppUpdateInterval")
            }
        }
    }

    private fun observe() {
        observeLocation()
    }

    private fun observeLocation() {
        locationJob?.cancel()
        locationJob =null
        locationJob = lifecycleScope.launch {
            location.getLocation(INTERVAL, this@MainAppService).catch { it.printStackTrace() }
                .collectLatest { location ->

                    Log.i("MainAppService", "locationUpdate: $INTERVAL")
                    mLocation = location
                    getSpeed(location)
                    updateLatLng()

                }
        }
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

    fun getData() {
        val packageManager = packageManager
        val intent1 = Intent(Intent.ACTION_MAIN, null)
        intent1.addCategory(Intent.CATEGORY_LAUNCHER)
        checkForLaunchIntent(packageManager!!.queryIntentActivities(intent1, 0))
//        appList.sortWith(Comparator { lhs, rhs ->
//            lhs.time.compareTo(rhs.time)
//        })
//        appList.reverse()
//        val list: MutableList<AppInfoFilter> = appList
//        CoroutineScope(Dispatchers.Main).launch {
//            Repository.appUsageUpdate(list)
//        }
//        if (SocketConnectionCall.isConnected) {
//            val json = JSONObject().put("data", jsonObjectList)
//            SocketConnectionCall.sendDataToServer(Const.appUseChildUpdate, json)
//        }
//        INTERVAL_APP = 10_000L
    }

    fun checkForLaunchIntent(list: List<ResolveInfo>) {
//        appList.clear()
        jsonObjectList = JSONArray()
        for (info in list) {
            try {
                val screenTimeList =
                    getScreenTimeForApp(this@MainAppService, info.activityInfo.packageName)
//                appList.add(
//                    AppInfoFilter(
//                        info.loadLabel(packageManager).toString(),
//                        info.activityInfo.packageName,
//                        info.loadIcon(packageManager!!),
//                        screenTimeList[0]
//                    )
//                )
                jsonObjectList.put(
                    JSONObject().put(
                        "appName", info.loadLabel(packageManager).toString()
                    ).put("appPkgName", info.activityInfo.packageName)
                        .put("appIcon", info.loadIcon(packageManager).toString())
                        .put("todayDuration", screenTimeList[0]).put(
                            "yesterdayDuration", screenTimeList[1]
                        ).put(
                            "weeklyDuration", screenTimeList[2]
                        ).put("mothlyDuration", screenTimeList[3])
                )
                job = CoroutineScope(Dispatchers.IO).launch {
//                    Log.e("TAG", "checkForLaunchIntent: "+info.loadIcon(packageManager).toString())
//                    applicationUsesViewModel.insertVideoData(
//                        AppInfoFilterModel(
//                            info.loadLabel(packageManager).toString(), AppInfoFilter(
//                                info.loadLabel(packageManager).toString(),
//                                info.activityInfo.packageName,
//                                info.loadIcon(packageManager),
//                                screenTimeList[0]
//                            )
//                        )
//                    )
                }

//                Log.e("TAG", "checkForLaunchIntent:"+ info.loadLabel(packageManager).toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (SocketConnectionCall.isConnected) {
            val json = JSONObject().put("data", jsonObjectList)
            SocketConnectionCall.sendDataToServer(Const.appUseChildUpdate, json)
        }
    }

    private fun getScreenTimeForApp(context: Context, packageName: String): MutableList<Long> {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var startMillis: Long = 0
        var endMillis = System.currentTimeMillis()
        val listOfTime: MutableList<Long> = ArrayList()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        //Today
        startMillis = calendar.timeInMillis
        val lUsageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)
        if (lUsageStatsMap.containsKey(packageName)) {
            listOfTime.add(lUsageStatsMap[packageName]!!.totalTimeInForeground)
        } else {
            listOfTime.add(0L)
        }

        //Yesterday
        calendar[Calendar.HOUR_OF_DAY] = -24
        startMillis = calendar.timeInMillis
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 99
        endMillis = calendar.timeInMillis
        val lUsageStatsMap1 = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)
        if (lUsageStatsMap1.containsKey(packageName)) {
            listOfTime.add(lUsageStatsMap1[packageName]!!.totalTimeInForeground)
        } else {
            listOfTime.add(0L)
        }

        //Weekly
        calendar[Calendar.DAY_OF_WEEK] = calendar.firstDayOfWeek
        startMillis = calendar.timeInMillis
        val lUsageStatsMap2 = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)
        if (lUsageStatsMap2.containsKey(packageName)) {
            listOfTime.add(lUsageStatsMap2[packageName]!!.totalTimeInForeground)
        } else {
            listOfTime.add(0L)
        }

        //Monthly
        calendar[Calendar.DAY_OF_MONTH] = 1
        startMillis = calendar.timeInMillis
        val lUsageStatsMap3 = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)
        if (lUsageStatsMap3.containsKey(packageName)) {
            listOfTime.add(lUsageStatsMap3[packageName]!!.totalTimeInForeground)
        } else {
            listOfTime.add(0L)
        }

        return listOfTime
    }


    fun updateLatLng() {
        if (utils.isNetworkAvailable()) {

            var latitude = 0.0
            var longitude = 0.0

            latitude = mLocation!!.latitude
            longitude = mLocation!!.longitude
//            Log.e("MainAppService", "updateLatLng: $latitude")

            //Sound setting
            var soundMode = ""
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            when (am.ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> {
                    soundMode = "SILENT"
//                    Log.i("MyApp", "Silent mode")
                }

                AudioManager.RINGER_MODE_VIBRATE -> {
                    soundMode = "VIBRATE"
//                    Log.i("MyApp", "Vibrate mode")
                }

                AudioManager.RINGER_MODE_NORMAL -> {
                    soundMode = "RING"
//                    Log.i("MyApp", "Normal mode")
                }
            }

            if (SocketConnectionCall.mSocket == null || !SocketConnectionCall.isConnected) {
                val socketRepository = SocketRepository()
                SocketEvents.setRepository(socketRepository, this@MainAppService)
                Log.e("MainAppService", "onResume: >>>>>>>>>>> not connected ")
                val mainSocket = MainSocket.getInstance(this@MainAppService)
                SocketConnectionCall.setSocket(mainSocket!!)
                SocketConnectionCall.socketConnect()
            }

            if (SocketConnectionCall.isConnected) {
                pref = PreferenceManager(this)
                pref.setString(Const.latitude, latitude.toString())
                pref.setString(Const.longitude, longitude.toString())
                val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
                Log.e("MainAppService", "updateLatLng: " + "socket connected")
                // Get the battery percentage and store it in a INT variable
                val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                foreGround = ForegroundCheckTask().execute(this).get()
//                Log.e("MainAppService", "foreGround>>>>>>>: $foreGround")
                val json = JSONObject()
                json.put("location", JSONObject().put("lat", latitude).put("long", longitude))
                json.put("batteryPercentage", batLevel)
                json.put("phoneSound", soundMode)
                json.put("isForegroundStatus", foreGround)
                SocketConnectionCall.sendDataToServer(Const.locationChildUpdate, json)
            }

        }
    }

    private fun overSpeed() {
        if (utils.isNetworkAvailable()) {
            if (SocketConnectionCall.isConnected) {
                val json = JSONObject()
                json.put("speed", kmphSpeed)
//                Log.e("TAG", "updateLatLng: >>>>>>>>>>>>>>>>>> $json")
                SocketConnectionCall.sendDataToServer(Const.sendSpped, json)
            }
        }
    }

    private fun getSpeed(location: Location) {

        speed = location.speed.toDouble()
//        Log.e("TAG", "getSpeed: " + speed)

        if (speed != 0.0) {
            currentSpeed = round(speed, 3, BigDecimal.ROUND_HALF_UP)
//            Log.e("TAG", "getSpeed111111: " + currentSpeed)
        }

        if (currentSpeed != 0.0) {
            kmphSpeed = round((currentSpeed * 3.6), 3, BigDecimal.ROUND_HALF_UP)
//            Log.e("TAG", "getSpeed2222: " + kmphSpeed)
        }

        overSpeed()

    }

    fun round(unrounded: Double, precision: Int, roundingMode: Int): Double {
        val bd = BigDecimal(unrounded)
        val rounded = bd.setScale(precision, roundingMode)
        return rounded.toDouble()
    }


    //appBlockFeature
    private var check: CountDownTimer = object : CountDownTimer(1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // do nothing
        }

        override fun onFinish() {
            var currentApp = ""
            val time = System.currentTimeMillis()

            // Get the current app package name
            var usm: UsageStatsManager =
                this@MainAppService!!.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

            // Get the UsageStats object for the currently running app.
            val currentTime = System.currentTimeMillis()
            val usageStats: List<UsageStats> = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, currentTime - 10000, currentTime
            )

            // Get the package name of the currently running app from the UsageStats object.
            if (usageStats != null && usageStats.size > 0) {
                val mySortedMap: SortedMap<Long, UsageStats> = TreeMap<Long, UsageStats>()
                for (usageStat in usageStats) {
                    mySortedMap[usageStat.getLastTimeUsed()] = usageStat
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp =
                        Objects.requireNonNull<UsageStats?>(mySortedMap[mySortedMap.lastKey()]).packageName
                }
            }
//            Log.d("Current App", "Current App in foreground is ---- : $currentApp")
            val packs: ArrayList<String> = db.readRestrictPacks()
            val packs1: ArrayList<String> = db1.readLimitPacks()
            val packs2: ArrayList<String> = db3.readInternetPacks()
            for (i in packs.indices) {
                Log.e("MainAppService", "onFinish: " + packs[i])
            }
            if (packs.contains(currentApp)) {
                Log.e("MainAppService", "onFinish: close")
                this.cancel()
                try {
                    val lockIntent = Intent(this@MainAppService, MainActivity::class.java)
                    lockIntent.putExtra(MainActivity.PACK_NAME, currentApp)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    lockIntent.putExtra("PACK_NAM", currentApp)
                    val msg = "This App Is Blocked By Parent App"
                    lockIntent.putExtra("MSG", msg)
                    db2.inAppBlocked(currentApp)
                    startActivity(lockIntent)
                } catch (e: Exception) {
                    Log.e("MainAppService", "onFinish: errer" + e.message)
                }

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
                    val lockIntent = Intent(this@MainAppService, MainActivity::class.java)
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
                    val lockIntent = Intent(this@MainAppService, MainActivity::class.java)
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
                ConnectivityManager.TYPE_WIFI, null, startMillis, endMillis, uid
            )
            val bucketWifi: NetworkStats.Bucket = NetworkStats.Bucket()
            while (nwStatsWifi.hasNextBucket()) {
                nwStatsWifi.getNextBucket(bucketWifi)
                receivedWifi = receivedWifi + bucketWifi.getRxBytes()
                sentWifi = sentWifi + bucketWifi.getTxBytes()
            }
            val nwStatsMobData: NetworkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_MOBILE, null, startMillis, endMillis, uid
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

    override fun onDestroy() {
        super.onDestroy()
        Log.e("MainAppService", "ondestroy!")
    }

}