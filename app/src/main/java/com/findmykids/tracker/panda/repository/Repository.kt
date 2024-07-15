package com.findmykids.tracker.panda.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.findmykids.tracker.panda.roomDatabase.AppInfoFilter
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
import com.findmykids.tracker.panda.model.response.BaseApiResponse
import com.findmykids.tracker.panda.model.response.ChatResponse
import com.findmykids.tracker.panda.model.response.CommonResponse
import com.findmykids.tracker.panda.model.response.ConnectedGuardianResponse
import com.findmykids.tracker.panda.model.response.LoginResponse
import com.findmykids.tracker.panda.model.response.MediaUploadResponse
import com.findmykids.tracker.panda.model.response.NotificationResponse
import com.findmykids.tracker.panda.model.response.PrivacyResponse
import com.findmykids.tracker.panda.model.response.UnReadNotificationResponse
import com.findmykids.tracker.panda.remote.RemoteDataSource
import com.findmykids.tracker.panda.util.NetworkResult
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@ActivityRetainedScoped
class Repository @Inject constructor(private val remoteDataSource: RemoteDataSource) :
    BaseApiResponse() {
    //Auth
    suspend fun singUp(request: SignupRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.sinUp(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun login(request: LoginRequest): Flow<NetworkResult<LoginResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.login(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun verifyOtp(request: VerifyRequest): Flow<NetworkResult<LoginResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.verifyOtp(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun resendOtp(request: ResendRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.resendOtp(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun forgotPassword(request: ResendRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.forgotPassword(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun forgotCheckOtp(request: VerifyRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.forgotCheckOtp(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.resetPassword(request) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun sosSend(request: SOSRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.sosSend(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getUnReadCount(): Flow<NetworkResult<UnReadNotificationResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.getUnReadCount() })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getProfile(): Flow<NetworkResult<LoginResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.getProfile() })
        }.flowOn(Dispatchers.IO)
    }


    suspend fun connectWithParent(request: ConnectParentRequest): Flow<NetworkResult<LoginResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.connectWithParent(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun editProfile(request: EditProfileRequest): Flow<NetworkResult<LoginResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.editProfile(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun connectedGuardian(): Flow<NetworkResult<ConnectedGuardianResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.connectedGuardian() })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun guardianInfo(id: String): Flow<NetworkResult<LoginResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.guardianInfo(id) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun allChatDetail(): Flow<NetworkResult<AllChatResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.allChatDetail() })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getChatMessage(
        connectionId: String,
        page: Int,
        limit: Int
    ): Flow<NetworkResult<ChatResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.getChatMessage(connectionId, page, limit) })
        }.flowOn(Dispatchers.IO)
    }

    val _appUsageResponse: LiveData<Any?>
        get() = appUsageResponseLiveData

    companion object {
        private var appUsageResponseLiveData = MutableLiveData<Any?>()
        fun appUsageUpdate(appList: MutableList<AppInfoFilter>) {
            appUsageResponseLiveData.postValue(appList)
        }
    }

    suspend fun getNotification(
        page: Int,
        limit: Int
    ): Flow<NetworkResult<NotificationResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.getNotificationList( page, limit) })
        }.flowOn(Dispatchers.IO)
    }

    fun logOutUserCall(request: RequestLogout): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.logOut(request) })
        }.flowOn(Dispatchers.IO)
    }
    fun unBlockCAll(request: UnBlockRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.unBlock(request) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPolicyData(type :String): Flow<NetworkResult<PrivacyResponse>>{
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.getPolicy(type) })
        }.flowOn(Dispatchers.IO)
    }

    fun deleteAccountCall(request: DeleteAccountRequest): Flow<NetworkResult<CommonResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.deleteAccount(request) })
        }.flowOn(Dispatchers.IO)
    }


    //image
    suspend fun mediaUpload(
        request: RequestBody, file: MultipartBody.Part
    ): Flow<NetworkResult<MediaUploadResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            emit(safeApiCall { remoteDataSource.mediaUpload(request, file) })
        }.flowOn(Dispatchers.IO)
    }


}