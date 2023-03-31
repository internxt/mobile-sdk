package com.internxt.mobilesdk

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.internxt.mobilesdk.config.MobileSdkConfigKey
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import java.security.Security

class MobileSdkModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Initialize the Mobile SDK from JS side when the app starts
  @ReactMethod
  fun init(config: ReadableMap) {
    // Convert ReadableMap to HashMap, we should get an util for this
  }

  companion object {
    const val NAME = "MobileSdk"
  }
}
