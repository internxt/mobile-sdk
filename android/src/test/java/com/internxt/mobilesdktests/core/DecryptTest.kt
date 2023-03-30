package com.internxt.mobilesdktests.core

import com.facebook.common.util.Hex
import com.internxt.mobilesdk.core.Decrypt
import com.internxt.mobilesdk.core.Encrypt
import com.internxt.mobilesdk.core.EncryptConfig
import com.internxt.mobilesdk.core.EncryptMode
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecryptTest {
  val SUT = Decrypt()
  @Test
  fun `Should decrypt an input stream to an output stream using AES256 CTR NOPadding`() {

    val input = ByteArrayInputStream(byteArrayOf(-30, 23, 39, -69))
    val output = ByteArrayOutputStream()
    SUT.decrypt(input, output, EncryptConfig(
      mode = EncryptMode.AesCTRNoPadding,
      key = Hex.decodeHex("50489ff30c51d5c400161ea442a8a12d2f936df6653a9caaafb36c27dec184d4"),
      iv = Hex.decodeHex("a879556ac137bfda0cbc48c60a005e05")
    )
    )


    assertEquals(String(output.toByteArray()),"test")
  }
}
