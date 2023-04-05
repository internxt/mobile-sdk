package com.internxt.mobilesdk.services.crypto

import com.facebook.common.util.Hex
import com.internxt.mobilesdk.config.MobileSdkConfigKey
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES {

  val keyDerivation = KeyDerivation()
  val hash = Hash()
  /**
   * Generates an AES-256-CTR Cipher for decrypt
   *
   * @param key
   * @param iv
   * @return
   */
  @Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class
  )
  fun AES256CTRDecrypt(key: ByteArray, iv: ByteArray): Cipher {
    val secretKey = SecretKeySpec(key, 0, key.size, "AES")
    val cipher: Cipher = Cipher.getInstance("AES/CTR/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(/* iv = */ iv))
    return cipher
  }

  @Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class
  )
  fun AES256CTREncrypt(key: ByteArray, iv: ByteArray): Cipher {
    val secretKey = SecretKeySpec(key, 0, key.size, "AES")
    val cipher: Cipher = Cipher.getInstance("AES/CTR/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(/* iv = */ iv))
    return cipher
  }
}
