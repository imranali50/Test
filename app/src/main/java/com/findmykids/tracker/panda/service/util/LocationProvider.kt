package com.findmykids.tracker.panda.service.util

import android.content.Context
import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationProvider {

    fun getLocation(interval: Long, context: Context): Flow<Location>

    class LocationException(message: String) : Exception(message)
}