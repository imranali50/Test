package com.findmykids.tracker.panda.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import com.findmykids.tracker.panda.R

class ProgressLoading {
    var progressDialog: Dialog? = null

    fun show(context: Context) {
        try {
            if (progressDialog == null) {
                progressDialog = Dialog(context)
                progressDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                progressDialog!!.setContentView(R.layout.custom_progress_bar)
                progressDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                progressDialog!!.window?.attributes?.gravity = Gravity.CENTER
                progressDialog!!.setCancelable(false)
            }
            if (!progressDialog!!.isShowing) {
                progressDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss() {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog?.dismiss()
                progressDialog = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}