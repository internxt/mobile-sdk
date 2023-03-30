package com.internxt.mobilesdk.utils

import java.security.Security

class CryptoUtils {
  public fun getAvailableAlgorithms(): List<String> {
    var providers = Security.getProviders()

    var availableAlgorithms = mutableListOf<String>()
    providers.forEach {it
      it
    }

    for (provider in Security.getProviders()) {
      for (key in provider.stringPropertyNames()) availableAlgorithms.add(provider.getProperty(key))
    }

    return availableAlgorithms
  }
}
