package com.morj12.cloudlist.utils

import androidx.core.util.PatternsCompat

object Credentials {

    fun isValidCredentials(email: String? = null, password: String? = null): Boolean {
        val emailValid =
            if (email != null) PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() else true
        val passwordValid =
            if (password != null) password.isNotBlank() && password.length >= 6 else true
        return emailValid && passwordValid
    }
}