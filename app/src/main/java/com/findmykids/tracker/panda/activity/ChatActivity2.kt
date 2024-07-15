package com.findmykids.tracker.panda.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.BuildConfig
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.adapter.ChatMessageAdapter
import com.findmykids.tracker.panda.databinding.ActivityChatBinding
import com.findmykids.tracker.panda.model.response.ChatResponse
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.popup.MenuEditText
import com.findmykids.tracker.panda.popup.SoftKeyBoardPopup
import com.findmykids.tracker.panda.socketMvvm.SocketEvents
import com.findmykids.tracker.panda.socketMvvm.SocketModelFactory
import com.findmykids.tracker.panda.socketMvvm.SocketRepository
import com.findmykids.tracker.panda.socketMvvm.SocketViewModel
import com.findmykids.tracker.panda.sockets.SocketConnectionCall
import com.findmykids.tracker.panda.util.CAMERA_PERMISSION
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.FileUtil
import com.findmykids.tracker.panda.util.GALLERY_PERMISSION
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.SocketResult
import com.findmykids.tracker.panda.util.checkSelfPermissions
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll
import com.bumptech.glide.Glide
import com.findmykids.tracker.panda.model.response.UserStatusResponse
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.vanniktech.emoji.EmojiPopup
import com.yalantis.ucrop.UCrop
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs


class ChatActivity2 : BaseActivity(), View.OnClickListener, MenuEditText.PopupListener {
    private var flingVx: Int = 0
    private var flingVy: Int = 0
    private val b: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by viewModels<ViewModel>()
    private var chatMessageList: MutableList<ChatResponse.Messages> = ArrayList()
    private lateinit var chatMessageAdapter: ChatMessageAdapter

    private lateinit var menuKeyboard: SoftKeyBoardPopup
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var connectionId: String
    private var pageNumber: Int = 1
    private var pageLimit: Int = -1
    private var lastScrollPosition: Int = -1
    private var limit: Int = 20
    private lateinit var layoutManager: LinearLayoutManager
    private var socketViewModel: SocketViewModel? = null
    val socketRepository = SocketRepository()
    private var isLoading = false
    private lateinit var currentPhotoPath: String
    private var send_click_response = true
    var receiverId = ""

    companion object {
        const val ConnectionId = "ConnectionId"
        const val ChatGroupIcon = "ChatGroupIcon"
        const val ChatGroupName = "ChatGroupName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        connectionId = intent.getStringExtra(ConnectionId).toString()
        Glide.with(this@ChatActivity2).load(intent.getStringExtra(ChatGroupIcon).toString())
            .centerCrop().placeholder(R.drawable.ic_placeholder).into(b.ivGuardianChatImage)
        b.tvGuardianName.text = intent.getStringExtra(ChatGroupName).toString()
        setChatAdapter()
        setAllClickListener()
        setKeyboardPopup()
        int()
        getDataAndSetObserver()
        setObservers()

        var json = JSONObject()
        json.put("connectionId", connectionId)
        SocketConnectionCall.sendDataToServer(Const.connectJoin, json)
    }

    private fun setAllClickListener() {
        b.ivDoc.setOnClickListener {
            toggle()
        }
        b.ivEmoji.setOnClickListener { emojiPopup.toggle() }
        b.ivSend.setOnClickListener {
            Log.e("TAG", "setAllClickListener:12 ")
            Log.e("TAG", "setAllClickListener: 13"+send_click_response )
            Log.e("TAG", "setAllClickListener: 14"+b.edtChat.text?.trim().toString().isNotEmpty() )
            Log.e("TAG", "setAllClickListener: 15"+b.edtChat.text?.trim() )
            if (send_click_response && b.edtChat.text?.trim().toString().isNotEmpty()) {
                send_click_response = false
                val json = JSONObject()
                json.put("connectionId", connectionId)
                json.put("text", b.edtChat.text.toString().trim())
                Log.e("TAG", "setAllClickListener: $json")
                SocketConnectionCall.sendDataToServer(Const.messageSend, json)
            }
        }
        b.ivChatBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        val json = JSONObject()
        json.put("connectionId", connectionId)
        SocketConnectionCall.sendDataToServer(Const.connectionLeave, json)
        val mIntent = Intent()
        setResult(RESULT_OK, mIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        SocketEvents.setRepository(socketRepository, applicationContext)
        NotificationManagerCompat.from(this).cancel(
            Integer.parseInt(
                connectionId.replace(("[^0-9]").toRegex(), "1").substring(0, 9)
            )
        )
    }

    private fun getDataAndSetObserver() {
        SocketEvents.setRepository(socketRepository, applicationContext)
        socketViewModel = ViewModelProvider(
            this, SocketModelFactory(socketRepository)
        )[SocketViewModel::class.java]
        socketViewModel?.connectJoinData?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
                    Log.e("TAG", "connectJoinData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
        socketViewModel?.userStatusData?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
                    Log.e("TAG", "connectJoinData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                    val response =
                        Gson().fromJson(it.data.toString(), UserStatusResponse::class.java)
                    if (response.user == receiverId) {
                        if (response.isOnline) {
                            b.tvOnlineStatus.setTextColor(getColor(R.color.green))
                            b.tvOnlineStatus.text = "Online"
                        } else {
                            b.tvOnlineStatus.setTextColor(getColor(R.color.description_color))
                            b.tvOnlineStatus.text = "Offline"
                        }
                    }
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
        socketViewModel?.connectionLeaveData?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
                    Log.e("TAG", "connectionLeaveData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
        socketViewModel?.messageSendData?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
                    runOnUiThread {
                        Log.e("TAG", "messageSendData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                    }
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
        socketViewModel?.messageReceiveData?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
                    runOnUiThread {
                        val chatData =
                            Gson().fromJson(it.data.toString(), ChatResponse.Messages::class.java)
                        Log.e("TAG", "messageReceiveData: >>>>>>>>>>>>>>>>>>>>>" + chatData)
                        if (chatData.connectionId == connectionId) {
                                if (chatData.senderId == pref.getString(Const.id)) {
                                    b.edtChat.setText("")
                                    send_click_response = true
                                }
                                chatMessageAdapter.notifyItem(chatData)
                                b.rvChatMessages.scrollToPosition(chatMessageAdapter.itemCount - 1)

                        }
                    }
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
        socketViewModel?.errorData?.observe(this) {
            when (it) {
                is SocketResult.Success -> {
                    Log.e("TAG", "errorData: >>>>>>>>>>>>>>>>>>>>>" + it.data)
                }

                is SocketResult.Loading -> {

                }

                is SocketResult.NoNetwork -> {

                }
            }
        }
    }

    private fun int() {
        getChatMessageAPI(pageNumber)
        b.rvChatMessages.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                // Storing these velocities into variables.
                flingVx = velocityX
                flingVy = velocityY
                return false
            }
        }
        b.rvChatMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    var velocity = 1f
                    var currentOffset = 0
                    var totalOffset = 0
                    var property: DynamicAnimation.ViewProperty = DynamicAnimation.SCALE_Y
                    if (flingVy != 0) {
                        currentOffset = recyclerView.computeVerticalScrollOffset()
                        totalOffset =
                            recyclerView.computeVerticalScrollRange() - recyclerView.measuredHeight
                        velocity = (abs(flingVy) / recyclerView.measuredHeight).toFloat()
                        property = DynamicAnimation.SCALE_Y
                        if (flingVy > 0) {
                            recyclerView.pivotY = recyclerView.measuredHeight.toFloat()
                        } else {
                            recyclerView.pivotY = 0f
                        }
                    } else if (flingVx != 0) {
                        currentOffset = recyclerView.computeHorizontalScrollOffset()
                        totalOffset =
                            recyclerView.computeHorizontalScrollRange() - recyclerView.measuredWidth
                        velocity = (abs(flingVx) / recyclerView.measuredWidth).toFloat()
                        property = DynamicAnimation.SCALE_X
                        if (flingVx > 0) {
                            recyclerView.pivotX = recyclerView.measuredWidth.toFloat()
                        } else {
                            recyclerView.pivotX = 0f
                        }
                    }
                    if (currentOffset == totalOffset || currentOffset == 0) {

                        Log.e(
                            "TAG",
                            "onScrollStateChanged: >>>>>>>>>>>> " + pageNumber + ">>>>>>>" + pageLimit
                        )
                        if (pageNumber < pageLimit) {
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val lastVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
//                            lastScrollPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            lastScrollPosition = lastVisiblePosition
                            b.progressLoad.visibility = View.VISIBLE
                            Log.e("TAG", "onScrollStateChanged: >>>>>>>>>>>> 555 " +lastVisiblePosition)
                            pageNumber++
                            getChatMessageAPI(pageNumber)
                        }
                    }
                }
            }
        })
//        b.rvChatMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val firstVisibleItemPosition =
//                    layoutManager.findFirstCompletelyVisibleItemPosition()
//
//                if (!isLoading && firstVisibleItemPosition <= 0) {
//                    b.progressLoad.visibility = View.VISIBLE
//                    pageNumber++
//                    getChatMessageAPI(pageNumber)
//                }
//            }
//        })
    }

    private fun getChatMessageAPI(page: Int) {
        isLoading = true
        viewModel.getChatMessage(connectionId, page, limit)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
        var json = JSONObject()
        json.put("connectionId", connectionId)
        SocketConnectionCall.sendDataToServer(Const.connectionLeave, json)
    }

    private fun setChatAdapter() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        b.rvChatMessages.layoutManager = layoutManager
        chatMessageAdapter = ChatMessageAdapter(this, ArrayList())
        b.rvChatMessages.adapter = chatMessageAdapter
    }

    private fun setKeyboardPopup() {
        menuKeyboard = SoftKeyBoardPopup(
            this, b.llSendMessage, b.edtChat, b.edtChat, b.menuChatContainer
        ) {
            when (it) {
                "Gallery" -> {
                    menuKeyboard.dismiss()
                    if (hasPermissions(this@ChatActivity2, GALLERY_PERMISSION)) {
                        openGalleryLauncherIntent()
                    } else {
                        permGalleryLauncher.launch(GALLERY_PERMISSION)
                    }
                }

//                "Video" -> {
//                    imageList = ArrayList()
//                    imagesEncodedList = ArrayList()
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                        VideoCallLaunch()
//                    } else {
//                        if (hasPermissions(this, PERMISSIONS)) {
//                            VideoCallLaunch()
//                        } else {
//                            permVideoReqLauncher.launch(PERMISSIONS)
//                        }
//                    }
//                }

                "Camera" -> {
                    menuKeyboard.dismiss()
                    if (hasPermissions(this@ChatActivity2, CAMERA_PERMISSION)) {
                        openCameraLauncherIntent()
                    } else {
                        permCameraLauncher.launch(CAMERA_PERMISSION)
                    }
                }

//                "Audio" -> {
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                        AudioCallLauncher()
//                    } else {
//                        if (hasPermissions(this, Audio_PERMISSION)) {
//                            AudioCallLauncher()
//                        } else {
//                            permAudioReqLauncher.launch(Audio_PERMISSION)
//                        }
//                    }
//                }
            }
        }

        emojiPopup = EmojiPopup(
            rootView = b.root,
            editText = b.edtChat,
            onEmojiBackspaceClickListener = { },
            onEmojiClickListener = { emoji -> },
            onEmojiPopupShownListener = { b.ivEmoji.setImageResource(R.drawable.ic_keyboard) },
            onSoftKeyboardOpenListener = { px -> },
            onEmojiPopupDismissListener = { b.ivEmoji.setImageResource(R.drawable.emoji_chat) },
            onSoftKeyboardCloseListener = { },
            keyboardAnimationStyle = 0,
            pageTransformer = null,
        )
    }

    override fun getPopup(): PopupWindow {
        return menuKeyboard
    }

    private fun toggle() {
        if (menuKeyboard.isShowing) {
            menuKeyboard.dismiss()
        } else {
            menuKeyboard.show()
        }
    }

    override fun onClick(v: View?) {

    }

    private fun setObservers() {
        viewModel.mediaResponse.observe(this) {
            runOnUiThread {
                when (it) {
                    is NetworkResult.Success -> {
                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            "1" -> {
                                send_click_response = false
                                var json = JSONObject()
                                json.put("connectionId", connectionId)
                                json.put("text", "")
                                json.put("image", it.data.data.imageUrls[0])
                                SocketConnectionCall.sendDataToServer(Const.messageSend, json)
                                MyApplication.progressBar.dismiss()
                            }

                            else -> {
                                send_click_response = true
                                checkStatus(it.data!!.status.toInt(), it.data.message)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        send_click_response = true
                        MyApplication.progressBar.dismiss()
                        utils.showToast(it.message.toString())
                    }

                    is NetworkResult.Loading -> {
                        if (this != null && !isFinishing && !isDestroyed) MyApplication.progressBar.show(
                            this
                        )
                    }

                    is NetworkResult.NoNetwork -> {
                        send_click_response = true
                        MyApplication.progressBar.dismiss()
                        netWorkNotAvailable()
                    }
                }
            }

        }
        viewModel.chatMessageResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    if (pageNumber == 1) MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            Log.e("TAG", "`setObservers: >>>>>>>>>` " + it.data.data.totalPages)
                            pageLimit = it.data.data.totalPages
                            receiverId = it.data.data.receiverId
                            isLoading = false
                            chatMessageList = it.data.data.messages.toMutableList()
                            chatMessageList.reverse()
//                            if (pageNumber == 1)
                            chatMessageAdapter.notifyWithList(chatMessageList)
//                            else
//                                chatMessageAdapter.notifyWithListONLY(chatMessageList)
                            b.rvChatMessages.isNestedScrollingEnabled = false
                            if (pageNumber == 1) {
                                b.rvChatMessages.scrollToPosition((chatMessageList.size).minus(1))
                            } else {
                                b.rvChatMessages.scrollToPosition(lastScrollPosition)
                            }
//                            if (pageNumber != 1)
                            b.progressLoad.visibility = View.GONE
//                            }

                            if (it.data.data.isOnline) {
                                b.tvOnlineStatus.setTextColor(getColor(R.color.green))
                                b.tvOnlineStatus.text = "Online"
                            } else {
                                b.tvOnlineStatus.setTextColor(getColor(R.color.description_color))
                                b.tvOnlineStatus.text = "Offline"
                            }
                        }

                        else -> {
                            checkStatus(it.data!!.status.toInt(), it.data.message)
                        }
                    }
                }

                is NetworkResult.Error -> {
                    if (pageNumber == 1) MyApplication.progressBar.dismiss()
                    utils.showToast(it.message.toString())
                }

                is NetworkResult.Loading -> {
                    if (pageNumber == 1) MyApplication.progressBar.show(activity)
                }

                is NetworkResult.NoNetwork -> {
                    if (pageNumber == 1) MyApplication.progressBar.dismiss()
                    netWorkNotAvailable()
                }
            }
        }
    }

    private var cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val tempUri = Uri.fromFile(File(currentPhotoPath))
                UCrop.of(
                    tempUri!!, Uri.fromFile(
                        File(
                            cacheDir, System.currentTimeMillis().toString() + "Crop_Image"
                        )
                    )
                ).withAspectRatio(9F, 16F).start(activity)
            }
        }

    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val selectedImage: Uri? = result.data!!.data
                    UCrop.of(
                        selectedImage!!, Uri.fromFile(
                            File(
                                cacheDir, System.currentTimeMillis().toString() + "Crop_Image"
                            )
                        )
                    ).withAspectRatio(9F, 16F).start(activity)
                }
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            val temp = FileUtil.getPath(this, resultUri!!)
            viewModel.mediaUpload("profileImage", File(temp.toString()), 2)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            utils.showToast(cropError.toString())
        }
    }

    private var settingCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (checkSelfPermissions(this@ChatActivity2, CAMERA_PERMISSION)) {
                openCameraLauncherIntent()
            }
        }

    private var settingGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (checkSelfPermissions(this@ChatActivity2, GALLERY_PERMISSION)) {
                openGalleryLauncherIntent()
            }
        }

    private val permCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                openCameraLauncherIntent()
            } else {
                if (shouldShow(this@ChatActivity2, CAMERA_PERMISSION)) {

                } else {
                    val snackBar = Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.permission_text),
                        Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        settingCameraLauncher.launch(
                            Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                            )
                        )
                    }
                    val snackBarView = snackBar.view
                    val textView =
                        snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    textView.maxLines = 5
                    snackBar.show()
                }
            }
        }

    private val permGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                openGalleryLauncherIntent()
            } else {
                if (shouldShow(this@ChatActivity2, GALLERY_PERMISSION)) {

                } else {
                    val snackBar = Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.permission_text),
                        Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        settingGalleryLauncher.launch(
                            Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                            )
                        )
                    }
                    val snackBarView = snackBar.view
                    val textView =
                        snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    textView.maxLines = 5  //Or as much as you need
                    snackBar.show()
                }
            }
        }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun openCameraLauncherIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                        applicationContext,
                        applicationContext.packageName + ".provider",
                        photoFile
                    )
                )
                cameraLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun openGalleryLauncherIntent() {
        val pictureActionIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pictureActionIntent)
    }
}