package com.morj12.cloudlist.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.morj12.cloudlist.data.dbmodel.ItemDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM item WHERE cartId = :cartId")
    fun getItems(cartId: Long): Flow<List<ItemDbModel>>

    @Query("SELECT * FROM item WHERE cartId = :cartId AND name = :name")
    suspend fun getItems(cartId: Long, name: String): List<ItemDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemDbModel)

    @Update
    suspend fun updateItem(item: ItemDbModel)

    @Delete
    suspend fun deleteItem(item: ItemDbModel)

}