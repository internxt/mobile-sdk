package com.internxt.mobilesdk.services.database.photos

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update


@Dao
interface SyncedPhotosDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertPhotosItem(item: SyncedPhotosItem)


  @Query("SELECT * FROM remote_photos WHERE id = :id")
  fun getSyncedPhotosItemById(id: String): SyncedPhotosItem?

  @Query("SELECT * FROM remote_photos WHERE name = :name")
  fun getSyncedPhotosItemByName(name: String): SyncedPhotosItem?

  @Query("SELECT * FROM remote_photos")
  fun getAll(): List<SyncedPhotosItem>

  @Update
  fun updateSyncedPhotosItem(syncedPhotosItem: SyncedPhotosItem)

  @Transaction
  fun updateOrCreateSyncedPhotosItem(photosItem: SyncedPhotosItem) {
    val syncedPhotosItem = getSyncedPhotosItemById(photosItem.id)

    if(syncedPhotosItem == null) {
      insertPhotosItem(photosItem)
    } else {
      updateSyncedPhotosItem(photosItem)
    }
  }
}



