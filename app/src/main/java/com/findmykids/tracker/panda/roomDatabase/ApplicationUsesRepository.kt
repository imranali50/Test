package com.findmykids.tracker.panda.roomDatabase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ApplicationUsesRepository(
    private val downloadVideoDatabase: ApplicationUseDatabase
) {
    private val getLiveVideoData = MutableLiveData<MutableList<AppInfoFilterModel>>()
    val getAllVideoData: LiveData<MutableList<AppInfoFilterModel>>/**/
        get() = getLiveVideoData

    suspend fun insertVideoData(downloadVideoModel: AppInfoFilterModel) =
        downloadVideoDatabase.getDownloadVideoDao().insertVideoData(downloadVideoModel)

    suspend fun updateVideoData(downloadVideoModel: AppInfoFilterModel) = downloadVideoDatabase.getDownloadVideoDao().updateVideoData(downloadVideoModel)

    suspend fun deleteVideoData(downloadVideoModel: AppInfoFilterModel) = downloadVideoDatabase.getDownloadVideoDao().deleteVideoData(downloadVideoModel)

    suspend fun deleteVideoDataById(jobId: String) = downloadVideoDatabase.getDownloadVideoDao().deleteVideoDataById(jobId)

    suspend fun clearVideoData() = downloadVideoDatabase.getDownloadVideoDao().clearVideoData()

    fun getAllVideoData() =
        getLiveVideoData.postValue(downloadVideoDatabase.getDownloadVideoDao().getAllVideoData())
}