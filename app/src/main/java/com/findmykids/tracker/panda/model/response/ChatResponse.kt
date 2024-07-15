package com.findmykids.tracker.panda.model.response

data class ChatResponse(
    val `data`: Data, val message: String, val status: String) {
    data class Data(val messages: List<Messages>,val totalPages: Int,val isOnline: Boolean,val receiverId: String)
    data class Messages(
        val _id: String,
        val connectionId: String,
        val createdAt: String,
        val image: String,
        val readAt: String,
        val receiverId: String,
        val senderId: String,
        val text: String
    )

}