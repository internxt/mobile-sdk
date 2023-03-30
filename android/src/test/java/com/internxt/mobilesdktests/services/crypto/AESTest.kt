package com.internxt.mobilesdktests.services.crypto

import com.facebook.common.util.Hex
import com.internxt.mobilesdk.services.crypto.AES
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AESTest {
  private val SUT = AES()
  // Use the same key for encrypt/decrypt only for test purposes
  private val key  = Hex.decodeHex("f8a04a1cb8c09ebbb78f8f1aba79f9a17cfcc3ed04202e05f5a33ed12a253a9e")
  // Use the same IV for encrypt/decrypt only for test purposes
  private val iv = Hex.decodeHex("8fab1bc120a4c53add1963418a47bb35")

  // Key value array with origin string and result in ByteArray
  private val plainWithResults =  mutableListOf<Pair<String, ByteArray>>(
    "randomStringNotSoRandom" to byteArrayOf(-33, -76, -53, 94, 12, 21, -77, -73, 82, 26, 45, 64, 72, -70, 102, 70, -109, 84, -34, -84, 71, -39, 74),
    "qaswofwqk4456wt3qfwqfqw0992" to byteArrayOf(-36, -76, -42, 77, 12, 30, -105, -78, 75, 71, 119, 18, 48, -94, 102, 38, -115, 96, -56, -77, 69, -57, 80, -43, -25, -75, -69)
  )
  @Test
  fun `Should encrypt an string correctly using AES256 CTR NoPadding`(){
    plainWithResults.forEach{
      val valueToEncrypt = it.first
      val encryptedBytes = it.second

      val cipher = SUT.AES256CTREncrypt(key, iv)

      val encryptResult = cipher.doFinal(valueToEncrypt.toByteArray(Charsets.UTF_8))
      assertTrue(encryptResult.contentEquals(encryptedBytes))
    }
  }

  @Test
  fun `Should decrypt an string correctly using AES256 CTR NoPadding`(){
    plainWithResults.forEach{
      val valueToDecrypt = it.second
      val plainValue = it.first

      val cipher = SUT.AES256CTREncrypt(key, iv)

      val decryptResult = cipher.doFinal(valueToDecrypt)

      assertEquals(String(decryptResult), plainValue)
    }
  }
}

