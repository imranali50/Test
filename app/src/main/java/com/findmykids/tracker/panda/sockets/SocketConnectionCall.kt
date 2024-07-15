package com.findmykids.tracker.panda.sockets

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.util.Const
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object SocketConnectionCall {

    private lateinit var mactivity: Activity
    var isConnected = false
    var function: (() -> Unit?)? = null
    var isFromBase: Boolean = false
    var mSocket: Socket? = null

    fun setSocket(socket: Socket?) {
        mSocket = socket
    }

    fun makeConnection(mActivity: Activity, callBack: () -> Unit) {
        if (!isConnected && mSocket?.connected() != true) {
            Log.e("SocketHandler", "makeConnection: ")
            function = callBack
            mactivity = mActivity
            mSocket?.on(Socket.EVENT_CONNECT, onConnect)
            mSocket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket?.connect()
            mactivity.runOnUiThread(Runnable {
                if (isFromBase) {
                    function?.invoke()
                    isFromBase = false
                    return@Runnable
                }
            })
            mSocket?.on(Const.audioStreamSend) { args ->
                val data = args[0]
                Log.e("SocketHandler", "Event: SocketHandler $data")
            }
        }
    }

    //without context connect
    fun socketConnect() {
        Log.e("SocketHandler", "socketConnect: $isConnected")
        if (!isConnected && mSocket?.connected() != true) {
            mSocket?.on(Socket.EVENT_CONNECT, onConnect)
            mSocket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket?.connect()
            mSocket?.on(Const.ringBellReceiver) { args ->
                val data = args[0]
                Log.e("SocketHandler", "Event: socketConnect $data")
            }
        }
    }

    fun disconnectSocket() {
        Log.e("TAG", "disconnectSocket: ")
        if (isConnected && mSocket?.connected() == false) {
            mSocket?.off(Socket.EVENT_CONNECT, onConnect)
            mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket?.disconnect()
        }
    }

    private val onConnect = Emitter.Listener {
        Log.e("SocketHandler", ": onConnect >>>>>>>>>>>> " + mSocket?.connected())
        if (mSocket?.connected() == true) {
            isConnected = true
            SocketEvents.registerAllEvents()
            if (isFromBase && this::mactivity.isInitialized) {
                mactivity.runOnUiThread {
                    function?.invoke()
                }
            }
        }

    }

    private val onConnectError = Emitter.Listener {
        Log.i("SocketHandler", "onConnectError >>>>>>>>>>>> : ${it[0]}")
        if (mSocket?.connected() == false) {
            isConnected = false
            disconnectSocket()
            SocketEvents.unRegisterAllEvents()
        }
    }

    private val onDisconnect = Emitter.Listener {
        Log.e("SocketHandler", "onDisconnect >>>>>>>>>>>> : " + mSocket?.connected())
        if (mSocket?.connected() == false) {
            isConnected = false
            disconnectSocket()
            SocketEvents.unRegisterAllEvents()
        }
    }


    fun registerHandler(methodOnServer: String?, handlerName: Emitter.Listener?) {
        try {
            if (mSocket?.connected() == true) {
                mSocket?.on(methodOnServer) { args ->
                    handlerName?.call(*args)
                }
                Log.e("SocketHandler", "Socket registering success")
            } else {
                Log.e("SocketHandler", "Socket not connected")
            }
        } catch (e: Exception) {
            Log.e("SocketHandler", "Error registering handler: ${e.message}")
            e.printStackTrace()
        }
    }


    fun unRegisterHandler(methodOnServer: String?, handlerName: Emitter.Listener?) {
        try {
            if (mSocket?.connected() == false) {
                mSocket?.off(methodOnServer, handlerName)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun sendDataToServer(methodOnServer: String?, request: JSONObject) {
        Log.e("SocketHandler", "sendDataToServer End Point: >>>>>>>>>>>>  $methodOnServer")
        try {
            if (mSocket?.connected() == true) {
                Log.e("SocketHandler ", "sendDataToServer Request $request")
                mSocket?.emit(methodOnServer, request)
            } else {
//              Handler(Looper.getMainLooper()).postDelayed({
//                    sendDataToServer(methodOnServer, request)
//                }, 1000)
//                mSocket?.connect()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}