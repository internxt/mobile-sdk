package com.internxt.mobilesdktests.utils

import com.internxt.mobilesdk.utils.CryptoUtils
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class CryptoUtilsTest {

  private val SUT: CryptoUtils = CryptoUtils()

  @Test
  fun `Should list available providers`() {
    // This is just to print the available algorithms in your JVM, is not asserting anything
    // and is not a test at all
    val available = SUT.getAvailableAlgorithms()

    println(available)

    assertTrue(available.isNotEmpty())
  }
}
