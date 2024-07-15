package com.findmykids.tracker.panda.service

import com.findmykids.tracker.panda.model.response.AudioResponse
import io.socket.emitter.Emitter
import org.json.JSONObject

interface EmitterListenerCallback {
    fun onEmitterListenerReceived(listener: AudioResponse)
}
