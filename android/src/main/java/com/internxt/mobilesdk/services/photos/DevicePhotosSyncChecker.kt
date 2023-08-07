package com.internxt.mobilesdk.services.photos

import androidx.room.Room
import com.facebook.react.bridge.ReactApplicationContext
import com.internxt.mobilesdk.data.photos.DevicePhotosItem
import com.internxt.mobilesdk.services.database.AppDatabase
import com.internxt.mobilesdk.utils.Logger
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DevicePhotosSyncChecker(private val context: ReactApplicationContext) {
  var appDatabase: AppDatabase = AppDatabase.getInstance(context)

  fun isSynced(devicePhoto: DevicePhotosItem): Boolean {
    val syncedPhotosItem = appDatabase.syncedPhotosDao()?.getSyncedPhotosItemByName(devicePhoto.displayName) ?: return false

    val takenAt = OffsetDateTime.parse(syncedPhotosItem.takenAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    if (takenAt.toInstant().toEpochMilli() !== devicePhoto.takenAt.toInstant().toEpochMilli()) {
      return false
    }

    return true
  }
}
