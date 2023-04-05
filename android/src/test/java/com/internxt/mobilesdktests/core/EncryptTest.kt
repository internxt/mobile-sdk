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
  fun `Should encrypt an input stream to an output stream using AES256 CTR NoPadding`() {
    val input = ByteArrayInputStream("test".toByteArray(Charsets.UTF_8))
    val output = ByteArrayOutputStream()
    SUT.encryptFromStream(input, output, EncryptConfig(
      mode = EncryptMode.AesCTRNoPadding,
      key = Hex.decodeHex("f95448880fbba38409ad9f2837a203825456da9e25d7fdb80dd14d008e79de73"),
      iv = Hex.decodeHex("77a6dfa9783e4fecb63d9fed25b556e3")
    )
    )

    assertTrue(output.toByteArray().contentEquals(Hex.decodeHex("ef9f6bd3")))
  }

  @Test
  fun `Should create a ripemd160 hash from the sha512 hash of the input stream`() {
    val input = ByteArrayInputStream("imTheContentOfThisFile".toByteArray(Charsets.UTF_8))
    val expectedValue = Hex.decodeHex("4eef3af75813f505b9050f575b8d2e782c9db5d7")

    val result = SUT.getFileContentHash(input)

    assertTrue(expectedValue.contentEquals(result))
  }

  @Test
  fun `Should create a fileKey correctly from the mnemonic and the bucketId`() {
    val expected = Hex.decodeHex("ff1ce423c9488d632ae151a1477a3701f9c92a8bb9205094bd41476e9849048e")
    // 32 bytes
    val index = Hex.decodeHex("631680636964a57b5527a413996dd9d240e6136f4e70d2942f9868ef7dc9f9c4")
    val mnemonic = "essence renew fish any airport nature tape gallery tobacco inside there enlist hub bring meat wing crack review logic open husband excite bag reflect"
    val bucketId = "e8f6c43b49d72e21aa6094f0"
    val result = SUT.generateFileKey(mnemonic, bucketId, index)

    assertTrue(expected.contentEquals(result))
  }
}
