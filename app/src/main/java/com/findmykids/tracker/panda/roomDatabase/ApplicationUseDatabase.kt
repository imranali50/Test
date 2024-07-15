package com.findmykids.tracker.panda.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [AppInfoFilterModel::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(ApplicationUsesDataConverter::class)
abstract class ApplicationUseDatabase : RoomDatabase() {
    abstract fun getDownloadVideoDao(): ApplicationUsesDao

    companion object {
        private const val DB_NAME = "ApplicationUses.db"

        @Volatile
        private var instance: ApplicationUseDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ApplicationUseDatabase::class.java,
            DB_NAME
        ).build()
    }
}