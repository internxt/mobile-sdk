package com.internxt.mobilesdk.services.crypto

import org.spongycastle.crypto.digests.RIPEMD160Digest
import org.spongycastle.jcajce.provider.digest.RIPEMD160
import java.security.MessageDigest

class Hash {

  /**
   * Given an array of bytes, generates a SHA256 Hash in bytes
   */
  public fun sha256(input: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")

    return digest.digest(input)
  }

  /**
   * Given an array of bytes, generates a SHA512 Hash in bytes
   */
  public fun sha512(input: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-512")

    return digest.digest(input)
  }

  /**
   * Given an arrat of bytes generates a RIPEMD160 Hash
   */
  public  fun ripemd160(input: ByteArray): ByteArray{
    val digest = RIPEMD160Digest()
    val out = ByteArray(digest.digestSize)
    digest.update(input, 0, input.size)

    digest.doFinal(out, 0);

    return out
  }


}
