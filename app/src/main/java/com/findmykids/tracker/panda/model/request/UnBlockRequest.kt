package com.findmykids.tracker.panda.model.request

data class UnBlockRequest(
    val appName: String,
    val appPkgName: String,
    val description: String
)