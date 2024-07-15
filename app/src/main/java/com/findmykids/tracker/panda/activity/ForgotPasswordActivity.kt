package com.findmykids.tracker.panda.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityForgotPasswordBinding
import com.findmykids.tracker.panda.model.request.LoginRequest
import com.findmykids.tracker.panda.model.request.ResendRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarForAll
import com.google.gson.Gson

class ForgotPasswordActivity : com.findmykids.tracker.panda.activity.BaseActivity() {

    private val b: ActivityForgotPasswordBinding by lazy {
        ActivityForgotPasswordBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()

    companion object {
        const val IS_BLANK = "IS_BLANK"
    }

    var isBlank = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        onClickListener()
        int()
        setObservers()
    }


    private fun int() {
        isBlank = intent.getBooleanExtra(IS_BLANK, false)
        if (isBlank) {
            b.tvResetPassword.text = getString(R.string.enter_email_address)
            b.tvSubContent.text =
                getString(R.string.enter_your_email_address_to_verify_your_account)
        }
    }

    private fun onClickListener() {
        b.header.flToolBack.visibility = View.VISIBLE
        b.header.flToolBack.setOnClickListener {
            onBackPressed()
        }
        b.tvNextBtn.setOnClickListener {
            if (isValid()) {
                if (isBlank) {
                    val loginRequest = LoginRequest(
                        Const.langType,
                        Const.authId,
                        "google",
                        pref.getString(Const.deviceId).toString(),
                        pref.getString(Const.fcmToken).toString(),
                        b.etEmail.text.toString().trim(),
                        Const.authName,
                        Const.child,
                        ""
                    )
                    viewModel.login(loginRequest)

                } else {
                    val request = ResendRequest(b.etEmail.text.toString().trim(), Const.langType)
                    viewModel.forgotPassword(request)
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

    private fun setObservers() {
        viewModel.loginResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "2" -> {
                            pref.setString(Const.userData, Gson().toJson(it.data))
                            pref.setString(Const.token, it.data.data.token)
                            pref.setString(Const.id, it.data.data._id)
                            Const.tokenForApi = pref.getString(Const.token).toString()
                            (application as MyApplication).initSocket()
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

        }

        viewModel.forgotPasswordResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            startActivity(
                                Intent(
                                    this@ForgotPasswordActivity, OTPVerificationActivity::class.java
                                ).putExtra(OTPVerificationActivity.IsForgot, true).putExtra(
                                    OTPVerificationActivity.EmailAddress,
                                    b.etEmail.text.toString().trim()
                                )
                            )
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

        }

    }

    private fun isValid(): Boolean {
        var message = ""
        if (b.etEmail.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_email_address)
            b.etEmail.requestFocus()
        } else if (!utils.isValidEmail(b.etEmail.text.toString().trim())) {
            message = getString(R.string.please_enter_valid_email_address)
            b.etEmail.requestFocus()
        }
        if (message.trim().isNotEmpty()) {
            utils.showToast(message)
            utils.hideKeyBoardFromView(this)
        }
        return message.trim().isEmpty()
    }

}