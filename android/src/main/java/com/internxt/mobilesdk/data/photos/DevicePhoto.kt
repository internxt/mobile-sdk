package com.internxt.mobilesdk.data.photos


import android.net.Uri
import java.io.File
import java.time.OffsetDateTime

enum class DevicePhotosItemType {
  IMAGE,
  VIDEO
}
data class DevicePhotosItem(
  val displayName: String,
  val takenAt: OffsetDateTime,
  val uri: Uri,
  val type: DevicePhotosItemType
)

data class DisplayablePhotosItem(
  val name: String,
  val type: DevicePhotosItemType,
  val takenAt: OffsetDateTime,
  val updatedAt: OffsetDateTime,
  val size: Long,
  val width: Int,
  val height: Int
)
