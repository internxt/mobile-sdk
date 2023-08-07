package com.internxt.mobilesdk.services.photos

import com.internxt.mobilesdk.config.MobileSdkConfigKey
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import com.internxt.mobilesdk.data.photos.CreatePhotoPayload
import com.internxt.mobilesdk.data.photos.SyncedPhoto
import com.internxt.mobilesdk.services.network.HttpClient
import com.internxt.mobilesdk.utils.JsonUtils
import com.internxt.mobilesdk.utils.Logger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PhotosApi {
  fun createPhoto(photoPayload: CreatePhotoPayload): SyncedPhoto? {
    val createPhotoPayloadAdapter = JsonUtils.moshi.adapter(CreatePhotoPayload::class.java)

    val url = getUrl("/photo/exists")
    val payload = createPhotoPayloadAdapter.toJson(photoPayload)
    Logger.info("Creating photo with payload: $payload")
    val request = Request.Builder().header("Authorization", getAuthTokenForHeader())
      .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType())).url(url).build()
    val response = HttpClient.fetchSync(request)

    val syncedPhotoAdapter = JsonUtils.moshi.adapter(SyncedPhoto::class.java)

    return syncedPhotoAdapter.fromJson(response.body!!.string())
  }

  private fun getUrl(path: String): String {
    val base = MobileSdkConfigLoader.getConfig(MobileSdkConfigKey.PHOTOS_API_URL)
    return "$base/photos$path"
  }

  private fun getAuthTokenForHeader(): String {
    if(MobileSdkConfigLoader.authTokens == null) throw Exception("Auth token not found")

    return "Bearer ${MobileSdkConfigLoader.authTokens!!.newToken}"
  }
}
