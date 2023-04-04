package com.internxt.mobilesdk.core


import android.os.Build
import androidx.annotation.RequiresApi
import com.internxt.mobilesdk.config.MobileSdkConfigKey
import com.internxt.mobilesdk.config.MobileSdkConfigLoader
import com.internxt.mobilesdk.data.*
import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.services.crypto.Hash
import com.internxt.mobilesdk.services.network.HttpClient
import com.internxt.mobilesdk.utils.*
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.transform.OutputKeys.MEDIA_TYPE
import kotlin.random.Random
import java.util.Base64

data class UploadFileConfig(
  val bucketId: String,
  val mnemonic:String,
  val plainStream: InputStream,
  val encryptedStream: OutputStream,
  val encryptedFilePath: String,
)

data class UploadFileResult(
  val hash: ByteArray,
  val path: String,
  val fileId: String,
  val size: Long
)


class Upload {

  private val encrypt = Encrypt()
  private val hash = Hash()
  private val ivSize = 32

  @RequiresApi(Build.VERSION_CODES.O)
  @Throws(
    InvalidMnemonicException::class,
    EmptyFileException::class,
    UrlNotReceivedFromNetworkException::class
  )
  fun uploadFile(
   config: UploadFileConfig
  ): UploadFileResult {

    // 1. Validate the mnemonic
    val mnemonicIsValid = CryptoUtils.validateMnemonic(config.mnemonic);
    if(!mnemonicIsValid) {
      throw InvalidMnemonicException("Provided mnemonic is not valid")
    }

    // 2. Create index, iv and fileKey to encrypt the file
    val index = CryptoUtils.getRandomBytes(ivSize)
    val hexIv = CryptoUtils.bytesToHex(index).slice(0..31)
    val iv = CryptoUtils.hexToBytes(hexIv)

    Logger.info("Using iv ${CryptoUtils.bytesToHex(iv)}")
    Logger.info("Using mnemonic ${config.mnemonic}")
    Logger.info("Using bucketId ${config.bucketId}")
    Logger.info("Using index ${CryptoUtils.bytesToHex(index)}")
    val fileKey = encrypt.generateFileKey(config.mnemonic, config.bucketId, index)


    Logger.info("Generated fileKey is ${CryptoUtils.bytesToHex(fileKey)}")
    Logger.info("Generated iv is ${CryptoUtils.bytesToHex(iv)}")
    // 3. Encrypt the file using AES CTR NoPadding
    encrypt.encryptFromStream(
      config.plainStream,
      config.encryptedStream,
      EncryptConfig(
        mode = EncryptMode.AesCTRNoPadding,
        key = fileKey,
        iv = iv
    ))

    // 4. Check if the file has content
    if(FS.fileIsEmpty(config.encryptedFilePath)) {
      throw EmptyFileException("File at $config.encryptedFilePath is empty")
    }

    Logger.info("Encrypted as BASE64 ${Base64.getEncoder().encodeToString(File(config.encryptedFilePath).readBytes())}")

    // 5. Get the content hash
    val fileHash = encrypt.getFileContentHash(FileInputStream(config.encryptedFilePath))

    Logger.info("Hash is ${CryptoUtils.bytesToHex(fileHash)}")

    val encryptedFileSize = File(config.encryptedFilePath).length()
    val uploads = listOf(NetworkUploadPayload(
      index = 0,
      size = encryptedFileSize
    ))
    val encryptedFile = File(config.encryptedFilePath)
    val startUploadResult = startNetworkUpload(
        config.bucketId,
        StartUploadPayload(uploads),
        parts = 1,
    ) ?:throw ApiResponseException("Unexpected empty API Response")



    val upload = startUploadResult.uploads[0]

    if (upload.url == null) {
      throw UrlNotReceivedFromNetworkException();
    }
    if (upload.UploadId == null) {
      throw ApiResponseException("Response does not contain UploadId");
    }

    uploadFileToStorage(upload.url, encryptedFile)


    val shard = NetworkUploadShard(
      uuid = upload.uuid,
      hash = CryptoUtils.bytesToHex(fileHash)
    )

    val finishedUpload = finishNetworkUpload(config.bucketId, FinishUploadPayload(
        index = CryptoUtils.bytesToHex(index),
        shards = listOf(shard)
    ))

    if(finishedUpload?.id == null) {
      throw ApiResponseException("Response does not contain FileId");
    }

    return UploadFileResult(
      fileId = finishedUpload.id,
      hash = fileHash,
      path = config.encryptedFilePath,
      size = encryptedFileSize
    )
  }


  private fun startNetworkUpload(bucketId: String, startUploadPayload: StartUploadPayload, parts: Number = 1): StartUploadResponse? {
    val startUploadAdapter = JsonUtils.moshi.adapter(StartUploadResponse::class.java)
    val startUploadPayloadAdapter = JsonUtils.moshi.adapter(StartUploadPayload::class.java)
    val base = MobileSdkConfigLoader.getConfig(MobileSdkConfigKey.BRIDGE_URL)
    val url = "${base}/v2/buckets/${bucketId}/files/start?multiparts=${parts}"
    val payload = startUploadPayloadAdapter.toJson(startUploadPayload)
    Logger.info("Starting upload with payload $payload")
    val request = Request.Builder().header("Authorization", getCredentials()).post(payload.toRequestBody()).url(url).build()
    val response = HttpClient.fetchSync(request)
    return startUploadAdapter.fromJson(response.body!!.source())
  }

  private fun finishNetworkUpload(bucketId: String, finishUploadPayload: FinishUploadPayload): FinishUploadResponse? {
    val finishUploadAdapter = JsonUtils.moshi.adapter(FinishUploadResponse::class.java)
    val uploadFailedAdapter = JsonUtils.moshi.adapter(UploadFailedResponse::class.java)
    val finishUploadPayloadAdapter = JsonUtils.moshi.adapter(FinishUploadPayload::class.java)
    val base = MobileSdkConfigLoader.getConfig(MobileSdkConfigKey.BRIDGE_URL)
    val url = "${base}/v2/buckets/${bucketId}/files/finish"

    val payload = finishUploadPayloadAdapter.toJson(finishUploadPayload)
    Logger.info("Finishing upload with payload $payload")
    val request = Request.Builder().header("Authorization", getCredentials()).post(payload.toRequestBody()).url(url).build()
    val response = HttpClient.fetchSync(request)

    if(!response.isSuccessful) {
      val body = uploadFailedAdapter.fromJson(response.body!!.source())

      Logger.error("Failed upload finish ${body!!.error}")
      if(body?.error?.startsWith("E11000 duplicate key error") == true) {
        throw DuplicatedUpload(body.error)
      }

      throw ApiResponseException("Finish upload not successful")
    }
    return finishUploadAdapter.fromJson(response.body!!.source())
  }

  private fun getCredentials(): String {
    return "Basic ${MobileSdkConfigLoader.getConfig(MobileSdkConfigKey.BRIDGE_AUTH_BASE64)}"
  }

  @Throws(IOException::class)
  private fun uploadFileToStorage(url: String, file: File) {

    val request = Request.Builder().put(file.asRequestBody()).addHeader("content-type", "application/octet-stream").url(url).build()
    val response = HttpClient.fetchSync(request)

    if(!response.isSuccessful) {
      throw IOException("Response for storage not successful")
    }
  }

}



