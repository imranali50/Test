package com.findmykids.tracker.panda.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.activity.viewModels


import com.findmykids.tracker.panda.databinding.ActivityPrivacyBinding
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarForAll


class PrivacyActivity : BaseActivity() {

    private val binding: ActivityPrivacyBinding by lazy {
        ActivityPrivacyBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        statusBarForAll()
        setObservers()

        binding.setupTool.flToolBack.visibility = View.VISIBLE
        binding.setupTool.flToolBack.setOnClickListener {
            onBackPressed()
        }

        viewModel.policy("PRIVACY_POLICY")
    }
    private fun setObservers() {
        viewModel.policyResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {

                            binding.webView.loadDataWithBaseURL(
                                null,
                                it.data.data.policy,
                                "text/html",
                                "utf-8",
                                null
                            )

                        }
                        else -> {
                            checkStatus(
                                it.data?.status.toString().toInt(), it.data?.message.toString()
                            )
                        }
                    }
                }

                is NetworkResult.Error -> {
                    MyApplication.progressBar.dismiss()
                    utils.showToast(it.message.toString())
                }

                is NetworkResult.Loading -> {
                    if (!isFinishing && !isDestroyed) MyApplication.progressBar.show(
                        this
                    )
                }

                is NetworkResult.NoNetwork -> {
                    MyApplication.progressBar.dismiss()
                    netWorkNotAvailable()
                }
            }


        }
    }



}