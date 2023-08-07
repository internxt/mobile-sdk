package com.internxt.mobilesdk.services.database.photos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface PhotosDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertPhotosItem(item: PhotosDBItem)

  @Query("SELECT * FROM photos WHERE name = :photoName")
  fun getPhotosItemByName(photoName: String): PhotosDBItem?

  @Update
  fun updatePhotosItem(photosItem: PhotosDBItem)

  @Transaction
  fun updateOrCreatePhotosItem(photosItem: PhotosDBItem) {
    val photosDBItem = getPhotosItemByName(photosItem.name)

    if(photosDBItem == null) {
      insertPhotosItem(photosItem)
    } else {
      if(photosDBItem.status == "DEVICE_ONLY" && photosItem.status !== "DEVICE_ONLY") {
        updatePhotosItem(photosItem)
      }
    }
  }
}
