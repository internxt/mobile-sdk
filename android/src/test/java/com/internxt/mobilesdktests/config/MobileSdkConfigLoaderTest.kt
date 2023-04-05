package com.internxt.mobilesdktests.config

import com.internxt.mobilesdk.config.MobileSdkConfigKey
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import com.internxt.mobilesdk.utils.ConfigValueMissingException
import junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MobileSdkConfigLoaderTest {

  @Test
  fun `Should throw if a value is missing in the initial config`() {
    val config = HashMap<String, String>()

    assertThrows<ConfigValueMissingException> {
      MobileSdkConfigLoader.init(config)
    }
  }

  @Test
  fun `Should throw if you try to access a missing value`() {
    assertThrows<ConfigValueMissingException> {
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
