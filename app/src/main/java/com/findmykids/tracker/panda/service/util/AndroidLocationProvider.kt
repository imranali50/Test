package com.findmykids.tracker.panda.service.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.findmykids.tracker.panda.util.checkSelfPermissions
import com.findmykids.tracker.panda.util.locationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class AndroidLocationProvider(
    private val context: Context,
    private val manager: LocationManager,
    private val client: FusedLocationProviderClient,
) : LocationProvider {

    override fun getLocation(interval: Long, context: Context): Flow<Location> {
        return callbackFlow {
            if (!checkSelfPermissions(
                    context,
                    locationPermission
                )
            ) throw LocationProvider.LocationException("Missing location permissions")
            if (locationDisabled()) throw LocationProvider.LocationException("Location is disabled")

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { launch { send(it) } }
                }
            }
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                client.requestLocationUpdates(
                    LocationRequest.Builder(interval).build(),
                    callback,
                    Looper.getMainLooper()
                )

                awaitClose {
                    client.removeLocationUpdates(callback)
                }

            } else {
                return@callbackFlow
            }

        }
    }

    private fun locationDisabled(): Boolean {
        return !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}