package com.findmykids.tracker.panda.model.response

data class NotificationResponse(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val notificationList: List<Notification>,
        val totalPage: Int
    ) {
        data class Notification(
            val _id: String,
            val appName: Any,
            val appPkgName: Any,
            val appUnblockRequestDescription: Any,
            val connectionId: String,
            val content: List<Content>,
            val createdAt: String,
            val senderName: String,
            val senderProfile: String,
            val timeSpan: String,
            val type: String
        ) {
            data class Content(
                val color: String,
                val text: String,
                val weight: String
            )
        }
    }
}