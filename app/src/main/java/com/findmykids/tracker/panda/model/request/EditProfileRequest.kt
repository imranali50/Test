package com.findmykids.tracker.panda.model.request

data class EditProfileRequest(
    val langType: String,
    val name: String,
    val age: String,
    val gender: String,
    val countryCode: String,
    val mobileNumber: String,
    val profileImage: String,
)