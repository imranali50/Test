package com.findmykids.tracker.panda.util

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.os.Process
import android.util.Base64
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.findmykids.tracker.panda.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.regex.Pattern

//permission
val locationCourse = Manifest.permission.ACCESS_COARSE_LOCATION
val locationFine = Manifest.permission.ACCESS_FINE_LOCATION

val locationPermission = arrayOf(
    locationCourse, locationFine
)
val backGroundLocationPermission = arrayOf(
    Manifest.permission.ACCESS_BACKGROUND_LOCATION
)


fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()
val CACHE_FOLDER = "PrivateThreadChat"


//val CAMERA_PERMISSION = Manifest.permission.CAMERA
val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
val CONTACT_PERMISSION = Manifest.permission.READ_CONTACTS

val WRITE_PERMISSION = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
val AUDIO_PERMISSION = arrayOf(Manifest.permission.RECORD_AUDIO)
val GALLERY_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
} else {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}

val notificationPermission = arrayOf(Manifest.permission.POST_NOTIFICATIONS)

val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
    )
} else {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}
val audioPermission = arrayOf(Manifest.permission.RECORD_AUDIO)
val CAMERA_RECORD_AUDIO_PERMISSION = arrayOf(
    CAMERA_PERMISSION, Manifest.permission.RECORD_AUDIO
)

val Audio_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO
    )
} else {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
}

val packageUsesPermission = arrayOf(
    Manifest.permission.PACKAGE_USAGE_STATS
)

fun isGPSEnabled(mContext: Context): Boolean {
    val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}


fun hasPermissions(context: Activity, permissions: Array<String>): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun checkSelfPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun checkSelfDefaultPermissions(context: Activity, permissions: Array<String>): Boolean =
    permissions.all {
        val appOps = context
            .getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager

        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), context.packageName
        )
        if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkCallingOrSelfPermission(it) == PackageManager.PERMISSION_DENIED
        }

    }

fun shouldShow(context: Activity, permissions: Array<String>): Boolean = permissions.all {
    ActivityCompat.shouldShowRequestPermissionRationale(context, it)
}

fun milliSecondsToTimer(milliseconds: Long): String {
    var finalTimerString = ""
    var secondsString = ""

    //Convert total duration into time
//    val hours = (milliseconds / (1000 * 60 * 60)).toInt()
    val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
    val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

    // Pre appending 0 to seconds if it is one digit
    val minutesString = if (minutes < 10) {
        "0$minutes"
    } else {
        "" + minutes
    }
    secondsString = if (seconds < 10) {
        "0$seconds"
    } else {
        "" + seconds
    }
    finalTimerString = "$minutesString:$secondsString"

    // return timer string
    return finalTimerString
}


fun Activity.hideSystemUI() {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.statusBarColor = resources.getColor(R.color.white)
}

fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


fun Activity.fullScreen() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
}

//fun Activity.statusBarForAll() {
//    val window: Window = window
//    val background = ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)
//    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//
//    window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
//    window.setBackgroundDrawable(background)
//}

fun Activity.statusBarForAll() {
    val window: Window = window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this@statusBarForAll, R.color.status_bar_color)
    window.navigationBarColor =
        ContextCompat.getColor(this@statusBarForAll, R.color.status_bar_color)
}
fun Activity.statusBarWhiteScreen() {
    val window: Window = window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this@statusBarWhiteScreen, R.color.white)
    window.navigationBarColor =
        ContextCompat.getColor(this@statusBarWhiteScreen, R.color.white)
}

fun getFilename(context: Context): String {
    val sd = context.cacheDir
    val folder = File(sd, File.separator + CACHE_FOLDER + File.separator)
    if (!folder.exists()) {
        folder.delete()
        if (!folder.mkdir()) {
//            Log.e("ERROR", "Cannot create a directory!")
        } else {
            folder.mkdirs()
        }
    }

    val fileName = File(folder, "Temp_save" + ".mp3")

    return fileName.absolutePath
}

val compile: Pattern = Pattern.compile(
    "\\+(?:998|996|995|994|993|992|977|976|975|974|973|972|971|970|968|967|966|965|964|963|962|961|960|886|880|856|855|853|852|850|692|691|690|689|688|687|686|685|683|682|681|680|679|678|677|676|675|674|673|672|670|599|598|597|595|593|592|591|590|509|508|507|506|505|504|503|502|501|500|423|421|420|389|387|386|385|383|382|381|380|379|378|377|376|375|374|373|372|371|370|359|358|357|356|355|354|353|352|351|350|299|298|297|291|290|269|268|267|266|265|264|263|262|261|260|258|257|256|255|254|253|252|251|250|249|248|246|245|244|243|242|241|240|239|238|237|236|235|234|233|232|231|230|229|228|227|226|225|224|223|222|221|220|218|216|213|212|211|98|95|94|93|92|91|90|86|84|82|81|66|65|64|63|62|61|60|58|57|56|55|54|53|52|51|49|48|47|46|45|44\\D?1624|44\\D?1534|44\\D?1481|44|43|41|40|39|36|34|33|32|31|30|27|20|7|1\\D?939|1\\D?876|1\\D?869|1\\D?868|1\\D?849|1\\D?829|1\\D?809|1\\D?787|1\\D?784|1\\D?767|1\\D?758|1\\D?721|1\\D?684|1\\D?671|1\\D?670|1\\D?664|1\\D?649|1\\D?473|1\\D?441|1\\D?345|1\\D?340|1\\D?284|1\\D?268|1\\D?264|1\\D?246|1\\D?242|1)\\D?"
)


fun phoneNumberWithoutCountryCode(phoneNumberWithCountryCode: String): String {
    //+91 7698989898
//        Log.e("TAG", "phoneNumberWithoutCountryCode: "+ )
    return phoneNumberWithCountryCode.replace(compile.pattern().toRegex(), "")
}

//fun imageToBase64(appDrawable: Drawable): String {
//    var bitmap = appDrawable.toBitmap()
//    val baos = ByteArrayOutputStream()
//    bitmap =Bitmap.createScaledBitmap(bitmap, 100, 100, false);
//    bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)
//    var imageBytes = baos.toByteArray()
//    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
//}
fun imageToBase64(appDrawable: Drawable): ByteArray {
    var bitmap = appDrawable.toBitmap()
    val baos = ByteArrayOutputStream()
//    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
    bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)
    var imageBytes = baos.toByteArray()
    return imageBytes
//    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}

fun base64toImage(imageString: String): Bitmap {
    //decode base64 string to image
    val imageAsBytes: ByteArray = Base64.decode(
        imageString.substring(imageString.indexOf(",") + 1),
        Base64.DEFAULT
    )
    return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
}

fun UTCToLocal(
    datesToConvert: String?
): String? {
    var dateToReturn = datesToConvert
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    var gmt: Date? = null
    val sdfOutPutToSend = SimpleDateFormat("h:mm aa")
    sdfOutPutToSend.timeZone = TimeZone.getDefault()
    try {
        gmt = sdf.parse(datesToConvert)
        dateToReturn = sdfOutPutToSend.format(gmt)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return dateToReturn
}
fun Activity.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name.equals(service.service.className)) {
            return true
        }
    }
    return false
}

