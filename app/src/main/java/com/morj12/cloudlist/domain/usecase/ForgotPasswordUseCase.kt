package com.morj12.cloudlist.domain.usecase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordUseCase {

    private var mAuth = FirebaseAuth.getInstance()

    operator fun invoke(email: String, callback: (Task<Void>) -> Unit) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(callback)
    }
}