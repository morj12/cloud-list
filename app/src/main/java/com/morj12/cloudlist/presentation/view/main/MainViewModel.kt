package com.morj12.cloudlist.presentation.view.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var mAuth = FirebaseAuth.getInstance()

    private val _signUpResult = MutableLiveData<Task<AuthResult>>()
    val signUpResult: LiveData<Task<AuthResult>>
        get() = _signUpResult

    fun signUp(email: String, pw: String) {
        mAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener {
            _signUpResult.value = it
        }
    }
}