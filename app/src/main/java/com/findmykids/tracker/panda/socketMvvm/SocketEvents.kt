package com.findmykids.tracker.panda.socketMvvm

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.database.AnalysisDatabase
import com.findmykids.tracker.panda.database.RestrictPackages
import com.findmykids.tracker.panda.databinding.DialogSignalBinding
import com.findmykids.tracker.panda.model.response.AppBlockResponse
import com.findmykids.tracker.panda.model.response.AudioResponse
import com.findmykids.tracker.panda.model.response.CommonResponse
import com.findmykids.tracker.panda.model.response.RingTheBellResponse
import com.findmykids.tracker.panda.service.EmitterListenerCallback
import com.findmykids.tracker.panda.service.MainAppService
import com.findmykids.tracker.panda.service.MicrophoneService
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.SocketResult
import com.findmykids.tracker.panda.util.Utils
import com.findmykids.tracker.panda.util.isMyServiceRunning
import com.google.gson.Gson
import io.socket.emitter.Emitter
import org.json.JSONObject


object SocketEvents {


    var socketRepository: SocketRepository? = null
    var mApplicationContext: Context? = null

    //dataBase
    lateinit var restrictPackage: RestrictPackages
    lateinit var analysisPackage: AnalysisDatabase

    //ArrayList
    var restrictPackageList: ArrayList<String> = ArrayList()
    var analysisPackageList: ArrayList<String> = ArrayList()

    private val SAMPLE_RATE = 44100

    //ring the bell dialog show

    var isShowDialog = false

    //    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO1
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    private var handlerAudio: Handler? = null
    private var audioRecord: AudioRecord? = null

    private var isStop: Boolean = true

    //dia+log
    private lateinit var dialogBinding: DialogSignalBinding
    private lateinit var dialog: AlertDialog

    //media player
    private lateinit var mediaPlayer: MediaPlayer

    //util
    lateinit var utils: Utils

    var parentId = ""
    var isPlay = false

    private var isRecording = false
    private var BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    )

    fun setRepository(socketRepo: SocketRepository, context: Context) {
        socketRepository = socketRepo
        mApplicationContext = context
    }

    val connectJoin: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        if (Integer.parseInt(temp.getString("status")) == 1) {
            socketRepository?.connectJoinLiveData?.postValue(SocketResult.Success(temp))
        }
    }
    val messageReceive: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        socketRepository?.messageReceiveLiveData?.postValue(SocketResult.Success(temp))
    }
    val error: Emitter.Listener = Emitter.Listener {

        val temp: JSONObject = it[0] as JSONObject
        Log.e("TAG", ">>>>>>>>>>>>>>>>>> : 123123$temp")
        val response = Gson().fromJson(temp.toString(), CommonResponse::class.java)
        if (response.status == "5") {
            utils = Utils(mApplicationContext!!)
            utils.logOutFromBackground(mApplicationContext!!)
        }

        socketRepository?.errorLiveData?.postValue(SocketResult.Success(temp))
    }


    // receiver side when receiver receive call
    val connectionLeave: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        socketRepository?.connectionLeaveLiveData?.postValue(SocketResult.Success(temp))
    }

    val messageSend: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        socketRepository?.messageSendLiveData?.postValue(SocketResult.Success(temp))
    }
    val sosDeactiveListen: Emitter.Listener = Emitter.Listener {
        Log.e("TAG123456678847", "12345: $it")
//        val temp: JSONObject = "" as JSONObject
        socketRepository?.sosDeActiveListenLiveData?.postValue(SocketResult.Success("temp"))
    }
    val locationChildUpdate: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        socketRepository?.locationChildUpdateLiveData?.postValue(SocketResult.Success(temp))
    }
    val sendSpeed: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        socketRepository?.sendSpeedLiveData?.postValue(SocketResult.Success(temp))
    }
    val appUseChildUpdate: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        socketRepository?.appUseChildUpdateLiveData?.postValue(SocketResult.Success(temp))
    }
    val userStatus: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        socketRepository?.userStatusUpdateLiveData?.postValue(SocketResult.Success(temp))
    }

    private var callback: EmitterListenerCallback? = null
    // Method to set the callback
    fun setCallback(callback: EmitterListenerCallback) {
        this.callback = callback
    }

    // Method to pass the Emitter.Listener object to the callback
    fun passListener(listener: AudioResponse) {
        callback?.onEmitterListenerReceived(listener)
    }
    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mApplicationContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }

    private fun startService(context: Context) {
            val intent = Intent(context, MicrophoneService::class.java)
            context.startService(intent)
    }

    private fun stopService(context: Context) {
            val intent = Intent(context, MicrophoneService::class.java)
            context.stopService(intent)
    }

    val audioNotify: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        val response = Gson().fromJson(temp.toString(), AudioResponse::class.java)
        Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 444  $temp")
        if (!temp.getBoolean("toStop")) {
            if(Const.isMicrophone){
                Const.isMicrophone = false
            Log.e("TAG", "hirenhiren: >>>>>>>>>>>>>>>>>>> 111  $temp")
//                if(!isMyServiceRunning(MicrophoneService::class.java)) {
                    Log.e("TAG", "hirenhiren: >>>>>>>>>>>>>>>>>>> 444  $temp")
                    startService(mApplicationContext!!)
                    Handler(Looper.getMainLooper()).postDelayed({
                        Const.isMicrophone = true
                        passListener(response)
                    }, 500)
                    Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 333  $temp")
//                }
            }
        } else {
            Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 3334  $temp")
//            if(isMyServiceRunning(MicrophoneService::class.java)) {
                passListener(response)
                stopService(mApplicationContext!!)
//            }
        }
    }

/*    val audioNotify: Emitter.Listener = Emitter.Listener {
        utils = Utils(mApplicationContext!!)
        val temp: JSONObject = it[0] as JSONObject
        Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 333  " + temp)
        Thread {
            Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 444 " + temp)
            isStop = temp.getBoolean("toStop")
            parentId = temp.getString("parentId")
            if (mApplicationContext?.let { it1 ->
                    ContextCompat.checkSelfPermission(
                        it1, Manifest.permission.RECORD_AUDIO
                    )
                } != PackageManager.PERMISSION_GRANTED) {
                Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> not allowed record : ")
                val json = JSONObject()
                json.put("requestAudioId", temp.getString("requestAudioId"))
                json.put("audioData", "")
                json.put("parentId", parentId)
                json.put("isAudioPermissionAllowed", false)
                SocketConnectionCall.sendDataToServer(Const.audioStreamSend, json)
            } else {
                Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> allowed record : " + isStop)
                if (!isStop) {

                    if (BUFFER_SIZE == AudioRecord.ERROR || BUFFER_SIZE == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e("TAG", "setObserver: >>>>>>>> bad value buffer increase CHILD ")
                        BUFFER_SIZE = SAMPLE_RATE * 4
                    }
                    //                    if (ContextCompat.checkSelfPermission(
                    //                            mApplicationContext!!, Manifest.permission.RECORD_AUDIO
                    //                        ) == PackageManager.PERMISSION_GRANTED
                    //                    ) {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        BUFFER_SIZE
                    )

                    //                    }
                    audioRecord?.startRecording()

                }

                while (!isStop) {
                    if (utils.isNetworkAvailable()) {
                        val buffer = ByteArray(BUFFER_SIZE)

                        Log.e("TAG", ">>>>>>>>>>>>>>>>>> do : " + isStop)
                        val byteRead = if (isStop) break else audioRecord?.read(buffer, 0, BUFFER_SIZE)
                        if (byteRead != null) {
                            if ((byteRead < -1) || isStop) break
                        }
                        val base64Data: String? = byteRead?.let { it1 ->
                            Base64.encodeToString(
                                buffer, 0, it1, Base64.NO_WRAP
                            )
                        }
                        val json = JSONObject()
                        json.put(
                            "requestAudioId", temp.getString("requestAudioId")
                        )
                        json.put("audioData", base64Data)
                        json.put("parentId", parentId)
                        json.put("isAudioPermissionAllowed", true)
                        SocketConnectionCall.sendDataToServer(
                            Const.audioStreamSend, json
                        )
                    }else{
                        audioRecord?.stop()
                        break
                    }
                }
                if (isStop) {
                    audioRecord?.stop()
                    //                    audioRecord?.release()
                    //                    audioRecord.release()
                }
            }
        }.start()
    }*/

    val audioStreamSend: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        Log.e("TAG", ">>>>>>>>>>>>>>>>> 22 : $temp")
//        socketRepository?.audioStreamSendLiveData?.postValue(SocketResult.Success(temp))
    }


    val appChange: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        Log.e("TAG", ">>>>>>>>>>>>>>>>> 22 : $temp")
//        socketRepository?.audioStreamSendLiveData?.postValue(SocketResult.Success(temp))
    }
    val ringTheBellStop: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        Log.e("TAG", ">>>>>>>>>>>>>>>>> 22 : $temp")

    }

    val audioStreamEnd: Emitter.Listener = Emitter.Listener {
//        val temp: JSONObject = it[0] as JSONObject
//        isStop = true
        Log.e("TAG", ">>>>>>>>>>>>>>>>>>>> END Child : ")
//        socketRepository?.audioStreamSendLiveData?.postValue(SocketResult.Success(temp))
    }

    val appBlockUpdate: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        restrictPackage = RestrictPackages(mApplicationContext)
        analysisPackage = AnalysisDatabase(mApplicationContext)
        restrictPackageList = restrictPackage.readRestrictPacks()
        analysisPackageList = analysisPackage.readAllApps()
        val blockAppResponse = Gson().fromJson(temp.toString(), AppBlockResponse::class.java)

        if (!analysisPackageList.contains(blockAppResponse.appPkgName)) {
            analysisPackage.addApp(blockAppResponse.appPkgName)
        } else {
            analysisPackage.inAppRestrict(blockAppResponse.appPkgName)
        }

        if (!restrictPackageList.contains(blockAppResponse.appPkgName)) {
            restrictPackage.addNewPackageInRestrict(blockAppResponse.appPkgName)
        } else {
            analysisPackage.inAppUnrestrict(blockAppResponse.appPkgName)
            restrictPackage.deleteRestrictPack(blockAppResponse.appPkgName)
        }
        Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>> : $temp")
    }


    val ringBellReceiver: Emitter.Listener = Emitter.Listener {
        val temp: JSONObject = it[0] as JSONObject
        Log.e("TAG", "RingBell >>>>>>>>>>>>>>>>>>>>> : $temp")
        val response = Gson().fromJson(temp.toString(), RingTheBellResponse::class.java)

        Handler(Looper.getMainLooper()).post {
            try {
                if (!isShowDialog) {
                    initDialog(response.parentId, response.parentName)
                }
            } catch (e: Exception) {
                Log.e("TAG", "ringBellReceiver error" + e.message)
            }
        }

    }

    private fun initDialog(parentId: String, parentName: String) {
        isShowDialog = true
        val li = LayoutInflater.from(mApplicationContext)
        dialogBinding = DialogSignalBinding.inflate(li)
        dialog = AlertDialog.Builder(mApplicationContext!!).create()
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setView(dialogBinding.root)

        val mParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
//                val testView = LayoutInflater.from(mApplicationContext).inflate(R.layout.activity_main, null)
        val wm = mApplicationContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.addView(dialogBinding.root, mParams)
        dialogBinding.txtTitle.text = "This signal has been sent by $parentName"


//        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//            AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
//        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);=

//        unCommit bellow point

        val audioManager =
            mApplicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

//        val maxMediaVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
//        if (maxMediaVolume != null) {
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMediaVolume, 0)
//        }

        mediaPlayer = MediaPlayer.create(mApplicationContext, R.raw.signal);
//        firstSound.setVolume(100f, 100f)

        mediaPlayer.start()
        mediaPlayer.isLooping = true
        Log.e("TAG", "initDialog: " + dialogBinding.root)
        dialogBinding.tvCancelBtn.setOnClickListener {
            wm.removeView(dialogBinding.root)
            isShowDialog = false
            mediaPlayer.stop()

            val json = JSONObject()
            json.put("parentId", parentId)
            SocketConnectionCall.sendDataToServer(Const.ringTheBellStop, json)
        }

    }


    fun registerAllEvents() {
        Log.e("SocketHandler", "registerAllEvents: >>>>>>>> ")
        SocketConnectionCall.registerHandler(Const.connectJoin, connectJoin)
        SocketConnectionCall.registerHandler(Const.connectionLeave, connectionLeave)
        SocketConnectionCall.registerHandler(Const.messageReceive, messageReceive)
        SocketConnectionCall.registerHandler(Const.messageSend, messageSend)
        SocketConnectionCall.registerHandler(Const.locationChildUpdate, locationChildUpdate)
        SocketConnectionCall.registerHandler(Const.appUseChildUpdate, appUseChildUpdate)
        SocketConnectionCall.registerHandler(Const.appBlockUpdate, appBlockUpdate)
        SocketConnectionCall.registerHandler(Const.sendSpped, sendSpeed)
        SocketConnectionCall.registerHandler(Const.audioNotify, audioNotify)
        SocketConnectionCall.registerHandler(Const.audioStreamSend, audioStreamSend)
        SocketConnectionCall.registerHandler(Const.appChange, appChange)
        SocketConnectionCall.registerHandler(Const.audioStreamEnd, audioStreamEnd)
        SocketConnectionCall.registerHandler(Const.sosDeActive, sosDeactiveListen)
        SocketConnectionCall.registerHandler(Const.ringTheBellStop, ringTheBellStop)
        SocketConnectionCall.registerHandler(Const.userStatus, userStatus)
        SocketConnectionCall.registerHandler(Const.ringBellReceiver, ringBellReceiver)
        SocketConnectionCall.registerHandler(Const.error, error)
    }


    fun unRegisterAllEvents() {
        Log.e("SocketHandler", "unRegisterAllEvents: >>>>>>>> ")
        SocketConnectionCall.unRegisterHandler(Const.connectJoin, connectJoin)
        SocketConnectionCall.unRegisterHandler(Const.connectionLeave, connectionLeave)
        SocketConnectionCall.unRegisterHandler(Const.messageReceive, messageReceive)
        SocketConnectionCall.unRegisterHandler(Const.messageSend, messageSend)
        SocketConnectionCall.unRegisterHandler(Const.locationChildUpdate, locationChildUpdate)
        SocketConnectionCall.unRegisterHandler(Const.appUseChildUpdate, appUseChildUpdate)
        SocketConnectionCall.unRegisterHandler(Const.appBlockUpdate, appBlockUpdate)
        SocketConnectionCall.unRegisterHandler(Const.ringBellReceiver, ringBellReceiver)
        SocketConnectionCall.unRegisterHandler(Const.sendSpped, sendSpeed)
        SocketConnectionCall.unRegisterHandler(Const.appChange, appChange)
        SocketConnectionCall.unRegisterHandler(Const.userStatus, userStatus)
        SocketConnectionCall.unRegisterHandler(Const.audioNotify, audioNotify)
        SocketConnectionCall.unRegisterHandler(Const.audioStreamSend, audioStreamSend)
        SocketConnectionCall.unRegisterHandler(Const.ringTheBellStop, ringTheBellStop)
        SocketConnectionCall.unRegisterHandler(Const.sosDeActive, sosDeactiveListen)
        SocketConnectionCall.unRegisterHandler(Const.audioStreamEnd, audioStreamEnd)
        SocketConnectionCall.unRegisterHandler(Const.error, error)
    }

}
