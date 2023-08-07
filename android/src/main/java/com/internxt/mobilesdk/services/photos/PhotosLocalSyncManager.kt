package com.internxt.mobilesdk.services.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size


import com.internxt.mobilesdk.core.*
import com.internxt.mobilesdk.data.photos.CreatePhotoPayload
import com.internxt.mobilesdk.data.photos.DevicePhotosItemType
import com.internxt.mobilesdk.data.photos.PhotoPreview
import com.internxt.mobilesdk.data.photos.SyncedPhoto
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.services.crypto.Hash
import com.internxt.mobilesdk.utils.CryptoUtils
import com.internxt.mobilesdk.utils.Logger
import com.internxt.mobilesdk.utils.Performance
import java.io.*

import kotlin.Exception



data class PhotosItemProcessConfig(
  val displayName: String,
  val source: File,
  val previewDestination: File,
  val encryptedPreviewDestination: File,
  val encryptedOriginalDestination: File,
  val mnemonic: String,
  val bucketId: String,
  val photosUserId: String,
  val deviceId: String,
  val takenAtISO: String,
  val type: DevicePhotosItemType
)

data class EncryptPhotosItemResult(
  val iv: ByteArray,
  val fileKey: ByteArray,
  val index: ByteArray
)

class PhotosLocalSyncManager(private val context: Context) {

  private val photosPreviewGenerator = PhotosPreviewGenerator()
  private val encrypt = Encrypt()
  private val upload = Upload()
  private val photosApi = PhotosApi()
  private val hash = Hash()

  /**
   * Processes a Photos item, this operation involves:
   *
   * 1. Preview generation
   * 2. Preview encryption
   * 3. Preview upload
   * 4. Original Photo encryption
   * 5. Original Photo upload
   * 6. Create Photo in photos api
   */
  fun processItem(config: PhotosItemProcessConfig): SyncedPhoto? {
    val totalTimeMeasurer = Performance.measureTime()
    val previewTimeMeasurer = Performance.measureTime()
    Logger.info("Processing PhotosItem at ${config.source.absolutePath}")


    // 1. Generate a preview for the item

    var previewBitmap: Bitmap? = null

    if (config.type == DevicePhotosItemType.IMAGE) {
      Logger.info("Generating image preview for ${config.source.absolutePath}")
      val inputStream = FileInputStream(config.source.absolutePath)
      previewBitmap = photosPreviewGenerator.generateImagePreview(inputStream = inputStream, 512)
      inputStream.close()
    }

    if(config.type == DevicePhotosItemType.VIDEO) {
      Logger.info("Generating video preview for ${config.source.absolutePath}")
      previewBitmap = photosPreviewGenerator.generateVideoPreview(config.source.absolutePath, 512)
    }

    if(previewBitmap == null) throw Exception("Unable to generate preview for ${config.displayName}")
    val previewFile = photosPreviewGenerator.writeBitmapToFile(previewBitmap, config.previewDestination.absolutePath)

    Logger.info("Generated preview at ${config.previewDestination} in ${previewTimeMeasurer.getMs()}ms")

    val previewEncryptTimeMeasurer = Performance.measureTime()
    Logger.info("Encrypting preview")

    val previewInputStream = FileInputStream(previewFile)
    val previewOutputStream = FileOutputStream(config.encryptedPreviewDestination)
    // 2. Encrypt the preview file
    val encryptPreviewResult = encryptPhotosItem(
      config.mnemonic,
      config.bucketId,
      previewInputStream,
      previewOutputStream
    )

    previewInputStream.close()
    previewOutputStream.close()

    Logger.info("Preview encrypted in ${previewEncryptTimeMeasurer.getMs()}ms")

    val uploadPreviewTimeMeasurer = Performance.measureTime()

    Logger.info("Uploading preview")
    // 3. Preview is encrypted, upload
    val uploadPreviewResult = upload.uploadFile(
      UploadFileConfig(
      bucketId = config.bucketId,
      mnemonic = config.mnemonic,
      encryptedFilePath = config.encryptedPreviewDestination.path,
      iv = encryptPreviewResult.iv,
      key = encryptPreviewResult.fileKey,
      index = encryptPreviewResult.index
    ))

    Logger.info("Preview uploaded in ${uploadPreviewTimeMeasurer.getMs()}ms")

    Logger.info("Copying EXIF metadata")

    val originalEncryptTimeMeasurer = Performance.measureTime()

    val sourceInputStream = FileInputStream(config.source.absolutePath)
    val encryptedOutputStream = FileOutputStream(config.encryptedOriginalDestination)
    // 4. Encrypt the original Photo
    val encryptOriginalResult = encryptPhotosItem(
      config.mnemonic,
      config.bucketId,
      sourceInputStream,
      encryptedOutputStream
    )

    sourceInputStream.close()
    encryptedOutputStream.close()
    Logger.info("Original photo encrypted in ${originalEncryptTimeMeasurer.getMs()}ms")

    val uploadOriginalTimeMeasurer = Performance.measureTime()
    Logger.info("Uploading original")
    // 5. Original is encrypted, upload
    val uploadOriginalResult = upload.uploadFile(
      UploadFileConfig(
        bucketId = config.bucketId,
        mnemonic = config.mnemonic,
        encryptedFilePath = config.encryptedOriginalDestination.path,
        iv = encryptOriginalResult.iv,
        key = encryptOriginalResult.fileKey,
        index = encryptOriginalResult.index
      )
    )

    Logger.info("Photo uploaded in ${uploadOriginalTimeMeasurer.getMs()}ms")
    Logger.info("Preview FileId: ${uploadPreviewResult.fileId}")
    Logger.info("Original Photo FileId: ${uploadOriginalResult.fileId}")
    Logger.info("Photo item uploaded in ${totalTimeMeasurer.getMs()}ms")

    val previewPayload = PhotoPreview(
      width = 512,
      height = 512,
      fileId = uploadPreviewResult.fileId,
      size = uploadPreviewResult.size,
      type = "JPEG"
    )

    Logger.info("Display name is ${config.displayName}")

    val parts = config.displayName.split(".")
    if(parts.size < 2) throw Exception("File is missing name or type")
    val name = parts[0]
    val type = parts[1]

    val dimensions = getItemDimensions(config.source, config.type) ?: throw Exception("Cannot get size for item at ${config.source.absolutePath}")
    val duration = getItemDuration(config.source, config.type)
    Logger.info("Photo dimensions ${dimensions.width}w x ${dimensions.height}h")
    val hasher = hash.getSha256Hasher()
    val hashInputStream = FileInputStream(config.source)
    val contentHash = hash.getHashFromStream(hashInputStream, hash.getSha256Hasher())

    Logger.info("Content hash is ${CryptoUtils.bytesToHex(contentHash)}")
    hasher.update(config.photosUserId.toByteArray(Charsets.UTF_8))

    Logger.info("Image name is $name")
    hasher.update(name.toByteArray(Charsets.UTF_8))

    hasher.update(config.takenAtISO.toByteArray())
    hasher.update(CryptoUtils.bytesToHex(contentHash).toByteArray())

    val hash = CryptoUtils.bytesToHex(hasher.digest())

    val itemType = if(config.type == DevicePhotosItemType.IMAGE) "PHOTO" else  "VIDEO"
    Logger.info("Photo hash $hash for itemType $itemType")
    val createPhotoPayload = CreatePhotoPayload(
      name = name,
      userId = config.photosUserId,
      deviceId = config.deviceId,
      fileId = uploadOriginalResult.fileId,
      width = dimensions.width,
      height = dimensions.height,
      itemType = itemType,
      size = uploadOriginalResult.size,
      type = type,
      hash = hash,
      networkBucketId = config.bucketId,
      takenAt = config.takenAtISO,
      previews = listOf(previewPayload),
      previewId = uploadPreviewResult.fileId,
      duration = duration
    )
    val result = photosApi.createPhoto(createPhotoPayload)

    hashInputStream.close()
    Logger.info("Photo item uploaded and created in ${totalTimeMeasurer.getMs()}ms")

    return result
  }

  private fun encryptPhotosItem(
    mnemonic: String,
    bucketId: String,
    inputStream: InputStream,
    outputStream: OutputStream
  ): EncryptPhotosItemResult {
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
      )
    )

    return EncryptPhotosItemResult(
      iv = iv,
      fileKey = fileKey,
      index = index
    )
  }

  private fun getItemDimensions(source: File, type: DevicePhotosItemType): Size? {
    if(type == DevicePhotosItemType.IMAGE) {
      val bitmapInputStream = FileInputStream(source)


      val options = BitmapFactory.Options().apply {
        // Set inJustDecodeBounds to true to only decode the image dimensions
        inJustDecodeBounds = true
      }
      options.inJustDecodeBounds = true
      BitmapFactory.decodeStream(bitmapInputStream, null, options)

      bitmapInputStream?.close()

      return Size(options.outWidth, options.outHeight)
    }

    if(type == DevicePhotosItemType.VIDEO) {
      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(source.absolutePath)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
        return if (width != null && height != null) {
          Size(width, height)
        } else {
          null
        }
      } catch (e: Exception) {
        throw e
      } finally {
        retriever.release()
      }
    }

    return null
  }

  private fun getItemDuration(source: File, type: DevicePhotosItemType): Long? {
    if(type == DevicePhotosItemType.IMAGE) {
      return null
    }

    if(type == DevicePhotosItemType.VIDEO) {
      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(source.absolutePath)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
          ?: return null

        // MS to seconds
        return duration / 1000
      } catch (e: Exception) {
        throw e
      } finally {
        retriever.release()
      }
    }

    return null
  }
}
