package com.internxt.mobilesdk


import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import com.facebook.common.util.Hex
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.internxt.mobilesdk.config.MobileSdkAuthTokens
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import com.internxt.mobilesdk.core.*
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.services.photos.PhotosProcessingWorker
import com.internxt.mobilesdk.utils.CryptoUtils
import com.internxt.mobilesdk.utils.InvalidArgumentException
import com.internxt.mobilesdk.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors


class MobileSdkModule(val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val upload = Upload()
  private val encrypt = Encrypt()
  private val photosEnqueuer = Executors.newFixedThreadPool(3)
  private var scheduledPhotosToProcess = 0
  private var processedPhotos = 0
  override fun getName(): String {
    return NAME
  }

  // Initialize the Mobile SDK from JS side when the app starts
  @ReactMethod
  fun init(config: ReadableMap, promise: Promise) {

    // Convert ReadableMap to HashMap, we should get an util for this
    try {
      val mobileSdkConfig = HashMap<String, String>()
      config.toHashMap().forEach{
        mobileSdkConfig[it.key] = it.value as String
      }
      MobileSdkConfigLoader.init(mobileSdkConfig)
      Logger.info("Internxt Mobile SDK initialized correctly")
      promise.resolve(true)
    } catch (exception: Exception) {
      promise.reject(exception)
    }

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
  fun setAuthTokens(config: ReadableMap, promise: Promise) {
    try {
      val accessToken  = config.getString("accessToken") ?: throw InvalidArgumentException("Missing accessToken")
      val newToken  = config.getString("newToken") ?: throw InvalidArgumentException("Missing newToken")
      MobileSdkConfigLoader.authTokens = MobileSdkAuthTokens(
        accessToken = accessToken,
        newToken = newToken
      )
      promise.resolve(true)
    } catch (exception: Exception) {
      promise.reject(exception)
    }
  }
  @ReactMethod
  fun processPhotosItem(config: ReadableMap, promise: Promise) {
    try {
      // Resolve fast, we are going to enqueue the work request
      promise.resolve(true)
      /**
       * We use this from JS since when we retrieve the EXIF data from the image
       * the times are not equal to the ones retrieved via media resolver, so
       * we are going to get the JS value right now which is retrieved via
       * expo-media-library media resolver
       */
      val photosItemTakenAtISO  = config.getString("photosItemTakenAtISO") ?: throw InvalidArgumentException("Missing photosItemTakenAtISO")
      val plainFilePath  = config.getString("plainFilePath") ?: throw InvalidArgumentException("Missing plainFilePath")
      val mnemonic = config.getString("mnemonic") ?: throw InvalidArgumentException("Missing mnemonic")
      val bucketId = config.getString("bucketId") ?: throw InvalidArgumentException("Missing bucketId")
      val photosUserId = config.getString("photosUserId") ?: throw InvalidArgumentException("Missing photosUserId")
      val userId = config.getString("userId") ?: throw InvalidArgumentException("Missing userId")
      val deviceId = config.getString("deviceId") ?: throw InvalidArgumentException("Missing deviceId")
      Logger.info("Scheduling photo processing")


      photosEnqueuer.execute {
        val events =
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)

        val requestBuilder = OneTimeWorkRequestBuilder<PhotosProcessingWorker>()

        val data = Data.Builder()
        data.putString("bucketId", bucketId)
        data.putString("plainFilePath", plainFilePath)
        data.putString("mnemonic", mnemonic)
        data.putString("userId", userId)
        data.putString("photosUserId", photosUserId)
        data.putString("deviceId", deviceId)
        data.putString("takenAtISO", photosItemTakenAtISO)

        requestBuilder.setInputData(data.build())
        requestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)


        val request = requestBuilder.build()
        val workManager = WorkManager
          .getInstance(reactContext)

        GlobalScope.launch(Dispatchers.Main) {
          workManager.getWorkInfoByIdLiveData(request.id).observeForever {
            val workInfo = it
            val result = workInfo.outputData.getString("result")
            Logger.info("Received live data event ${workInfo.state}")
            if (workInfo?.state == WorkInfo.State.SUCCEEDED && result !== null) {
              processedPhotos += 1
              scheduledPhotosToProcess -= 1
              val payload = Arguments.createMap()
              payload.putString("result", result)
              events.emit("onPhotoProcessed", payload)
              Logger.info("$processedPhotos photos processed, $scheduledPhotosToProcess pending")
            }
          }
        }

        workManager.enqueue(request)
        scheduledPhotosToProcess += 1
        Logger.info("$scheduledPhotosToProcess scheduled photos for processing")
      }


    } catch (e: Exception) {
      e.printStackTrace()
     // To late to reject, we only enqueue the job
    }
  }


  companion object {
    const val NAME = "MobileSdk"
  }
}

class CustomLifecycleOwner(val activity: AppCompatActivity) : LifecycleOwner {

  override fun getLifecycle(): Lifecycle = activity.lifecycle
}
