package com.internxt.mobilesdk.core

import com.internxt.mobilesdk.services.crypto.AES
import com.internxt.mobilesdk.services.crypto.Hash
import com.internxt.mobilesdk.utils.NotSupportedEncryptModeException
import java.io.*
import javax.crypto.CipherOutputStream
class Decrypt {
  val AES = AES()
  /**
   * Decrypts an InputStream to an OutputStream
   */
  @Throws(NotSupportedEncryptModeException::class)
  fun decryptFromStream(input: InputStream, output: OutputStream, config: EncryptConfig) {

    if(config.mode != EncryptMode.AesCTRNoPadding) {
      throw NotSupportedEncryptModeException("${config.mode} is not supported" )
    }

    // 1. Get the AES256CTR Cipher
    val cipher = AES.AES256CTRDecrypt(config.key, config.iv)


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
}
