package com.internxt.mobilesdk.services.photos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.internxt.mobilesdk.R
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.utils.Logger
import com.internxt.mobilesdk.utils.Performance
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PhotosProcessingWorker(private val context: Context, private val workerParams: WorkerParameters):
  Worker(context, workerParams) {
  private val notificationManager = context.getSystemService(NotificationManager::class.java)

  private val photosLocalSyncManager = PhotosLocalSyncManager()
  override fun doWork(): Result {

    val bucketId = workerParams.inputData.getString("bucketId") ?: throw Exception("Missing bucketId")
    val plainFilePath = workerParams.inputData.getString("plainFilePath") ?: throw Exception("Missing plainFilePath")
    val mnemonic = workerParams.inputData.getString("mnemonic") ?: throw Exception("Missing mnemonic")
    val userId = workerParams.inputData.getString("userId") ?: throw Exception("Missing userId")
    val photosUserId = workerParams.inputData.getString("photosUserId") ?: throw Exception("Missing photosUserId")
    val deviceId = workerParams.inputData.getString("deviceId") ?: throw Exception("Missing deviceId")
    val takenAtISO = workerParams.inputData.getString("takenAtISO") ?: throw Exception("Missing takenAtISO")

    Logger.info("Begin of Photo process")

    val processStart = Performance.measureTime()
    val outputDir: File = context.cacheDir
    val previewDestination = File.createTempFile("tmp", ".jpg", outputDir)
    val uri = FS.getFileUri(plainFilePath, true)
    val originalSourceStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open preview input stream at ${uri.path}")
    val previewSourceStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open input stream at ${uri.path}")

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
      userId = userId,
      photosUserId = photosUserId,
      deviceId = deviceId,
      takenAtISO = takenAtISO
    )
    val serializedResult = photosLocalSyncManager.processItem(config)


    // Cleanup
    FS.unlinkIfExists(encryptedOriginalDestination.path)
    FS.unlinkIfExists(encryptedPreviewFile.path)

    Logger.info("Photo processing completed in ${processStart.getMs()}ms")
    val data = Data.Builder()
    data.putString("result", serializedResult)
    return Result.success(data.build())
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannel = notificationManager?.getNotificationChannel(CHANNEL_ID)
      if (notificationChannel == null) {
        notificationManager?.createNotificationChannel(
          NotificationChannel(
            CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_LOW
          )
        )
      }
    }
  }

  companion object {

    const val TAG = "PhotosProcessing"
    const val NOTIFICATION_ID = 42
    const val CHANNEL_ID = "photos_work_request"
  }
}


