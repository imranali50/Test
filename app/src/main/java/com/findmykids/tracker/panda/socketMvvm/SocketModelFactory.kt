package com.findmykids.tracker.panda.socketMvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SocketModelFactory (private val socketRepository: SocketRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SocketViewModel(socketRepository) as T
    }
}