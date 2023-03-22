package com.morj12.cloudlist.data.local.repository


import com.morj12.cloudlist.data.local.AppDatabase
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.domain.mapper.Mapper
import com.morj12.cloudlist.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepositoryImpl(db: AppDatabase): CartRepository {

    private val dao = db.cartDao()

    override fun getCarts(): Flow<List<Cart>> {
        return dao.getCarts().map(Mapper::mapCartListToEntity)
    }

    override suspend fun insertCart(cart: Cart) {
        dao.insertCart(Mapper.mapCartToDbModel(cart))
    }

    override suspend fun updateCart(cart: Cart) {
        dao.updateCart(Mapper.mapCartToDbModel(cart))
    }

    override suspend fun deleteCart(cart: Cart) {
        dao.deleteCart(Mapper.mapCartToDbModel(cart))
    }

}