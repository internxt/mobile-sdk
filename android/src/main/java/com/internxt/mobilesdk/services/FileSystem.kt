package com.internxt.mobilesdk.services

import android.content.ContentResolver
import android.content.ContentValues
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.facebook.react.bridge.ReactApplicationContext
import com.internxt.mobilesdk.utils.FileAccessRejectionException
import com.internxt.mobilesdk.utils.Logger
import java.io.*


// This class is called FS to avoid naming conflicts with Java FileSystem class
object FS {

  /**
   * Check if a file exists at a given path
   */
  fun fileExists(path: String): Boolean {
    return File(path).exists()
  }


  /**
   * Unlink a file only if it exists
   *
   * Return true if the file is successfully deleted, false otherwise
   */
  fun unlinkIfExists(path: String): Boolean {
    val file = File(path)
    val exists = this.fileExists(path)

    try {
      if(exists) {
        return file.delete()
      }

    } catch (exception: IOException) {
      exception.printStackTrace()
    }

    return false
  }

  @Throws(FileAccessRejectionException::class)
  public fun getFileUri(filepath: String, isDirectoryAllowed: Boolean): Uri {
    var uri: Uri = Uri.parse(filepath)
    if (uri.getScheme() == null) {
      // No prefix, assuming that provided path is absolute path to file
      val file = File(filepath)
      if (!isDirectoryAllowed && file.isDirectory) {
        throw FileAccessRejectionException(
          "You don't have access to $filepath"
        )
      }
      uri = Uri.parse("file://$filepath")
    }
    return uri
  }

  fun createFile(path: String) : Boolean{
    return File(path).createNewFile()
  }

  fun getFilenameFromPath(path: String): String {
    val name = path.substring(path.lastIndexOf(File.separator));
    val index = name.lastIndexOf(".")
    return name.substring(1, index)
  }

  fun getFileTypeFromPath(path: String): String {
    val filename = path.substring(path.lastIndexOf(File.separator));
    val index = filename.lastIndexOf(".")
    if(index == -1 ) throw Exception("This file does not have an extension")
    return filename.substring(index+1)
  }

  @Throws(IOException::class)
  fun copyExif(oldFileStream: InputStream, newFileStream: InputStream) {
    val oldExif = ExifInterface(oldFileStream)
    val attributes = arrayOf(
      ExifInterface.TAG_F_NUMBER,
      ExifInterface.TAG_DATETIME,
      ExifInterface.TAG_DATETIME_DIGITIZED,
      ExifInterface.TAG_EXPOSURE_TIME,
      ExifInterface.TAG_FLASH,
      ExifInterface.TAG_FOCAL_LENGTH,
      ExifInterface.TAG_GPS_ALTITUDE,
      ExifInterface.TAG_GPS_ALTITUDE_REF,
      ExifInterface.TAG_GPS_DATESTAMP,
      ExifInterface.TAG_GPS_LATITUDE,
      ExifInterface.TAG_GPS_LATITUDE_REF,
      ExifInterface.TAG_GPS_LONGITUDE,
      ExifInterface.TAG_GPS_LONGITUDE_REF,
      ExifInterface.TAG_GPS_PROCESSING_METHOD,
      ExifInterface.TAG_GPS_TIMESTAMP,
      ExifInterface.TAG_IMAGE_LENGTH,
      ExifInterface.TAG_IMAGE_WIDTH,
      ExifInterface.TAG_ISO_SPEED_RATINGS,
      ExifInterface.TAG_MAKE,
      ExifInterface.TAG_MODEL,
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.TAG_SUBSEC_TIME,
      ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
      ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
      ExifInterface.TAG_WHITE_BALANCE
    )
    val newExif = ExifInterface(newFileStream)
    for (i in attributes.indices) {
      val value = oldExif.getAttribute(attributes[i])
      if (value != null) newExif.setAttribute(attributes[i], value)
    }
    newExif.saveAttributes()
  }
  fun fileIsEmpty(path: String): Boolean {
    val file = File(path)
    try {
      val br = BufferedReader(FileReader(file))
      if (br.readLine() == null) {
        return true
      }

      return false
    } catch (e: IOException) {
      e.printStackTrace()
      return true
    }
  }


  @Throws(NoSuchFileException::class, FileAlreadyExistsException::class, IOException::class)
  fun saveFileToDownloadsDirectory(context: ReactApplicationContext, originalFilePath: String) {
    val filename =  originalFilePath.substring(originalFilePath.lastIndexOf(File.separator));
    val fileInputStream: InputStream =
      context.contentResolver.openInputStream(getFileUri(originalFilePath, true))
        ?: throw Exception("Cannot open Input stream at path $originalFilePath")


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

      val contentValues = ContentValues();

      contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
      val mimeType = getMimeType(context.contentResolver, originalFilePath)
      Logger.info("Mime type")
      if (mimeType != null) {
        Logger.info(mimeType)
      } else {
        Logger.info("No mime")
      }
      contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
      contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
      context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
          ?: throw Exception("FileUri not inserted in content resolver");

      Logger.info("File inserted in content database")
    }

    val buffer = ByteArray(8 * 1024) // 8KB buffer
    var bytesRead: Int

    val downloadsDir =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()

    val destination = File(downloadsDir, filename)
    val outputStream = FileOutputStream(destination)

    try {
      while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
      }
    } catch (e: IOException) {
      e.printStackTrace()
    } finally {
      fileInputStream.close()
      outputStream.close()
    }

    Logger.info("Closed stream")

  }

  fun getMimeType(contentResolver: ContentResolver, uri: String): String? {
    return contentResolver.getType(getFileUri(uri, true))
  }

  fun getExtension(filePath: String): String? {
    val file = File(filePath)
    val fileName = file.name
    val dotIndex = fileName.lastIndexOf('.')
    return if (dotIndex > 0 && dotIndex < fileName.length - 1) {
      fileName.substring(dotIndex + 1)
    } else {
      null
    }
  }
}
