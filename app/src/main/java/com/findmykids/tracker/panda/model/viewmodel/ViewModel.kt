package com.findmykids.tracker.panda.model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.findmykids.tracker.panda.model.request.ConnectParentRequest
import com.findmykids.tracker.panda.model.request.DeleteAccountRequest
import com.findmykids.tracker.panda.model.request.EditProfileRequest
import com.findmykids.tracker.panda.model.request.LoginRequest
import com.findmykids.tracker.panda.model.request.RequestLogout
import com.findmykids.tracker.panda.model.request.ResendRequest
import com.findmykids.tracker.panda.model.request.ResetPasswordRequest
import com.findmykids.tracker.panda.model.request.SOSRequest
import com.findmykids.tracker.panda.model.request.SignupRequest
import com.findmykids.tracker.panda.model.request.UnBlockRequest
import com.findmykids.tracker.panda.model.request.VerifyRequest
import com.findmykids.tracker.panda.model.response.AllChatResponse
import com.findmykids.tracker.panda.model.response.ChatResponse
import com.findmykids.tracker.panda.model.response.CommonResponse
import com.findmykids.tracker.panda.model.response.ConnectedGuardianResponse
import com.findmykids.tracker.panda.model.response.LoginResponse
import com.findmykids.tracker.panda.model.response.MediaUploadResponse
import com.findmykids.tracker.panda.model.response.NotificationResponse
import com.findmykids.tracker.panda.model.response.PrivacyResponse
import com.findmykids.tracker.panda.model.response.UnReadNotificationResponse
import com.findmykids.tracker.panda.repository.Repository
import com.findmykids.tracker.panda.util.NetworkResult
import com.findmykids.tracker.panda.util.UtilJ
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repository: Repository, application: Application
) : AndroidViewModel(application) {
    val appUsageResponse: LiveData<Any?>
        get() = repository._appUsageResponse

    //sinUpResponse
    val _sinUpResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val sinUpResponse: LiveData<NetworkResult<CommonResponse>> = _sinUpResponse

    fun singUp(request: SignupRequest) = viewModelScope.launch {
        repository.singUp(request).collect() {
            _sinUpResponse.value = it
        }
    }

    //get Notification
    val _getNotificationResponse: MutableLiveData<NetworkResult<NotificationResponse>> =
        MutableLiveData()
    val getNotificationResponse: LiveData<NetworkResult<NotificationResponse>> =
        _getNotificationResponse

    fun getNotification(
        page: Int, limit: Int
    ) = viewModelScope.launch {
        repository.getNotification(page, limit).collect() {
            _getNotificationResponse.value = it
        }
    }

    //login
    val _loginResponse: MutableLiveData<NetworkResult<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<NetworkResult<LoginResponse>> = _loginResponse

    fun login(request: LoginRequest) = viewModelScope.launch {
        repository.login(request).collect() {
            _loginResponse.value = it
        }
    }

    //verifyOtp
    val _verifyResponse: MutableLiveData<NetworkResult<LoginResponse>> = MutableLiveData()
    val verifyResponse: LiveData<NetworkResult<LoginResponse>> = _verifyResponse

    fun verifyOtp(request: VerifyRequest) = viewModelScope.launch {
        repository.verifyOtp(request).collect() {
            _verifyResponse.value = it
        }
    }

    //resendOtp
    val _resendResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val resendResponse: LiveData<NetworkResult<CommonResponse>> = _resendResponse

    fun resendOtp(request: ResendRequest) = viewModelScope.launch {
        repository.resendOtp(request).collect() {
            _resendResponse.value = it
        }
    }

    //login
    val _forgotPasswordResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val forgotPasswordResponse: LiveData<NetworkResult<CommonResponse>> = _forgotPasswordResponse

    fun forgotPassword(request: ResendRequest) = viewModelScope.launch {
        repository.forgotPassword(request).collect() {
            _forgotPasswordResponse.value = it
        }
    }

    //forgotCheckOtp
    val _forgotCheckOtpResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val forgotCheckOtpResponse: LiveData<NetworkResult<CommonResponse>> = _forgotCheckOtpResponse

    fun forgotCheckOtp(request: VerifyRequest) = viewModelScope.launch {
        repository.forgotCheckOtp(request).collect() {
            _forgotCheckOtpResponse.value = it
        }
    }

    //resetPassword
    val _resetPasswordResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val resetPasswordResponse: LiveData<NetworkResult<CommonResponse>> = _resetPasswordResponse

    fun resetPassword(request: ResetPasswordRequest) = viewModelScope.launch {
        repository.resetPassword(request).collect() {
            _resetPasswordResponse.value = it
        }
    }

    //getUnReadCount
    val _getUnReadCountResponse: MutableLiveData<NetworkResult<UnReadNotificationResponse>> =
        MutableLiveData()
    val getUnReadCountResponse: LiveData<NetworkResult<UnReadNotificationResponse>> =
        _getUnReadCountResponse

    fun getUnReadCount() = viewModelScope.launch {
        repository.getUnReadCount().collect() {
            _getUnReadCountResponse.value = it
        }
    }

    //getProfile
    val _getProfileResponse: MutableLiveData<NetworkResult<LoginResponse>> = MutableLiveData()
    val getProfileResponse: LiveData<NetworkResult<LoginResponse>> = _getProfileResponse

    fun getProfile() = viewModelScope.launch {
        repository.getProfile().collect() {
            _getProfileResponse.value = it
        }
    }

    //sosSend
    val _sosSendResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val sosSendResponse: LiveData<NetworkResult<CommonResponse>> = _sosSendResponse

    fun sosSendResponse(request: SOSRequest) = viewModelScope.launch {
        repository.sosSend(request).collect() {
            _sosSendResponse.value = it
        }
    }

    //editProfile
    val _editProfileResponse: MutableLiveData<NetworkResult<LoginResponse>> = MutableLiveData()
    val editProfileResponse: LiveData<NetworkResult<LoginResponse>> = _editProfileResponse

    fun editProfile(request: EditProfileRequest) = viewModelScope.launch {
        repository.editProfile(request).collect() {
            _editProfileResponse.value = it
        }
    }

    //connectWithParent
    val _connectParentResponse: MutableLiveData<NetworkResult<LoginResponse>> = MutableLiveData()
    val connectParentResponse: LiveData<NetworkResult<LoginResponse>> = _connectParentResponse

    fun connectWithParent(request: ConnectParentRequest) = viewModelScope.launch {
        repository.connectWithParent(request).collect() {
            _connectParentResponse.value = it
        }
    }

    //Connected Guardian Info
    val _connectedGuardianResponse: MutableLiveData<NetworkResult<ConnectedGuardianResponse>> =
        MutableLiveData()
    val connectedGuardianResponse: LiveData<NetworkResult<ConnectedGuardianResponse>> =
        _connectedGuardianResponse

    fun connectedGuardian() = viewModelScope.launch {
        repository.connectedGuardian().collect() {
            _connectedGuardianResponse.value = it
        }
    }

    //Guardian Info
    val _guardianInfoResponse: MutableLiveData<NetworkResult<LoginResponse>> = MutableLiveData()
    val guardianInfoResponse: LiveData<NetworkResult<LoginResponse>> = _guardianInfoResponse

    fun guardianInfo(id: String) = viewModelScope.launch {
        repository.guardianInfo(id).collect() {
            _guardianInfoResponse.value = it
        }
    }

    //All Chat Detail
    val _allChatDetailResponse: MutableLiveData<NetworkResult<AllChatResponse>> = MutableLiveData()
    val allChatDetailResponse: LiveData<NetworkResult<AllChatResponse>> = _allChatDetailResponse

    fun allChatDetail() = viewModelScope.launch {
        repository.allChatDetail().collect() {
            _allChatDetailResponse.value = it
        }
    }

    //get Chat Message
    val _chatMessageResponse: MutableLiveData<NetworkResult<ChatResponse>> = MutableLiveData()
    val chatMessageResponse: LiveData<NetworkResult<ChatResponse>> = _chatMessageResponse

    fun getChatMessage(
        connectionId: String, page: Int, limit: Int
    ) = viewModelScope.launch {
        repository.getChatMessage(connectionId, page, limit).collect() {
            _chatMessageResponse.value = it
        }
    }

    //mediaUpload
    private val _mediaResponse: MutableLiveData<NetworkResult<MediaUploadResponse>> =
        MutableLiveData()
    val mediaResponse: LiveData<NetworkResult<MediaUploadResponse>> = _mediaResponse

    fun mediaUpload(imageType: String, file: File, imageOrVide: Int) = viewModelScope.launch {
        repository.mediaUpload(
            com.findmykids.tracker.panda.util.UtilJ.getRequestTextBody(imageType),
            com.findmykids.tracker.panda.util.UtilJ.getFileType(file, imageOrVide)
        ).collect() {
            _mediaResponse.value = it
        }
    }

    //logout
    private val _logoutResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val logoutResponse: LiveData<NetworkResult<CommonResponse>> = _logoutResponse

    fun logOutUser(request: RequestLogout) = viewModelScope.launch {
        repository.logOutUserCall(request).collect() {
            _logoutResponse.value = it
        }
    }

    //logout
    private val _unBlockResponse: MutableLiveData<NetworkResult<CommonResponse>> = MutableLiveData()
    val unBlockResponse: LiveData<NetworkResult<CommonResponse>> = _unBlockResponse

    fun unBlock(request: UnBlockRequest) = viewModelScope.launch {
        repository.unBlockCAll(request).collect() {
            _unBlockResponse.value = it
        }
    }

    //policy
    val _policyResponse: MutableLiveData<NetworkResult<PrivacyResponse>> = MutableLiveData()
    val policyResponse: LiveData<NetworkResult<PrivacyResponse>> = _policyResponse

    fun policy(type: String) = viewModelScope.launch {
        repository.getPolicyData(type).collect() {
            _policyResponse.value = it
        }
    }


    //Delete Account
    private val _deleteAccountResponse: MutableLiveData<NetworkResult<CommonResponse>> =
        MutableLiveData()
    val deleteAccountResponse: LiveData<NetworkResult<CommonResponse>> = _deleteAccountResponse

    fun deleteAccount(request: DeleteAccountRequest) = viewModelScope.launch {
        repository.deleteAccountCall(request).collect() {
            _deleteAccountResponse.value = it
        }
    }

    init {
        setUpRepository(repository)
    }

    companion object {
        lateinit var Companion_repository: Repository
        fun setUpRepository(repository: Repository) {
            this.Companion_repository = repository
        }
    }

}