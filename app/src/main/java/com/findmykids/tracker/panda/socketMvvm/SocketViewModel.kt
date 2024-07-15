package com.findmykids.tracker.panda.socketMvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.findmykids.tracker.panda.util.SocketResult

class SocketViewModel constructor(private val socketRepository: SocketRepository) : ViewModel() {

    val connectJoinData: LiveData<SocketResult<Any>>
        get() = socketRepository.connectJoin

    val messageReceiveData: LiveData<SocketResult<Any>>
        get() = socketRepository.messageReceive
    val errorData: LiveData<SocketResult<Any>>
        get() = socketRepository.error
    val connectionLeaveData: LiveData<SocketResult<Any>>
        get() = socketRepository.connectionLeave

    val messageSendData: LiveData<SocketResult<Any>>
        get() = socketRepository.messageSend
    val locationChildUpdateData: LiveData<SocketResult<Any>>
        get() = socketRepository.locationChildUpdate
    val sendSpeedData: LiveData<SocketResult<Any>>
        get() = socketRepository.sendSpeedData
  val userStatusData: LiveData<SocketResult<Any>>
        get() = socketRepository.userStatusData
    val appUseChildUpdateData: LiveData<SocketResult<Any>>
        get() = socketRepository.appUseChildUpdate
    val audioNotifyData: LiveData<SocketResult<Any>>
        get() = socketRepository.audioNotify
    val audioStreamSendData: LiveData<SocketResult<Any>>
        get() = socketRepository.audioStreamSend
    val sosDeActive: LiveData<SocketResult<Any>>
        get() = socketRepository.sosDeActive

}