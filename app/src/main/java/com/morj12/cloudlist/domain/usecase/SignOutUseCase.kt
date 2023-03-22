package com.morj12.cloudlist.domain.usecase

import com.google.firebase.auth.FirebaseAuth

class SignOutUseCase {

    private var mAuth = FirebaseAuth.getInstance()

    operator fun invoke() = mAuth.signOut()

}