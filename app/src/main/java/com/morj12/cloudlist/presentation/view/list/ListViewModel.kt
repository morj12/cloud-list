package com.morj12.cloudlist.presentation.view.list

import android.content.Context
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.morj12.cloudlist.R
import com.morj12.cloudlist.data.local.AppDatabase
import com.morj12.cloudlist.data.local.repository.CartRepositoryImpl
import com.morj12.cloudlist.data.local.repository.ItemRepositoryImpl
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.domain.entity.Channel
import com.morj12.cloudlist.domain.entity.Item
import com.morj12.cloudlist.domain.usecase.SignOutUseCase
import com.morj12.cloudlist.utils.Constants.ANONYMOUS_EMAIL
import com.morj12.cloudlist.utils.Datetime
import kotlinx.coroutines.launch

class ListViewModel(localDb: AppDatabase) : ViewModel() {

    private val itemRepo = ItemRepositoryImpl(localDb)
    private val cartRepo = CartRepositoryImpl(localDb)

    lateinit var mode: Mode

    private val signOutUseCase = SignOutUseCase()

    private var firestoreDb = FirebaseFirestore.getInstance()

    private val _userEmail = MutableLiveData("")
    val userEmail: LiveData<String>
        get() = _userEmail

    private val _channel = MutableLiveData<Channel?>()
    val channel: LiveData<Channel?>
        get() = _channel

    private val _userLastChannel = MutableLiveData<Channel?>()
    val userLastChannel: LiveData<Channel?>
        get() = _userLastChannel

    private val _carts = MutableLiveData<List<Cart>>()
    val carts: LiveData<List<Cart>>
        get() = _carts

    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>>
        get() = _items

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _cart = MutableLiveData<Cart?>()
    val cart: LiveData<Cart?>
        get() = _cart

    private var localItems = mutableListOf<Item>()

    private var channelUpdates: ListenerRegistration? = null

    private var cartUpdates: ListenerRegistration? = null

    fun setUserEmail(email: String) {
        _userEmail.value = email
        mode = if (email == ANONYMOUS_EMAIL) Mode.LOCAL else Mode.CLOUD
    }

    fun signOut() {
        _userEmail.value = ""
        signOutUseCase()
    }

    fun setChannel(channel: Channel?) {
        _channel.value = channel
        if (channel == null) {
            stopRealtimeChannelUpdates()
            _carts.value = listOf()
        }
    }

    private fun setCarts(carts: List<Cart>) {
        _carts.value = carts
    }

    fun setCart(cart: Cart?) {
        _cart.value = cart
        if (cart == null) {
            stopRealtimeCartUpdates()
            _items.value = listOf()
        }
    }

    private fun setItems(items: List<Item>) {
        localItems = items as MutableList<Item>
        _items.value = localItems
    }

    val loadLocalCarts = cartRepo.getCarts().asLiveData()

    private fun deleteLocalCart(cart: Cart) = viewModelScope.launch {
        cartRepo.deleteCart(cart)
    }

    fun loadLocalItems() = itemRepo.getItems(cart.value!!.timestamp).asLiveData()

    private fun updateCart(cart: Cart) = viewModelScope.launch {
        cartRepo.updateCart(cart)
    }

    private fun addOrUpdateLocalItem(item: Item, updateCheck: Boolean = false) =
        viewModelScope.launch {
            item.cartId = cart.value!!.timestamp
            val possibleItem = itemRepo.getItems(item.cartId, item.name).firstOrNull()
            if (possibleItem == null) itemRepo.insertItem(item)
            else {
                itemRepo.updateItem(
                    item.copy(
                        price = item.price,
                        isChecked = if (updateCheck) item.isChecked else possibleItem.isChecked,
                        id = possibleItem.id
                    )
                )
            }
        }

    private fun deleteLocalItem(item: Item) = viewModelScope.launch {
        itemRepo.deleteItem(item)
    }

    private fun createLocalCart(cart: Cart) = viewModelScope.launch {
        cartRepo.insertCart(cart)
    }

    fun loadCartsFromDb() {
        firestoreDb.collection("channels")
            .document(channel.value!!.name)
            .collection("carts")
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty)
                    setCarts(it.map { doc -> doc.toObject(Cart::class.java) })
            }
    }

    fun connectToChannel(context: Context, name: String, key: Long) {
        firestoreDb.collection("channels")
            .whereEqualTo("name", name)
            .whereEqualTo("key", key)
            .get()
            .addOnSuccessListener {
                val channel = it.firstOrNull()?.toObject(Channel::class.java)
                if (channel == null) {
                    _error.value = context.getString(R.string.channel_not_found)
                } else {
                    setChannel(channel)
                    saveLastChannel(channel)
                }
            }
    }

    private fun saveLastChannel(channel: Channel) {
        firestoreDb.collection("users")
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
        firestoreDb.collection("channels")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener {
                if (it.firstOrNull() == null) {
                    val newChannel = Channel(name, key)
                    firestoreDb.collection("channels")
                        .document(name)
                        .set(newChannel)
                        .addOnSuccessListener {
                            setChannel(newChannel)
                        }
                }
            }
    }

    fun searchForLastChannel() {
        firestoreDb.collection("users")
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
        if (mode == Mode.LOCAL) {
            deleteLocalCart(cart)
            setCart(null)
        } else
            firestoreDb.collection("channels")
                .document(channel.value!!.name)
                .collection("carts")
                .document(cart.timestamp.toString())
                .delete()
                .addOnSuccessListener {
                    setCart(null)
                }
    }

    fun createNewCart() {
        val time = Datetime.getTimeStamp(Datetime.getCurrentTime())
        val cart = Cart(time, 0.0)
        if (mode == Mode.LOCAL) {
            createLocalCart(cart)
        } else
            firestoreDb.collection("channels")
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

    fun setupRealtimeChannelUpdates() {
        if (mode == Mode.CLOUD) {
            channelUpdates = firestoreDb.collection("channels")
                .document(channel.value!!.name)
                .collection("carts")
                .addSnapshotListener { value, _ ->
                    // Update cart list
                    if (value != null) {
                        _carts.value =
                            value.map { it.toObject(Cart::class.java) } as MutableList<Cart>
                    }
                    // Check if current cart was removed from outside
                    if (value!!.documentChanges.any {
                            it.type == DocumentChange.Type.REMOVED
                                    && it.document.toObject(Cart::class.java) == _cart.value
                        }) {
                        setCart(null)
                    }
                }
        }
    }

    private fun stopRealtimeChannelUpdates() {
        channelUpdates = null
    }

    fun loadItemsFromDb() {
        firestoreDb.collection("channels")
            .document(channel.value!!.name)
            .collection("carts")
            .document(cart.value!!.timestamp.toString())
            .collection("items")
            .get()
            .addOnSuccessListener {
                setItems(it.map { doc ->
                    Item(
                        doc["name"] as String,
                        doc["price"] as Double,
                        doc["isChecked"] as Boolean
                    )
                })
                setCartPrice(localItems)
            }
    }

    fun setCartPrice(list: List<Item>) {
        val price = if (list.isEmpty()) 0.0
        else list.map(Item::price).reduce { acc, d -> acc + d }

        val copy = cart.value!!.copy(price = price)
        if (mode == Mode.LOCAL) {
            updateCart(copy)
            _cart.value = copy
        } else
            firestoreDb.collection("channels")
                .document(channel.value!!.name)
                .collection("carts")
                .document(cart.value!!.timestamp.toString())
                .update("price", price)
                .addOnSuccessListener {
                    _cart.value = copy
                }
    }

    fun addOrUpdateItem(item: Item, updateCheck: Boolean = false) {
        if (mode == Mode.LOCAL) {
            addOrUpdateLocalItem(item, updateCheck)
        } else {
            val isChecked = if (updateCheck) {
                item.isChecked
            } else {
                // Get current item state if exists
                localItems.firstOrNull { item.name == it.name }?.isChecked ?: item.isChecked
            }
            firestoreDb.collection("channels")
                .document(channel.value!!.name)
                .collection("carts")
                .document(cart.value!!.timestamp.toString())
                .collection("items")
                .document(item.name)
                .set(
                    mapOf(
                        "name" to item.name,
                        "price" to item.price,
                        "isChecked" to isChecked
                    )
                )
        }
    }

    fun deleteItem(item: Item) {
        if (mode == Mode.LOCAL) {
            deleteLocalItem(item)
        } else
            firestoreDb.collection("channels")
                .document(channel.value!!.name)
                .collection("carts")
                .document(cart.value!!.timestamp.toString())
                .collection("items")
                .document(item.name)
                .delete()
    }

    fun setupRealtimeCartUpdates() {
        if (mode == Mode.CLOUD) {
            cartUpdates = firestoreDb.collection("channels")
                .document(channel.value!!.name)
                .collection("carts")
                .document(cart.value!!.timestamp.toString())
                .collection("items")
                .addSnapshotListener { value, _ ->
                    if (value != null) {
                        val items = value.map {
                            Item(
                                it["name"] as String,
                                it["price"] as Double,
                                it["isChecked"] as Boolean
                            )
                        } as MutableList<Item>
                        localItems = items
                        _items.value = localItems
                        setCartPrice(localItems)
                    }
                }
        }
    }

    private fun stopRealtimeCartUpdates() {
        cartUpdates = null
    }

    class ListViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
                return ListViewModel(database) as T
            }
            throw IllegalArgumentException("Unknown view model class")
        }
    }

}