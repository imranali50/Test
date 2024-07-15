package com.findmykids.tracker.panda.roomDatabase

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.findmykids.tracker.panda.roomDatabase.AppInfoFilterModel

class ApplicationUsesViewModel(
    private val repository: ApplicationUsesRepository
) : ViewModel() {

    val getAllVideoData: LiveData<MutableList<AppInfoFilterModel>>
        get() = repository.getAllVideoData

    suspend fun insertVideoData(downloadVideoModal : AppInfoFilterModel) = repository.insertVideoData(downloadVideoModal)

    suspend fun updateVideoData(downloadVideoModal : AppInfoFilterModel) = repository.updateVideoData(downloadVideoModal)

    suspend fun deleteVideoData(downloadVideoModal : AppInfoFilterModel) = repository.deleteVideoData(downloadVideoModal)

    suspend fun deleteVideoDataById(jobId: String) = repository.deleteVideoDataById(jobId)

    suspend fun clearVideoData() = repository.clearVideoData()

    fun getAllVideoData() = repository.getAllVideoData()
}