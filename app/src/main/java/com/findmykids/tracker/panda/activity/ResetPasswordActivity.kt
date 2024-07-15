package com.findmykids.tracker.panda.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.viewModels
import com.findmykids.tracker.panda.databinding.ActivityChangePasswordBinding
import com.findmykids.tracker.panda.model.request.ResetPasswordRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarForAll

class ResetPasswordActivity : BaseActivity() {
    private val b: ActivityChangePasswordBinding by lazy {
        ActivityChangePasswordBinding.inflate(
            layoutInflater
        )
    }
    var email = ""
    var otp = ""
    private val viewModel by viewModels<ViewModel>()

    companion object {
        const val EmailAddress = "EmailAddress"
        const val OTP = "OTP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        onClickListener()
        int()
    }

    private fun int() {
        email = intent.getStringExtra(EmailAddress).toString()
        otp = intent.getStringExtra(OTP).toString()
        setObservers()
    }

    private fun onClickListener() {
        b.header.flToolBack.visibility = View.VISIBLE
        b.header.flToolBack.setOnClickListener {
            onBackPressed()
        }
        b.cbPasswordEye.setOnClickListener {
            if (b.cbPasswordEye.isChecked) {
                b.evPassword.transformationMethod = null
            } else {
                b.evPassword.transformationMethod = PasswordTransformationMethod()
            }
            b.evPassword.setSelection(b.evPassword.length())
        }
        b.cbPasswordConfirmEye.setOnClickListener {
            if (b.cbPasswordConfirmEye.isChecked) {
                b.evPasswordConfirm.transformationMethod = null
            } else {
                b.evPasswordConfirm.transformationMethod = PasswordTransformationMethod()
            }
            b.evPasswordConfirm.setSelection(b.evPasswordConfirm.length())
        }
        b.tvSaveBtn.setOnClickListener {
            if (isValid()) {
                val request =
                    ResetPasswordRequest(email, Const.langType, b.evPassword.text.toString().trim())
                viewModel.resetPassword(request)
            }
        }
    }

    private fun setObservers() {
        viewModel.resetPasswordResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            utils.showToast(it.data.message)
                            startActivity(
                                Intent(activity, SignInActivity::class.java)
                            )
                            finishAffinity()
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
    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }

    private fun isValid(): Boolean {
        var message = ""

        if (b.evPassword.text.toString().trim().isEmpty()) {
            message = "Please Enter New Password"
            b.evPassword.requestFocus()
        } else if (b.evPassword.text.toString().length < 8 || b.evPassword.text.toString().length > 15) {
            message = "Password must be 8 to 15 character"
            b.evPassword.requestFocus()

        } else if (b.evPasswordConfirm.text.toString().trim().isEmpty()) {
            message = "Please Enter Confirm Password"
            b.evPassword.requestFocus()
        } else if (b.evPassword.text.toString() != b.evPasswordConfirm.text.toString()) {
            message = "New password and confirm password do not match"
            b.evPassword.requestFocus()
        }
        if (message.trim().isNotEmpty()) {
            utils.showToast(message)
        }
        return message.trim().isEmpty()
    }

}