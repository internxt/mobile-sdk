package com.internxt.mobilesdk.services.database.photos

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "photos")
data class PhotosDBItem (
  @PrimaryKey() val name: String,
  val photoId: String?,
  // DEVICE_ONLY | SYNCED | REMOTE_ONLY
  val status: String?,
  // ISO string
  val takenAt: Long
)
