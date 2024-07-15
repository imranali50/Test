package com.findmykids.tracker.panda.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.findmykids.tracker.panda.BuildConfig
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityQrScannerBinding
import com.findmykids.tracker.panda.model.request.ConnectParentRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.service.MainAppService
import com.findmykids.tracker.panda.util.CAMERA_PERMISSION
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.checkSelfPermissions
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.isMyServiceRunning
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class QrScannerActivity : com.findmykids.tracker.panda.activity.BaseActivity() {
    private lateinit var codeScanner: CodeScanner
    private val b: ActivityQrScannerBinding by lazy {
        ActivityQrScannerBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        setObserver()
        if (hasPermissions(this@QrScannerActivity, CAMERA_PERMISSION)) {
            setAllData()
        } else {
            permCameraLauncher.launch(CAMERA_PERMISSION)
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
                            Log.e("TAG", "connectParentResponse: >>>>>>>>>>>>> 22 " + it.data.data)
                            if (it.data.data.isConnectionExists && !isMyServiceRunning(
                                    MainAppService::class.java
                                )
                            ) {
                                Log.e("TAG", "connectParentResponse: >>>>>>>>>>>>>>>> 5555555 22 ")
                                Intent(this, MainAppService::class.java).apply {
                                    action = MainAppService.ACTION_START
                                    startService(this)
                                }
                            }
                            checkProfileStatus()
                        }

                        else -> {
                            checkStatus(
                                it.data!!.status.toInt(), it.data.message
                            )
                            codeScanner.startPreview()
                            b.linScan.visibility = View.VISIBLE
                            b.scanningView.startAnimation()
                        }
                    }
                }

                is NetworkResult.Error -> {
                    MyApplication.progressBar.dismiss()
                    utils.showToast(it.message.toString())
                    codeScanner.startPreview()
                    b.linScan.visibility = View.VISIBLE
                    b.scanningView.startAnimation()

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

    private val permCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                setAllData()
            } else {
                if (shouldShow(this@QrScannerActivity, CAMERA_PERMISSION)) {

                } else {
                    val snackBar = Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.permission_text),
                        Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        settingCameraLauncher.launch(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                            )
                        )
                    }
                    val snackBarView = snackBar.view
                    val textView =
                        snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    textView.maxLines = 5
                    snackBar.show()
                }
            }
        }

    private var settingCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (checkSelfPermissions(this@QrScannerActivity, CAMERA_PERMISSION)) {
                setAllData()
            }
        }

    private fun setAllData() {
        openQRCode()
        b.header.flToolBack.visibility = View.VISIBLE
        b.header.flToolBack.setOnClickListener {
            onBackPressed()
        }
        b.header.toolbarTitle.visibility = View.VISIBLE
        b.header.toolbarTitle.text = "QR Scan"
    }

    private fun openQRCode() {
//        permissionUtils!!.requestPermission(PermissionUtils.PERMISSION_CAMERA_PICTURE)
        codeScanner = CodeScanner(this, b.scannerView)
        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        codeScanner.startPreview()
        // Callbacks
        var codeQr: String = ""
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                codeQr = it.text
//                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                b.scanningView.stopAnimation()
                b.linScan.visibility = View.GONE
                Log.e("TAG", "openQRCode: >>>>>>>>>>>>>>>> " + codeQr)
//                if (codeQr.length == 5) {
                connectParentCodeAPI(codeQr)
//                } else {
//                    utils.showToast("Code is not valid, please try again")
//                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(
                    this, "Camera initialization error: ${it.message}", Toast.LENGTH_LONG
                ).show()
            }
        }

        b.scannerView.setOnClickListener {
            codeScanner.startPreview()

        }
        b.linScan.visibility = View.VISIBLE
        b.scanningView.startAnimation()
    }

    private fun connectParentCodeAPI(codeQr: String) {
        val request = ConnectParentRequest(codeQr)
        viewModel.connectWithParent(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }

}