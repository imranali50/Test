package com.findmykids.tracker.panda.model.request

data class VerifyRequest(
    val email: String,
    val langType: String,
    val otp: String
)