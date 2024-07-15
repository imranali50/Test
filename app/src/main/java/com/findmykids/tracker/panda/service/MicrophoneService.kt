package com.findmykids.tracker.panda.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.findmykids.tracker.panda.model.response.AudioResponse
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.socketMvvm.SocketEvents.mApplicationContext
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.Utils
import org.json.JSONObject

class MicrophoneService : Service(), EmitterListenerCallback {

    // Constants for audio recording
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private var BUFFER_SIZE =
        AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    // AudioRecord object for recording audio
    private var audioRecord: AudioRecord? = null

    var parentId = ""
    private var isStop: Boolean = true
    var response: AudioResponse? = null
    lateinit var context: Context
    lateinit var utils: Utils

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start executing background tasks
        context = this
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        // Set the callback in the object class
        SocketEvents.setCallback(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (audioRecord != null) {
            audioRecord?.stop()
            audioRecord?.release()
        }
    }

    private fun startBackgroundTasks() {
        // Start executing your background tasks here

        utils = Utils(context)
        Log.e("TAG", "setObserver: >>>>>>>>>>>>>>>>>>> 33312  $response")
        if (response != null) {
            /*            Thread {
                            isStop = response?.toStop == true
                            parentId = response?.parentId.toString()

                            if (ContextCompat.checkSelfPermission(
                                    applicationContext, Manifest.permission.RECORD_AUDIO
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> not allowed record : ")
                                val json = JSONObject()
                                json.put("requestAudioId", response?.requestAudioId)
                                json.put("audioData", "")
                                json.put("parentId", parentId)
                                json.put("isAudioPermissionAllowed", false)
                                SocketConnectionCall.sendDataToServer(Const.audioStreamSend, json)
                            } else {
                                Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> allowed record : " + isStop)
                                if (!isStop && audioRecord == null) {
                                    if (BUFFER_SIZE == AudioRecord.ERROR || BUFFER_SIZE == AudioRecord.ERROR_BAD_VALUE) {
                                        Log.e("TAG", "setObserver: >>>>>>>> bad value buffer increase CHILD ")
                                        BUFFER_SIZE = SAMPLE_RATE * 4
                                    }

                                    audioRecord = AudioRecord(
                                        MediaRecorder.AudioSource.MIC,
                                        SAMPLE_RATE,
                                        CHANNEL_CONFIG,
                                        AUDIO_FORMAT,
                                        BUFFER_SIZE
                                    )
                                    audioRecord?.startRecording()
                                }

                                while (!isStop) {
                                    if (utils.isNetworkAvailable()) {
                                        if(response?.requestAudioId?.isNotEmpty() == true) {
                                            val buffer = ByteArray(BUFFER_SIZE)
                                            Log.e("TAG", ">>>>>>>>>>>>>>>>>> do : " + isStop)
                                            Log.e("TAG", "startBackgroundTasks:12 ${buffer.size}")
                                            Log.e("TAG", "startBackgroundTasks:1212 ${BUFFER_SIZE}")

                                            val byteRead =
                                                if (isStop) {break
                                                    Log.e("TAG", "startBackgroundTasks: hiren 321", )} else{audioRecord?.read(buffer, 0, BUFFER_SIZE)
                                                    Log.e("TAG", "startBackgroundTasks: hiren 123", )}



                                            Log.e("TAG", "startBackgroundTasks:34 ${buffer.size}")
                                            Log.e("TAG", "startBackgroundTasks:3434 ${BUFFER_SIZE}")

                                            if (byteRead != null) {
                                                if ((byteRead < -1) || isStop) break
                                            }

                                            val base64Data: String? = byteRead?.let { it1 ->
                                                Base64.encodeToString(
                                                    buffer, 0, it1, Base64.NO_WRAP
                                                )
                                            }
                                            if (response?.parentId.toString()
                                                    .isNotEmpty() && response?.requestAudioId.toString()
                                                    .isNotEmpty()
                                            ) {
                                                val json = JSONObject()
                                                json.put("requestAudioId", response?.requestAudioId)
                                                json.put("audioData", base64Data)
                                                json.put("parentId", parentId)
                                                json.put("isAudioPermissionAllowed", true)
                                                SocketConnectionCall.sendDataToServer(
                                                    Const.audioStreamSend,
                                                    json
                                                )
                                            } else {
                                                break
                                            }
                                        }else{
                                            break
                                        }
                                    } else {
                                        if (audioRecord != null) {
                                            audioRecord?.stop()
                                            break
                                        }
                                    }
                                }

            //                    if (isStop) {
            //                        if (audioRecord != null) {
            //                            audioRecord?.stop()
            //                        }
            //                    }
                            }
                        }.start()*/
            Thread {
                isStop = response?.toStop == true
                parentId = response?.parentId.toString()
                if (mApplicationContext?.let { it1 ->
                        ContextCompat.checkSelfPermission(
                            it1, Manifest.permission.RECORD_AUDIO
                        )
                    } != PackageManager.PERMISSION_GRANTED) {
                    Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> not allowed record : ")
                    val json = JSONObject()
                    json.put("requestAudioId", response?.requestAudioId)
                    json.put("audioData", "")
                    json.put("parentId", parentId)
                    json.put("isAudioPermissionAllowed", false)
                    SocketConnectionCall.sendDataToServer(Const.audioStreamSend, json)
                } else {
                    Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>> allowed record : $isStop")
                    if (!isStop) {
                        if (BUFFER_SIZE == AudioRecord.ERROR || BUFFER_SIZE == AudioRecord.ERROR_BAD_VALUE) {
                            Log.e("TAG", "setObserver: >>>>>>>> bad value buffer increase CHILD ")
                            BUFFER_SIZE = SAMPLE_RATE * 4
                        }
                        audioRecord = AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            SAMPLE_RATE,
                            CHANNEL_CONFIG,
                            AUDIO_FORMAT,
                            BUFFER_SIZE
                        )
                        audioRecord?.startRecording()

                    }

                    while (!isStop ) {
                        if (utils.isNetworkAvailable() && utils.isNetworkAvailable() && SocketConnectionCall.mSocket != null && SocketConnectionCall.isConnected) {
                            val buffer = ByteArray(BUFFER_SIZE)

                            Log.e("TAG", ">>>>>>>>>>>>>>>>>> do : $isStop")
                            val byteRead =
                                if (isStop) break else audioRecord?.read(buffer, 0, BUFFER_SIZE)
                            if (byteRead != null) {
                                if ((byteRead < -1) || isStop) break
                            }
                            val base64Data: String? = byteRead?.let { it1 -> Base64.encodeToString(buffer, 0, it1, Base64.NO_WRAP) }
                            if (response?.requestAudioId.toString().isNotEmpty()) {
                                val json = JSONObject()
                                json.put("requestAudioId", response?.requestAudioId)
                                json.put("audioData", base64Data)
                                json.put("parentId", parentId)
                                json.put("isAudioPermissionAllowed", true)
                                SocketConnectionCall.sendDataToServer(Const.audioStreamSend, json)
                            }
                        } else {
                            audioRecord?.stop()
                            break
                        }
                    }
                }
            }.start()
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onEmitterListenerReceived(listener: AudioResponse) {
        Log.e("TAG", "onEmitterListenerReceived: $response")
        response = listener
        startBackgroundTasks()

    }
}
