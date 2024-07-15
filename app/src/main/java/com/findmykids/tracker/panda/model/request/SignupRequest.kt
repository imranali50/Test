package com.findmykids.tracker.panda.model.request

data class SignupRequest(
    val langType: String,
    val name: String,
    val age: String,
    val countryCode: String,
    val deviceId: String,
    val deviceToken: String,
    val timeZone: String,
    val email: String,
    val gender: String,
    val mobileNumber: String,
    val password: String,
    val role: String
)