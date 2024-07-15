package com.findmykids.tracker.panda.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityDashBoardBinding
import com.findmykids.tracker.panda.databinding.DialogLogoutBinding
import com.findmykids.tracker.panda.databinding.DialogUnblockBinding
import com.findmykids.tracker.panda.fragment.ChatFragment
import com.findmykids.tracker.panda.fragment.HomeFragment
import com.findmykids.tracker.panda.model.request.DeleteAccountRequest
import com.findmykids.tracker.panda.model.request.RequestLogout
import com.findmykids.tracker.panda.model.request.SOSRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.service.MainAppService
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.socketMvvm.SocketModelFactory
import com.findmykids.tracker.panda.socketMvvm.SocketRepository
import com.findmykids.tracker.panda.socketMvvm.SocketViewModel
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.SocketResult
import com.findmykids.tracker.panda.util.isMyServiceRunning
import com.findmykids.tracker.panda.util.statusBarForAll
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable


class DashBoardActivity : BaseActivity(), View.OnClickListener {

    private val b: ActivityDashBoardBinding by lazy {
        ActivityDashBoardBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var dialogBinding: DialogLogoutBinding
    private lateinit var dialog: AlertDialog
    val socketRepository = SocketRepository()
    var socketViewModel: SocketViewModel? = null
    private val viewModel by viewModels<ViewModel>()

    private lateinit var dialogBindingDelete: DialogUnblockBinding
    private lateinit var dialogDelete: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()

        SocketEvents.setRepository(socketRepository, applicationContext)
        socketViewModel = ViewModelProvider(
            this, SocketModelFactory(socketRepository)
        )[SocketViewModel::class.java]

        setVisibilityAndData()
        setBootmNavigation()
        setCurves()
        setClicklistener()
        initDialog()
        initDeleteDialog()
        setObserver()
//        SocketEvents.registerAllEvents()
    }

    @SuppressLint("RestrictedApi")
    private fun setCurves() {
        val bottomAppBarTopEdgeTreatment = BottomAppBarTopEdgeTreatment(
            15f, 30f, 0f
        )
        bottomAppBarTopEdgeTreatment.fabDiameter =
            resources.getDimension(com.intuit.sdp.R.dimen._60sdp)
        b.rlBottomNav.shapeAppearanceModel =
            b.rlBottomNav.shapeAppearanceModel.toBuilder().setTopEdge(bottomAppBarTopEdgeTreatment)
                .build()

    }

    @SuppressLint("SetTextI18n")
    private fun initDeleteDialog() {
        dialogBindingDelete = DialogUnblockBinding.inflate(layoutInflater)

        dialogDelete = AlertDialog.Builder(this@DashBoardActivity).create()
        dialogDelete.setCancelable(true)
        dialogDelete.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogBindingDelete.tvDialogTitle.text = "Delete Account"
        dialogDelete.setView(dialogBindingDelete.root)
        dialogBindingDelete.txtSubTitle.text =
            getString(R.string.provide_reason_why_you_delete_this_account)
        dialogBindingDelete.tvSubmitBtn.setOnClickListener {
            if (dialogBindingDelete.etReason.text.toString().trim().isEmpty()) {
                utils.showToast(getString(R.string.please_provide_a_reason_for_the_deletion_of_this_account))
            } else {
                val request =
                    DeleteAccountRequest(dialogBindingDelete.etReason.text.toString().trim())
                viewModel.deleteAccount(request)
            }
        }
        dialogBindingDelete.ivCloseDialog.setOnClickListener {
            dialogDelete.dismiss()
        }

    }

    private fun initDialog() {
        dialogBinding = DialogLogoutBinding.inflate(layoutInflater)

        dialog = AlertDialog.Builder(this@DashBoardActivity).create()
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setView(dialogBinding.root)

        dialogBinding.tvCancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.tvLogoutBtn.setOnClickListener {
            viewModel.logOutUser(RequestLogout(pref.getString(Const.fcmToken).toString()))
            dialog.dismiss()
        }
    }


    private fun setObserver() {
        viewModel.logoutResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            utils.logOut(this, it.data.message)
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
        viewModel.deleteAccountResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            dialogDelete.dismiss()
                            utils.logOut(this, it.data.message)
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

        viewModel.sosSendResponse.observe(this) {
            activity.runOnUiThread {
                when (it) {
                    is NetworkResult.Success -> {
                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            "1" -> {
                                startActivity(
                                    Intent(
                                        this@DashBoardActivity, SOSActivity::class.java
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
                        MyApplication.progressBar.show(activity)
                    }

                    is NetworkResult.NoNetwork -> {
                        MyApplication.progressBar.dismiss()
                        netWorkNotAvailable()
                    }
                }
            }
        }
        viewModel.getUnReadCountResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
//                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        1 -> {
                            if (it.data.data.unreadCount != 0) {
                                b.headerDashboardTool.tvNotifyCount.visibility = View.VISIBLE
                                b.headerDashboardTool.tvNotifyCount.text =
                                    it.data.data.unreadCount.toString()
                            } else {
                                b.headerDashboardTool.tvNotifyCount.visibility = View.GONE
                            }
                        }

                        else -> {
                            checkStatus(it.data!!.status.toInt(), it.data.message)
                        }
                    }
                }

                is NetworkResult.Error -> {
//                    MyApplication.progressBar.dismiss()
                    utils.showToast(it.message.toString())
                }

                is NetworkResult.Loading -> {
//                    MyApplication.progressBar.show(this)
                }

                is NetworkResult.NoNetwork -> {
//                    MyApplication.progressBar.dismiss()
                    netWorkNotAvailable()
                }

            }
        }
        SocketEvents.setRepository(socketRepository, applicationContext)
        socketViewModel = ViewModelProvider(
            this, SocketModelFactory(socketRepository)
        )[SocketViewModel::class.java]

//        handlerAudio = Handler(Looper.getMainLooper())
        socketViewModel?.audioNotifyData?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
//                    Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 333  " + it.data)
//                    Thread {
//                        it.data as JSONObject
//                        Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 444 " + it.data)
//                        isStop = it.data.getString("toStop").equals("1")
//                        if (ContextCompat.checkSelfPermission(
//                                mApplicationContext!!, Manifest.permission.RECORD_AUDIO
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> not allowed record : ")
//                            val json = JSONObject()
//                            json.put("requestAudioId", it.data.getString("requestAudioId"))
//                            json.put("audioData", "")
//                            json.put("isAudioPermissionAllowed", 0)
//                            SocketConnectionCall.sendDataToServer(Const.audioStreamSend, json)
//                        } else {
//                            Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> allowed record : " + isStop)
//                            if (!isStop) {
//                                audioRecord?.startRecording()
//                            }
//
//                            while (!isStop) {
//
//                                val buffer = ByteArray(BUFFER_SIZE)
//
//                                Log.e("TAG", ">>>>>>>>>>>>>>>>>> do : " + isStop)
//                                val byteRead =
//                                    if (isStop) break else audioRecord?.read(
//                                        buffer,
//                                        0,
//                                        BUFFER_SIZE
//                                    )!!
//                                if ((byteRead < -1) || isStop) break
//                                val base64Data: String =
//                                    Base64.encodeToString(
//                                        buffer,
//                                        0,
//                                        byteRead,
//                                        Base64.NO_WRAP
//                                    )
//                                val json = JSONObject()
//                                json.put(
//                                    "requestAudioId",
//                                    it.data.getString("requestAudioId")
//                                )
//                                json.put("audioData", base64Data)
//                                json.put("isAudioPermissionAllowed", 1)
//                                SocketConnectionCall.sendDataToServer(
//                                    Const.audioStreamSend, json
//                                )
//                            }
//                            if (isStop) {
//                                audioRecord?.stop()
////                                audioRecord.release()
//                            }
//                        }
//                    }.start()
                }

                is SocketResult.Loading -> {
                }

                is SocketResult.NoNetwork -> {
                }

            }
        }
    }

    override fun onBackPressed() {
        // Get the current fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.flContainer)

        // Check if the current fragment is the first fragment
        if (currentFragment is HomeFragment) {
            finishAffinity()
        } else {
            makeSelection(1)
        }
    }

    private fun setClicklistener() {
        b.ivHome.setOnClickListener(this)
        b.ivChat.setOnClickListener(this)
        b.headerDashboardTool.flToolSort.setOnClickListener {
            if (b.mainDrawer.isDrawerOpen(GravityCompat.START)) b.mainDrawer.closeDrawer(
                GravityCompat.END
            )
            else b.mainDrawer.openDrawer(GravityCompat.START)
        }
        b.headerDashboardTool.flToolNotify.setOnClickListener {
            startActivity(Intent(this@DashBoardActivity, NotificationActivity::class.java))
        }
//        pref.setString(Const.latitude).toString()
        b.tvSOSBtn.setOnClickListener {
            if (loginResponse?.data?.isConnectionExists == true) {
                val request = SOSRequest(
                    SOSRequest.CurrentLocation(
                        pref.getString(Const.latitude).toString(),
                        pref.getString(Const.longitude).toString()
                    ), Const.ActiveSOS
                )
                viewModel.sosSendResponse(request)
            } else {
                utils.showToast("Please connect to your parent phone")
            }
        }
        b.llUserSettings.setOnClickListener {
            if (b.mainDrawer.isDrawerOpen(GravityCompat.START)) {
                b.mainDrawer.closeDrawer(GravityCompat.START)
            }
            startActivity(
                Intent(this@DashBoardActivity, GuardianProfileActivity::class.java).putExtra(
                    GuardianProfileActivity.IsGuardianProfile, false
                )
            )
        }
        b.llLogOut.setOnClickListener {
            if (b.mainDrawer.isDrawerOpen(GravityCompat.START)) {
                b.mainDrawer.closeDrawer(GravityCompat.START)
            }
            dialog.show()
        }
        b.llRate.setOnClickListener {
            if (b.mainDrawer.isDrawerOpen(GravityCompat.START)) {
                b.mainDrawer.closeDrawer(GravityCompat.START)
            }
            rateApp()

        }
        b.llDelete.setOnClickListener {
            if (b.mainDrawer.isDrawerOpen(GravityCompat.START)) {
                b.mainDrawer.closeDrawer(GravityCompat.START)
            }
            dialogDelete.show()
        }
        b.llPrivachy.setOnClickListener {
            if (b.mainDrawer.isDrawerOpen(GravityCompat.START)) {
                b.mainDrawer.closeDrawer(GravityCompat.START)
            }
//            startActivity(Intent(activity, PrivacyActivity::class.java))
            val viewIntent = Intent(
                "android.intent.action.VIEW",
                Uri.parse("https://mykidscontrol.com/privacy-policy/")
            )
            startActivity(viewIntent)
        }
        b.llUserAdd.setOnClickListener {
            if (b.mainDrawer.isDrawerOpen(GravityCompat.START)) {
                b.mainDrawer.closeDrawer(GravityCompat.START)
            }
            startActivity(
                Intent(this@DashBoardActivity, GuardianCodeActivity::class.java).putExtra(
                    GuardianCodeActivity.IsFromHomePage, true
                )
            )
        }
    }

    private fun rateApp() {
        val uri = Uri.parse("market://details?id=$packageName")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show()
        }
    }

    private fun setBootmNavigation() {
        setBottomNaviagtion()
        makeSelection(1)
    }

    private fun setVisibilityAndData() {
        b.headerDashboardTool.flToolSort.visibility = View.VISIBLE
        b.headerDashboardTool.flToolNotify.visibility = View.VISIBLE
//        b.headerDashboardTool.tvNotifyCount.visibility = View.VISIBLE
        b.headerDashboardTool.toolbarTitle.visibility = View.VISIBLE
        b.headerDashboardTool.toolbarTitle.text = getString(R.string.app_name)

    }

    fun NavigationView.changeCornerRadius() {
        val navViewBackground: MaterialShapeDrawable = background as MaterialShapeDrawable
        val radius = 390f
        navViewBackground.shapeAppearanceModel = navViewBackground.shapeAppearanceModel.toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, radius).build()
    }

    private fun setBottomNaviagtion() {

    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flContainer, fragment)
            commit()
        }

    override fun onClick(v: View?) {
        if (v?.id == R.id.ivHome) {
            makeSelection(1)
        } else if (v?.id == R.id.ivChat) {
            makeSelection(2)
        }
    }

    private fun makeSelection(i: Int) {
        b.apply {
            if (i == 1) {
                ivChat.setColorFilter(
                    ContextCompat.getColor(
                        this@DashBoardActivity, R.color.nav_text_unselect
                    )
                )
                ivHome.setColorFilter(
                    ContextCompat.getColor(
                        this@DashBoardActivity, R.color.text_black_color
                    )
                )
                setCurrentFragment(HomeFragment(this@DashBoardActivity))
            } else {
                ivHome.setColorFilter(
                    ContextCompat.getColor(
                        this@DashBoardActivity, R.color.nav_text_unselect
                    )
                )
                ivChat.setColorFilter(
                    ContextCompat.getColor(
                        this@DashBoardActivity, R.color.text_black_color
                    )
                )
                setCurrentFragment(ChatFragment(this@DashBoardActivity))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
//        audioRecord?.release()
    }

    override fun onResume() {
        super.onResume()
        if (loginResponse?.data?.isConnectionExists == true && !isMyServiceRunning(
                MainAppService::class.java
            )
        ) {
            Log.e("TAG", "onResume: >>>>>>>>>>>>>>>> 5555555 ")
            Intent(this, MainAppService::class.java).apply {
                action = MainAppService.ACTION_START
                startService(this)
            }
        }
        SocketEvents.setRepository(socketRepository, applicationContext)
        viewModel.getUnReadCount()
    }

}