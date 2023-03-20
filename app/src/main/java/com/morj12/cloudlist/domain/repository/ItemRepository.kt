package com.morj12.cloudlist.domain.repository

import com.morj12.cloudlist.domain.entity.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    fun getItems(cartId: Long): Flow<List<Item>>

    suspend fun getItems(cartId: Long, name: String): List<Item>

    suspend fun insertItem(item: Item)

    suspend fun updateItem(item: Item)

    suspend fun deleteItem(item: Item)
}