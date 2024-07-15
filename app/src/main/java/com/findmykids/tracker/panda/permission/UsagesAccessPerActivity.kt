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
import com.findmykids.tracker.panda.util.packageUsesPermission
import com.findmykids.tracker.panda.util.checkSelfDefaultPermissions
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll


class UsagesAccessPerActivity : BaseActivity() {

    val b: ActivityPermissionBinding by lazy { ActivityPermissionBinding.inflate(layoutInflater) }
    private lateinit var permission_dialog_binding: DialogPermissionBinding
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        int()
        clickEvent()
    }


    private fun int() {
        statusBarForAll()
        initPermissionDialog()
        b.mainImg.setImageResource(R.drawable.ic_permit_uses_per)
        b.title.text = getString(R.string.permit_usage_access)
        b.subTitle.text =
            getString(R.string.analysing_usage_data_helps_us_understand_your_preferences_and_deliver_a_more_perso_nalised_and_relevant_experience)
        b.subTitle2.text =
            getString(R.string.we_use_usage_information_to_improve_our_services_identify_potential_issues_e_g_optimise_apps_screen_time)
        b.tvNext.text = getString(R.string.turn_on)

    }

    private fun clickEvent() {
        b.tvNext.setOnClickListener {
            if (checkSelfDefaultPermissions(this, packageUsesPermission)) {
                checkProfileStatus()
            } else {
                permUsageStatsReqLauncher.launch(
                    packageUsesPermission
                )
            }
        }
    }

    private val permUsageStatsReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                checkProfileStatus()
            } else {
                if (shouldShow(this, packageUsesPermission)) {

                } else {
                    settingUsageAccessLauncher.launch(
                        Intent(
                            Settings.ACTION_USAGE_ACCESS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        )
                    )
//                    dialog.show()
                }
            }
        }

    private var settingUsageAccessLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (checkSelfDefaultPermissions(this, packageUsesPermission)) {
                dialog.dismiss()
                checkProfileStatus()
            } else {
                dialog.show()
            }
        }

    private fun initPermissionDialog() {
        permission_dialog_binding = DialogPermissionBinding.inflate(layoutInflater)
        dialog = AlertDialog.Builder(activity).create()
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setView(
            permission_dialog_binding.root
        )

        permission_dialog_binding.tvPermissionText.text = getString(R.string.permission_text)
//        permission_dialog_binding.tvContinuePermission.text = "Setting"

        permission_dialog_binding.tvCancelPermission.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }
        permission_dialog_binding.closeImg.setOnClickListener {
            dialog.dismiss()
        }
        permission_dialog_binding.tvContinuePermission.setOnClickListener {
            settingUsageAccessLauncher.launch(
                Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
        }
    }


}