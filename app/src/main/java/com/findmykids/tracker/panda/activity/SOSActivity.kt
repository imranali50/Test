package com.findmykids.tracker.panda.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.findmykids.tracker.panda.databinding.ActivitySosactivityBinding
import com.findmykids.tracker.panda.model.request.SOSRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.socketMvvm.SocketModelFactory
import com.findmykids.tracker.panda.socketMvvm.SocketRepository
import com.findmykids.tracker.panda.socketMvvm.SocketViewModel
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.SocketResult
import com.findmykids.tracker.panda.util.statusBarForAll

class SOSActivity : com.findmykids.tracker.panda.activity.BaseActivity() {
    private val b: ActivitySosactivityBinding by lazy {
        ActivitySosactivityBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()
    private var socketViewModel: SocketViewModel? = null
    val socketRepository = SocketRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        setObserver()
        getDataAndSetObserver()
        b.tvdeactivateBtn.setOnClickListener {
            val request = SOSRequest(
                SOSRequest.CurrentLocation(
                    pref.getString(Const.latitude).toString(),
                    pref.getString(Const.longitude).toString()
                ), Const.DeActiveSOS
            )
            viewModel.sosSendResponse(request)
        }
    }

    private fun setObserver() {
        viewModel.sosSendResponse.observe(this) {
            runOnUiThread {
                when (it) {
                    is NetworkResult.Success -> {
                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            "1" -> {
                                finish()
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
    private fun getDataAndSetObserver() {
        SocketEvents.setRepository(socketRepository, applicationContext)
        socketViewModel = ViewModelProvider(
            this, SocketModelFactory(socketRepository)
        )[SocketViewModel::class.java]
        socketViewModel?.sosDeActive?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
                    Log.e("TAG", "connectJoinData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                    utils.showToast("Your parent has received SOS signal")
                    finish()
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }
}