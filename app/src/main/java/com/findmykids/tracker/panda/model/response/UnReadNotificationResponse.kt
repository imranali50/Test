package com.findmykids.tracker.panda.model.response

data class UnReadNotificationResponse(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val unreadCount: Int
    )
}