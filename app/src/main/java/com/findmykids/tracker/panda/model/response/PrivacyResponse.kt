package com.findmykids.tracker.panda.model.response

data class PrivacyResponse(
    val `data`: Data,
    val message: String,
    val status: String
) {
    data class Data(
        val _id: String,
        val policy: String
    )
}