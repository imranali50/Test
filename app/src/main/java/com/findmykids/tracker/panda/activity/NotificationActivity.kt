package com.findmykids.tracker.panda.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.adapter.NotificationAdapter
import com.findmykids.tracker.panda.databinding.ActivityNotificationBinding
import com.findmykids.tracker.panda.model.response.NotificationResponse
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.statusBarForAll

class NotificationActivity : BaseActivity() {
    private val b: ActivityNotificationBinding by lazy {
        ActivityNotificationBinding.inflate(
            layoutInflater
        )
    }
    
    lateinit var notificationAdapter: NotificationAdapter
    private var list = ArrayList<NotificationResponse.Data.Notification>()
    private val viewModel by viewModels<ViewModel>()

    //pagination
    var isClearList = true
    var pageNum = 1
    var totalPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        setObservers()

        notificationAdapter = NotificationAdapter(activity, list)
        b.rvGuardianChat.adapter = notificationAdapter

        b.header.flToolBack.visibility = View.VISIBLE
        b.header.toolbarTitle.visibility = View.VISIBLE
        b.header.toolbarTitle.text = "Notification"

        b.header.flToolBack.setOnClickListener {
            onBackPressed()
        }
        viewModel.getNotification(pageNum, 20)
        b.rvGuardianChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if ((b.rvGuardianChat.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() == list.size - 1 && pageNum != totalPage) {
                    pageNum++
                    isClearList = false
                    viewModel.getNotification(pageNum, 20)
                }
            }
        })

    }
    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }
    private fun setObservers() {
        viewModel.getNotificationResponse.observe(this) {
                when (it) {
                    is NetworkResult.Success -> {
                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            1 -> {
                                MyApplication.progressBar.dismiss()
                                if (isClearList) {
                                    list.clear()
                                }
                                totalPage = it.data.data.totalPage
                                list.addAll(it.data.data.notificationList)
                                notificationAdapter.notifyDataSetChanged()

                                if(list.isEmpty()){
                                    b.noDataImg.visibility = View.VISIBLE
                                }else{
                                    b.noDataImg.visibility = View.GONE
                                }
                            }

                            else -> {
                                checkStatus(it.data!!.status, it.data.message)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        MyApplication.progressBar.dismiss()
                        utils.showToast(it.message.toString())
                    }

                    is NetworkResult.Loading -> {
                        if (this != null && !isFinishing && !isDestroyed) MyApplication.progressBar.show(
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