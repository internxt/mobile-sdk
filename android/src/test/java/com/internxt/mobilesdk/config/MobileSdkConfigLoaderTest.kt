package com.internxt.mobilesdk.config

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.internxt.mobilesdk.utils.ConfigValueMissing
import junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MobileSdkConfigLoaderTest {

  @Test
  fun `Should throw if a value is missing in the initial config`() {
    val config = HashMap<String, String>()

    assertThrows<ConfigValueMissing> {
      MobileSdkConfigLoader.init(config)
    }
  }

  @Test
  fun `Should throw if you try to access a missing value`() {
    assertThrows<ConfigValueMissing> {
      MobileSdkConfigLoader.getConfig(MobileSdkConfigKey.BRIDGE_URL)
    }
  }

  @Test
  fun `Should load values from a HashMap`() {
    val config = HashMap<String, String>()
    val bridgeUrl = "http://BRIDGE_URL"
    val driveNewApiUrl = "http://DRIVE_NEW_API_URL"
    MobileSdkConfigKey.values().forEach { it
      val name = it.name
      // Create fake config values to populate the config
      config[name] = "http://$name";
    }

    MobileSdkConfigLoader.init(config)

    assertTrue(MobileSdkConfigLoader.getConfig(MobileSdkConfigKey.BRIDGE_URL).contentEquals(bridgeUrl))
    assertTrue(MobileSdkConfigLoader.getConfig(MobileSdkConfigKey.DRIVE_NEW_API_URL).contentEquals(driveNewApiUrl))
  }
}
