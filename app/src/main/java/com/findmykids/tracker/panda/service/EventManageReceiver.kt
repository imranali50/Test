package com.findmykids.tracker.panda.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.Const
import org.json.JSONObject

class EventManageReceiver : BroadcastReceiver() {

    var packageName = ""
    var appName = ""
    var appType = ""
    override fun onReceive(context: Context, intent: Intent) {
        val packageName: String = intent.data?.schemeSpecificPart.toString()

        try {
            appName = context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA
                )
            ) as String
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (!intent.action.equals(Intent.ACTION_PACKAGE_REPLACED) && intent.getBooleanExtra(
                Intent.EXTRA_REPLACING,
                false
            )
        ) return

        Log.e("TAG", "onReceive Package name: $packageName app name  $appName")
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                appType = "INSTALL"
                Log.e("TAG", "onReceive: " + "ACTION_PACKAGE_ADDED")
            }

            Intent.ACTION_PACKAGE_REMOVED -> {
                appType = "UNINSTALL"
                Log.e("TAG", "onReceive: " + "ACTION_PACKAGE_REMOVED")
            }

            Intent.ACTION_PACKAGE_REPLACED -> {
                appType = "UPDATE"
                Log.e("TAG", "onReceive: " + "ACTION_PACKAGE_REPLACED")
            }
        }

        if (SocketConnectionCall.isConnected && appType.isNotEmpty() && packageName.isNotEmpty()) {
            val json = JSONObject()
            json.put("appName", appName)
            json.put("appPkgName", packageName)
            json.put("type", appType)
            SocketConnectionCall.sendDataToServer(Const.appChange, json)
        }

    }
}