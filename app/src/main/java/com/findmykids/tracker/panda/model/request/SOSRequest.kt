package com.findmykids.tracker.panda.model.request

data class SOSRequest(
    val currentLocation: CurrentLocation,
    val type: String
) {
    data class CurrentLocation(
        val lat: String,
        val long: String
    )
}