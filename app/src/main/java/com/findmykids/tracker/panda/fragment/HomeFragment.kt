package com.findmykids.tracker.panda.fragment

import android.Manifest
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.findmykids.tracker.panda.activity.DashBoardActivity
import com.findmykids.tracker.panda.activity.GuardianCodeActivity
import com.findmykids.tracker.panda.activity.GuardianProfileActivity
import com.findmykids.tracker.panda.activity.LocationActivity
import com.findmykids.tracker.panda.adapter.GuardianAdapter
import com.findmykids.tracker.panda.adapter.MostUsageAppsAdapter
import com.findmykids.tracker.panda.databinding.FragmentHomeBinding
import com.findmykids.tracker.panda.roomDatabase.AppInfoFilter
import com.findmykids.tracker.panda.model.response.ConnectedGuardianResponse
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.permission.GpsPerActivity
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.GPSTracker
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.getMarkerIconFromDrawable
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.isGPSEnabled
import com.findmykids.tracker.panda.util.locationPermission
import com.bumptech.glide.Glide
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.AppUsageActivity
import com.findmykids.tracker.panda.roomDatabase.AppInfoFilterModel
import com.findmykids.tracker.panda.roomDatabase.ApplicationUseDatabase
import com.findmykids.tracker.panda.roomDatabase.ApplicationUsesModelFactory
import com.findmykids.tracker.panda.roomDatabase.ApplicationUsesRepository
import com.findmykids.tracker.panda.roomDatabase.ApplicationUsesViewModel
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.GridSpacingItemDecoration
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class HomeFragment(private val mActivity: DashBoardActivity) : BaseFragment(), OnMapReadyCallback {

    private lateinit var b: FragmentHomeBinding

    private lateinit var mostUsageAppsAdapter: MostUsageAppsAdapter
    private val viewModel by viewModels<ViewModel>()
    private var appList = ArrayList<AppInfoFilter>()
    private var connectedGuardianList: List<ConnectedGuardianResponse.Data> = ArrayList()
    private lateinit var guardianAdapter: GuardianAdapter
//    private val userViewModel: UserViewModel by viewModels()

    //map
    var lat: String? = null
    var lon: String? = null
    var address: String? = null
    var apartment: String? = null
    var city: String? = null
    var state: String? = null
    var zipCode: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var tracker: GPSTracker? = null
    var locationManager: LocationManager? = null
    private val REQUEST_LOCATION = 1
    var mMap: GoogleMap? = null
    var mapFragment: SupportMapFragment? = null

    val executor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private var packageManager: PackageManager? = null

    //appUses
    lateinit var applicationUsesRepository: ApplicationUsesRepository
    lateinit var applicationUsesViewModel: ApplicationUsesViewModel
    lateinit var downloadVideoDatabase: ApplicationUseDatabase

    var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        b = FragmentHomeBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        int()
        setData()
        setAdapterUseApps()
        setAdapterGuardians()
        getGuardianInfoAPI()
        setObserver()
        mapFragment = childFragmentManager.findFragmentById(R.id.mapHome) as SupportMapFragment?
        setClickListeners()
        maps()
        getData()

    }

    private fun int() {
        downloadVideoDatabase = ApplicationUseDatabase(activity)

//        applicationUsesRepository = ApplicationUsesRepository(downloadVideoDatabase)

//        applicationUsesViewModel = ViewModelProvider(this, ApplicationUsesModelFactory(applicationUsesRepository))[ApplicationUsesViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        if (mActivity.loginResponse != null) {
            Glide.with(mActivity).load(mActivity.loginResponse?.data?.profileImage).centerCrop()
                .placeholder(R.drawable.ic_placeholder).into(b.sivProfileImage)
            b.tvChildName.text = mActivity.loginResponse?.data?.name
//            if(mActivity.loginResponse?.data?.speedLimit?.toInt() !=0  && mActivity.loginResponse?.data?.speedLimit.toString().isNotEmpty()){
//                b.pgSpeed.progress = mActivity.loginResponse?.data?.speedLimit?.toInt()!!
//                b.tvSpeedText1.text = mActivity.loginResponse?.data?.speedLimit.toString()
//            }
//            if (mActivity.loginResponse?.data?.isConnectionExists.equals("1")) {
//
//            } else {
//                AppUsageClass(mActivity).getData()
//            }

            Log.e("TAG", "onResume: " + mActivity.loginResponse?.data)
            if (mActivity.loginResponse?.data?.isConnectionExists == false) {
                b.tvGuardian.visibility = View.GONE
                b.llGuardian.visibility = View.GONE
                b.tvDriveSafe.visibility = View.GONE
                b.clSpeedMeter.visibility = View.GONE
            } else {
                b.tvGuardian.visibility = View.VISIBLE
                b.llGuardian.visibility = View.VISIBLE
                viewModel.getProfile()
            }

            Log.e("TAG", "onResume: >>>>>>>>>>>>>" + mActivity.loginResponse?.data?._id)
        }
    }

    private fun setObserver() {
        viewModel.connectedGuardianResponse.observe(viewLifecycleOwner) {
            mActivity.runOnUiThread {
                when (it) {
                    is NetworkResult.Success -> {
                        when (it.data?.status) {
                            "1" -> {
                                connectedGuardianList = it.data.data
                                guardianAdapter.addList(it.data.data)
                            }

                            else -> {
                                checkStatus(it.data!!.status.toInt(), it.data.message)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        utils.showToast(it.message.toString())
                    }

                    is NetworkResult.Loading -> {
//                        MyApplication.progressBar.show(activity)
                    }

                    is NetworkResult.NoNetwork -> {
                        netWorkNotAvailable()
                    }
                }
            }
        }
        viewModel.getProfileResponse.observe(viewLifecycleOwner) {
            mActivity.runOnUiThread {
                when (it) {
                    is NetworkResult.Success -> {
//                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            "1" -> {
                                if (it.data.data.driveSpeedLimit.isNotEmpty()) {
                                    b.tvDriveSafe.visibility = View.VISIBLE
                                    b.clSpeedMeter.visibility = View.VISIBLE
                                    b.tvSpeedText1.text = it.data.data.driveSpeedLimit
                                    b.pgSpeed.progress = it.data.data.driveSpeedLimit.toInt()
                                }
                            }

                            else -> {
                                checkStatus(it.data!!.status.toInt(), it.data.message)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
//                        MyApplication.progressBar.dismiss()
                        utils.showToast(it.message.toString())
                    }

                    is NetworkResult.Loading -> {
//                        MyApplication.progressBar.show(activity)
                    }

                    is NetworkResult.NoNetwork -> {
//                      MyApplication.progressBar.dismiss()
                        netWorkNotAvailable()
                    }
                }
            }
        }
        viewModel.appUsageResponse.observe(mActivity) {
            mActivity.runOnUiThread {
                mostUsageAppsAdapter.setAppInfoNewList(it)
            }
        }
    }

    private fun getGuardianInfoAPI() {
        viewModel.connectedGuardian()
    }

    private fun setData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            b.clChildLocationView.outlineAmbientShadowColor = resources.getColor(R.color.gradiant1)
            b.clChildLocationView.outlineSpotShadowColor = resources.getColor(R.color.gradiant1)
            b.addGuardian.outlineAmbientShadowColor = resources.getColor(R.color.gradiant1)
            b.addGuardian.outlineSpotShadowColor = resources.getColor(R.color.gradiant1)
        }
        val params: CoordinatorLayout.LayoutParams =
            b.appBar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
        params.behavior = behavior
    }

    private fun maps() {
//        latitude = pref.getString(Const.latitude)
//        longitude = pref.getString(Const.longitude)

        tracker = GPSTracker(activity)
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!isGPSEnabled(activity)) {
            startActivity(Intent(activity, GpsPerActivity::class.java))
        } else {
            getLocation()
            mapFragment?.getMapAsync(this)
        }

    }

    private fun getLocation() {
        if (!hasPermissions(activity, locationPermission)) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
            )
        } else {
            if (tracker!!.canGetLocation()) {
                latitude = java.lang.String.valueOf(tracker!!.latitude)
                longitude = java.lang.String.valueOf(tracker!!.longitude)
                lat = latitude
                lon = longitude
                if (lat.equals("0.0", ignoreCase = true)) {
                    callApiForLatLong()
                } else {
//                    Intent(context, LocationService::class.java).apply {
//                        action = LocationService.ACTION_START
//                        activity.startService(this)
//                    }
                }
                Log.e("lat", "initMapView: $lat")
                Log.e("long", "initMapView: $lon")
            } else {
                Toast.makeText(activity, "Unable to find location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun callApiForLatLong() {
        val jsonObject = JSONObject()
        val client = OkHttpClient()
        val JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(JSON, jsonObject.toString())
        val request: Request =
            Request.Builder().url(Const.LocationNotAllowThenGetLatLng).post(body).build()
        val thread = Thread {
            try {
                val resStr = client.newCall(request).execute().body!!.string()
                val jsonObject1 = JSONObject(resStr)
                val jsonObject2 = jsonObject1.getJSONObject("location")
                activity.runOnUiThread {
                    try {
                        latitude = jsonObject2.getString("lat")
                        longitude = jsonObject2.getString("lng")
                        lat = latitude
                        lon = longitude
                        Log.e("MapAPI lat Long", "lan $lon" + "lat $lat")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MapAPI Error", "" + e.toString())
            }
        }
        thread.start()
    }


    private fun setClickListeners() {
        b.tvSeeAll.setOnClickListener {
            startActivity(Intent(mActivity, AppUsageActivity::class.java))
        }
        b.tvViewLocation.setOnClickListener {
            startActivity(Intent(mActivity, LocationActivity::class.java))
        }
        b.addGuardian.setOnClickListener {
            startActivity(
                Intent(mActivity, GuardianCodeActivity::class.java).putExtra(
                    GuardianCodeActivity.IsFromHomePage, true
                )
            )
        }
    }

    private fun setAdapterGuardians() {
        b.rvGuardian.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        guardianAdapter = GuardianAdapter(mActivity) {
            startActivity(
                Intent(mActivity, GuardianProfileActivity::class.java).putExtra(
                    GuardianProfileActivity.IsGuardianProfile, true
                ).putExtra(GuardianProfileActivity.GuardianProfileResponse, Gson().toJson(it))
            )
        }
//        b.rvGuardian.addItemDecoration(AdaptiveSpacingItemDecoration(10,false))
        b.rvGuardian.adapter = guardianAdapter

    }

    private fun setAdapterUseApps() {
        b.rvMostUsageApp.layoutManager = GridLayoutManager(context, 4)
        mostUsageAppsAdapter = MostUsageAppsAdapter(requireContext(), appList)
        b.rvMostUsageApp.adapter = mostUsageAppsAdapter
        b.rvMostUsageApp.addItemDecoration(
            GridSpacingItemDecoration(
                4, 20, true
            )
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (latitude.toString().isNotEmpty() && longitude.toString().isNotEmpty()) {
            Log.e("TAG", "onMapReady: >>>>>>>>>>> ")
            mMap = googleMap
            mMap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
            mMap!!.uiSettings.isZoomControlsEnabled = false
            mMap!!.uiSettings.isRotateGesturesEnabled = true
            mMap!!.uiSettings.isScrollGesturesEnabled = true
            mMap!!.uiSettings.isTiltGesturesEnabled = true
            mMap!!.uiSettings.isMapToolbarEnabled = false
            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitude!!.toDouble(), longitude!!.toDouble()
                    ), 15f
                )
            )
            mMap!!.clear()
            val circleDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_map_pin)
            val markerIcon: BitmapDescriptor = getMarkerIconFromDrawable(circleDrawable!!)
            val marker = mMap!!.addMarker(
                MarkerOptions().position(
                    LatLng(
                        latitude!!.toDouble(), longitude!!.toDouble()
                    )
                )/*.title(loginResponse.data.name)*/.icon(markerIcon).anchor(0.5f, 0.5f)
            )
            marker?.showInfoWindow()

            getCompleteAddressString(latitude!!.toDouble(), longitude!!.toDouble())
//        b.tvLocationChild.text = temp
            //Initialize Google Play Services
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
//            buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = false
            }
            mMap!!.setOnMapClickListener { arg0: LatLng ->
                Log.e("onMapClick", "Horray!" + arg0.latitude)
            }
        }
    }

    private fun getCompleteAddressString(
        LATITUDE: Double, LONGITUDE: Double
    ) {
        val geocoder = Geocoder(activity, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                address = addresses[0].getAddressLine(1)
                val returnedAddress = addresses[0]
                apartment = returnedAddress.subLocality
                city = returnedAddress.locality
                state = returnedAddress.adminArea
                zipCode = returnedAddress.postalCode
                val strReturnedAddress = StringBuilder()
                for (i in 0..returnedAddress.maxAddressLineIndex) {

                    strReturnedAddress.append(returnedAddress.getAddressLine(i))
                    if (i != returnedAddress.maxAddressLineIndex) strReturnedAddress.append(",") else strReturnedAddress.append(
                        "."
                    )
                }
                address = strReturnedAddress.toString()
                b.tvLocationChild.text = address
                val lat = returnedAddress.latitude.toString()
                val lon = returnedAddress.longitude.toString()
                latitude = lat
                longitude = lon
                Log.e(
                    "Appart",
                    "$address\n Apart $apartment\n City $city\n State $state\n zipCode $zipCode"
                )
            } else {
                Log.e("MyCurrentLocation", "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MyCurrentLocation", "Can't get Address!")
        }

    }

    private fun getData() {
        b.shimmerEffect.startShimmer()
        b.rvMostUsageApp.visibility = View.GONE
        b.shimmerEffect.visibility = View.VISIBLE
        executor.execute {
            packageManager = mActivity.packageManager
            val intent1 = Intent(Intent.ACTION_MAIN, null)
            intent1.addCategory(Intent.CATEGORY_LAUNCHER)
            checkForLaunchIntent(
                packageManager!!.queryIntentActivities(intent1, 0)
            )
            appList.sortWith(Comparator { lhs, rhs ->
                lhs.today.compareTo(rhs.today)
            })
            appList.reverse()
            handler.post {
                mostUsageAppsAdapter.setAppInfoNewList(appList)
//                MyApplication.progressBar.dismiss()
                b.shimmerEffect.stopShimmer()
                b.rvMostUsageApp.visibility = View.VISIBLE
                b.shimmerEffect.visibility = View.GONE
            }
        }
    }

    private fun checkForLaunchIntent(list: List<ResolveInfo>) {
        appList.clear()
        for (info in list) {
            try {
                val screenTime = getScreenTimeForApp(mActivity, info.activityInfo.packageName)
//                if (appList.size.toInt() <= 5) {
////                    job = CoroutineScope(Dispatchers.IO).launch {
//////                        while (true) {
////                            Log.e("TAG", "56565656565 " + appList.size)
////
////                            applicationUsesViewModel.insertVideoData(
////                                AppInfoFilterModel( info.loadLabel(packageManager).toString(),
////                                    AppInfoFilter(info.loadLabel(packageManager).toString(),info.activityInfo.packageName,info.loadIcon(packageManager),screenTime)
////                                )
////                            )
//////                        }
////                    }
//
//
////                    userViewModel.insertApplication(
////                        AppInfoFilter(
////                            info.loadLabel(packageManager).toString(),
////                            info.activityInfo.packageName,
////                            info.loadIcon(packageManager!!),
////                            screenTime
////                        )
////                    )
//
//                } else {
//                    break
//                }
                appList.add(
                    AppInfoFilter(
                        info.loadLabel(packageManager).toString(),
                        info.activityInfo.packageName,
                        info.loadIcon(packageManager!!),
                        screenTime
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getScreenTimeForApp(context: Context, packageName: String): Long {

        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var startMillis: Long = 0
        val endMillis = System.currentTimeMillis()
        startMillis = calendar.timeInMillis

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val lUsageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)

        if (lUsageStatsMap.containsKey(packageName)) {
            return lUsageStatsMap[packageName]!!.totalTimeInForeground
        }

        return 0

//        val calendar = Calendar.getInstance()
//        calendar[Calendar.HOUR_OF_DAY] = 0
//        calendar[Calendar.MINUTE] = 0
//        calendar[Calendar.SECOND] = 0
//        calendar[Calendar.MILLISECOND] = 0
//        val startMillis = calendar.timeInMillis
//        val endMillis = System.currentTimeMillis()
//
//        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
//
//        val lUsageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)
//
//        var foregroundTime: Long = 0
//
//        for (stats in lUsageStatsMap.values) {
//            if (stats.packageName == packageName) {
//                foregroundTime += stats.totalTimeInForeground
//            }
//        }
//
//        return foregroundTime



    }
}