package com.findmykids.tracker.panda.util

sealed class SocketResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : SocketResult<T>(data)
    class Loading<T> : SocketResult<T>()
    class NoNetwork<T> : SocketResult<T>()
}