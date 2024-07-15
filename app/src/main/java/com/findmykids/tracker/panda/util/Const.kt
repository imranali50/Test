package com.findmykids.tracker.panda.util

object Const {
    //   google login
    var authId = "authId"
    var authName = "authName"
    var authEmail = "authEmail"

    var langType = "IN"
    var child = "CHILD"

    //SharePreference Value
    var token = "token"
    var tokenForApi = "tokenForApi"
    var id = "id"
    const val userData = "userData"
    var deviceId = "deviceId"
    val fcmToken = "fcmToken"
    val isIntroDone = "isIntroDone"
    var isParentConnect = false

    //map
    val latitude = "latitude"
    val longitude = "longitude"

    //SOS
    val ActiveSOS = "SOS_ACTIVE"
    val DeActiveSOS = "SOS_DE_ACTIVE"
    val MESSAGES = "MESSAGES"

    const val GOOGLE_MAP_API_KEY_FOR_PLACE = "AIzaSyCnxUue2b3EmwMwGvbffAtXcnrwapAddf0"
    var LocationNotAllowThenGetLatLng =
        "https://www.googleapis.com/geolocation/v1/geolocate?key=$GOOGLE_MAP_API_KEY_FOR_PLACE"

    // Socket -------------------------------------------------------------------------------------------------------------
    // Chat
    val connectJoin = "connection:join"
    val messageSend = "message:send"
    val connectionLeave = "connection:leave"
    val messageReceive = "message:receive"

    //LocationUpdate
    val locationChildUpdate = "location:child:update"

    //AppUsageUpdate
    val appUseChildUpdate = "appUse:child:update"

    //AppBlock
    val appBlockUpdate = "app:child:block:list:update"

    //Error
    val error = "error"

    //Speed
    val sendSpped = "speed:child:update"

    //Ring the bell
    const val ringBellReceiver = "ring:child:receive"

    //Audio
    const val audioNotify = "audio:notify"
    const val audioStreamSend = "audio:stream:send"
    const val audioStreamEnd = "audio:stream:end"

    //Sos DeActive
    const val sosDeActive = "sos:deActive"

    //Ring the bell
    const val ringTheBellStop = "ring:child:stop"

    //Ring the bell
    const val appChange = "app:change"

    //chatOnlineOffline
    const val userStatus = "user:status"

    external fun getMain(): String

    var isMicrophone = true
}