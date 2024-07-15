package com.findmykids.tracker.panda.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.activity.viewModels
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivitySignUpBinding
import com.findmykids.tracker.panda.model.request.SignupRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarForAll


class SignUpActivity : com.findmykids.tracker.panda.activity.BaseActivity() {
    private val b: ActivitySignUpBinding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ViewModel>()
    private var phoneCode = ""
    private var phoneNumber = ""
    var phoneCodeValidator = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        int()
        onClickListener()
    }

    private fun int() {
        setObservers()
        //phone number validator
        b.ccpLoadFullNumber.setPhoneNumberValidityChangeListener { isValidNumber ->
            phoneCodeValidator = isValidNumber
        }
        //phone number formatting into country wise
        b.ccpLoadFullNumber.registerCarrierNumberEditText(b.evMobileField)
        //phoneCode
        phoneCode = b.ccpLoadFullNumber.selectedCountryCode.toString()
        b.ccpLoadFullNumber.setOnCountryChangeListener {
            phoneCode = b.ccpLoadFullNumber.selectedCountryCode
        }
    }

    private fun onClickListener() {
        b.tvSignIn.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            finish()
        }
        b.cbPasswordEye.setOnClickListener {
            if (b.cbPasswordEye.isChecked) {
                b.evPassword.transformationMethod = null
            } else {
                b.evPassword.transformationMethod = PasswordTransformationMethod()
            }
            b.evPassword.setSelection(b.evPassword.length())
        }
        b.tvSignUpBtn.setOnClickListener {
            if (isValid()) {
                sinUpAPI()
            }
        }
        b.txtAgree.setOnClickListener {

            val viewIntent = Intent(
                "android.intent.action.VIEW",
                Uri.parse("https://mykidscontrol.com/privacy-policy/")
            )
            startActivity(viewIntent)
//            startActivity(Intent(activity, PrivacyActivity::class.java))
        }
    }

    private fun sinUpAPI() {
        val avtarTokenRequest = SignupRequest(
            Const.langType,
            b.etName.text.toString().trim(),
            "",
            "+$phoneCode",
            pref.getString(Const.deviceId).toString(),
            pref.getString(Const.fcmToken).toString(),
            timezone,
            b.etEmail.text.toString().trim(),
            "",
            phoneNumber,
            b.evPassword.text.toString().trim(),
            Const.child
        )
        viewModel.singUp(avtarTokenRequest)
    }

    private fun setObservers() {
        viewModel.sinUpResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            startActivity(
                                Intent(this, OTPVerificationActivity::class.java).putExtra(
                                    OTPVerificationActivity.EmailAddress,
                                    b.etEmail.text.toString().trim()
                                ).putExtra(OTPVerificationActivity.IsForgot, false)
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

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }

    private fun isValid(): Boolean {
        var message = ""
        phoneNumber = b.ccpLoadFullNumber.fullNumber.substring(phoneCode.length)

        if (b.etName.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_name)
            b.etName.requestFocus()
        } else if (b.etEmail.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_email_address)
            b.etEmail.requestFocus()
        } else if (!utils.isValidEmail(b.etEmail.text.toString().trim())) {
            message = getString(R.string.please_enter_valid_email_address)
            b.etEmail.requestFocus()
        } else if (b.evPassword.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_password)
            b.evPassword.requestFocus()
        } else if (b.evPassword.text.toString().length < 8 || b.evPassword.text.toString().length > 15) {
            message = getString(R.string.password_must_be_8_to_15_character)
            b.evPassword.requestFocus()
        } else if (!phoneCodeValidator && phoneNumber.isNotEmpty()) {
            message = "Please enter valid phone number"
            b.evMobileField.requestFocus()
        } else if (!b.checkbox.isChecked) {
            message = "Please accept Privacy policy & Terms condition"
        }
        if (message.trim().isNotEmpty()) {
            utils.showToast(message)
            utils.hideKeyBoardFromView(this)
        }
        return message.trim().isEmpty()
    }


}