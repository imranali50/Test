package com.findmykids.tracker.panda.remote


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
import com.findmykids.tracker.panda.model.response.CommonResponse
import com.findmykids.tracker.panda.model.response.MediaUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiInterface: APIInterface) {

    //Auth
    suspend fun sinUp(signupRequest: SignupRequest) = apiInterface.signup(signupRequest)
    suspend fun login(request: LoginRequest) = apiInterface.login(request)
    suspend fun verifyOtp(request: VerifyRequest) = apiInterface.verifyOtp(request)
    suspend fun resendOtp(request: ResendRequest) = apiInterface.resendOtp(request)
    suspend fun forgotPassword(request: ResendRequest) = apiInterface.forgotPassword(request)
    suspend fun forgotCheckOtp(request: VerifyRequest) = apiInterface.forgotCheckOtp(request)
    suspend fun resetPassword(request: ResetPasswordRequest) = apiInterface.resetPassword(request)
    suspend fun sosSend(request: SOSRequest) = apiInterface.sosSend(request)
    suspend fun editProfile(request: EditProfileRequest) = apiInterface.editProfile(request)

    suspend fun getUnReadCount() = apiInterface.getUnReadCount()
    suspend fun getProfile() = apiInterface.getProfile()

    suspend fun connectWithParent(request: ConnectParentRequest) =
        apiInterface.connectWithParent(request)

    suspend fun connectedGuardian() = apiInterface.connectedGuardian()
    suspend fun guardianInfo(id : String) = apiInterface.guardianInfo(id)

    suspend fun allChatDetail() = apiInterface.allChatDetail()
    suspend fun logOut(request: RequestLogout): Response<CommonResponse> =
        apiInterface.logOut(request)

  suspend fun unBlock(request: UnBlockRequest): Response<CommonResponse> =
        apiInterface.unblockApp(request)

    suspend fun getPolicy(type: String) = apiInterface.getPolicy(type)

    suspend fun deleteAccount(request: DeleteAccountRequest): Response<CommonResponse> =
        apiInterface.deleteAccount(request)

    suspend fun getChatMessage(
        connectionId: String,
        page: Int,
        limit: Int
    ) = apiInterface.getChatMessage(connectionId, page, limit)

    //image
    suspend fun mediaUpload(
        request: RequestBody,
        file: MultipartBody.Part
    ): Response<MediaUploadResponse> =
        apiInterface.mediaUpload(request, file)

    suspend fun getNotificationList(
        page: Int, limit: Int
    ) = apiInterface.getNotificationList(page, limit)


}