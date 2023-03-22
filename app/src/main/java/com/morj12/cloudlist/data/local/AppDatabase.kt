package com.morj12.cloudlist.data.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.morj12.cloudlist.data.local.dao.CartDao
import com.morj12.cloudlist.data.local.dao.ItemDao
import com.morj12.cloudlist.data.local.dbmodel.CartDbModel
import com.morj12.cloudlist.data.local.dbmodel.ItemDbModel

@Database(entities = [ItemDbModel::class, CartDbModel::class], version = 1, exportSchema = true)
abstract class AppDatabase: RoomDatabase() {

    abstract fun cartDao(): CartDao
    abstract fun itemDao(): ItemDao

    companion object {

        private const val DB_NAME = "cloud_list_db"
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(application: Application): AppDatabase {
            INSTANCE?.let { return it }
            synchronized(this) {
                INSTANCE?.let { return it }
                val db = Room.databaseBuilder(
                    application.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = db
                return db
            }
        }
    }
}