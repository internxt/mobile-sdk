package com.internxt.mobilesdktests.services.crypto

import com.facebook.common.util.Hex
import com.internxt.mobilesdk.services.crypto.KeyDerivation
import junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.Test

class KeyDerivationTest {
  val SUT = KeyDerivation()

  @Test
  fun `Should generate a pbkdf2 derivated key correctly`() {
    val expectedValue = byteArrayOf(114, 98, -102, 65, -80, 118, -27, -120)

    val password = "password"
    val salt = "salt"
    val rounds = 10000
    val derivedKeyLength = 256 / 32

    val derivedKey = SUT.pbkdf2(password, salt, rounds, derivedKeyLength)

    assertTrue(derivedKey.contentEquals(expectedValue))

  }
}
