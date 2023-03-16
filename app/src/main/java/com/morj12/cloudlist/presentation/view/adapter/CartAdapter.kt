package com.morj12.cloudlist.presentation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.morj12.cloudlist.databinding.CartItemBinding
import com.morj12.cloudlist.domain.entity.Cart

class CartAdapter : ListAdapter<Cart, CartAdapter.ViewHolder>(CartCallback()) {

    class CartCallback : DiffUtil.ItemCallback<Cart>() {
        override fun areItemsTheSame(oldItem: Cart, newItem: Cart) = oldItem.id == newItem.id

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
            cartItemDatetime.text = item.datetime
            cartItemPrice.text = item.price.toString()
        }
    }
}