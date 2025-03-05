package com.example.paging3demo.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.paging3demo.model.UnsplashImage

@Dao
interface UnsplashImageDao {
    // getAll Unsplash From Table
    // PageSource <pageNumber, UnsplashImage>   it means  룸 DB를 페이징 하고  모든 아이템을 안가져오게 한다
    @Query("SELECT * FROM unsplash_image_table")
    fun getAllImages(): PagingSource<Int, UnsplashImage>

    //
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImages(images: List<UnsplashImage>)

    @Query("DELETE FROM unsplash_image_table")
    suspend fun deleteAllImages()

}