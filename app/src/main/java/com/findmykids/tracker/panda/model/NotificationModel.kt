package com.findmykids.tracker.panda.model

data class NotificationModel(
    val childId: String,
    val senderProfile: String,
    val senderName: String,
    val connectionId: String,
    val notificationType: String,
    val currentLocation: CurrentLocation
) {
    data class CurrentLocation(val lat: String, val long: String)
}
