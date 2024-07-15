package com.findmykids.tracker.panda.socketMvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.findmykids.tracker.panda.util.SocketResult

class SocketRepository() {

    val connectJoin: LiveData<SocketResult<Any>>
        get() = connectJoinLiveData
    val messageReceive: LiveData<SocketResult<Any>>
        get() = messageReceiveLiveData

    val error: LiveData<SocketResult<Any>>
        get() = errorLiveData

    val connectionLeave: LiveData<SocketResult<Any>>
        get() = connectionLeaveLiveData
    val messageSend: LiveData<SocketResult<Any>>
        get() = messageSendLiveData
    val locationChildUpdate: LiveData<SocketResult<Any>>
        get() = locationChildUpdateLiveData
    val sendSpeedData: LiveData<SocketResult<Any>>
        get() = sendSpeedLiveData
val userStatusData: LiveData<SocketResult<Any>>
        get() = userStatusUpdateLiveData
    val appUseChildUpdate: LiveData<SocketResult<Any>>
        get() = appUseChildUpdateLiveData
    val audioNotify: LiveData<SocketResult<Any>>
        get() = audioNotifyLiveData
    val audioStreamSend: LiveData<SocketResult<Any>>
        get() = audioStreamSendLiveData
    val sosDeActive: LiveData<SocketResult<Any>>
        get() = sosDeActiveListenLiveData


    val connectJoinLiveData = MutableLiveData<SocketResult<Any>>()
    val messageReceiveLiveData = MutableLiveData<SocketResult<Any>>()
    val errorLiveData = MutableLiveData<SocketResult<Any>>()
    val connectionLeaveLiveData = MutableLiveData<SocketResult<Any>>()
    val messageSendLiveData = MutableLiveData<SocketResult<Any>>()
    val sosDeActiveListenLiveData = MutableLiveData<SocketResult<Any>>()
    val locationChildUpdateLiveData = MutableLiveData<SocketResult<Any>>()
    val sendSpeedLiveData = MutableLiveData<SocketResult<Any>>()
    val appUseChildUpdateLiveData = MutableLiveData<SocketResult<Any>>()
    val userStatusUpdateLiveData = MutableLiveData<SocketResult<Any>>()
    val audioNotifyLiveData = MutableLiveData<SocketResult<Any>>()
    val audioStreamSendLiveData = MutableLiveData<SocketResult<Any>>()


}