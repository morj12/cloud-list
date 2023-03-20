package com.morj12.cloudlist.domain.repository

import com.morj12.cloudlist.domain.entity.Cart
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    fun getCarts(): Flow<List<Cart>>

    suspend fun insertCart(cart: Cart)

    suspend fun updateCart(cart: Cart)

    suspend fun deleteCart(cart: Cart)
}