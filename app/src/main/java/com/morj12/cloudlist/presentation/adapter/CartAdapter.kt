package com.morj12.cloudlist.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.CartItemBinding
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.utils.Datetime

class CartAdapter : ListAdapter<Cart, CartAdapter.ViewHolder>(CartCallback()) {

    var onItemClickedListener: ((Cart) -> Unit)? = null

    var onItemDeleteClickedListener: ((Cart) -> Unit)? = null

    class CartCallback : DiffUtil.ItemCallback<Cart>() {
        override fun areItemsTheSame(oldItem: Cart, newItem: Cart) = oldItem.timestamp == newItem.timestamp

        override fun areContentsTheSame(oldItem: Cart, newItem: Cart) = oldItem == newItem
    }

    class ViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CartItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            cartItemDatetime.text = Datetime.getDateTime(item.timestamp)
            cartItemPrice.text = root.context.getString(R.string.cart_price, item.price.toString())
            root.setOnClickListener {
                onItemClickedListener?.invoke(item)
            }
            btRemoveCartItem.setOnClickListener {
                onItemDeleteClickedListener?.invoke(item)
            }
        }
    }
}