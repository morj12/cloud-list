package com.morj12.cloudlist.presentation.view.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.morj12.cloudlist.domain.usecase.ForgotPasswordUseCase
import com.morj12.cloudlist.domain.usecase.RegisterUseCase

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private var registerUseCase = RegisterUseCase()
    private var forgotPasswordUseCase = ForgotPasswordUseCase()

    private val _registerResult = MutableLiveData<Task<AuthResult>>()
    val registerResult: LiveData<Task<AuthResult>>
        get() = _registerResult

    private val _forgotPasswordResult = MutableLiveData<Task<Void>>()
    val forgotPasswordResult: LiveData<Task<Void>>
        get() = _forgotPasswordResult

    fun register(email: String, pw: String) {
        registerUseCase(email, pw) {
            _registerResult.value = it
        }
    }

    fun forgotPassword(email: String) {
        forgotPasswordUseCase(email) { _forgotPasswordResult.value = it }
    }
}