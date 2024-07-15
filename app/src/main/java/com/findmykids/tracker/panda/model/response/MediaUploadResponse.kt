package com.findmykids.tracker.panda.model.response

data class MediaUploadResponse(
    val `data`: Data,
    val message: String,
    val status: String
) {
    data class Data(
        val imageUrls: List<String>
    )
}