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
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.locationPermission
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll

class LocationPerActivity : BaseActivity() {
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
        b.mainImg.setImageResource(R.drawable.ic_location_per)
        b.title.text = getString(R.string.location_permission)
        b.subTitle.text =
            getString(R.string.knowing_your_location_helps_us_tailor_our_services_to_your_local_context_making_your_experience_more_relevant_and_useful)
        b.subTitle2.text =
            getString(R.string.your_location_data_is_only_used_within_the_app_for_the_specified_purposes_and_isn_t_shared_with_others)
        b.tvNext.text = getString(R.string.allow)
    }

    private fun clickEvent() {
        b.tvNext.setOnClickListener {
            if (hasPermissions(this, locationPermission)) {
                checkProfileStatus()
            } else {
                locationReqLauncher.launch(
                    locationPermission
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
                if (hasPermissions(this, locationPermission)) {
                    checkProfileStatus()
                }
            } else {
                if (shouldShow(this, locationPermission)) {

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
            if (hasPermissions(this, locationPermission)) {
                dialog.dismiss()
                checkProfileStatus()
            } else {
                dialog.show()
            }
        }


}