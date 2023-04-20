package com.internxt.mobilesdk.data.photos

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoPreview(
  @Json()
  val width: Int,
  @Json()
  val height: Int,
  @Json()
  val size: Long,
  @Json()
  val fileId: String,
  @Json()
  val type: String
)

@JsonClass(generateAdapter = true)
data class CreatePhotoPayload(
  @Json()
  val name: String,
  @Json()
  val type: String,
  @Json()
  val size: Long,
  @Json()
  val width: Int,
  @Json()
  val height: Int,
  @Json()
  val fileId: String,
  @Json()
  val previewId: String,
  @Json()
  val previews: List<PhotoPreview>,
  @Json()
  val deviceId: String,
  @Json()
  val userId: String,
  @Json()
  val hash: String,
  @Json()
  val itemType: String,
  @Json()
  val takenAt: String,
  @Json()
  val networkBucketId: String,
)


