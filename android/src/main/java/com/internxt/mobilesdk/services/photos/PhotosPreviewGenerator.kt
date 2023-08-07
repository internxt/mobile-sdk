package com.internxt.mobilesdk.services.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Size
import androidx.core.os.CancellationSignal
import com.internxt.mobilesdk.data.photos.DevicePhotosItemType
import com.internxt.mobilesdk.utils.Logger
import java.io.*



class PhotosPreviewGenerator {


  fun writeBitmapToFile(bitmap: Bitmap, destination: String): File {

    val file = File(destination)
    val stream: OutputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG,85,stream)
    stream.flush()
    stream.close()
    return file
  }



  fun generateImagePreview(inputStream: InputStream, width: Int): Bitmap {

      val bitmap = BitmapFactory.decodeStream(inputStream)
      val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
      val resizedHeight = (width / aspectRatio).toInt()


      return ThumbnailUtils.extractThumbnail(bitmap, width, resizedHeight)
  }

  fun generateVideoPreview(path: String, width: Int): Bitmap? {
    val mediaMetadataRetriever = MediaMetadataRetriever()
    try {
      return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND)
      //return Bitmap.createScaledBitmap(bitmap, width, height, false)
    } catch (e: Exception) {
      throw e
    } finally {
      mediaMetadataRetriever.release()
    }
  }
}
