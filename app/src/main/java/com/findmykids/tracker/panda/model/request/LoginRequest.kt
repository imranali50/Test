package com.findmykids.tracker.panda.model.request

data class LoginRequest(
    val langType: String,
    val authId: String,
    val authProvider: String,
    val deviceId: String,
    val deviceToken: String,
    val email: String,
    val name: String,
    val role: String,
    val password: String
)