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
import com.findmykids.tracker.panda.util.backGroundLocationPermission
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll


class BackgroundLocationPerActivity : BaseActivity() {
    val b: ActivityPermissionBinding by lazy {
        ActivityPermissionBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var dialog: AlertDialog
    private lateinit var permissionDialogBinding: DialogPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        int()
        clickEvent()
    }

    private fun int() {
        statusBarForAll()
        initPermissionDialog()

        b.mainImg.setImageResource(R.drawable.ic_backroung_per)
        b.title.text = getString(R.string.now_choose_allow_all_the_time)
        b.subTitle.text =
            getString(R.string.this_app_will_have_access_to_your_location_even_when_the_app_is_in_the_background_this_is_recommended_for_guardian_s_for_child_real_time_update)
        b.subTitle2.text =
            getString(R.string.this_option_is_used_in_real_time_to_provide_you_with_timely_information_and_services)
        b.tvNext.text = getString(R.string.go_to_setting)
    }

    private fun clickEvent() {
        b.tvNext.setOnClickListener {
            if (hasPermissions(this, backGroundLocationPermission)) {
                checkProfileStatus()
            } else {
                locationReqLauncher.launch(
                    backGroundLocationPermission
                )
            }
        }

    }

    private val locationReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                checkProfileStatus()
            } else {
                if (shouldShow(this, backGroundLocationPermission)) {
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
            settingLocationReqLauncher.launch(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )

            )
        }
    }

    var settingLocationReqLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (hasPermissions(this, backGroundLocationPermission)) {
                dialog.dismiss()
                checkProfileStatus()
            } else {
                dialog.show()
            }
        }

}