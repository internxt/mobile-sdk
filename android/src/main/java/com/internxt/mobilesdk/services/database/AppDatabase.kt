package com.internxt.mobilesdk.services.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.internxt.mobilesdk.services.database.photos.PhotosDBItem
import com.internxt.mobilesdk.services.database.photos.PhotosDao
import com.internxt.mobilesdk.services.database.photos.SyncedPhotosDao
import com.internxt.mobilesdk.services.database.photos.SyncedPhotosItem


@Database(entities = [PhotosDBItem::class, SyncedPhotosItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun photosDao(): PhotosDao?

  abstract fun syncedPhotosDao(): SyncedPhotosDao?
  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "internxt_app_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
