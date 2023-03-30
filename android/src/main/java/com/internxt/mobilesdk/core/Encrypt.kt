package com.internxt.mobilesdk.core

import com.facebook.common.util.Hex
import com.internxt.mobilesdk.services.crypto.AES
import com.internxt.mobilesdk.services.crypto.Hash
import com.internxt.mobilesdk.utils.NotSupportedEncryptMode
import java.io.*
import java.security.MessageDigest
import javax.crypto.CipherOutputStream


class Encrypt {
  val AES = AES()
  val Hash = Hash()
  /**
   * Encrypts an InputStream to an OutputStream
   */
  @Throws(NotSupportedEncryptMode::class)
  fun encrypt(input: InputStream, output: OutputStream, config: EncryptConfig) {

    if(config.mode != EncryptMode.AesCTRNoPadding) {
      throw NotSupportedEncryptMode("${config.mode} is not supported" )
    }

    // 1. Get the AES256CTR Cipher
    val cipher = AES.AES256CTREncrypt(config.key, config.iv)


    // 2. Create the Cipher stream;
    val cipherOutput = CipherOutputStream(output, cipher)

    var b: Int
    val buffer = ByteArray(4096)


    // 3. Start writing to the buffer
    while (input.read(buffer).also { b = it } != -1) {
      cipherOutput.write(buffer, 0, b)
    }

    // 4. Cleanup
    cipherOutput.flush()
    cipherOutput.close()
    input.close()
    output.close()
  }

  /**
   * Creates a RIPEMD160 Hash from the hash in SHA512 of the content of the file
   * File -> SHA512 -> RIPEMD160
   */
  fun getFileContentHash(input: InputStream): ByteArray {
    val sha512Hash = Hash.getHashFromStream(input, Hash.getSha512Hasher())

    return Hash.ripemd160(sha512Hash)
  }

}
