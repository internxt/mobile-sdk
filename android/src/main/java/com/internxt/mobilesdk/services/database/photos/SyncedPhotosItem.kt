package com.internxt.mobilesdk.services.database.photos

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "remote_photos")
data class SyncedPhotosItem (
  @PrimaryKey() val id: String,
  val name: String,
  val type: String,
  val size: Long,
  val width: Int,
  val height: Int,
  val fileId: String,
  val previewId: String,
  val deviceId: String,
  val userId: String,
  val hash: String,
  val itemType: String,
  val takenAt: String,
  val createdAt: String,
  val updatedAt: String,
  val status: String,
  val networkBucketId: String,
  val duration: Long?,
)
