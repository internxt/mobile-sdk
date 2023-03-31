package com.internxt.mobilesdk.utils

import org.junit.jupiter.api.Test

internal class CryptoUtilsTest {

  private val SUT: CryptoUtils = CryptoUtils()

  @Test
  fun `Should list available providers`() {
    SUT.getAvailableAlgorithms()
  }
}
