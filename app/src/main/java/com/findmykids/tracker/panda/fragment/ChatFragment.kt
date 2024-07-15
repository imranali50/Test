package com.findmykids.tracker.panda.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.findmykids.tracker.panda.activity.DashBoardActivity
import com.findmykids.tracker.panda.adapter.GuardianChatAdapter
import com.findmykids.tracker.panda.databinding.FragmentChatBinding
import com.findmykids.tracker.panda.model.response.AllChatResponse
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.socketMvvm.SocketModelFactory
import com.findmykids.tracker.panda.socketMvvm.SocketViewModel
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.SocketResult


class ChatFragment(private val dashBoardActivity: DashBoardActivity) : BaseFragment() {
    private lateinit var b: FragmentChatBinding
    private val viewModel by viewModels<ViewModel>()
    private var allChatDetails: List<AllChatResponse.Data> = ArrayList()
    private lateinit var guardianChatAdapter: GuardianChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        b = FragmentChatBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapterGuardians()
        int()
        getDataAndSetObserver()
    }

    private fun int() {
        setObserver()
        if (dashBoardActivity.loginResponse?.data?.isConnectionExists == true) {
            b.txtNotFound.visibility = View.GONE
            getAllChatDetailAPI()
        } else {
            b.txtNotFound.visibility = View.VISIBLE
        }
    }

    private fun setObserver() {
        viewModel.allChatDetailResponse.observe(viewLifecycleOwner) {
            activity.runOnUiThread {


                when (it) {
                    is NetworkResult.Success -> {
                        Log.e("TAG", "setObserver: "+"54545454545454545454545454545454" )
                        b.rvGuardianChat.visibility = View.VISIBLE
                        b.shimmerEffect.visibility = View.GONE
                        b.llShimmer.visibility = View.GONE
                        b.shimmerEffect.stopShimmer()
                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            "1" -> {
                                allChatDetails = it.data.data
                                guardianChatAdapter.addList(it.data.data)

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
                        b.rvGuardianChat.visibility = View.GONE
                        b.shimmerEffect.visibility = View.VISIBLE
                        b.llShimmer.visibility = View.VISIBLE
                        b.shimmerEffect.startShimmer()
//                    MyApplication.progressBar.show(activity)
                    }

                    is NetworkResult.NoNetwork -> {
//                    MyApplication.progressBar.dismiss()
                        netWorkNotAvailable()
                    }
                }
            }
        }
    }

    private fun getAllChatDetailAPI() {
        viewModel.allChatDetail()
    }

    private var chatLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e("TAG", ">>>>>>>>>>>>  : " + result.resultCode)
            getAllChatDetailAPI()
        }

    private fun setAdapterGuardians() {
        b.rvGuardianChat.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        guardianChatAdapter = GuardianChatAdapter(requireContext(), ArrayList()) {
            chatLauncher.launch(it)
        }
        b.rvGuardianChat.adapter = guardianChatAdapter
    }


    private fun getDataAndSetObserver() {
        SocketEvents.setRepository(dashBoardActivity.socketRepository, dashBoardActivity)
        dashBoardActivity.socketViewModel = ViewModelProvider(
            this, SocketModelFactory(dashBoardActivity.socketRepository)
        )[SocketViewModel::class.java]
        dashBoardActivity.socketViewModel?.messageReceiveData?.observe(viewLifecycleOwner) {
            when (it) {
                is SocketResult.Success -> {
                    Log.e("TAG", "messageReceiveData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                    viewModel.allChatDetail()
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
        dashBoardActivity.socketViewModel?.errorData?.observe(dashBoardActivity) {
            when (it) {
                is SocketResult.Success -> {
                    Log.e("TAG", "errorData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
    }


}