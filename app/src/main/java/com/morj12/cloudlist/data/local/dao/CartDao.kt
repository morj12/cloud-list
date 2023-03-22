package com.morj12.cloudlist.data.local.dao

import androidx.room.*
import com.morj12.cloudlist.data.local.dbmodel.CartDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart")
    fun getCarts(): Flow<List<CartDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cart: CartDbModel)

    @Update
    suspend fun updateCart(cart: CartDbModel)

    @Delete
    suspend fun deleteCart(cart: CartDbModel)


}