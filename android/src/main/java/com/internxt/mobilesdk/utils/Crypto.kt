package com.internxt.mobilesdk.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.common.util.Hex
import com.internxt.mobilesdk.services.crypto.Hash
import com.internxt.mobilesdk.services.crypto.KeyDerivation
import java.security.SecureRandom
import java.security.Security
import java.util.regex.Pattern

object CryptoUtils {
  val Hash = Hash()
  val KeyDerivation = KeyDerivation()
  public fun getAvailableAlgorithms(): List<String> {
    var providers = Security.getProviders()

    var availableAlgorithms = mutableListOf<String>()
    providers.forEach {it
      it
    }

    for (provider in Security.getProviders()) {
      for (key in provider.stringPropertyNames()) availableAlgorithms.add(provider.getProperty(key))
    }

    return availableAlgorithms
  }

  public fun validateMnemonic(mnemonic: String): Boolean {
    return true
  }

  public fun isValisHex(hex: String): Boolean {
    return hex.matches(Pattern.compile("\\p{XDigit}+").toRegex())
  }

  public fun mnemonicToSeed(mnemonic: String, password: String): ByteArray {
    return KeyDerivation.pbkdf2(mnemonic, "mnemonic", 2048, 64);
  }

  fun getDeterministicKey(key: ByteArray, data: ByteArray): ByteArray {
    val hasher = Hash.getSha512Hasher()
    hasher.update(key)
    hasher.update(data)

    return hasher.digest()
  }
  fun generateBucketKey(mnemonic: String, bucketId: String): ByteArray {
    val seed = mnemonicToSeed(mnemonic, "")
    return getDeterministicKey(seed, Hex.decodeHex(bucketId));
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun getRandomBytes(howMany: Int): ByteArray {
    val instance = SecureRandom.getInstanceStrong()
    val init = ByteArray(howMany)
    instance.nextBytes(init)
    return init
  }
  fun hexToBytes(hex: String): ByteArray {
      return Hex.decodeHex(hex)
  }

  fun bytesToHex(bytes: ByteArray): String {
    return Hex.encodeHex(bytes, false).lowercase()
  }
}
