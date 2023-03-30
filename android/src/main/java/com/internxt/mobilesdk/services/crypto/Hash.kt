package com.internxt.mobilesdk.services.crypto

import org.spongycastle.crypto.digests.RIPEMD160Digest
import org.spongycastle.crypto.io.DigestInputStream
import java.io.InputStream
import java.security.MessageDigest

class Hash {

  /**
   * Given an array of bytes, generates a SHA256 Hash in bytes
   */
  public fun sha256(input: ByteArray): ByteArray {
    val hasher = getSha256Hasher()

    return hasher.digest(input)
  }

  /**
   * Given an array of bytes, generates a SHA512 Hash in bytes
   */
  public fun sha512(input: ByteArray): ByteArray {
    val hasher = getSha512Hasher()

    return hasher.digest(input)
  }

  public fun getSha256Hasher(): MessageDigest {
      return MessageDigest.getInstance("SHA-256")
  }

  public fun getSha512Hasher(): MessageDigest {
    return MessageDigest.getInstance("SHA-512")
  }



  public fun getHashFromStream(input: InputStream, hasher: MessageDigest): ByteArray {
    val BUFFER_LENGTH = 4096
    val buffer = ByteArray(BUFFER_LENGTH)
    var b: Int
    while (input.read(buffer, 0, BUFFER_LENGTH).also { b = it } != -1) {
      hasher.update(buffer, 0, b)
    }

    return hasher.digest()
  }
  /**
   * Given an array of bytes generates a ripemd160 Hash
   */
  public  fun ripemd160(input: ByteArray): ByteArray{
    val digest = RIPEMD160Digest()
    val out = ByteArray(digest.digestSize)
    digest.update(input, 0, input.size)

    digest.doFinal(out, 0);

    return out
  }


}
