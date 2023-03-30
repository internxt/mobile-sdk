package com.internxt.mobilesdktests.core

import com.facebook.common.util.Hex
import com.internxt.mobilesdk.core.Encrypt
import com.internxt.mobilesdk.core.EncryptConfig
import com.internxt.mobilesdk.core.EncryptMode
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertTrue

class EncryptTest {
  val SUT = Encrypt()
  @Test
  fun `Should encrypt an input stream to an output stream using AES256 CTR NOPadding`() {
    val input = ByteArrayInputStream("test".toByteArray(Charsets.UTF_8))
    val output = ByteArrayOutputStream()
    SUT.encrypt(input, output, EncryptConfig(
      mode = EncryptMode.AesCTRNoPadding,
      key = Hex.decodeHex("50489ff30c51d5c400161ea442a8a12d2f936df6653a9caaafb36c27dec184d4"),
      iv = Hex.decodeHex("a879556ac137bfda0cbc48c60a005e05")
    )
    )

    assertTrue(output.toByteArray().contentEquals(byteArrayOf(-30, 23, 39, -69)))
  }

  @Test
  fun `Should create a ripemd160 hash from the sha512 hash of the input stream`() {
    val input = ByteArrayInputStream("imTheContentOfThisFile".toByteArray(Charsets.UTF_8))
    val expectedValue = byteArrayOf(118, 89, -75, -88, 115, 56, 26, -37, -9, -123, 38, -70, 35, -35, -104, 6, -110, -24, 29, 29)
    val result = SUT.getFileContentHash(input)

    assertTrue(expectedValue.contentEquals(result))
  }
}
