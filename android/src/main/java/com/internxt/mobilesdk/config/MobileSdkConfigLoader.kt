package com.internxt.mobilesdk.config

import com.internxt.mobilesdk.utils.ConfigValueMissing

enum class MobileSdkConfigKey {
  DRIVE_API_URL,
  DRIVE_NEW_API_URL,
  BRIDGE_URL,
  PHOTOS_API_URL,
  PHOTOS_NETWORK_API_URL
}
object MobileSdkConfigLoader {
  private val config: HashMap<MobileSdkConfigKey, String> = HashMap()

  fun init(config: HashMap<String, String>) {
    MobileSdkConfigKey.values().forEach { it
      val key = it.name
      val configValue = config[key] ?: throw ConfigValueMissing("$key is missing in the config provided")

      setConfig(it, configValue)
    }
  }
  /**
   * Sets a config value
   */
  private fun setConfig(key: MobileSdkConfigKey, value: String ) {
    config[key] = value
  }

  /**
   * Get a config value or throws an error if the value is missing
   */
  @Throws(ConfigValueMissing::class)
  fun getConfig(key: MobileSdkConfigKey): String {
    return config[key] ?: throw ConfigValueMissing("$key is missing on the MobileSDKConfig")
  }
}
