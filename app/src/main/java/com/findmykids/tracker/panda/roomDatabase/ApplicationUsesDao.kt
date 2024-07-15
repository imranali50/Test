package com.findmykids.tracker.panda.roomDatabase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface ApplicationUsesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //if some data is same/conflict, it'll be replace with new data.
    suspend fun insertVideoData(downloadVideoModal: AppInfoFilterModel)

    @Update
    suspend fun updateVideoData(downloadVideoModal: AppInfoFilterModel)

    @Delete
    suspend fun deleteVideoData(downloadVideoModal: AppInfoFilterModel)

    @Query("SELECT * FROM applicationTable")
     fun getAllVideoData(): MutableList<AppInfoFilterModel>
    // why not use suspend ? because Room does not support LiveData with suspended functions.
    // LiveData already works on a background thread and should be used directly without using coroutines

    @Query("DELETE FROM applicationTable")
    suspend fun clearVideoData()

    @Query("DELETE FROM applicationTable WHERE appPkgName = :jobId") //you can use this too, for delete note by id.
    suspend fun deleteVideoDataById(jobId: String)

//    @Query("SELECT * FROM applicationTable")
//    fun isDbEmpty(): LiveData<Int?>?
}