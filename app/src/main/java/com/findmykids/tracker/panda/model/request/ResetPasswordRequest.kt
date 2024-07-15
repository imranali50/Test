package com.findmykids.tracker.panda.model.request

data class ResetPasswordRequest(
    val email: String,
    val langType: String,
    val password: String
)