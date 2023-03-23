package com.morj12.cloudlist.domain.repository

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.domain.entity.Channel
import com.morj12.cloudlist.domain.entity.Item

interface RemoteRepository {

    fun loadCarts(channelName: String, callback: (QuerySnapshot) -> Unit)

    fun connectToChannel(name: String, key: Long, callback: (QuerySnapshot) -> Unit)

    fun saveChannel(channel: Channel, email: String)

    fun getChannel(name: String, callback: (QuerySnapshot) -> Unit)

    fun createChannel(channel: Channel, callback: () -> Unit)

    fun getLastChannel(email: String, callback: (QuerySnapshot) -> Unit)

    fun deleteCart(channelName: String, timestamp: String, callback: () -> Unit)

    fun createCart(channelName: String, cart: Cart)

    fun setupRealtimeChannelUpdates(
        channelName: String,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration

    fun loadItems(channelName: String, timestamp: String, callback: (QuerySnapshot) -> Unit)

    fun updateCartPrice(
        channelName: String,
        timestamp: String,
        price: Double,
        callback: () -> Unit
    )

    fun upsertItem(channelName: String, timestamp: String, item: Item)

    fun deleteItem(channelName: String, timestamp: String, item: Item)

    fun setupRealtimeCartUpdates(
        channelName: String,
        timestamp: String,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration

}