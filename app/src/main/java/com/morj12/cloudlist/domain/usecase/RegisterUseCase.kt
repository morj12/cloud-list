package com.morj12.cloudlist.domain.usecase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class RegisterUseCase {

    private var mAuth = FirebaseAuth.getInstance()

    operator fun invoke(email: String, pw: String, callback: (Task<AuthResult>) -> Unit) {
        mAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(callback)
    }
}