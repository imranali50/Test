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
import com.findmykids.tracker.panda.model.response.AllChatResponse
import com.findmykids.tracker.panda.model.response.ChatResponse
import com.findmykids.tracker.panda.model.response.CommonResponse
import com.findmykids.tracker.panda.model.response.ConnectedGuardianResponse
import com.findmykids.tracker.panda.model.response.LoginResponse
import com.findmykids.tracker.panda.model.response.MediaUploadResponse
import com.findmykids.tracker.panda.model.response.NotificationResponse
import com.findmykids.tracker.panda.model.response.PrivacyResponse
import com.findmykids.tracker.panda.model.response.UnReadNotificationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface APIInterface {
    //Auth
    @POST("auth/register")
    suspend fun signup(@Body request: SignupRequest): Response<CommonResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyRequest): Response<LoginResponse>

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendRequest): Response<CommonResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ResendRequest): Response<CommonResponse>

    @POST("auth/forgot-password/verify-otp")
    suspend fun forgotCheckOtp(@Body request: VerifyRequest): Response<CommonResponse>

    @POST("auth/update-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<CommonResponse>

    @POST("user/edit-profile")
    suspend fun editProfile(@Body request: EditProfileRequest): Response<LoginResponse>

    @POST("child/connection/connect-with-parent")
    suspend fun connectWithParent(@Body request: ConnectParentRequest): Response<LoginResponse>

    @GET("child/connection/parents")
    suspend fun connectedGuardian(): Response<ConnectedGuardianResponse>

    @GET("child/connection/parent/{id}")
    suspend fun guardianInfo(@Path("id") id: String): Response<LoginResponse>

    //sos
    @POST("child/sos/send")
    suspend fun sosSend(@Body request: SOSRequest): Response<CommonResponse>

    @GET("child/chat/list")
    suspend fun allChatDetail(): Response<AllChatResponse>

    @GET("policy")
    suspend fun getPolicy(@Query("type") type: String): Response<PrivacyResponse>

    @GET("notification")
    suspend fun getNotificationList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<NotificationResponse>

    @GET("child/chat/get-messages/{connectionId}")
    suspend fun getChatMessage(
        @Path("connectionId") connectionId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ChatResponse>

    @POST("user/logout")
    suspend fun logOut(@Body request: RequestLogout): Response<CommonResponse>

    @GET("notification/unreadCount")
    suspend fun getUnReadCount(): Response<UnReadNotificationResponse>

    @POST("child/app-block/request-unblock")
    suspend fun unblockApp(@Body request: UnBlockRequest): Response<CommonResponse>

     @POST("user/delete")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): Response<CommonResponse>

    @GET("user/profile")
    suspend fun getProfile(): Response<LoginResponse>

    //image
    @Multipart
    @POST("upload")
    suspend fun mediaUpload(
        @Part("uploadType") fullName: RequestBody?, @Part file: MultipartBody.Part
    ): Response<MediaUploadResponse>
}