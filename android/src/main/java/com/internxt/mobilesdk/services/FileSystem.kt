package com.internxt.mobilesdk.services
import android.net.Uri
import com.internxt.mobilesdk.utils.FileAccessRejectionException
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

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

      return false
    } catch (exception: IOException) {
      exception.printStackTrace()
      return false
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
}
