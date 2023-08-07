package com.internxt.mobilesdk.services.photos

import android.content.Context
import androidx.work.*
import com.internxt.mobilesdk.data.photos.DevicePhotosItemType
import com.internxt.mobilesdk.data.photos.SyncedPhoto
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.services.database.AppDatabase
import com.internxt.mobilesdk.services.database.photos.PhotosDBItem
import com.internxt.mobilesdk.services.database.photos.SyncedPhotosItem
import com.internxt.mobilesdk.utils.JsonUtils
import com.internxt.mobilesdk.utils.Logger
import com.internxt.mobilesdk.utils.Performance
import java.io.File
import java.io.IOException
import java.time.OffsetDateTime


class PhotosProcessingWorker(private val context: Context, private val workerParams: WorkerParameters):
  Worker(context, workerParams) {

  private val photosLocalSyncManager = PhotosLocalSyncManager(context)
  override fun doWork(): Result {

    try {

      val displayName = workerParams.inputData.getString("displayName") ?: throw Exception("Missing displayName")
      val bucketId = workerParams.inputData.getString("bucketId") ?: throw Exception("Missing bucketId")
      val plainFilePath = workerParams.inputData.getString("plainFilePath") ?: throw Exception("Missing plainFilePath")
      val mnemonic = workerParams.inputData.getString("mnemonic") ?: throw Exception("Missing mnemonic")
      val photosUserId = workerParams.inputData.getString("photosUserId") ?: throw Exception("Missing photosUserId")
      val deviceId = workerParams.inputData.getString("deviceId") ?: throw Exception("Missing deviceId")
      val takenAtISO = workerParams.inputData.getString("takenAtISO") ?: throw Exception("Missing takenAtISO")
      val type = workerParams.inputData.getString("type") ?: throw Exception("Missing type")

      val appDatabase = AppDatabase.getInstance(context)

      Logger.info("Begin of Photo process")

      Logger.info("Reading photo from $plainFilePath")


      val processStart = Performance.measureTime()
      val outputDir: File = context.cacheDir

      val sourceFileExtension = FS.getExtension(displayName) ?: throw Exception("Cannot get extension for $plainFilePath")


      val previewTmpDestination = File.createTempFile("tmp_",".jpg", outputDir)
      val sourceTmpDestination = File.createTempFile("tmp_source",
        ".$sourceFileExtension", outputDir)

      val uri = FS.getFileUri(plainFilePath, true)



      val sourceTmpUri = FS.getFileUri(sourceTmpDestination.absolutePath, true)

      val sourceTmpCopyOutputStream = context.contentResolver.openOutputStream(sourceTmpUri) ?: throw IOException("Unable to open output stream to write to ${sourceTmpDestination.absolutePath}")
      val originalSourceStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open preview input stream at ${uri.path}")

      // Copy the source to our cache directory
      originalSourceStream.copyTo(sourceTmpCopyOutputStream)


      val isEmpty = FS.fileIsEmpty(sourceTmpDestination.absolutePath)

      Logger.info("File at ${sourceTmpDestination.absolutePath} is empty $isEmpty")

      // Write here the encrypted photo result
      val encryptedOriginalFileDestination = File.createTempFile("tmp_original_", ".enc", outputDir)

      // Write here the encrypted preview result
      val encryptedPreviewFileDestination = File.createTempFile("tmp_preview_", ".enc", outputDir)


      val config = PhotosItemProcessConfig(
        bucketId = bucketId,
        mnemonic = mnemonic,
        source = sourceTmpDestination,
        previewDestination  = previewTmpDestination,
        encryptedPreviewDestination = encryptedPreviewFileDestination,
        encryptedOriginalDestination = encryptedOriginalFileDestination,
        photosUserId = photosUserId,
        deviceId = deviceId,
        takenAtISO = takenAtISO,
        displayName = displayName,
        type = enumValueOf<DevicePhotosItemType>(type)
      )
      val syncedPhoto = photosLocalSyncManager.processItem(config) ?: throw Exception("Photos item with name $displayName was not processed correctly")

      appDatabase.syncedPhotosDao()?.updateOrCreateSyncedPhotosItem(
       SyncedPhotosItem(
         id = syncedPhoto.id,
         createdAt = syncedPhoto.createdAt,
         updatedAt = syncedPhoto.updatedAt,
         deviceId = syncedPhoto.deviceId,
         userId = syncedPhoto.userId,
         fileId = syncedPhoto.fileId,
         previewId = syncedPhoto.previewId,
         hash = syncedPhoto.hash,
         name = syncedPhoto.name,
         takenAt = syncedPhoto.takenAt,
         width = syncedPhoto.width,
         height = syncedPhoto.height,
         networkBucketId = syncedPhoto.networkBucketId,
         size = syncedPhoto.size,
         status = syncedPhoto.status,
         type = syncedPhoto.type,
         itemType = syncedPhoto.itemType,
         duration = syncedPhoto.duration
       )
      )
      // Cleanup
      FS.unlinkIfExists(previewTmpDestination.absolutePath)
      FS.unlinkIfExists(sourceTmpDestination.absolutePath)
      FS.unlinkIfExists(encryptedOriginalFileDestination.absolutePath)

      Logger.info("Photo processing completed in ${processStart.getMs()}ms")

      val data = Data.Builder()

      val adapter = JsonUtils.moshi.adapter(SyncedPhoto::class.java)

      data.putString("result", adapter.toJson(syncedPhoto))

      return Result.success(data.build())

    } catch (exception: Exception) {
      Logger.error("Error processing photo: $exception")

      return if(runAttemptCount <= 3) {
        Result.retry()
      } else {
        Result.failure()
      }
    }
  }
}


