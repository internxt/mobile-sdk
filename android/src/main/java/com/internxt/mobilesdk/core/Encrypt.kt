package com.internxt.mobilesdk.core


import com.facebook.common.util.Hex
import com.internxt.mobilesdk.config.MobileSdkConfigKey
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import com.internxt.mobilesdk.services.crypto.AES
import com.internxt.mobilesdk.services.crypto.Hash
import com.internxt.mobilesdk.services.crypto.KeyDerivation
import com.internxt.mobilesdk.utils.CryptoUtils
import com.internxt.mobilesdk.utils.CryptoUtils.generateBucketKey
import com.internxt.mobilesdk.utils.CryptoUtils.getDeterministicKey
import com.internxt.mobilesdk.utils.Logger
import com.internxt.mobilesdk.utils.NotSupportedEncryptModeException
import java.io.*
import javax.crypto.CipherOutputStream


class Encrypt {
  val AES = AES()
  val Hash = Hash()
  val keyDerivation = KeyDerivation()

  /**
   * Encrypts an InputStream to an OutputStream
   */
  @Throws(NotSupportedEncryptModeException::class)
  fun encryptFromStream(input: InputStream, output: OutputStream, config: EncryptConfig) {

    if(config.mode != EncryptMode.AesCTRNoPadding) {
      throw NotSupportedEncryptModeException("${config.mode} is not supported" )
    }

    // 1. Get the AES256CTR Cipher
    val cipher = AES.AES256CTREncrypt(config.key, config.iv)


    // 2. Create the Cipher stream;
    val cipherOutput = CipherOutputStream(output, cipher)

    var b: Int
    val buffer = ByteArray(8192)


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
   * Creates a RIPEMD160 Hash from the hash in SHA256 of the content of the file
   * File -> SHA256 -> RIPEMD160
   */
  fun getFileContentHash(input: InputStream): ByteArray {
    val sha256Hash = Hash.getHashFromStream(input, Hash.getSha256Hasher())

    return Hash.ripemd160(sha256Hash)
  }


  fun generateFileKey(mnemonic: String, bucketId: String, index: ByteArray): ByteArray {
    if(index.size !== 32) throw Exception("Index should be 32 bytes")
    val bucketKey = generateBucketKey(mnemonic, bucketId);
    println("Bucket ${Hex.encodeHex(bucketKey, false)}")
    val deterministicKey = getDeterministicKey(bucketKey.sliceArray(0..31), index);
    return deterministicKey.sliceArray(0..31)
  }
}
