package com.internxt.mobilesdk.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FinishUploadPayload(
  @Json()
  val index: String,
  @Json()
  val shards: List<NetworkUploadShard>
)

@JsonClass(generateAdapter = true)
data class NetworkUploadShard(
  @Json()
  val uuid: String,
  @Json()
  val hash: String,
)

@JsonClass(generateAdapter = true)
data class StartUploadPayload(
  @Json()
  val uploads: List<NetworkUploadPayload>
)

@JsonClass(generateAdapter = true)
data class NetworkUploadPayload(
  @Json()
  val index: Int,
  @Json()
  val size: Long
)

@JsonClass(generateAdapter = true)
data class StartUploadResponse(
  val uploads: List<NetworkUpload>
)
@JsonClass(generateAdapter = true)
data class NetworkUpload(
  val index: Int,
  val uuid: String,
  val url: String? = null,
  val urls: List<String>? = null,
  val UploadId: String? = byteArrayOf(0).contentToString()
)

@JsonClass(generateAdapter = true)
data class FinishUploadResponse(
  val id: String,
  val bucket: String,
)

@JsonClass(generateAdapter = true)
data class UploadFailedResponse(
  val error: String,
)





