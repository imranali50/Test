package com.findmykids.tracker.panda.permission

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.BaseActivity
import com.findmykids.tracker.panda.databinding.ActivityPermissionBinding
import com.findmykids.tracker.panda.util.isGPSEnabled
import com.findmykids.tracker.panda.util.statusBarForAll


class GpsPerActivity : BaseActivity() {

    val b: ActivityPermissionBinding by lazy { ActivityPermissionBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        int()
        clickEvent()
    }

    private fun int() {
        statusBarForAll()
        b.mainImg.setImageResource(R.drawable.ic_gps)
        b.title.text =
            getString(R.string.let_s_set_up_geolocation)
        b.subTitle.text =
            getString(R.string.if_you_re_using_it_we_need_your_location_to_provide_accurate_directions_and_real_time_updates)
        b.subTitle2.text = getString(R.string.your_parents_won_t_stress_and_you_can_find_your_phone_if_misplaced)
        b.tvNext.text = getString(R.string.enable_location)

    }

    private fun clickEvent() {
        b.tvNext.setOnClickListener {
            if (isGPSEnabled(activity)) {
                checkProfileStatus()
            }else{
                val intent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isGPSEnabled(activity)) {
            checkProfileStatus()
        }
    }

}