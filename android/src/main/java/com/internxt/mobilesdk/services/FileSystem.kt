package com.internxt.mobilesdk.services
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.facebook.react.bridge.ReactApplicationContext
import com.internxt.mobilesdk.utils.FileAccessRejectionException
import java.io.*
import com.internxt.mobilesdk.utils.Logger
import kotlin.io.path.Path


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


    if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

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
}
