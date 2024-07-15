package com.findmykids.tracker.panda.model.response

import android.util.Log
import com.findmykids.tracker.panda.util.NetworkResult
import retrofit2.Response
import java.io.IOException

abstract class BaseApiResponse {

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return NetworkResult.Success(body)
                }
            }
            Log.e("TAG", "safeApiCall: >>>>>>>>>>>>>> Api call failed Error ${response.code()} ${response.message()}")
            return error("${response.code()} ${response.message()}")
        }catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> return NetworkResult.NoNetwork()
            }
            return error(throwable.message ?: throwable.toString())
        }
       /* catch (e: Exception) {
            Log.e("TAG", "safeApiCall: >>>>>>>>>>>>>> Api call failed Error Exception ${e.message}")
            return error(e.message ?: e.toString())
        }*/
    }

    private fun <T> error(errorMessage: String): NetworkResult<T> =
        NetworkResult.Error("Api call failed $errorMessage")

}