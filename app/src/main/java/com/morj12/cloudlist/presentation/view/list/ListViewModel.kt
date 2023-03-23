package com.morj12.cloudlist.presentation.view.list

import android.content.Context
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.morj12.cloudlist.R
import com.morj12.cloudlist.data.local.AppDatabase
import com.morj12.cloudlist.data.local.repository.CartRepositoryImpl
import com.morj12.cloudlist.data.local.repository.ItemRepositoryImpl
import com.morj12.cloudlist.data.remote.repository.RemoteRepositoryImpl
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
    private val remoteRepo = RemoteRepositoryImpl()

    lateinit var mode: Mode

    private val signOutUseCase = SignOutUseCase()


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
        remoteRepo.loadCarts(channel.value!!.name) {
            if (!it.isEmpty)
                setCarts(it.map { doc -> doc.toObject(Cart::class.java) })
        }
    }

    fun connectToChannel(context: Context, name: String, key: Long) {
        remoteRepo.connectToChannel(name, key) {
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
        remoteRepo.saveChannel(channel, userEmail.value!!)
    }

    fun createNewChannel(name: String, key: Long) {
        remoteRepo.getChannel(name) {
            if (it.firstOrNull() == null) {
                val newChannel = Channel(name, key)
                remoteRepo.createChannel(newChannel) {
                    setChannel(newChannel)
                }
            }
        }
    }

    fun searchForLastChannel() {
        remoteRepo.getLastChannel(userEmail.value!!) {
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
            remoteRepo.deleteCart(channel.value!!.name, cart.timestamp.toString()) {
                setCart(null)
            }
    }

    fun createNewCart() {
        val time = Datetime.getTimeStamp(Datetime.getCurrentTime())
        val cart = Cart(time, 0.0)
        if (mode == Mode.LOCAL) {
            createLocalCart(cart)
        } else
            remoteRepo.createCart(channel.value!!.name, cart)
    }

    fun setupRealtimeChannelUpdates() {
        if (mode == Mode.CLOUD) {
            channelUpdates = remoteRepo.setupRealtimeChannelUpdates(channel.value!!.name) {
                if (it != null) {
                    _carts.value =
                        it.map { obj -> obj.toObject(Cart::class.java) } as MutableList<Cart>
                }
                if (it!!.documentChanges.any { change ->
                        change.type == DocumentChange.Type.REMOVED
                                && change.document.toObject(Cart::class.java) == _cart.value
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
        remoteRepo.loadItems(channel.value!!.name, cart.value!!.timestamp.toString()) {
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
            remoteRepo.updateCartPrice(
                channel.value!!.name,
                cart.value!!.timestamp.toString(),
                price
            ) {
                _cart.value = copy
            }
    }

    fun addOrUpdateItem(item: Item, updateCheck: Boolean = false) {
        if (mode == Mode.LOCAL) {
            addOrUpdateLocalItem(item, updateCheck)
        } else {
            val newItem = item.copy(
                isChecked = if (updateCheck) {
                    item.isChecked
                } else {
                    // Get current item state if exists
                    localItems.firstOrNull { item.name == it.name }?.isChecked ?: item.isChecked
                }
            )
            remoteRepo.upsertItem(channel.value!!.name, cart.value!!.timestamp.toString(), newItem)
        }
    }

    fun deleteItem(item: Item) {
        if (mode == Mode.LOCAL) {
            deleteLocalItem(item)
        } else
            remoteRepo.deleteItem(channel.value!!.name, cart.value!!.timestamp.toString(), item)
    }

    fun setupRealtimeCartUpdates() {
        if (mode == Mode.CLOUD) {
            cartUpdates = remoteRepo.setupRealtimeCartUpdates(
                channel.value!!.name,
                cart.value!!.timestamp.toString()
            ) {
                if (it != null) {
                    val items = it.map { obj ->
                        Item(
                            obj["name"] as String,
                            obj["price"] as Double,
                            obj["isChecked"] as Boolean
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