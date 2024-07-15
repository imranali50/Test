package com.findmykids.tracker.panda.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.findmykids.tracker.panda.BuildConfig
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ActivityProfileCreateBinding
import com.findmykids.tracker.panda.databinding.DialogCameraBinding
import com.findmykids.tracker.panda.model.request.EditProfileRequest
import com.findmykids.tracker.panda.model.viewmodel.ViewModel
import com.findmykids.tracker.panda.util.CAMERA_PERMISSION
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.FileUtil
import com.findmykids.tracker.panda.util.GALLERY_PERMISSION
import com.findmykids.tracker.panda.util.MyApplication
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.checkSelfPermissions
import com.findmykids.tracker.panda.util.hasPermissions
import com.findmykids.tracker.panda.util.shouldShow
import com.findmykids.tracker.panda.util.statusBarForAll
import com.bumptech.glide.Glide
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class ProfileCreateActivity : com.findmykids.tracker.panda.activity.BaseActivity() {
    private val b: ActivityProfileCreateBinding by lazy {
        ActivityProfileCreateBinding.inflate(
            layoutInflater
        )
    }

    companion object {
        const val IsEdit = "IsEdit"
    }

    private lateinit var dialogBinding: DialogCameraBinding
    private lateinit var dialog: AlertDialog
    private lateinit var currentPhotoPath: String
    var phoneCodeValidator = false

    private val viewModel by viewModels<ViewModel>()
    var phoneNumber = ""
    private lateinit var phoneCode: String
    var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        statusBarForAll()
        int()
        onClickListener()

    }

    @SuppressLint("RestrictedApi")
    private fun int() {
        isEdit = intent.getBooleanExtra(IsEdit, false)
        b.header.flToolBack.visibility = View.VISIBLE
        b.header.toolbarTitle.visibility = View.VISIBLE
        if (isEdit) {
            b.tvCreateProfileBtn.text = getString(R.string.save_changes)
            b.header.toolbarTitle.text = getString(R.string.my_profile)
            b.rlBG.setBackgroundColor(getColor(R.color.status_bar_color))
        } else {
            b.header.toolbarTitle.text = getString(R.string.create_profile)
        }
        val bottomAppBarTopEdgeTreatment = BottomAppBarTopEdgeTreatment(
            30f, 30f, 0f
        )

        bottomAppBarTopEdgeTreatment.fabDiameter =
            resources.getDimension(com.intuit.sdp.R.dimen._80sdp)
        b.rlMain.shapeAppearanceModel =
            b.rlMain.shapeAppearanceModel.toBuilder().setTopEdge(bottomAppBarTopEdgeTreatment)
                .build()
        b.rlMain.elevation = resources.getDimension(com.intuit.sdp.R.dimen._8sdp)

        b.apply {
            ccpLoadFullNumber.setPhoneNumberValidityChangeListener { isValidNumber ->
//                evMobileField.error = "not valid"
            }
            //phone number formatting into country wise
            ccpLoadFullNumber.registerCarrierNumberEditText(evMobileField)
            //phoneCode
            phoneCode = ccpLoadFullNumber.selectedCountryCode.toString()
            ccpLoadFullNumber.setOnCountryChangeListener {
                phoneCode = ccpLoadFullNumber.selectedCountryCode
            }
        }
        setObservers()
        initDialog()
    }

    private fun onClickListener() {
        b.header.flToolBack.setOnClickListener {
            onBackPressed()
        }
        b.tvCreateProfileBtn.setOnClickListener {
            if (isValid()) {
                editProfileAPI()
            }
        }
        b.imgProfile.setOnClickListener {
            if (!dialog.isShowing) {
                dialog.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        phoneCode = loginResponse!!.data.countryCode
        Log.e("TAG", "onResume: " + phoneCode)
        if (phoneCode.isNotEmpty()) {
            b.ccpLoadFullNumber.setCountryForPhoneCode(phoneCode.toInt())
        } else {
            phoneCode = b.ccpLoadFullNumber.selectedCountryCode
        }
        //phoneNumber
        b.evMobileField.setText(loginResponse?.data?.mobileNumber)
        b.ccpLoadFullNumber.registerCarrierNumberEditText(b.evMobileField)
        b.ccpLoadFullNumber.setPhoneNumberValidityChangeListener { isValidNumber ->
            phoneCodeValidator = isValidNumber
        }
        if (loginResponse?.data?.mobileNumber.toString().isNotEmpty()) {
            b.evMobileField.isEnabled = false
            b.ccpLoadFullNumber.isEnabled = false
            b.ccpLoadFullNumber.setCcpClickable(false)
        }
        b.etEmail.setText(loginResponse?.data?.email)
        b.ccpLoadFullNumber.registerCarrierNumberEditText(b.evMobileField)
        b.etName.setText(loginResponse?.data?.name)
        if (loginResponse?.data?.gender.toString().isNotEmpty()) {
            if (loginResponse?.data?.gender.toString() == "MALE") {
                b.radioMale.isChecked = true
            } else {
                b.radioFeMale.isChecked = true
            }
        }
        if (loginResponse?.data?.age.toString().isNotEmpty()) {
            b.etAge.setText(loginResponse?.data?.age.toString())
        }
        if (loginResponse?.data?.profileImage.toString().isNotEmpty()) {
            Log.e("TAG", "onResume: " + loginResponse?.data)
            profileImageUrl = loginResponse?.data?.profileImage.toString()
            Glide.with(activity).load(loginResponse?.data?.profileImage)
                .placeholder(R.drawable.ic_placeholder).into(b.imgProfile)
            b.editImg.setImageResource(R.drawable.ic_profile_edit)
        } else {
            b.editImg.setImageResource(R.drawable.ic_edit_camera)
        }
    }

    private fun editProfileAPI() {
        var gender = "MALE"
        gender = if (b.radioMale.isChecked) {
            "MALE"
        } else {
            "FEMALE"
        }
        val request = EditProfileRequest(
            Const.langType,
            b.etName.text.toString().trim(),
            b.etAge.text.toString().trim(),
            gender,
            "+$phoneCode",
            phoneNumber,
            profileImageUrl
        )
        viewModel.editProfile(request)
    }

    private fun setObservers() {
        viewModel.editProfileResponse.observe(this) {
            when (it) {
                is NetworkResult.Success -> {
                    MyApplication.progressBar.dismiss()
                    when (it.data?.status) {
                        "1" -> {
                            pref.setString(Const.userData, Gson().toJson(it.data))
                            if (isEdit) {
                                utils.showToast("Profile updated successfully")
                                onBackPressed()
                            } else {
                                utils.showToast("Profile created successfully")
                                checkProfileStatus()
                            }
                        }

                        else -> {
                            checkStatus(it.data!!.status.toInt(), it.data.message)
                        }
                    }
                }

                is NetworkResult.Error -> {
                    MyApplication.progressBar.show(this)
                    utils.showToast(it.message.toString())
                }

                is NetworkResult.Loading -> {
                    MyApplication.progressBar.dismiss()
                }

                is NetworkResult.NoNetwork -> {
                    MyApplication.progressBar.dismiss()
                    netWorkNotAvailable()
                }
            }
        }
        viewModel.mediaResponse.observe(this) {
            runOnUiThread {
                when (it) {
                    is NetworkResult.Success -> {
                        MyApplication.progressBar.dismiss()
                        when (it.data?.status) {
                            "1" -> {
                                profileImageUrl = it.data.data.imageUrls[0]
                                Log.e(
                                    "TAG", "setObservers: " + it.data.data.imageUrls[0].toString()
                                )
                                utils.showToast(it.data.message)
                                Glide.with(activity).load(it.data.data.imageUrls[0])
                                    .placeholder(R.drawable.ic_placeholder).into(b.imgProfile)
                                MyApplication.progressBar.dismiss()
                            }

                            else -> {
                                checkStatus(it.data!!.status.toInt(), it.data.message)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        MyApplication.progressBar.dismiss()
                        utils.showToast(it.message.toString())
                    }

                    is NetworkResult.Loading -> {
                        if (this != null && !isFinishing && !isDestroyed) MyApplication.progressBar.show(
                            this
                        )
                    }

                    is NetworkResult.NoNetwork -> {
                        MyApplication.progressBar.dismiss()
                        netWorkNotAvailable()
                    }
                }
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyApplication.progressBar.progressDialog != null && MyApplication.progressBar.progressDialog!!.isShowing()) {
            MyApplication.progressBar.dismiss()
        }
    }

    private fun initDialog() {
        dialogBinding = DialogCameraBinding.inflate(layoutInflater)

        dialog = AlertDialog.Builder(this@ProfileCreateActivity).create()
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setView(dialogBinding.root)

        dialogBinding.llGallery.setOnClickListener {
            if (hasPermissions(this@ProfileCreateActivity, GALLERY_PERMISSION)) {
                openGalleryLauncherIntent()
            } else {
                permGalleryLauncher.launch(GALLERY_PERMISSION)
            }
            dialog.dismiss()
        }

        dialogBinding.llCamera.setOnClickListener {
            if (hasPermissions(this@ProfileCreateActivity, CAMERA_PERMISSION)) {
                openCameraLauncherIntent()
            } else {
                permCameraLauncher.launch(CAMERA_PERMISSION)
            }
            dialog.dismiss()
        }

        dialogBinding.ivCloseDialog.setOnClickListener {
            dialog.dismiss()
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
            if (checkSelfPermissions(this@ProfileCreateActivity, CAMERA_PERMISSION)) {
                openCameraLauncherIntent()
            }
        }

    private var settingGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (checkSelfPermissions(this@ProfileCreateActivity, GALLERY_PERMISSION)) {
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
                if (shouldShow(this@ProfileCreateActivity, CAMERA_PERMISSION)) {

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
                if (shouldShow(this@ProfileCreateActivity, GALLERY_PERMISSION)) {

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
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                            applicationContext,
                            applicationContext.packageName + ".provider",
                            photoFile
                        )
                    )
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                }
                cameraLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun openGalleryLauncherIntent() {
        val pictureActionIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pictureActionIntent)
    }

    private fun isValid(): Boolean {
        var message = ""
        phoneNumber = b.ccpLoadFullNumber.fullNumber.substring(phoneCode.length)

        if (b.etName.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_name)
            b.etName.requestFocus()
        } else if (b.etEmail.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_email_address)
            b.etEmail.requestFocus()
        } else if (!phoneCodeValidator && phoneNumber.isNotEmpty()) {
            message = "Please enter valid phone number"
            b.evMobileField.requestFocus()
        } else if (b.etAge.text.toString().trim().isEmpty()) {
            message = getString(R.string.please_enter_your_age)
            b.etAge.requestFocus()
        }
        if (message.trim().isNotEmpty()) {
            utils.showToast(message)
            utils.hideKeyBoardFromView(this)
        }
        return message.trim().isEmpty()
    }


}