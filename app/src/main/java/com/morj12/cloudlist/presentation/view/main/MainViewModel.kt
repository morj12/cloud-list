package com.morj12.cloudlist.presentation.view.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.morj12.cloudlist.domain.usecase.GetCurrentUserUsecase
import com.morj12.cloudlist.domain.usecase.LoginUseCase

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var loginUseCase = LoginUseCase()
    private var getCurrentUserUsecase = GetCurrentUserUsecase()

    private val _signUpResult = MutableLiveData<Task<AuthResult>>()
    val signUpResult: LiveData<Task<AuthResult>>
        get() = _signUpResult

    private val _currentUser = MutableLiveData<String>()
    val currentUser: LiveData<String>
        get() = _currentUser

    fun signUp(email: String, pw: String) {
        loginUseCase(email, pw) {
            _signUpResult.value = it
        }
    }

    fun checkForUser() {
        val user = getCurrentUserUsecase()
        if (user != null) {
            _currentUser.value = user.email.toString()
        }
    }
}