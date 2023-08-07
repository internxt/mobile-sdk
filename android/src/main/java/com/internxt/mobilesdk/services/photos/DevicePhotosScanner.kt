package com.internxt.mobilesdk.services.photos

import android.content.ContentUris
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import com.facebook.react.bridge.ReactApplicationContext
import com.internxt.mobilesdk.data.photos.DevicePhotosItem
import com.internxt.mobilesdk.data.photos.DevicePhotosItemType
import com.internxt.mobilesdk.services.database.AppDatabase
import com.internxt.mobilesdk.services.database.photos.PhotosDBItem
import com.internxt.mobilesdk.utils.Logger
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId


class DevicePhotosScanner(private val context: ReactApplicationContext) {
  private val appDatabase = AppDatabase.getInstance(context)

  fun getDevicePhotos(): Array<DevicePhotosItem> {
    val devicePhotos = arrayListOf<DevicePhotosItem>()
    val contentResolver = context.contentResolver

    // Get all photos in external content
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    // Order them by taken at date
    val orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC"

    val projection: Array<String> = arrayOf<String>(
      MediaStore.Images.Media.DISPLAY_NAME,
      MediaStore.Images.Media.DATE_TAKEN,
      MediaStore.Images.Media._ID,
      MediaStore.Images.Media.SIZE
    )


    Logger.info("Using projection: ${projection.contentToString()}")
    val cursor = contentResolver.query(uri, projection, null, null, orderBy)
      ?: throw Exception("Unable to get cursor from content resolver")

    cursor.use {

      val displayNameColumn: Int = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
      val takenAtColumn: Int = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
      val idColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)
      while(it.moveToNext()) {
        Logger.info("Getting data for DevicePhoto")
        val displayName = it.getString(displayNameColumn)
        val takenAt = it.getString(takenAtColumn)
        val imageUri = ContentUris
          .withAppendedId(
            Media.EXTERNAL_CONTENT_URI,
            cursor.getInt(idColumn).toLong()
          )
        if(displayName !== null && takenAt !== null && imageUri !== null) {

          val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(takenAt.toLong()), ZoneId.systemDefault())
          val offsetDate = OffsetDateTime.of(localDateTime, OffsetDateTime.now().offset)

          appDatabase.photosDao()?.updateOrCreatePhotosItem(
            PhotosDBItem(
              name = displayName,
              photoId = null,
              status = "DEVICE_ONLY",
              takenAt = offsetDate.toInstant().toEpochMilli()
            )

          )
          val devicePhotosItem = DevicePhotosItem(displayName,
            offsetDate,
            imageUri,
            type = DevicePhotosItemType.IMAGE
          )
          devicePhotos.add(devicePhotosItem)
        } else {
          Logger.info("Missing values for device photo, ignoring it: displayName => $displayName, takenAt => $takenAt")
        }
      }
    }


    return devicePhotos.toTypedArray()
  }

  fun getDeviceVideos(): Array<DevicePhotosItem> {
    val devicePhotos = arrayListOf<DevicePhotosItem>()
    val contentResolver = context.contentResolver

    // Get all photos in external content
    val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    // Order them by taken at date
    val orderBy = MediaStore.Video.Media.DATE_TAKEN + " DESC"

    val projection: Array<String> = arrayOf<String>(
      MediaStore.Video.Media.DISPLAY_NAME,
      MediaStore.Video.Media.DATE_TAKEN,
      MediaStore.Video.Media._ID,
    )


    Logger.info("Using projection: ${projection.contentToString()}")
    val cursor = contentResolver.query(uri, projection, null, null, orderBy)
      ?: throw Exception("Unable to get cursor from content resolver")

    cursor.use {

      val displayNameColumn: Int = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
      val takenAtColumn: Int = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
      val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
      while(it.moveToNext()) {
        Logger.info("Getting data for DeviceVideo")
        val displayName = it.getString(displayNameColumn)
        val takenAt = it.getString(takenAtColumn)
        val imageUri = ContentUris
          .withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            cursor.getInt(idColumn).toLong()
          )
        if(displayName !== null && takenAt !== null && imageUri !== null) {

          val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(takenAt.toLong()), ZoneId.systemDefault())
          val offsetDate = OffsetDateTime.of(localDateTime, OffsetDateTime.now().offset)

          appDatabase.photosDao()?.updateOrCreatePhotosItem(
            PhotosDBItem(
              name = displayName,
              photoId = null,
              status = "DEVICE_ONLY",
              takenAt = offsetDate.toInstant().toEpochMilli()
            )

          )
          val devicePhotosItem = DevicePhotosItem(displayName,
            offsetDate,
            imageUri,
            type = DevicePhotosItemType.VIDEO
          )
          devicePhotos.add(devicePhotosItem)
        } else {
          Logger.info("Missing values for device photo, ignoring it: displayName => $displayName, takenAt => $takenAt")
        }
      }
    }


    return devicePhotos.toTypedArray()
  }


}
