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

  /** Check if a file exists at a given path */
  fun fileExists(path: String): Boolean {
    return File(path).exists()
  }

  /**
   * Delete a file only if it exists
   *
   * @return true if the file was successfully deleted, false otherwise
   */
  fun unlinkIfExists(path: String): Boolean {
    val file = File(path)
    if (!file.exists()) return false

    return try {
      file.delete()
    } catch (e: IOException) {
      e.printStackTrace()
      false
    }
  }

  /**
   * Converts a file path to a proper Uri. If no scheme is present, assumes it's a local file path.
   */
  @Throws(FileAccessRejectionException::class)
  fun getFileUri(filepath: String, isDirectoryAllowed: Boolean): Uri {
    var uri = Uri.parse(filepath)
    if (uri.scheme == null) {
      val file = File(filepath)
      if (!isDirectoryAllowed && file.isDirectory) {
        throw FileAccessRejectionException("You don't have access to $filepath")
      }
      uri = Uri.parse("file://$filepath")
    }
    return uri
  }

  /** Create a new empty file at the given path */
  fun createFile(path: String): Boolean {
    return File(path).createNewFile()
  }

  /** Extracts the filename (without extension) from a full path */
  fun getFilenameFromPath(path: String): String {
    val name = path.substring(path.lastIndexOf(File.separator))
    val index = name.lastIndexOf(".")
    return name.substring(1, index)
  }

  /** Extracts the file extension from a full path */
  fun getFileTypeFromPath(path: String): String {
    val filename = path.substring(path.lastIndexOf(File.separator))
    val index = filename.lastIndexOf(".")
    if (index == -1) throw Exception("This file does not have an extension")
    return filename.substring(index + 1)
  }

  /** Copies all relevant EXIF tags from one image stream to another */
  @Throws(IOException::class)
  fun copyExif(oldFileStream: InputStream, newFileStream: InputStream) {
    val oldExif = ExifInterface(oldFileStream)
    val newExif = ExifInterface(newFileStream)

    val attributes =
            arrayOf(
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

    for (attr in attributes) {
      val value = oldExif.getAttribute(attr)
      if (value != null) newExif.setAttribute(attr, value)
    }
    newExif.saveAttributes()
  }

  /** Checks if a file is empty (has no content) */
  fun fileIsEmpty(path: String): Boolean {
    val file = File(path)
    return try {
      BufferedReader(FileReader(file)).use { it.readLine() == null }
    } catch (e: IOException) {
      e.printStackTrace()
      true
    }
  }

  /** Generates a unique filename by appending (1), (2), ... if the file already exists */
  fun getUniqueFilename(directory: File, filename: String): String {
    var uniqueFilename = filename
    var counter = 0
    val nameWithoutExtension = filename.substringBeforeLast('.', filename)
    val extension = filename.substringAfterLast('.', "")

    while (File(directory, uniqueFilename).exists()) {
      counter++
      uniqueFilename =
              if (extension.isNotEmpty()) {
                "$nameWithoutExtension ($counter).$extension"
              } else {
                "$nameWithoutExtension ($counter)"
              }
    }
    return uniqueFilename
  }

  /**
   * Generates a unique filename for MediaStore (Android 10+) by querying existing files
   * in the Downloads directory and appending (1), (2), ... before the extension if needed.
   */
  fun getUniqueFilenameForMediaStore(contentResolver: ContentResolver, filename: String): String {
    var uniqueFilename = filename
    var counter = 0
    val nameWithoutExtension = filename.substringBeforeLast('.', filename)
    val extension = filename.substringAfterLast('.', "")

    while (fileExistsInDownloads(contentResolver, uniqueFilename)) {
      counter++
      uniqueFilename =
              if (extension.isNotEmpty()) {
                "$nameWithoutExtension ($counter).$extension"
              } else {
                "$nameWithoutExtension ($counter)"
              }
    }
    return uniqueFilename
  }

  /**
   * Checks if a file with the given name exists in the Downloads directory via MediaStore.
   */
  private fun fileExistsInDownloads(contentResolver: ContentResolver, filename: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      return false
    }

    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(filename)

    contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
    )?.use { cursor ->
      return cursor.count > 0
    }
    return false
  }

  /**
   * Saves a file to the device's Downloads directory.
   * - On Android 10+ (Q+): Uses Scoped Storage with ContentResolver
   * - On Android 9 and below: Uses direct file access with unique filename to prevent overwrites
   */
  @Throws(Exception::class)
  fun saveFileToDownloadsDirectory(context: ReactApplicationContext, originalFilePath: String) {
    // Extract clean filename without leading slash
    val filename = originalFilePath.substring(originalFilePath.lastIndexOf(File.separator) + 1)

    val fileInputStream =
            context.contentResolver.openInputStream(getFileUri(originalFilePath, true))
                    ?: throw Exception("Cannot open input stream at path $originalFilePath")

    val buffer = ByteArray(8 * 1024) // 8KB buffer
    var bytesRead: Int

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentValues = ContentValues()
      val mimeType = getMimeType(context.contentResolver, originalFilePath)
              ?: "application/octet-stream" // Fallback for empty or unknown files

      // Generate unique filename to avoid MediaStore's default behavior of appending suffix after extension
      val cleanFilename = filename.removePrefix(File.separator)
      val uniqueFilename = getUniqueFilenameForMediaStore(context.contentResolver, cleanFilename)

      contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, uniqueFilename)
      contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
      contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)

      Logger.info("Mime type: ${mimeType ?: "No mime"}")

      val fileUri =
              context.contentResolver.insert(
                      MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                      contentValues
              )
                      ?: throw Exception("FileUri not inserted in content resolver")

      Logger.info("File inserted in content database with uri: $fileUri")

      val outputStream =
              context.contentResolver.openOutputStream(fileUri)
                      ?: throw Exception("Cannot open output stream for uri: $fileUri")

      try {
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
          outputStream.write(buffer, 0, bytesRead)
        }
      } catch (e: IOException) {
        e.printStackTrace()
        throw e
      } finally {
        fileInputStream.close()
        outputStream.close()
      }
    } else {
      val downloadsDir =
              Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      val resolvedFilename = getUniqueFilename(downloadsDir, filename.removePrefix(File.separator))

      val destination = File(downloadsDir, resolvedFilename)
      val outputStream = FileOutputStream(destination)

      try {
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
          outputStream.write(buffer, 0, bytesRead)
        }
      } catch (e: IOException) {
        e.printStackTrace()
        throw e
      } finally {
        fileInputStream.close()
        outputStream.close()
      }
    }

    Logger.info("Closed stream")
  }

  /** Gets the MIME type of a file from its Uri */
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
