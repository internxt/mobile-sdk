package com.internxt.mobilesdk

import com.facebook.common.util.Hex
import com.facebook.react.bridge.*
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import com.internxt.mobilesdk.core.*
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.utils.CryptoUtils
import com.internxt.mobilesdk.utils.InvalidArgumentException
import com.internxt.mobilesdk.utils.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MobileSdkModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val upload = Upload()
  private val uploadThreadPool = Executors.newFixedThreadPool(2)
  override fun getName(): String {
    return NAME
  }

  // Initialize the Mobile SDK from JS side when the app starts
  @ReactMethod
  fun init(config: ReadableMap) {
    val mobileSdkConfig = HashMap<String, String>()
    config.toHashMap().forEach{
      mobileSdkConfig[it.key] = it.value as String
    }
    // Convert ReadableMap to HashMap, we should get an util for this
    MobileSdkConfigLoader.init(mobileSdkConfig)
    Logger.info("Internxt Mobile SDK initialized correctly")
  }


  @ReactMethod
  fun uploadFile(config: ReadableMap, promise: Promise) {
    try {
      val plainFilePath  = config.getString("plainFilePath") ?: throw InvalidArgumentException("Missing plainFilePath")
      val mnemonic = config.getString("mnemonic") ?: throw InvalidArgumentException("Missing mnemonic")
      val bucketId = config.getString("bucketId") ?: throw InvalidArgumentException("Missing bucketId")
      Logger.info("Starting file encrypt process, creating Thread")


      val runnable = Runnable {
        try {
          Logger.info("Begin of encryption")
          val start = Date()
          val outputDir: File = reactApplicationContext.cacheDir
          val outputFile = File.createTempFile("tmp", ".enc", outputDir)
          val outputStream = FileOutputStream(outputFile)

          Logger.info("Encrypt process started for file at $plainFilePath")

          val uri = FS.getFileUri(plainFilePath, true)
          val inputStream = reactApplicationContext.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream at ${uri.path}")

          Logger.info("Input stream opened")

          val uploadResult = upload.uploadFile(UploadFileConfig(
            bucketId = bucketId,
            mnemonic = mnemonic,
            plainStream = inputStream,
            encryptedStream = outputStream,
            encryptedFilePath = outputFile.path
          ))

          val duration = Date().time - start.time;
          Logger.info("[UPLOAD_COMPLETED] Uploaded in ${duration}ms correctly with fileId ${uploadResult.fileId} and hash ${Hex.encodeHex(uploadResult.hash, false)}")

          val result = Arguments.createMap();
          val hexContentHash = CryptoUtils.bytesToHex(uploadResult.hash)

          result.putString("fileId", uploadResult.fileId)
          result.putString("encryptedFilePath", outputFile.path)
          result.putString("encryptedFileHash", hexContentHash)
          

          // Resolve the result to JS
          promise.resolve(result)
        } catch(e: Exception) {
          e.printStackTrace()
          promise.reject(e)
        }
      }
      Thread(runnable).start()
    } catch (e: Exception) {
      e.printStackTrace()
      promise.reject(e)
    }
  }


  companion object {
    const val NAME = "MobileSdk"
  }
}
