package com.findmykids.tracker.panda.activity

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityMainBinding
import com.findmykids.tracker.panda.databinding.DialogUnblockBinding
import com.findmykids.tracker.panda.model.request.UnBlockRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarWhiteScreen
import java.io.DataOutputStream
import java.io.IOException


class MainActivity : BaseActivity() {
    private val b: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ViewModel>()

    companion object {
        const val PACK_NAME = "PACK_NAME"
    }

    var packName = ""
    var appName = ""

    private lateinit var dialogBinding: DialogUnblockBinding

    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarWhiteScreen()
        int()
        clickEvent()
        setObserver()
    }

    private fun int() {
        packName = intent.getStringExtra(PACK_NAME).toString()
        try {
            appName = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(
                    packName, PackageManager.GET_META_DATA
                )
            ) as String
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
//        utils.showToast(packName + "  " + appName)
        initDialog()
    }

    private fun initDialog() {
        dialogBinding = DialogUnblockBinding.inflate(layoutInflater)

        dialog = AlertDialog.Builder(activity).create()
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setView(dialogBinding.root)

        dialogBinding.ivCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.tvSubmitBtn.setOnClickListener {
            if (dialogBinding.etReason.text.toString().trim().isEmpty()) {
                utils.showToast(getString(R.string.please_enter_reason_to_unblock_this_app))
            } else {
                sendUnBlockApi(dialogBinding.etReason.text.toString().trim())
            }
        }
    }


    private fun clickEvent() {
        b.llExit.setOnClickListener {
//            closePackageApp(packName)
//            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//            am.killBackgroundProcesses(packName)
//            getAppUid(packName)
//            android.os.Process.killProcess();
//            finishAffinity()


            try {
                val suProcess = Runtime.getRuntime().exec(packName)
                val os = DataOutputStream(suProcess.outputStream)
                os.writeBytes("adb shell" + "\n")
                os.flush()
                val newContext: Context = this
                val activityManager =
                    newContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val appProcesses = activityManager.runningAppProcesses
                for (appProcess in appProcesses) {
                    if (appProcess.processName == packName) {
                    } else {
                        os.writeBytes("am force-stop " + appProcess.processName + "\n")
                    }
                }
                os.flush()
                os.close()
                suProcess.waitFor()
            } catch (ex: IOException) {
                ex.message
                Toast.makeText(applicationContext, ex.message, Toast.LENGTH_LONG).show()
            } catch (ex: SecurityException) {
                Toast.makeText(
                    applicationContext, "Can't get root access2", Toast.LENGTH_LONG
                ).show()
            } catch (ex: Exception) {
                Toast.makeText(
                    applicationContext, "Can't get root access3", Toast.LENGTH_LONG
                ).show()
            }
        }
        b.txtRequest.setOnClickListener {
            dialog.show()
        }
    }

    private fun sendUnBlockApi(text: String) {
        val request = UnBlockRequest(appName, packName, text)
        viewModel.unBlock(request)
    }

    private fun setObserver() {
        viewModel.unBlockResponse.observe(this) {
            dialog.dismiss()
            activity.runOnUiThread {
                when (it) {
                    is NetworkResult.Success -> {
                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            "1" -> {
                                dialogBinding.etReason.setText("")
                                utils.showToast(it.data.message)
                            }

                            else -> {
                                checkStatus(it.data!!.status.toInt(), it.data.message)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        MyApplication.progressBar.dismiss()
                        utils.showToast(it.message.toString())
                    }

                    is NetworkResult.Loading -> {
                        MyApplication.progressBar.show(activity)
                    }

                    is NetworkResult.NoNetwork -> {
                        MyApplication.progressBar.dismiss()
                        netWorkNotAvailable()
                    }
                }
            }
        }

    }


    private fun getAppUid(packageName: String): Int {
        val mActivityManager = activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val pidsTask = mActivityManager.runningAppProcesses
        for (i in pidsTask.indices) {
            if (pidsTask[i].processName == packageName) {
                Log.e("TAG", "getAppUid: " + pidsTask[i].uid)
                return pidsTask[i].uid
            }
        }
        return -1
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }
}