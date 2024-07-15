package com.findmykids.tracker.panda.roomDatabase

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "applicationTable")
class AppInfoFilterModel() {
    @PrimaryKey(autoGenerate = false)
    var appPkgName: String = ""

    @ColumnInfo("applicationData")
    var applicationData: AppInfoFilter? = null

    constructor(appPkgName: String, downloadData: AppInfoFilter) : this() {
        this.appPkgName = appPkgName
        this.applicationData = downloadData
    }

    override fun toString(): String {
        return "AppInfoFilterModel(appPkgName='$appPkgName', applicationData=$applicationData)"
    }

}

data class AppInfoFilter(
    @SerializedName("appName") var appName: String,
    @SerializedName("appPkgName") var appPkgName: String,
    @SerializedName("appIcon") var appIcon: Drawable?,
    @SerializedName("today") var today: Long
//    @SerializedName("yesterday") var yesterDay: Long,
//    @SerializedName("weekly") var weekly: Long,
//    @SerializedName("monthly") var monthly: Long,
)