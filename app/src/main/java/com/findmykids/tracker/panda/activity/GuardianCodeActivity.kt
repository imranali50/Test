package com.findmykids.tracker.panda.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityGuardianCodeBinding
import com.findmykids.tracker.panda.databinding.BottomSheetHowToGetCodeBinding
import com.findmykids.tracker.panda.model.request.ConnectParentRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.service.MainAppService
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.isMyServiceRunning
import com.findmykids.tracker.panda.util.statusBarForAll
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson

class GuardianCodeActivity : BaseActivity() {
    private val b: ActivityGuardianCodeBinding by lazy {
        ActivityGuardianCodeBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetHowToGetCodeBinding: BottomSheetHowToGetCodeBinding

    private var isFromHomePage: Boolean = false

    companion object {
        const val IsFromHomePage = "IsFromHomePage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        isFromHomePage = intent.getBooleanExtra(IsFromHomePage, false)
        int()
        clickEvent()
    }

    private fun int() {
        if (isFromHomePage) {
            b.GuardianCodeTool.flToolBack.visibility = View.VISIBLE
            b.GuardianCodeTool.toolBarSkip.visibility = View.GONE
        } else {
            b.GuardianCodeTool.toolBarSkip.visibility = View.VISIBLE
            b.GuardianCodeTool.flToolBack.visibility = View.GONE
        }
        b.GuardianCodeTool.flToolScan.visibility = View.VISIBLE
        setObserver()
        initBottomSheetDialog()
    }

    private fun clickEvent() {
        b.guardianCodeTitle3.setOnClickListener {
            bottomSheetDialog.show()
        }

        b.GuardianCodeTool.flToolScan.setOnClickListener {
            startActivity(Intent(this@GuardianCodeActivity, QrScannerActivity::class.java))
        }
        b.tvConnect.setOnClickListener {
            if (b.guardianCodeView.text.toString().length == 5) {
                connectParentCodeAPI()
            } else if(b.guardianCodeView.text.toString().isEmpty()){
                utils.showToast("Please enter parent’s code")
            } else {
                utils.showToast(getString(R.string.please_enter_parent_s_code))
            }

        }
        b.GuardianCodeTool.flToolBack.setOnClickListener {
            onBackPressed()
        }
        b.GuardianCodeTool.toolBarSkip.setOnClickListener {
            Const.isParentConnect = true
            checkProfileStatus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }


    private fun setObserver() {
        viewModel.connectParentResponse.observe(this, Observer {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            pref.setString(Const.userData, Gson().toJson(it.data))
                            Log.e("TAG", "connectParentResponse: >>>>>>>>>>>>> 11 " + it.data.data)
                            checkProfileStatus()
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
                    MyApplication.progressBar.show(this)
                }

                is NetworkResult.NoNetwork -> {
                    MyApplication.progressBar.dismiss()
                    netWorkNotAvailable()
                }
            }
        })
    }

    private fun connectParentCodeAPI() {
        val request = ConnectParentRequest(b.guardianCodeView.text.toString())
        viewModel.connectWithParent(request)
    }

    private fun initBottomSheetDialog() {
        bottomSheetHowToGetCodeBinding = BottomSheetHowToGetCodeBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialog.setContentView(bottomSheetHowToGetCodeBinding.root)
    }

}