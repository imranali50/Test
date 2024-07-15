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
import com.findmykids.tracker.panda.util.audioPermission
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll

class AudioRecordPerActivity : BaseActivity() {
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
        initPermissionDialog()

        int()
        clickEvent()

    }

    private fun int() {
        statusBarForAll()
        b.mainImg.setImageResource(R.drawable.ic_audio_per)
        b.title.text = getString(R.string.allow_audio_recording)
        b.subTitle.text =
            getString(R.string.microphone_access_enables_you_to_interact_guardian_s_or_parent_with_the_voice_sound_activity_that_one_experienced_around_child)
        b.subTitle2.text =
            getString(R.string.your_audio_data_is_processed_in_real_time_to_enable_voice_commands_and_provide_you_with_interactive_experiences)
        b.tvNext.text = getString(R.string.allow)
    }

    private fun clickEvent() {
        b.tvNext.setOnClickListener {
            if (hasPermissions(this, audioPermission)) {
                checkProfileStatus()
            } else {
                audioReqLauncher.launch(
                    audioPermission
                )
            }
        }
    }


    private val audioReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                checkProfileStatus()
            } else {
                if (shouldShow(this, audioPermission)) {
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
            settingAudioReqLauncher.launch(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )

            )
        }
    }

    var settingAudioReqLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (hasPermissions(this, audioPermission)) {
                dialog.dismiss()
                checkProfileStatus()
            } else {
                dialog.show()

            }
        }


}