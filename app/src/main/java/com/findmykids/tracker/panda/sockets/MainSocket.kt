package com.findmykids.tracker.panda.sockets

import android.content.Context
import android.util.Log
import com.findmykids.tracker.panda.BuildConfig
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.PreferenceManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject
import java.net.URISyntaxException

object MainSocket {

    private var mSocket: Socket? = null

    lateinit var pref: PreferenceManager
    fun getInstance(context: Context): Socket? {
        try {
            pref = PreferenceManager(context)
            val options = IO.Options()
            options.forceNew = true
            options.reconnection = false
//            options.reconnectionAttempts = 0
            options.timeout = 10000
            options.transports = arrayOf(WebSocket.NAME)
            options.query =
                "token=" + pref.getString(Const.token).toString()
            mSocket = IO.socket(BuildConfig.okBrother, options)

//            mSocket?.on("message:receive") { args ->
//                if (args[0] != null) {
//                    val counter = args[0] as JSONObject
//                    Log.e("MainParentSocket",counter.toString())
//
//                }
//            }
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        return mSocket

    }

}