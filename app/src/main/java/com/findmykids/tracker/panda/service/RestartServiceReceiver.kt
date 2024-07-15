package com.findmykids.tracker.panda.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.findmykids.tracker.panda.model.response.LoginResponse
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.PreferenceManager
import com.google.gson.Gson

class RestartServiceReceiver : BroadcastReceiver() {
    lateinit var pref: PreferenceManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.e("TAG", "onReceive: +++++++++++111")
        pref = PreferenceManager(context)
        if (pref.getString(Const.userData).toString().isNotEmpty()) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                Intent(context, MainAppService::class.java).apply {
                    action = MainAppService.ACTION_START
                    context.startService(this)
                }
            }
        }
    }
}