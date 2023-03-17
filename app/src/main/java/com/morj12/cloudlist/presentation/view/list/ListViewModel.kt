package com.morj12.cloudlist.presentation.view.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.domain.entity.Channel
import com.morj12.cloudlist.utils.Datetime

class ListViewModel(application: Application) : AndroidViewModel(application) {

    private var mAuth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()

    private val _userEmail = MutableLiveData("")
    val userEmail: LiveData<String>
        get() = _userEmail

    private val _channel = MutableLiveData<Channel?>()
    val channel: LiveData<Channel?>
        get() = _channel

    private val _carts = MutableLiveData<List<Cart>>()
    val carts: LiveData<List<Cart>>
        get() = _carts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _userLastChannel = MutableLiveData<Channel?>()
    val userLastChannel: LiveData<Channel?>
        get() = _userLastChannel

    private var localCarts = mutableListOf<Cart>()

    private var updates: ListenerRegistration? = null

    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    fun signOut() {
        _userEmail.value = ""
        mAuth.signOut()
    }

    fun setChannel(channel: Channel?) {
        _channel.value = channel
        if (channel == null) stopRealtimeUpdates()
    }

    private fun setCarts(carts: List<Cart>) {
        localCarts = carts as MutableList<Cart>
        _carts.value = carts
    }

    fun loadCartsFromDb() {
        db.collection("channels")
            .document(channel.value!!.name)
            .collection("carts")
            .get()
            .addOnSuccessListener {
                setCarts(it.map { doc -> doc.toObject(Cart::class.java) })
            }
    }

    fun connectToChannel(name: String, key: Long) {
        db.collection("channels")
            .whereEqualTo("name", name)
            .whereEqualTo("key", key)
            .get()
            .addOnSuccessListener {
                val channel = it.firstOrNull()?.toObject(Channel::class.java)
                if (channel == null) {
                    _error.value = "Channel not found"
                } else {
                    setChannel(channel)
                    saveLastChannel(channel)
                }
            }
    }

    private fun saveLastChannel(channel: Channel) {
        db.collection("users")
            .document(userEmail.value!!)
            .set(
                mapOf(
                    "channelKey" to channel.key,
                    "channelName" to channel.name,
                    "email" to userEmail.value!!
                )
            )
    }

    fun createNewChannel(name: String, key: Long) {
        db.collection("channels")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener {
                if (it.firstOrNull() == null) {
                    val newChannel = Channel(name, key)
                    db.collection("channels")
                        .document(name)
                        .set(newChannel)
                        .addOnSuccessListener {
                            setChannel(newChannel)
                        }
                }
            }
    }

    fun searchForLastChannel() {
        db.collection("users")
            .whereEqualTo("email", userEmail.value)
            .get()
            .addOnSuccessListener {
                var channel: Channel? = null
                val channelData = it.firstOrNull()
                if (channelData != null) {
                    channel = Channel(
                        name = channelData["channelName"] as String,
                        key = channelData["channelKey"] as Long
                    )
                }
                _userLastChannel.value = channel
            }
    }

    fun deleteCart(cart: Cart) {
        db.collection("channels")
            .document(channel.value!!.name)
            .collection("carts")
            .document(cart.timestamp.toString())
            .delete()
    }

    fun createNewCart() {
        val time = Datetime.getTimeStamp(Datetime.getCurrentTime())
        val cart = Cart(time, 0)
        db.collection("channels")
            .document(channel.value!!.name)
            .collection("carts")
            .document(time.toString())
            .set(
                mapOf(
                    "timestamp" to cart.timestamp,
                    "price" to cart.price
                )
            )
    }

    fun setupRealtimeUpdates() {
        updates = db.collection("channels")
            .document(channel.value!!.name)
            .collection("carts")
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    localCarts = value.map { it.toObject(Cart::class.java) } as MutableList<Cart>
                    _carts.value = localCarts
                }
            }
    }

    fun stopRealtimeUpdates() {
        updates = null
    }

}