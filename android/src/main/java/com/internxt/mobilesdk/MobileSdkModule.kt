package com.internxt.mobilesdk

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import java.security.Security

class MobileSdkModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Initialize the Mobile SDK from JS side when the app starts
  @ReactMethod
  fun init() {
    Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
  }

  companion object {
    const val NAME = "MobileSdk"
  }
}
