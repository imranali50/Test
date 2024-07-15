package com.findmykids.tracker.panda.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import com.findmykids.tracker.panda.model.response.LoginResponse
import com.findmykids.tracker.panda.permission.AudioRecordPerActivity
import com.findmykids.tracker.panda.permission.BackgroundLocationPerActivity
import com.findmykids.tracker.panda.permission.DisplayOverPerActivity
import com.findmykids.tracker.panda.permission.GpsPerActivity
import com.findmykids.tracker.panda.permission.LocationPerActivity
import com.findmykids.tracker.panda.permission.NotificationPerActivity
import com.findmykids.tracker.panda.permission.UsagesAccessPerActivity
import com.findmykids.tracker.panda.roomDatabase.ApplicationUseDatabase
import com.findmykids.tracker.panda.service.MainAppService
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.DefaultExceptionHandler
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.PreferenceManager
import com.findmykids.tracker.panda.util.Utils
import com.findmykids.tracker.panda.util.audioPermission
import com.findmykids.tracker.panda.util.backGroundLocationPermission
import com.findmykids.tracker.panda.util.checkSelfDefaultPermissions
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.isGPSEnabled
import com.findmykids.tracker.panda.util.isMyServiceRunning
import com.findmykids.tracker.panda.util.locationPermission
import com.findmykids.tracker.panda.util.notificationPermission
import com.findmykids.tracker.panda.util.packageUsesPermission
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.util.TimeZone

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity(), DefaultLifecycleObserver {

    lateinit var activity: Activity
    lateinit var utils: Utils
    lateinit var pref: PreferenceManager
    val timeDefault = TimeZone.getDefault()
    val timezone = timeDefault.id
    var loginResponse: LoginResponse? = null
    var profileImageUrl = ""
    lateinit var applicationUseDatabase: ApplicationUseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        activity = this
        utils = Utils(activity)
        pref = PreferenceManager(this)
        inti()
    }

    private fun inti() {
        applicationUseDatabase = ApplicationUseDatabase(this@BaseActivity)

        Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler(this));
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result != null && !TextUtils.isEmpty(it.result)) {
                    val token: String = it.result
                    pref.setString(
                        Const.deviceId,
                        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    )
                    pref.setString(Const.fcmToken, token)
                    Log.e("TAG", "onCreate: >>>>>>>>>> " + token)
                }
            }
        }

    }

    override fun onResume() {
        super<AppCompatActivity>.onResume()

        if (pref.getString(Const.userData).toString().isNotEmpty()) {
            Const.tokenForApi = pref.getString(Const.token).toString()
            loginResponse =
                Gson().fromJson(pref.getString(Const.userData), LoginResponse::class.java)
        }

    }

    fun checkProfileStatus() {
        if (pref.getString(Const.userData).toString().isNotEmpty()) {
            loginResponse =
                Gson().fromJson(pref.getString(Const.userData), LoginResponse::class.java)
        }

        if (pref.getString(Const.token).toString().isEmpty()) {
            startActivity(Intent(activity, SignInActivity::class.java))
            finishAffinity()
        } else if (loginResponse?.data?.gender.toString().isEmpty()) {
            startActivity(Intent(activity, ProfileCreateActivity::class.java))
            finishAffinity()
        } else if (loginResponse?.data?.isConnectionExists == false && !Const.isParentConnect) {
            startActivity(
                Intent(activity, GuardianCodeActivity::class.java).putExtra(
                    GuardianCodeActivity.IsFromHomePage, false
                )
            )
            finishAffinity()
        } else if (!isGPSEnabled(activity)) {
            startActivity(Intent(activity, GpsPerActivity::class.java))
            finish()
        } else if (!hasPermissions(this, locationPermission)) {
            startActivity(Intent(activity, LocationPerActivity::class.java))
            finish()
        } else if (!hasPermissions(
                activity, backGroundLocationPermission
            ) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            startActivity(Intent(activity, BackgroundLocationPerActivity::class.java))
            finish()
        } else if (!hasPermissions(this, audioPermission)) {
            startActivity(Intent(activity, AudioRecordPerActivity::class.java))
            finish()
        } else if (!hasPermissions(
                this, notificationPermission
            ) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            startActivity(Intent(activity, NotificationPerActivity::class.java))
            finish()
        } else if (!checkSelfDefaultPermissions(this, packageUsesPermission)) {
            startActivity(Intent(activity, UsagesAccessPerActivity::class.java))
            finish()
        } else if (!Settings.canDrawOverlays(activity)) {
            startActivity(Intent(activity, DisplayOverPerActivity::class.java))
            finish()
        } else {
            Log.e("TAG", "checkProfileStatus: " + SocketConnectionCall.isConnected)
            if (loginResponse?.data?.isConnectionExists == true) {
                if ((application as MyApplication).mainSocket == null || !SocketConnectionCall.isConnected) {
                    SocketConnectionCall.setSocket((application as MyApplication).mainSocket)
                    SocketConnectionCall.isFromBase = true
                    SocketConnectionCall.makeConnection(this) {
                        Log.e("TAG", "checkProfileStatus: " + "success")
//                        serviceStart()
                        utils.registerReceiver()
                    }
                } else if (!isMyServiceRunning(MainAppService::class.java)) {
                    Log.e("TAG", "checkProfileStatus: 1212", )
                    serviceStart()
                }
            }
//            serviceStart()
            startActivity(Intent(activity, DashBoardActivity::class.java))
            finishAffinity()

        }
    }

    fun serviceStart() {
        if (!isMyServiceRunning(MainAppService::class.java)) {
            Log.e("TAG", "connectParentResponse: >>>>>>>>>>>>>>>> 5555555 11 ")
            Intent(this, MainAppService::class.java).apply {
                action = MainAppService.ACTION_START
                startService(this)
            }
        }

    }
//    private fun openScreenByFlags() {
//        if (pref.getString(Const.token).toString().isNotEmpty() && !SocketConnectionCall.isConnected) {
//            if ((application as MyApplication).mainSocket != null && !SocketConnectionCall.isConnected) {
//                SocketConnectionCall.setSocket((application as MyApplication).mainSocket!!)
//                SocketConnectionCall.makeConnection(this) {
//                    openScreenByFlags()
//                }
//            }
//        } else {
//            SocketConnectionCall.isFromBase = false
//            openScreenByFlags()
//        }
//
//    }

    fun netWorkNotAvailable() {
        utils.showToast("Network not available please connect with the internet")
    }

    fun checkStatus(status: Int, msg: String) {
        if (status == 0) {
            utils.showToast(msg)

        } else if (status == 2 || status == 5) {
            utils.logOut(this, msg)

        }
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
//        SocketConnectionCall.tempDisconnect()
    }


}