package com.internxt.mobilesdktests.services.crypto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.facebook.common.util.Hex;
import com.internxt.mobilesdk.services.crypto.Hash
import java.io.ByteArrayInputStream
import java.util.Arrays
import kotlin.test.assertFalse


internal class HashTest {
  private val SUT: Hash = Hash()

    @Test
    fun `Should create a SHA256 hash for "test" string correctly`() {
      // SHA256 "test" string in bytes
      val expectedValue = byteArrayOf(-97, -122, -48, -127, -120, 76, 125, 101, -102, 47, -22, -96, -59, 90, -48, 21, -93, -65, 79, 27, 43, 11, -126, 44, -47, 93, 108, 21, -80, -16, 10, 8)

      val result = SUT.sha256("test".toByteArray());
      assertTrue(expectedValue.contentEquals(result))
    }

  @Test
  fun `Should create a SHA256 hash from an input stream correctly`() {
    val expectedValue = Hex.decodeHex("8c208b7f67da26f09bedaed7b8f5b8c073131792ff8bf7fce9290ede8798c32d")

    val result = SUT.getHashFromStream(ByteArrayInputStream("thisIsAlongStringToCreateAHashFrom".toByteArray()), SUT.getSha256Hasher())
    assertTrue(expectedValue.contentEquals(result))
  }
    @Test
    fun `Should create a SHA512 hash for "test" string correctly`() {

      // SHA512 "test" string in bytes
      val expectedValue = byteArrayOf(-18, 38, -80, -35, 74, -9, -25, 73, -86, 26, -114, -29, -63, 10, -23, -110, 63, 97, -119, -128, 119, 46, 71, 63, -120, 25, -91, -44, -108, 14, 13, -78, 122, -63, -123, -8, -96, -31, -43, -8, 79, -120, -68, -120, 127, -42, 123, 20, 55, 50, -61, 4, -52, 95, -87, -83, -114, 111, 87, -11, 0, 40, -88, -1)

      val result = SUT.sha512("test".toByteArray());
      assertTrue(expectedValue.contentEquals(result))
    }


    @Test
    fun `Should create a ripemd160 hash for "test" string correctly`() {

      // RIPEMD160 "test" string in bytes
      val expectedValue = byteArrayOf(94, 82, -2, -28, 126, 107, 7, 5, 101, -9, 67, 114, 70, -116, -36, 105, -99, -24, -111, 7)

      val result = SUT.ripemd160("test".toByteArray());
      assertTrue(expectedValue.contentEquals(result))
    }
}
