package com.findmykids.tracker.panda.activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityOtpverificationBinding
import com.findmykids.tracker.panda.model.request.ResendRequest
import com.findmykids.tracker.panda.model.request.VerifyRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarForAll
import com.google.gson.Gson

class OTPVerificationActivity : BaseActivity() {

    private val b: ActivityOtpverificationBinding by lazy {
        ActivityOtpverificationBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()

    companion object {
        const val EmailAddress = "EmailAddress"
        const val IsForgot = "IsForgot"
    }

    var email = ""
    var isforgot = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        int()
        clickEvent()
    }

    private fun int() {
        email = intent.getStringExtra(EmailAddress).toString()
        isforgot = intent.getBooleanExtra(IsForgot, false)
        b.OTPTool.flToolBack.visibility = View.VISIBLE
        b.OTPTool.flToolBack.setOnClickListener {
            finish()
        }
        val text = getString(R.string.enter_email_otp_text, "<b><font color=#1c1c1c>$email</font></b>")
        b.otpTitle2.text = Html.fromHtml(text)
        if (isforgot) {
            b.otpTitle1.text = getString(R.string.verification_code)
            b.ivImageOTP.setImageResource(R.drawable.ic_otp_verification)
        }
        setObservers()
    }

    private fun clickEvent() {
        b.tvVerifyOTPBtn.setOnClickListener {
            if (b.pinView.text.toString().length == 5) {
                if (isforgot) {
                    checkForgotCodeAPi()
                } else {
                    verify()
                }
            } else if (b.pinView.text.toString().isEmpty()) {
                utils.showToast("Please enter OTP")
            } else {
                utils.showToast(getString(R.string.please_enter_otp))
            }

        }

        b.tvResend.setOnClickListener {
//            if (isforgot) {
//                sendForgotCodeApi()
//            } else {
//                resendVerificationAPI()
//            }
            resendVerificationAPI()
        }
    }

    fun sendForgotCodeApi() {
        val request = ResendRequest(email, Const.langType)
        viewModel.resendOtp(request)
    }

    fun checkForgotCodeAPi() {
        val request = VerifyRequest(email, Const.langType, b.pinView.text.toString())
        viewModel.forgotCheckOtp(request)
    }

    private fun resendVerificationAPI() {
        val request = ResendRequest(email, Const.langType)
        viewModel.resendOtp(request)
    }

    private fun verify() {
        val request = VerifyRequest(email, Const.langType, b.pinView.text.toString())
        viewModel.verifyOtp(request)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }

    private fun setObservers() {
        viewModel.resendResponse.observe(this, Observer {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
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
                    MyApplication.progressBar.show(this)
                }

                is NetworkResult.NoNetwork -> {
                    MyApplication.progressBar.dismiss()
                    netWorkNotAvailable()
                }
            }
        })

        viewModel.verifyResponse.observe(this, Observer {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
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
        })

        /*
                viewModel.forgotPasswordResponse.observe(this) {
                    when (it) {
                        is NetworkResult.Success -> {
                            utils.dismissProgress()
                            when (it.data?.status) {
                                "1" -> {
                                    utils.dismissProgress()
                                    utils.showToast(it.data.message)
                                }

                                else -> {
                                    checkStatus(it.data!!.status.toInt(), it.data.message)
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            utils.dismissProgress()
                            utils.showToast(it.message.toString())
                        }

                        is NetworkResult.Loading -> {
                            utils.showProgress(activity)
                        }

                        is NetworkResult.NoNetwork -> {
                            utils.dismissProgress()
                            netWorkNotAvailable()
                        }
                    }

                }
        */
        viewModel.forgotCheckOtpResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            startActivity(
                                Intent(this, ResetPasswordActivity::class.java).putExtra(
                                    ResetPasswordActivity.EmailAddress, email
                                )
                            )
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
                    MyApplication.progressBar.show(this)
                }

                is NetworkResult.NoNetwork -> {
                    MyApplication.progressBar.dismiss()
                    netWorkNotAvailable()
                }
            }

        }
    }

}

