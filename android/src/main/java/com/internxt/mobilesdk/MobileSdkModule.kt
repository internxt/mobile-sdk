package com.internxt.mobilesdk


import com.facebook.common.util.Hex
import com.facebook.react.bridge.*
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import com.internxt.mobilesdk.core.*
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.services.photos.PhotosItemProcessConfig
import com.internxt.mobilesdk.services.photos.PhotosLocalSyncManager
import com.internxt.mobilesdk.utils.CryptoUtils
import com.internxt.mobilesdk.utils.InvalidArgumentException
import com.internxt.mobilesdk.utils.Logger
import com.internxt.mobilesdk.utils.Performance
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

class MobileSdkModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val upload = Upload()
  private val encrypt = Encrypt()
  private val photosLocalSyncManager = PhotosLocalSyncManager()

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

          // Generate random index, IV and fileKey
          val index = CryptoUtils.getRandomBytes(32)
          val hexIv = CryptoUtils.bytesToHex(index).slice(0..31)
          val iv = CryptoUtils.hexToBytes(hexIv)
          val fileKey = encrypt.generateFileKey(mnemonic, bucketId, index)

          // 1. Encrypt from input stream to output stream
          encrypt.encryptFromStream(
            inputStream,
            outputStream,
            EncryptConfig(
              mode = EncryptMode.AesCTRNoPadding,
              key = fileKey,
              iv = iv
            ))


          // 2. Upload the encrypted file
          val uploadResult = upload.uploadFile(UploadFileConfig(
            bucketId = bucketId,
            mnemonic = mnemonic,
            encryptedFilePath = outputFile.path,
            iv = iv,
            key = fileKey,
            index = index
          ))

          val duration = Date().time - start.time
          Logger.info("[UPLOAD_COMPLETED] Uploaded in ${duration}ms correctly with fileId ${uploadResult.fileId} and hash ${Hex.encodeHex(uploadResult.hash, false)}")

          val result = Arguments.createMap()
          val hexContentHash = CryptoUtils.bytesToHex(uploadResult.hash)

          result.putString("fileId", uploadResult.fileId)
          result.putString("encryptedFileHash", hexContentHash)

          // Remove the encrypted file
          FS.unlinkIfExists(outputFile.path)

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

  @ReactMethod
  fun processPhotosItem(config: ReadableMap, promise: Promise) {
    try {
      val plainFilePath  = config.getString("plainFilePath") ?: throw InvalidArgumentException("Missing plainFilePath")
      val mnemonic = config.getString("mnemonic") ?: throw InvalidArgumentException("Missing mnemonic")
      val bucketId = config.getString("bucketId") ?: throw InvalidArgumentException("Missing bucketId")
      val photosUserId = config.getString("photosUserId") ?: throw InvalidArgumentException("Missing photosUserId")
      val deviceId = config.getString("deviceId") ?: throw InvalidArgumentException("Missing deviceId")
      Logger.info("Starting to process Photo, creating Thread")
      val runnable = Runnable {
        try {
          Logger.info("Begin of Photo process")
          val processStart = Performance.measureTime()
          val outputDir: File = reactApplicationContext.cacheDir
          val previewDestination = File.createTempFile("tmp", ".jpg", outputDir)
          val uri = FS.getFileUri(plainFilePath, true)
          val originalSourceStream = reactApplicationContext.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open preview input stream at ${uri.path}")
          val previewSourceStream = reactApplicationContext.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream at ${uri.path}")

          // Write here the encrypted photo result
          val encryptedOriginalDestination = File.createTempFile("tmp_original", ".enc", outputDir)
          val encryptedOriginalStream = FileOutputStream(encryptedOriginalDestination)

          // Write here the encrypted preview result
          val encryptedPreviewFile = File.createTempFile("tmp_preview", ".enc", outputDir)

          val config = PhotosItemProcessConfig(
            bucketId = bucketId,
            mnemonic = mnemonic,
            sourcePath = plainFilePath.split(":")[1],
            originalSourceStream = originalSourceStream,
            previewSourceStream = previewSourceStream,
            previewDestination  = previewDestination.path,
            encryptedPreviewDestination = encryptedPreviewFile.path,
            encryptedOriginalDestination = encryptedOriginalDestination.path,
            encryptedOriginalStream = encryptedOriginalStream,
            userId = photosUserId,
            deviceId = deviceId
          )
          photosLocalSyncManager.processItem(config)
          promise.resolve(true)
          Logger.info("Photo processing completed in ${processStart.getMs()}ms")
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
