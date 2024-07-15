package com.findmykids.tracker.panda.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivitySignInBinding
import com.findmykids.tracker.panda.model.request.LoginRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarForAll
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson

class SignInActivity : com.findmykids.tracker.panda.activity.BaseActivity() {
    private val b: ActivitySignInBinding by lazy { ActivitySignInBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ViewModel>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        onClickListener()
        setObserver()
        googleAuth()
    }

    private fun googleAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }


    private fun onClickListener() {
        b.tvSignUp.setOnClickListener {
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            finish()
        }

        b.tvForget.setOnClickListener {
            startActivity(Intent(this@SignInActivity, ForgotPasswordActivity::class.java))
        }

        b.tvSignInBtn.setOnClickListener {
            if (isValid()) {
                val loginRequest = LoginRequest(
                    Const.langType,
                    "",
                    "local",
                    pref.getString(Const.deviceId).toString(),
                    pref.getString(Const.fcmToken).toString(),
                    b.etEmail.text.toString().trim(),
                    "",
                    Const.child,
                    b.etPassword.text.toString().trim()
                )
                viewModel.login(loginRequest)
            }
        }

        b.cbPasswordEye.setOnClickListener {
            if (b.cbPasswordEye.isChecked) {
                b.etPassword.transformationMethod = null
            } else {
                b.etPassword.transformationMethod = PasswordTransformationMethod()
            }
            b.etPassword.setSelection(b.etPassword.length())
        }
        b.llGoogle.setOnClickListener {
            if(utils.isNetworkAvailable()){
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }else{
                netWorkNotAvailable()
            }

        }
    }

    private var googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val googleAccount = task.getResult(ApiException::class.java)
                    makeAuthentication(googleAccount.idToken)

                } catch (e: Exception) {
                    utils.showToast(e.message.toString())
                }
            }
        }

    private fun makeAuthentication(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = firebaseAuth.currentUser
                Log.e("TAG", "onComplete: >>>>>>>>>>  " + user?.email)
                Log.e("TAG", "onComplete: >>>>>>>>>>  " + user?.phoneNumber)
                Log.e("TAG", "onComplete: >>>>>>>>>>  " + user?.displayName)
                Log.e("TAG", "onComplete: >>>>>>>>>>  " + user?.displayName)
                Log.e("TAG", "providerId: >>>>>>>>>>  " + user?.uid)

                if (user != null) {
                    Const.authId = user.uid
                    Const.authName = user.displayName.toString()
                    Const.authEmail = user.email.toString()

                    val loginRequest = LoginRequest(
                        Const.langType,
                        Const.authId,
                        "google",
                        pref.getString(Const.deviceId).toString(),
                        pref.getString(Const.fcmToken).toString(),
                        Const.authEmail,
                        Const.authName,
                        Const.child,
                        ""
                    )
                    viewModel.login(loginRequest)
                }

            }
        }
    }


    private fun setObserver() {
        viewModel.loginResponse.observe(this) {
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

                        "3" -> {
                            startActivity(
                                Intent(this, OTPVerificationActivity::class.java).putExtra(
                                    OTPVerificationActivity.EmailAddress,
                                    b.etEmail.text.toString().trim()
                                ).putExtra(OTPVerificationActivity.IsForgot, false)
                            )

                        }

                        "2" -> {
                            pref.setString(Const.userData, Gson().toJson(it.data))
                            pref.setString(Const.token, it.data.data.token)
                            pref.setString(Const.id, it.data.data._id)
                            Const.tokenForApi = pref.getString(Const.token).toString()
                            (application as MyApplication).initSocket()
                            checkProfileStatus()
                        }

                        "4" -> {
                            startActivity(
                                Intent(
                                    this, ForgotPasswordActivity::class.java
                                ).putExtra(
                                    ForgotPasswordActivity.IS_BLANK, true
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
    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
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
        } else if (b.etPassword.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_password)
            b.etPassword.requestFocus()
        } else if (b.etPassword.text.toString().length < 8 || b.etPassword.text.toString().length > 15) {
            message = getString(R.string.password_must_be_8_to_15_character)
            b.etPassword.requestFocus()
        }
//        else if (!b.chkTerms.isChecked) {
//            message = getString(R.string.first_accept_terms_and_conditions)
//        }
        if (message.trim().isNotEmpty()) {
            utils.showToast(message)
            utils.hideKeyBoardFromView(this)
        }
        return message.trim().isEmpty()
    }

}