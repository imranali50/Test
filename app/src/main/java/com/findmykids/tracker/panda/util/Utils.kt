package com.findmykids.tracker.panda.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.SignInActivity
import com.findmykids.tracker.panda.service.EventManageReceiver
import com.findmykids.tracker.panda.service.MainAppService
import com.findmykids.tracker.panda.service.RestartServiceReceiver
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.regex.Pattern

class Utils(val context: Context) {
    private val eventReceiver = EventManageReceiver()
    private val restartReceiver = RestartServiceReceiver()

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }


    //hide keyboard
    fun hideKeyBoardFromView(context: Context) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        // Find the currently focused view, so we can grab the correct window
        // token from it.
        var view = (context as Activity).currentFocus
        // If no view currently has focus, create a new one, just so we can grab
        // a window token from it
        if (view == null) {
            view = View(context)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun registerReceiver() {
        val installReceiverFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
//            addAction(Intent.ACTION_APP_ERROR)
//            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
//            addAction(Intent.ACTION_PACKAGE_CHANGED)
//            addAction(Intent.ACTION_BUG_REPORT)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
//            addAction(Intent.ACTION_BOOT_COMPLETED)
            addDataScheme("package")
        }
        val restartReceiverFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BOOT_COMPLETED)
        }

        Log.e("Registering the installation receiver ..", "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(
                eventReceiver, installReceiverFilter, AppCompatActivity.RECEIVER_EXPORTED
            )
            context.registerReceiver(
                restartReceiver, restartReceiverFilter, AppCompatActivity.RECEIVER_EXPORTED
            )
        }

    }

    fun unRegisterReceiver() {
        context.unregisterReceiver(eventReceiver)
        context.unregisterReceiver(restartReceiver)
    }


    fun statusBarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color
        }
    }

    fun hideKeyBoardFromView() {
        val activity = context as AppCompatActivity
        val view = activity.currentFocus
        if (view != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun logOut(activity: Activity, msg: String) {
        showToast(msg)
//      unRegisterReceiver()
        Intent(context, MainAppService::class.java).apply {
            action = MainAppService.ACTION_STOP
            activity.startService(this)
        }
        SocketConnectionCall.disconnectSocket()
        val preferenceManager = PreferenceManager(activity)
        preferenceManager.clearPreferences()
        SocketConnectionCall.isConnected = false
        val intentCreateGroup = Intent(activity, SignInActivity::class.java)
        intentCreateGroup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intentCreateGroup)
        activity.finishAffinity()
    }

    fun logOutFromBackground(context: Context) {
        Intent(context, MainAppService::class.java).apply {
            action = MainAppService.ACTION_STOP
            context.startService(this)
        }
        SocketConnectionCall.disconnectSocket()
        val preferenceManager = PreferenceManager(context)
        preferenceManager.clearPreferences()
        SocketConnectionCall.isConnected = false
    }

    fun showAlert(msg: String?) {
        MaterialAlertDialogBuilder(context).setCancelable(false).setTitle("Alert").setMessage(msg)
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }.show();
    }

    fun isMyServiceRunning(serviceClass: Class<*>, mActivity: Activity): Boolean {
        val manager: ActivityManager =
            mActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }


//    fun showCustomToast(msg: String?) {
//        val view =
//            LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
//        val tvProgress = view.findViewById<TextView>(R.authId.custom_toast_text)
//        tvProgress.text = msg
//
//        val myToast = Toast(context)
//        myToast.duration = Toast.LENGTH_SHORT
//        myToast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
//        myToast.view = view//setting the view of custom toast layout
//        myToast.show()
//    }


    fun getStyledFont(html: String): String {
        val addBodyStart = !html.toLowerCase().contains("<body>")
        val addBodyEnd = !html.toLowerCase().contains("</body")
        return "<style type=\"text/css\">@font-face {font-family: CustomFont;" + "src: url(\"file:///android_asset/poppins_regular.ttf\")}" + "body {color: #011502; background-color: #0000000; font-size: 15px}</style>" + (if (addBodyStart) "<body>" else "") + html + if (addBodyEnd) "</body>" else ""
    }

    fun getStyledFontBlack(html: String): String {
        val addBodyStart = !html.toLowerCase().contains("<body>")
        val addBodyEnd = !html.toLowerCase().contains("</body")
        return "<style type=\"text/css\">@font-face {font-family: CustomFont;" + "src: url(\"file:///android_asset/poppins_regular.ttf\")}" + "body {font-family: CustomFont;font-size: medium;text-align: justify;}" + "body{color: #607698; background-color: #0000000; font-size: 13px}</style>" + (if (addBodyStart) "<body>" else "") + html + if (addBodyEnd) "</body>" else ""
    }


    fun getScreenSizeDialog(context: Activity, resourceId: Int): View? {
        val displayRectangle = Rect()
        val window = context.window
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(resourceId, null)
        layout.minimumWidth = (displayRectangle.width() * 1.0f).toInt()
        layout.minimumHeight = (displayRectangle.height() * 1.0f).toInt()
        return layout
    }

    fun isValidEmail(email: String?): Boolean {
        return email != null && Pattern.compile(emailPattern).matcher(email).matches()
    }

    companion object {
        private const val emailPattern =
            ("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        val pattern = Pattern.compile(passwordPattern)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }


    fun checkDigit(number: Int): String {
        // less than 10 add 0
        return if (number <= 9) "0$number" else number.toString()
    }

    fun addDecimal(number: String): String {
        // add decimal
        return if (!number.contains(".")) "$number.00" else number
    }

    fun showToast(msg: String) {
        val toast = Toast(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
        val textView: TextView = view.findViewById(R.id.custom_toast_text)
        textView.text = msg
        toast.view = view
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }


    fun readJsonAsset(name: String, context: Context): String {
        try {
            val inputStream = context.assets.open(name)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            return String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


}