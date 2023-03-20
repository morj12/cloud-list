package com.morj12.cloudlist.presentation.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.ItemItemBinding
import com.morj12.cloudlist.domain.entity.Item

class ItemAdapter : ListAdapter<Item, ItemAdapter.ViewHolder>(ItemCallback()) {

    var onCheckClickedListener: ((Item) -> Unit)? = null

    var onItemDeleteClickedListener: ((Item) -> Unit)? = null

    class ItemCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
    }

    class ViewHolder(val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            itemName.text = item.name
            itemPrice.text = root.context.getString(R.string.cart_price, item.price.toString())
            cbItem.isChecked = item.isChecked
            setAppearance(this)
            cbItem.setOnClickListener {
                onCheckClickedListener?.invoke(item)
            }
            btRemoveItemItem.setOnClickListener {
                onItemDeleteClickedListener?.invoke(item)
            }
        }
    }

    private fun setAppearance(binding: ItemItemBinding) =
        binding.apply {
            if (cbItem.isChecked) {
                itemName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gray))
            } else {
                itemName.paintFlags = Paint.ANTI_ALIAS_FLAG
                itemName.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.black)
                )
            }

        }
}