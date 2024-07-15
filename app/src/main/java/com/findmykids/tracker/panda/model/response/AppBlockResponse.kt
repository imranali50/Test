package com.findmykids.tracker.panda.model.response

data class AppBlockResponse(
    val appPkgName: String,
    val isBlocked: Boolean,
    val limitTime: String
)