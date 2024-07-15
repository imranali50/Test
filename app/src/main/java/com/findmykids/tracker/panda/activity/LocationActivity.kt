package com.findmykids.tracker.panda.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityLocationBinding
import com.findmykids.tracker.panda.permission.GpsPerActivity
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.GPSTracker
import com.findmykids.tracker.panda.util.getMarkerIconFromDrawable
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.isGPSEnabled
import com.findmykids.tracker.panda.util.locationPermission
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.statusBarForAll
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class LocationActivity : com.findmykids.tracker.panda.activity.BaseActivity(), OnMapReadyCallback {
    private val b: ActivityLocationBinding by lazy {
        ActivityLocationBinding.inflate(
            layoutInflater
        )
    }

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
    var tracker: com.findmykids.tracker.panda.util.GPSTracker? = null
    var locationManager: LocationManager? = null
    private val REQUEST_LOCATION = 1
    var mMap: GoogleMap? = null
    var mapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapLocation) as SupportMapFragment?
        b.headerLocationTool.flToolBack.visibility = View.VISIBLE
        b.headerLocationTool.flToolBack.setOnClickListener {
            finish()
        }
        maps()
    }

    private fun maps() {
//        latitude = pref.getString(Const.latitude)
//        longitude = pref.getString(Const.longitude)

        tracker = GPSTracker(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!isGPSEnabled(this)) {
            startActivity(Intent(this, GpsPerActivity::class.java))
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

    override fun onMapReady(googleMap: GoogleMap) {
        if (latitude!!.isNotEmpty() && longitude!!.isNotEmpty()) {
            Log.e("TAG", "onMapReady: >>>>>>>>>>> ")
            mMap = googleMap
//        mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            mMap!!.uiSettings.isZoomControlsEnabled = true
            mMap!!.uiSettings.isRotateGesturesEnabled = true
            mMap!!.uiSettings.isScrollGesturesEnabled = true
            mMap!!.uiSettings.isTiltGesturesEnabled = true
//            mMap!!.uiSettings.
            mMap!!.uiSettings.isZoomControlsEnabled = true
            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitude!!.toDouble(), longitude!!.toDouble()
                    ), 15f
                )
            )
            mMap!!.clear()
            val circleDrawable =
                ContextCompat.getDrawable(activity, R.drawable.ic_map_pin)
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
                    this, Manifest.permission.ACCESS_FINE_LOCATION
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

    fun getCompleteAddressString(
        LATITUDE: Double,
        LONGITUDE: Double
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
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(",")
                }
                address = strReturnedAddress.toString()
                b.tvMyLocation.text = address
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
    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }
}