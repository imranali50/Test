package com.findmykids.tracker.panda.model.response

data class ConnectedGuardianResponse(
    val `data`: List<Data>,
    val message: String,
    val status: String
) {
    data class Data(
        val _id: String,
        val name: String,
        val email: String,
        val mobileNumber: String,
        val countryCode: String,
        val age: String,
        val gender: String,
        val connectionId: String,
        val profileImage: String
    )
}