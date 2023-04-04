package com.internxt.mobilesdk.utils

import android.util.Log

object Logger {
  private val TAG = "[INTERNXT_MOBILE_SDK]"

  fun debug(message: String) {
    Log.d(TAG, message)
  }
  fun info(message: String) {
    Log.i(TAG, message)
  }

  fun error(message: Throwable) {
    Log.e(TAG,message.toString())
  }
  fun error(message: String) {
    Log.e(TAG,message)
  }

  fun warning(message: String) {
    Log.w(TAG,message)
  }
}
