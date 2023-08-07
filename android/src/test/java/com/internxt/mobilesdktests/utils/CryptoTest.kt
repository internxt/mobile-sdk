package com.internxt.mobilesdktests.utils

import com.facebook.common.util.Hex
import com.internxt.mobilesdk.utils.CryptoUtils
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class CryptoUtilsTest {

  private val SUT = CryptoUtils

  @Test
  fun `Should list available providers`() {
    // This is just to print the available algorithms in your JVM, is not asserting anything
    // and is not a test at all
    val available = SUT.getAvailableAlgorithms()

    println(available)

    assertTrue(available.isNotEmpty())
  }

  @Test
  fun `Should generate a bucket key correctly`() {
    val expected = Hex.decodeHex("2afacb1df30708c6c705c7acb0222c6db803086b3b47c65ca2785ba36a07399f8a28d645084ce87a64eeaec25fdcebf07236f02a9de38df3729b4ee57caa9428")
    val mnemonic = "essence renew fish any airport nature tape gallery tobacco inside there enlist hub bring meat wing crack review logic open husband excite bag reflect"
    val bucketId = "e8f6c43b49d72e21aa6094f0"
    val bucketKey = SUT.generateBucketKey(mnemonic, bucketId)

    assertTrue(expected.contentEquals(bucketKey))
  }

  @Test
  fun `Should generate a deterministic key correctly`() {
    val expected = Hex.decodeHex("cfd67e51df9354be8fbc8a3552674f1a2ed2ec8dd7ba2d93621a2ff3ee4862bffdc7d921469232ac4f00c0d605f7379e2b343466413e79f4bdaef3d2713fe525")
    val deterministicKey = SUT.getDeterministicKey("imthekey".toByteArray(), "imthedata".toByteArray())

    assertTrue(expected.contentEquals(deterministicKey))
  }
}
