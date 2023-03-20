package com.morj12.cloudlist.domain.entity

data class Item(
    val name: String = "",
    val price: Double = 0.0,
    var isChecked: Boolean = false,
    val cartId: Long = 0,
    val id: Int = 0
)
