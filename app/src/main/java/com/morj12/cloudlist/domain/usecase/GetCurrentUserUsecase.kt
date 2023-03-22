package com.morj12.cloudlist.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class GetCurrentUserUsecase {

    private var mAuth = FirebaseAuth.getInstance()

    operator fun invoke(): FirebaseUser? {
        return mAuth.currentUser
    }
}