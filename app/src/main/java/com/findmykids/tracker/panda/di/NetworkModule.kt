package com.findmykids.tracker.panda.di

import android.util.Log
import com.findmykids.tracker.panda.BuildConfig
import com.findmykids.tracker.panda.remote.APIInterface
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    lateinit var preferenceManager: PreferenceManager

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .addInterceptor(provideHttpLoggingInterceptor())
            .addInterceptor(provideTokenInterceptor())
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideTokenInterceptor(): ServiceInterceptor {
        return ServiceInterceptor()
    }

    class ServiceInterceptor : Interceptor {
        private var token = ""
        override fun intercept(chain: Interceptor.Chain): Response {
//            preferenceManager = PreferenceManager(MyApplication.instance.baseContext)
//            token = preferenceManager.getString(Const.token).toString()
            token = Const.tokenForApi
            Log.e("TAG", "intercept: " + token)
            var request = chain.request()
            if (request.header("No-Authentication") == null) {
                if (!token.isNullOrEmpty()) {
                    val finalToken = "Bearer $token"
                    Log.e("TAG", "intercept: >>>>>>>>>>>>>>>>>> $token")

                    request = request.newBuilder()
                        .addHeader("Authorization", finalToken)
                        .build()
                }
            }
            return chain.proceed(request)
        }
    }

    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.okBrother)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()

    }

    @Singleton
    @Provides
    fun provideCurrencyService(retrofit: Retrofit): APIInterface =
        retrofit.create(APIInterface::class.java)

}