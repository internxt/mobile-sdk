package com.internxt.mobilesdk.config

import com.internxt.mobilesdk.utils.ConfigValueMissingException

enum class MobileSdkConfigKey {
  DRIVE_API_URL,
  DRIVE_NEW_API_URL,
  BRIDGE_URL,
  PHOTOS_API_URL,
  PHOTOS_NETWORK_API_URL,
  MAGIC_IV,
  MAGIC_SALT,
  BRIDGE_AUTH_BASE64
}
object MobileSdkConfigLoader {
  private val config: HashMap<MobileSdkConfigKey, String> = HashMap()

  fun init(config: HashMap<String, String>) {
    MobileSdkConfigKey.values().forEach { it
      val key = it.name
      val configValue = config[key] ?: throw ConfigValueMissingException("$key is missing in the config provided")

      if(configValue !is String) {
        throw ConfigValueMissingException("Found config value for $key but is not an String type ")
      }
      setConfig(it, configValue as String)
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
  @Throws(ConfigValueMissingException::class)
  fun getConfig(key: MobileSdkConfigKey): String {
    return config[key] ?: throw ConfigValueMissingException("$key is missing on the MobileSDKConfig")
  }
}
