package com.findmykids.tracker.panda.permission

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.findmykids.tracker.panda.BuildConfig
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.BaseActivity
import com.findmykids.tracker.panda.databinding.ActivityPermissionBinding
import com.findmykids.tracker.panda.databinding.DialogPermissionBinding
import com.findmykids.tracker.panda.util.notificationPermission
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll

class NotificationPerActivity : BaseActivity() {
    val b: ActivityPermissionBinding by lazy {
        ActivityPermissionBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var dialog: AlertDialog
    private lateinit var permissionDialogBinding : DialogPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        initPermissionDialog()

        int()
        clickEvent()

    }

    private fun int() {
        statusBarForAll()
        b.mainImg.setImageResource(R.drawable.ic_notification_per)
        b.title.text = getString(R.string.notification_permission)
        b.subTitle.text =
            getString(R.string.notifications_are_used_to_deliver_alerts_and_updates_directly_to_your_device_keep_you_informed_and_provide_timely_updates)
        b.subTitle2.text =
            getString(R.string.certain_features_such_as_bike_speed_alert_sos_alert_require_notification_access_for_optimal_functionality)
        b.tvNext.text = getString(R.string.allow)
    }

    private fun clickEvent() {
        b.tvNext.setOnClickListener {
            if (hasPermissions(this, notificationPermission)) {
                checkProfileStatus()
            } else {
                notificationReqLauncher.launch(
                    notificationPermission
                )
            }
        }
    }


    private val notificationReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                checkProfileStatus()
            } else {
                if (shouldShow(this, notificationPermission)) {
                } else {
                    dialog.show()
                }
            }
        }

    private fun initPermissionDialog() {
        permissionDialogBinding = DialogPermissionBinding.inflate(layoutInflater)
        dialog = AlertDialog.Builder(activity).create()
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setView(
            permissionDialogBinding.root
        )

        permissionDialogBinding.tvPermissionText.text = getString(R.string.permission_text)
//        permissionDialogBinding.tvContinuePermission.text = "Setting"

        permissionDialogBinding.tvCancelPermission.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }
        permissionDialogBinding.closeImg.setOnClickListener {
            dialog.dismiss()
        }
        permissionDialogBinding.tvContinuePermission.setOnClickListener {
            settingNotificationReqLauncher.launch(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )

            )
        }
    }

    var settingNotificationReqLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (hasPermissions(this, notificationPermission)) {
                dialog.dismiss()
                checkProfileStatus()
            } else {
                dialog.show()

            }
        }


}