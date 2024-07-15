package com.findmykids.tracker.panda.model.response

import com.google.gson.JsonElement

data class AllChatResponse(
    val `data`: List<Data>,
    val message: String,
    val status: String
) {
    data class Data(
        val _id: String,
        val lastMessage: JsonElement,
        val name: String,
        val profileImage: String,
        val unreadCount: String
    ) {
        data class LastMessage(
            val _id: String,
            val connectionId: String,
            val createdAt: String,
            val deletedAt: Any,
            val image: String,
            val readAt: String,
            val receiverId: String,
            val senderId: String,
            val text: String,
            val updatedAt: String
        ){

        }
    }
}