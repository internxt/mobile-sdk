package com.internxt.mobilesdk.services.network

import com.internxt.mobilesdk.utils.ResultCallback
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType

import java.io.IOException

val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

object HttpClient {
  private val client = OkHttpClient()

  @Throws(IOException::class)
  fun fetchSync(request: Request ): Response {
    return client.newCall(request).execute()
  }
}


