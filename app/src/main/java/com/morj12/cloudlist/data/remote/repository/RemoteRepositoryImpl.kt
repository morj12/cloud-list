package com.morj12.cloudlist.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.domain.entity.Channel
import com.morj12.cloudlist.domain.entity.Item
import com.morj12.cloudlist.domain.repository.RemoteRepository

class RemoteRepositoryImpl : RemoteRepository {

    private var db = FirebaseFirestore.getInstance()

    override fun loadCarts(channelName: String, callback: (QuerySnapshot) -> Unit) {
        db.collection("channels")
            .document(channelName)
            .collection("carts")
            .get()
            .addOnSuccessListener(callback)
    }

    override fun connectToChannel(name: String, key: Long, callback: (QuerySnapshot) -> Unit) {
        db.collection("channels")
            .whereEqualTo("name", name)
            .whereEqualTo("key", key)
            .get()
            .addOnSuccessListener(callback)
    }

    override fun saveChannel(channel: Channel, email: String) {
        db.collection("users")
            .document(email)
            .set(
                mapOf(
                    "channelKey" to channel.key,
                    "channelName" to channel.name,
                    "email" to email
                )
            )
    }

    override fun getChannel(name: String, callback: (QuerySnapshot) -> Unit) {
        db.collection("channels")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener(callback)
    }

    override fun createChannel(channel: Channel, callback: () -> Unit) {
        db.collection("channels")
            .document(channel.name)
            .set(channel)
            .addOnSuccessListener { callback() }
    }

    override fun getLastChannel(email: String, callback: (QuerySnapshot) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener(callback)
    }

    override fun deleteCart(channelName: String, timestamp: String, callback: () -> Unit) {
        db.collection("channels")
            .document(channelName)
            .collection("carts")
            .document(timestamp)
            .delete()
            .addOnSuccessListener { callback() }
    }

    override fun createCart(channelName: String, cart: Cart) {
        db.collection("channels")
            .document(channelName)
            .collection("carts")
            .document(cart.timestamp.toString())
            .set(
                mapOf(
                    "timestamp" to cart.timestamp,
                    "price" to cart.price
                )
            )
    }

    override fun setupRealtimeChannelUpdates(
        channelName: String,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration {
        return db.collection("channels")
            .document(channelName)
            .collection("carts")
            .addSnapshotListener { value, _ -> callback(value) }
    }

    override fun loadItems(
        channelName: String,
        timestamp: String,
        callback: (QuerySnapshot) -> Unit
    ) {
        db.collection("channels")
            .document(channelName)
            .collection("carts")
            .document(timestamp)
            .collection("items")
            .get()
            .addOnSuccessListener(callback)
    }

    override fun updateCartPrice(
        channelName: String,
        timestamp: String,
        price: Double,
        callback: () -> Unit
    ) {
        db.collection("channels")
            .document(channelName)
            .collection("carts")
            .document(timestamp)
            .update("price", price)
            .addOnSuccessListener { callback() }
    }

    override fun upsertItem(channelName: String, timestamp: String, item: Item) {
        db.collection("channels")
            .document(channelName)
            .collection("carts")
            .document(timestamp)
            .collection("items")
            .document(item.name)
            .set(
                mapOf(
                    "name" to item.name,
                    "price" to item.price,
                    "isChecked" to item.isChecked
                )
            )
    }

    override fun deleteItem(channelName: String, timestamp: String, item: Item) {
        db.collection("channels")
            .document(channelName)
            .collection("carts")
            .document(timestamp)
            .collection("items")
            .document(item.name)
            .delete()
    }

    override fun setupRealtimeCartUpdates(
        channelName: String,
        timestamp: String,
        callback: (QuerySnapshot?) -> Unit
    ): ListenerRegistration {
        return db.collection("channels")
            .document(channelName)
            .collection("carts")
            .document(timestamp)
            .collection("items")
            .addSnapshotListener { value, _ -> callback(value) }
    }
}