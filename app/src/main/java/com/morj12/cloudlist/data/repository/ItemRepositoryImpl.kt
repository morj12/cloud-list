package com.morj12.cloudlist.data.repository

import com.morj12.cloudlist.data.AppDatabase
import com.morj12.cloudlist.domain.entity.Item
import com.morj12.cloudlist.domain.mapper.Mapper
import com.morj12.cloudlist.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemRepositoryImpl(db: AppDatabase): ItemRepository {

    private val dao = db.itemDao()

    override fun getItems(cartId: Long): Flow<List<Item>> {
        return dao.getItems(cartId).map(Mapper::mapItemListToEntity)
    }

    override suspend fun getItems(cartId: Long, name: String): List<Item> {
        return Mapper.mapItemListToEntity(dao.getItems(cartId, name))
    }

    override suspend fun insertItem(item: Item) {
        dao.insertItem(Mapper.mapItemToDbModel(item))
    }

    override suspend fun updateItem(item: Item) {
        dao.updateItem(Mapper.mapItemToDbModel(item))
    }

    override suspend fun deleteItem(item: Item) {
        dao.deleteItem(Mapper.mapItemToDbModel(item))
    }
}