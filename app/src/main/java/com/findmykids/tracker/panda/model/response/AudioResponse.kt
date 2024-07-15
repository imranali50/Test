package com.findmykids.tracker.panda.model.response

data class AudioResponse(
    val parentId: String,
    val requestAudioId: String,
    val toStop: Boolean
)