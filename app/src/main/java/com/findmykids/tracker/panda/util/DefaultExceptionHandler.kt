package com.findmykids.tracker.panda.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.findmykids.tracker.panda.service.MainAppService
import kotlin.system.exitProcess

class DefaultExceptionHandler(var activity: Context) : Thread.UncaughtExceptionHandler {
    /*
    //
        if you call activity then must be provide application activity not context
    //
    */
    private val defaultUEH: Thread.UncaughtExceptionHandler? = null
    override fun uncaughtException(thread: Thread, ex: Throwable) {
//        val intent = Intent(activity, SplashActivity::class.java)
//        intent.putExtra("crash", true)
        Log.e("TAG", "uncaughtException: ")

//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//        val pendingIntent = PendingIntent.getActivity(activity.baseContext, 0, intent, intent.flags)

        //Following code will restart your application after 0.5 seconds
//        val mgr = activity.baseContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        mgr[AlarmManager.RTC, System.currentTimeMillis() + 500] = pendingIntent

        //This will finish your activity manually


        Intent(activity, MainAppService::class.java).apply {
            action = MainAppService.ACTION_START
            activity.startService(this)
        }


//        activity.finish()


        //This will stop your application and take out from it.
        exitProcess(2)


    }
}