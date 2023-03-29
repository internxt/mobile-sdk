package com.internxt.mobilesdk.services.crypto

import com.facebook.common.util.Hex
import junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.Test

class KeyDerivationTest {
  val SUT = KeyDerivation()

  @Test
  fun `Should generate a pbkdf2 derivated key correctly`() {
    val expectedValue = byteArrayOf(-111, -66, 35, 86, 79, 9, -4, -123, 92, -126, -50, -124, -94, 35, -21, -25, -42, 61, -117, 73, -42, -109, 114, 89, 58, 13, -98, -45, -98, 20, 60, -125, -31, -85, 47, 114, 42, 93, -37, -106, -97, -18, -4, -120, 64, 63, 126, 42, -2, 26, -5, -117, 47, 14, 107, 32, -83, -48, -5, 123, 40, 54, -120, 7)

    val password = "password"
    val salt = "salt"
    val rounds = 2048
    val derivedKeyLength = 64

    val derivedKey = SUT.pbkdf2(password, salt, rounds, derivedKeyLength)

    assertTrue(derivedKey.contentEquals(expectedValue))

  }
}
