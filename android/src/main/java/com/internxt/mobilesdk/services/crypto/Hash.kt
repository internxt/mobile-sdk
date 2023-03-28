package com.internxt.mobilesdk.services.crypto

import org.spongycastle.crypto.digests.RIPEMD160Digest
import java.security.MessageDigest

class Hash {

  /**
   * Get an SHA256 Digest
   */
  public fun sha256(input: ByteArray): MessageDigest {
    return MessageDigest.getInstance("SHA-256")
  }

  /**
   * Get an SHA512 Digest
   */
  public fun sha512(input: ByteArray): MessageDigest {
    return MessageDigest.getInstance("SHA-512")
  }

  /**
   * Get a RIPEMD160 Digest
   */
  public  fun ripemd160(input: ByteArray): ByteArray {
    val digest = RIPEMD160Digest()
    val out = ByteArray(digest.digestSize)
    digest.update(input, 0, input.size)

    digest.doFinal(out, 0);

    return out
  }


}
