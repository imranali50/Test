package com.findmykids.tracker.panda.model.response

data class LoginResponse(
    val `data`: Data,
    val message: String,
    val status: String
) {
    data class Data(
        val _id: String,
        val age: String,
        val countryCode: String,
        val email: String,
        val gender: String,
        val mobileNumber: String,
        val isConnectionExists: Boolean,
        val connectionId: String,
        val name: String,
        val driveSpeedLimit: String,
        val profileImage: String,
        val token: String
    )
}



