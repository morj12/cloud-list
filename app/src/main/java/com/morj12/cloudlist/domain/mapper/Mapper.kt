package com.morj12.cloudlist.domain.mapper

import com.morj12.cloudlist.data.local.dbmodel.CartDbModel
import com.morj12.cloudlist.data.local.dbmodel.ItemDbModel
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.domain.entity.Item

object Mapper {

    fun mapCartToDbModel(cart: Cart) = CartDbModel(
        cart.price,
        cart.timestamp
    )

    fun mapCartToEntity(cartDbModel: CartDbModel) = Cart(
        cartDbModel.timestamp,
        cartDbModel.price
    )

    fun mapItemToDbModel(item: Item) = ItemDbModel(
        item.name,
        item.price,
        item.isChecked,
        item.cartId,
        item.id
    )

    fun mapItemToEntity(itemDbModel: ItemDbModel) = Item(
        itemDbModel.name,
        itemDbModel.price,
        itemDbModel.isChecked,
        itemDbModel.cartId,
        itemDbModel.id
    )

    fun mapItemListToDbModel(items: List<Item>) = items.map(::mapItemToDbModel)

    fun mapItemListToEntity(items: List<ItemDbModel>) = items.map(::mapItemToEntity)

    fun mapCartListToDbModel(carts: List<Cart>) = carts.map(::mapCartToDbModel)

    fun mapCartListToEntity(carts: List<CartDbModel>) = carts.map(::mapCartToEntity)
}