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
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll


class DisplayOverPerActivity : BaseActivity() {

    val b: ActivityPermissionBinding by lazy { ActivityPermissionBinding.inflate(layoutInflater) }
    private lateinit var permissionDialogBinding: DialogPermissionBinding
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
        b.mainImg.setImageResource(R.drawable.ic_display_over_per)
        b.title.text = getString(R.string.allow_display_over_other_apps)
        b.subTitle.text =
            getString(R.string.enabling_this_feature_allows_you_to_provide_the_following_benefits_e_g_overlay_content_for_multitasking_quick_actions_etc)
        b.subTitle2.text =
            getString(R.string.your_screen_may_display_overlays_related_to_this_app_restricted_app_ensuring_a_seamless_user_experience)
        b.tvNext.text = getString(R.string.turn_on)
    }

    private fun clickEvent() {
        b.tvNext.setOnClickListener {
            if (Settings.canDrawOverlays(activity)) {
                checkProfileStatus()
            } else {
                permManageScreenOverLayReqLauncher.launch(
                    packageUsesPermission
                )

            }
        }
    }

    private val permManageScreenOverLayReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                checkProfileStatus()
            } else {
                if (shouldShow(this, packageUsesPermission)) {
                } else {
//                    dialog.show()
                    settingManageOverlayLauncher.launch(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        )
                    )
                }
            }
        }

    private var settingManageOverlayLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Settings.canDrawOverlays(activity)) {
                dialog.dismiss()
                checkProfileStatus()
            } else {
                dialog.show()
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
            settingManageOverlayLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
        }
    }



}