package com.internxt.mobilesdk.services.photos

import android.graphics.BitmapFactory
import android.media.ExifInterface

import com.internxt.mobilesdk.core.*
import com.internxt.mobilesdk.data.photos.CreatePhotoPayload
import com.internxt.mobilesdk.data.photos.PhotoPreview
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.services.crypto.Hash
import com.internxt.mobilesdk.utils.CryptoUtils
import com.internxt.mobilesdk.utils.Logger
import com.internxt.mobilesdk.utils.Performance
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds


data class PhotosItemProcessConfig(
  val sourcePath: String,
  val originalSourceStream: InputStream,
  val previewSourceStream: InputStream,
  val previewDestination: String,
  val encryptedPreviewDestination: String,
  val encryptedOriginalDestination: String,
  val encryptedOriginalStream: OutputStream,
  val mnemonic: String,
  val bucketId: String,
  val photosUserId: String,
  val userId: String,
  val deviceId: String,
  val takenAtISO: String
)

data class EncryptPhotosItemResult(
  val iv: ByteArray,
  val fileKey: ByteArray,
  val index: ByteArray
)

class PhotosLocalSyncManager {

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
  fun processItem(config: PhotosItemProcessConfig): String {
    val totalTimeMeasurer = Performance.measureTime()
    val previewTimeMeasurer = Performance.measureTime()
    Logger.info("Processing PhotosItem at ${config.sourcePath}")
    // 1. Generate a preview for the item
    val previewBitmap = photosPreviewGenerator.generatePreview(config.previewSourceStream)
    val previewFile = photosPreviewGenerator.writeBitmapToFile(previewBitmap, config.previewDestination)

    Logger.info("Generated preview at ${config.previewDestination} in ${previewTimeMeasurer.getMs()}ms")

    val previewEncryptTimeMeasurer = Performance.measureTime()
    Logger.info("Encrypting preview")
    // 2. Encrypt the preview file
    val encryptPreviewResult = encryptPhotosItem(
      config.mnemonic,
      config.bucketId,
      FileInputStream(previewFile),
      FileOutputStream(config.encryptedPreviewDestination)
    )

    Logger.info("Preview encrypted in ${previewEncryptTimeMeasurer.getMs()}ms")

    val uploadPreviewTimeMeasurer = Performance.measureTime()

    Logger.info("Uploading preview")
    // 3. Preview is encrypted, upload
    val uploadPreviewResult = upload.uploadFile(
      UploadFileConfig(
      bucketId = config.bucketId,
      mnemonic = config.mnemonic,
      encryptedFilePath = config.encryptedPreviewDestination,
      iv = encryptPreviewResult.iv,
      key = encryptPreviewResult.fileKey,
      index = encryptPreviewResult.index
    ))

    Logger.info("Preview uploaded in ${uploadPreviewTimeMeasurer.getMs()}ms")
    val originalEncryptTimeMeasurer = Performance.measureTime()

    // 4. Encrypt the original Photo
    val encryptOriginalResult = encryptPhotosItem(
      config.mnemonic,
      config.bucketId,
      config.originalSourceStream,
      config.encryptedOriginalStream
    )
    Logger.info("Original photo encrypted in ${originalEncryptTimeMeasurer.getMs()}ms")

    val uploadOriginalTimeMeasurer = Performance.measureTime()
    Logger.info("Uploading original")
    // 5. Original is encrypted, upload
    val uploadOriginalResult = upload.uploadFile(
      UploadFileConfig(
        bucketId = config.bucketId,
        mnemonic = config.mnemonic,
        encryptedFilePath = config.encryptedOriginalDestination,
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

    val name = FS.getFilenameFromPath(config.sourcePath)
    val type = FS.getFileTypeFromPath(config.sourcePath)

    val exif = ExifInterface(config.sourcePath)

    Logger.info("Exif width ${exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)}")
    Logger.info("Exif height ${exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)}")

    val options: BitmapFactory.Options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(config.sourcePath, options)
    Logger.info("Photo dimensions ${options.outWidth}w x ${options.outHeight}h")
    val hasher = hash.getSha256Hasher()
    val contentHash = hash.getHashFromStream(FileInputStream(config.sourcePath), hash.getSha256Hasher())

    Logger.info("Content hash is ${CryptoUtils.bytesToHex(contentHash)}")
    hasher.update(config.userId.toByteArray(Charsets.UTF_8))

    Logger.info("Image name is $name")
    hasher.update(name.toByteArray(Charsets.UTF_8))

    hasher.update(config.takenAtISO.toByteArray())
    hasher.update(CryptoUtils.bytesToHex(contentHash).toByteArray())

    val hash = CryptoUtils.bytesToHex(hasher.digest())

    Logger.info("Photo hash $hash")
    val createPhotoPayload = CreatePhotoPayload(
      name = name,
      userId = config.photosUserId,
      deviceId = config.deviceId,
      fileId = uploadOriginalResult.fileId,
      width = options.outWidth,
      height = options.outHeight,
      itemType = "PHOTO",
      size = uploadOriginalResult.size,
      type = type,
      hash = hash,
      networkBucketId = config.bucketId,
      takenAt = config.takenAtISO,
      previews = listOf(previewPayload),
      previewId = uploadPreviewResult.fileId
    )
    val result = photosApi.createPhoto(createPhotoPayload)

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
}
