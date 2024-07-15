package com.findmykids.tracker.panda.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.findmykids.tracker.panda.util.PreferenceManager
import com.findmykids.tracker.panda.util.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseFragment : Fragment() {
    lateinit var activity: Activity
    lateinit var utils: Utils
    lateinit var pref:PreferenceManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity()
        utils = Utils(activity)
        pref = PreferenceManager(activity)
    }


    fun netWorkNotAvailable() {

        utils.showToast("Network not available please connect with the internet")
    }

    fun checkStatus(status: Int, msg: String) {
        if (status == 0) {
            utils.showToast(msg)

        } else if (status == 2 || status == 5) {
            utils.logOut(activity, msg)

        }
    }

}