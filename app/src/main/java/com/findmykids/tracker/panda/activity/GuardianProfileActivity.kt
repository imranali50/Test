package com.findmykids.tracker.panda.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityGuardianProfileBinding
import com.findmykids.tracker.panda.model.response.ConnectedGuardianResponse
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.statusBarForAll
import com.bumptech.glide.Glide
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment
import com.google.gson.Gson

class GuardianProfileActivity : BaseActivity() {

    private val b: ActivityGuardianProfileBinding by lazy {
        ActivityGuardianProfileBinding.inflate(
            layoutInflater
        )
    }
    private var isGuardianProfile: Boolean = false
    private lateinit var guardianProfileString: String
    private lateinit var guardianProfileData: ConnectedGuardianResponse.Data
    private lateinit var guardianProfileImage: String
    private lateinit var guardianProfileName: String
    private lateinit var guardianProfileConnectionID: String
    private val viewModel by viewModels<ViewModel>()

    companion object {
        const val IsGuardianProfile = "IsGuardianProfile"
        const val GuardianProfileResponse = "GuardianProfileResponse"
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        b.header.flToolBack.visibility = View.VISIBLE
        b.header.toolbarTitle.visibility = View.VISIBLE
        onClickListener()
//        setObserver()
        isGuardianProfile = intent.getBooleanExtra(IsGuardianProfile, false)
        if (isGuardianProfile) {
            guardianProfileString = intent.getStringExtra(GuardianProfileResponse).toString()
            b.header.toolbarTitle.text = "Guardian Profile"
            b.llChatAndCall.visibility = View.VISIBLE
            b.vLine.visibility = View.GONE
            b.llAge.visibility = View.GONE
            b.tvEditProfileBtn.visibility = View.GONE
            guardianProfileData =
                Gson().fromJson(guardianProfileString, ConnectedGuardianResponse.Data::class.java)
            setData()
//            getGuardianProfileAPI()
        } else {
            b.header.toolbarTitle.text = "My Profile"
            b.llChatAndCall.visibility = View.GONE
            b.vLine.visibility = View.VISIBLE
            b.llAge.visibility = View.VISIBLE
            b.tvEditProfileBtn.visibility = View.VISIBLE
        }


        val bottomAppBarTopEdgeTreatment = BottomAppBarTopEdgeTreatment(
            30f,
            30f,
            0f
        )
        bottomAppBarTopEdgeTreatment.fabDiameter =
            resources.getDimension(com.intuit.sdp.R.dimen._80sdp)
        b.rlMain.shapeAppearanceModel = b.rlMain.shapeAppearanceModel.toBuilder()
            .setTopEdge(bottomAppBarTopEdgeTreatment)
            .build()
        b.rlMain.elevation = resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
    }

    private fun setData() {
        b.tvUserName.text = guardianProfileData.name
        b.tvEmail.text = guardianProfileData.email
        b.tvMobileNo.text =
            "${guardianProfileData.countryCode}  ${guardianProfileData.mobileNumber}"
        b.tvGender.text =
            guardianProfileData.gender.substring(0, 1)
                .toUpperCase() + guardianProfileData.gender.substring(1)
                .toLowerCase()
        Glide.with(this@GuardianProfileActivity).load(guardianProfileData.profileImage).placeholder(
            R.drawable.ic_placeholder)
            .centerCrop().into(b.imgProfile)
        guardianProfileName = guardianProfileData.name
        guardianProfileImage = guardianProfileData.profileImage
        guardianProfileConnectionID = guardianProfileData.connectionId
    }

//    private fun getGuardianProfileAPI() {
//        viewModel.guardianInfo(guardianProfileID)
//    }

//    private fun setObserver() {
//        viewModel.guardianInfoResponse.observe(this) {
//            when (it) {
//                is NetworkResult.Success -> {
//                    MyApplication.progressBar.dismiss()
//                    when (it.data?.status) {
//                        "1" -> {
//                            val data = it.data.data
//                            b.tvUserName.text = data.name
//                            b.tvEmail.text = data.email
//                            b.tvMobileNo.text =
//                                "${data.countryCode}  ${data.mobileNumber}"
//                            b.tvGender.text =
//                                data.gender.substring(0, 1)
//                                    .toUpperCase() + data.gender.substring(1)
//                                    .toLowerCase()
//                            Glide.with(this@GuardianProfileActivity).load(data.profileImage)
//                                .centerCrop().into(b.imgProfile)
//                            guardianProfileName = data.name
//                            guardianProfileImage = data.profileImage
//                            guardianProfileConnectionID = data.connectionId
//                        }
//
//                        else -> {
//                            checkStatus(it.data!!.status.toInt(), it.data.message)
//                        }
//                    }
//                }
//
//                is NetworkResult.Error -> {
//                    MyApplication.progressBar.dismiss()
//                    utils.showToast(it.message.toString())
//                }
//
//                is NetworkResult.Loading -> {
//                    MyApplication.progressBar.show(activity)
//                }
//
//                is NetworkResult.NoNetwork -> {
//                    MyApplication.progressBar.dismiss()
//                    netWorkNotAvailable()
//                }
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        if (!isGuardianProfile) {
            b.tvUserName.text = loginResponse?.data?.name
            b.tvEmail.text = loginResponse?.data?.email
            b.tvMobileNo.text =
                "${loginResponse?.data?.countryCode}  ${loginResponse?.data?.mobileNumber}"
            b.tvGender.text =
                loginResponse?.data?.gender?.substring(0, 1)
                    ?.toUpperCase() + loginResponse?.data?.gender?.substring(1)
                    ?.toLowerCase()
            b.tvAge.text = loginResponse?.data?.age
            Glide.with(this@GuardianProfileActivity).load(loginResponse?.data?.profileImage).placeholder(R.drawable.ic_placeholder)
                .centerCrop().into(b.imgProfile)
        }
    }

    private fun onClickListener() {
        b.tvEditProfileBtn.setOnClickListener {
            startActivity(
                Intent(activity, ProfileCreateActivity::class.java).putExtra(
                    ProfileCreateActivity.IsEdit,
                    true
                )
            )
        }
        b.header.flToolBack.setOnClickListener {
            onBackPressed()
        }

        b.tvChatBtn.setOnClickListener {
            startActivity(
                Intent(this, ChatActivity::class.java).putExtra(
                    ChatActivity.ConnectionId, guardianProfileConnectionID
                ).putExtra(
                    ChatActivity.ChatGroupIcon, guardianProfileImage
                ).putExtra(
                    ChatActivity.ChatGroupName, guardianProfileName
                )
            )
        }

        b.tvCallBtn.setOnClickListener { }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }
}