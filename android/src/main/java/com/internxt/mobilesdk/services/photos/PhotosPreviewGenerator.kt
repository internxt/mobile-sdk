package com.internxt.mobilesdk.services.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Size
import androidx.core.os.CancellationSignal
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
  fun generatePreview(inputStream: InputStream): Bitmap {

    // TODO, add dynamic height and previews for videos
    return generateImagePreview(inputStream, 512, 512)
  }

  private fun generateImagePreview(inputStream: InputStream, width: Int, height: Int): Bitmap {

      return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(inputStream), width, height)
  }
}
