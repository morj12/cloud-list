package com.morj12.cloudlist.data.local.dbmodel

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "item",
foreignKeys = [ForeignKey(
    entity = CartDbModel::class,
    parentColumns = arrayOf("timestamp"),
    childColumns = arrayOf("cartId"),
    onDelete = CASCADE
)])
data class ItemDbModel(
    val name: String,
    val price: Double,
    val isChecked: Boolean,
    val cartId: Long,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
