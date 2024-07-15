package com.findmykids.tracker.panda.activity

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.adapter.AppUsageAdapter
import com.findmykids.tracker.panda.databinding.ActivityAppUsageBinding
import com.findmykids.tracker.panda.roomDatabase.AppInfoFilter
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.CustomListBalloonFactory
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.statusBarForAll
import com.skydoves.balloon.Balloon
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class AppUsageActivity : BaseActivity() {
    private val b: ActivityAppUsageBinding by lazy {
        ActivityAppUsageBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()
    private var appList = ArrayList<AppInfoFilter>()
    private lateinit var appUsageAdapter: AppUsageAdapter
    private var packageManager: PackageManager? = null
    lateinit var appBarText: String
    private lateinit var customListBalloon: Balloon
    val executor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    val handler: Handler by lazy { Handler(Looper.getMainLooper()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        b.header.flToolBack.visibility = View.VISIBLE
        b.header.toolbarTitle.visibility = View.VISIBLE
        b.header.toolbarTitle.text = "Applications"
        b.header.flToolFilter.visibility = View.VISIBLE
        appUsageAdapter = AppUsageAdapter(this@AppUsageActivity)
        b.rvGuardianChat.adapter = appUsageAdapter
        appBarText = "Today"

        getData()
        setObserve()
        customListBalloon = CustomListBalloonFactory().create(this, this)
        val tvToday: TextView =
            customListBalloon.getContentView().findViewById(R.id.tvToday)
        val tvYesterday: TextView =
            customListBalloon.getContentView().findViewById(R.id.tvYesterday)
        val tvWeekly: TextView =
            customListBalloon.getContentView().findViewById(R.id.tvWeekly)
        val tvMonthly: TextView =
            customListBalloon.getContentView().findViewById(R.id.tvMonthly)
        tvToday.setOnClickListener {
            appBarText = "Today"
            getData()
            customListBalloon.dismiss()
        }
        tvYesterday.setOnClickListener {
            appBarText = "Yesterday"
            getData()
            customListBalloon.dismiss()
        }
        tvWeekly.setOnClickListener {
            appBarText = "Weekly"
            getData()
            customListBalloon.dismiss()
        }
        tvMonthly.setOnClickListener {
            appBarText = "Monthly"
            getData()
            customListBalloon.dismiss()
        }
        b.header.flToolFilter.setOnClickListener {
            customListBalloon.showAlignBottom(it, 0, 36)
        }
        b.header.flToolBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }

    private fun setObserve() {
//        viewModel.appUsageResponse.observe(this) {
//            Log.e("TAG", "onCreate: >>>>>>>>> $it")
//            runOnUiThread {
//                if (appBarText == "Today")
//                    appUsageAdapter.setAppInfoNewList(it)
//            }
//        }
    }


    private fun getData() {
        b.rvGuardianChat.visibility = View.GONE
        b.shimmerEffect.visibility = View.VISIBLE
        b.shimmerEffect.startShimmer()

        MyApplication.progressBar.show(this)
        executor.execute {
            packageManager = getPackageManager()
            val intent1 = Intent(Intent.ACTION_MAIN, null)
            intent1.addCategory(Intent.CATEGORY_LAUNCHER)
            checkForLaunchIntent(
                packageManager!!.queryIntentActivities(intent1, 0)
            )
            appList.sortWith(Comparator { lhs, rhs ->
                lhs.today.compareTo(rhs.today)
            })
            appList.reverse()

            handler.post {
                b.rvGuardianChat.visibility = View.VISIBLE
                b.shimmerEffect.visibility = View.GONE
                b.shimmerEffect.stopShimmer()

                appUsageAdapter.setAppInfoNewList(appList)
                appUsageAdapter.notifyDataSetChanged()
                MyApplication.progressBar.dismiss()
            }
        }
    }

    private fun checkForLaunchIntent(list: List<ResolveInfo>) {
        appList.clear()
        for (info in list) {
            try {
                val screenTime =
                    getScreenTimeForApp(this@AppUsageActivity, info.activityInfo.packageName)
                if(info.activityInfo.packageName == "com.findmykids.tracker.panda"){
                    Log.e("TAG", "checkForLaunchIntent   : "+getTime(screenTime))
                }

                appList.add(
                    AppInfoFilter(
                        info.loadLabel(packageManager).toString(),
                        info.activityInfo.packageName,
                        info.loadIcon(packageManager!!),
                        screenTime
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun getTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return if ((minutes % 60).toInt() == 0 && (hours % 24).toInt() == 0) {
            "No Use"
        } else if ((hours % 24).toInt() == 0) {
            String.format("%02d", minutes % 60) + " min"
        } else {
            String.format("%02d", hours % 24) + ":" + String.format(
                "%02d",
                minutes % 60
            ) + " hr"
        }
    }

    private fun getScreenTimeForApp(context: Context, packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var startMillis: Long = 0
        var endMillis = System.currentTimeMillis()

        when (appBarText) {
            "Today" -> {
                startMillis = calendar.timeInMillis
            }

            "Yesterday" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1) // Go back one day
                startMillis = calendar.timeInMillis
                calendar[Calendar.HOUR_OF_DAY] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                calendar[Calendar.MILLISECOND] = 999
                endMillis = calendar.timeInMillis
            }

            "Weekly" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                startMillis = calendar.timeInMillis
            }

            "Monthly" -> {
                calendar[Calendar.DAY_OF_MONTH] = 1
                startMillis = calendar.timeInMillis
            }

            else -> {
                startMillis = calendar.timeInMillis
            }
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val lUsageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)

        var foregroundTime: Long = 0

        for (stats in lUsageStatsMap.values) {
            if (stats.packageName == packageName) {
                foregroundTime = stats.totalTimeInForeground
                break // No need to continue, we found the desired package
            }
        }

        return foregroundTime
    }

 /*   private fun getScreenTimeForApp(context: Context, packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var startMillis: Long = 0
        var endMillis = System.currentTimeMillis()

        when (appBarText) {
            "Today" -> {
                startMillis = calendar.timeInMillis
            }
            "Yesterday" -> {
                calendar[Calendar.HOUR_OF_DAY] = -24
                startMillis = calendar.timeInMillis
                calendar[Calendar.HOUR_OF_DAY] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                calendar[Calendar.MILLISECOND] = 999
                endMillis = calendar.timeInMillis
            }
            "Weekly" -> {
                calendar[Calendar.DAY_OF_WEEK] = calendar.firstDayOfWeek
                startMillis = calendar.timeInMillis
            }
            "Monthly" -> {
                calendar[Calendar.DAY_OF_MONTH] = 1
                startMillis = calendar.timeInMillis
            }
            else -> {
                startMillis = calendar.timeInMillis
            }
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis)

        var foregroundTime: Long = 0

        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                foregroundTime += usageStats.totalTimeInForeground
            }
        }

        return foregroundTime
    }*/




  /*  private fun getScreenTimeForApp(context: Context, packageName: String): Long {

        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var startMillis: Long = 0
        var endMillis = System.currentTimeMillis()

        when (appBarText) {
            "Today" -> {
                startMillis = calendar.timeInMillis
            }

            "Yesterday" -> {
                calendar[Calendar.HOUR_OF_DAY] = -24
                startMillis = calendar.timeInMillis
                calendar[Calendar.HOUR_OF_DAY] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                calendar[Calendar.MILLISECOND] = 99
                endMillis = calendar.timeInMillis
            }

            "Weekly" -> {
//                Log.e("TAG", "getScreenTimeForApp: ", )
                calendar[Calendar.DAY_OF_WEEK] = calendar.firstDayOfWeek
                startMillis = calendar.timeInMillis
            }

            "Monthly" -> {
                calendar[Calendar.DAY_OF_MONTH] = 1
                startMillis = calendar.timeInMillis
            }

            else -> {
                startMillis = calendar.timeInMillis
            }
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
//        val use = UsageStatsManager.INTERVAL_DAILY
        val lUsageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)

        if (lUsageStatsMap.containsKey(packageName)) {
            return lUsageStatsMap[packageName]!!.totalTimeInForeground
        }

        return 0
    }*/
}