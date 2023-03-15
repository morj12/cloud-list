package com.morj12.cloudlist.presentation.view.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

class ListViewModel(application: Application) : AndroidViewModel(application) {

    private var mAuth = FirebaseAuth.getInstance()

    private val _userEmail = MutableLiveData("")
    val userEmail: LiveData<String>
        get() = _userEmail

    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    fun signOut() {
        _userEmail.value = ""
        mAuth.signOut()
    }

}