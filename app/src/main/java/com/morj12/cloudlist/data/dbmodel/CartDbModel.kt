package com.morj12.cloudlist.data.dbmodel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartDbModel(
    val price: Double,
    @PrimaryKey
    val timestamp: Long,
)
