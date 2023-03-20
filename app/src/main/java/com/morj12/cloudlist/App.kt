package com.morj12.cloudlist

import android.app.Application
import com.morj12.cloudlist.data.AppDatabase

class App: Application() {
    val db by lazy { AppDatabase.getDatabase(this) }
}