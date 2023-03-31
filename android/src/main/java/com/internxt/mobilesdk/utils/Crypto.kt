package com.internxt.mobilesdk.utils

import java.security.Security

class CryptoUtils {
  public fun getAvailableAlgorithms() {
    var providers = Security.getProviders()

    providers.forEach {it
      println(it.name)
    }
  }
}
