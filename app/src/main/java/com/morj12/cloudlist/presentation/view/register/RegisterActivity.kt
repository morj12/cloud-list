package com.morj12.cloudlist.presentation.view.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.morj12.cloudlist.R
import com.morj12.cloudlist.utils.Credentials.isValidCredentials
import com.morj12.cloudlist.databinding.ActivityRegisterBinding
import com.morj12.cloudlist.utils.startLoading
import com.morj12.cloudlist.utils.stopLoading

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        setupListeners()
        observe()
    }

    private fun setupListeners() = with(binding) {
        edRegisterEmail.doOnTextChanged { text, _, _, _ ->
            btRegisterCreate.isEnabled =
                isValidCredentials(text.toString(), edRegisterPw.text.toString())
            btRegisterForgot.isEnabled = isValidCredentials(text.toString())
        }
        edRegisterPw.doOnTextChanged { text, _, _, _ ->
            btRegisterCreate.isEnabled =
                isValidCredentials(edRegisterEmail.text.toString(), text.toString())
        }

        btRegisterCreate.setOnClickListener {
            btRegisterCreate.startLoading(pbRegister)
            register(edRegisterEmail.text.toString(), edRegisterPw.text.toString())
        }

        btRegisterForgot.setOnClickListener {
            btRegisterForgot.startLoading(pbForgot)
            forgotPassword(edRegisterEmail.text.toString())
        }
    }

    private fun register(email: String, pw: String) = viewModel.register(email, pw)

    private fun forgotPassword(email: String) = viewModel.forgotPassword(email)

    private fun observe() {
        viewModel.registerResult.observe(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                showSnackbar(getString(R.string.any_exception))
                binding.btRegisterCreate.stopLoading(binding.pbRegister)
            }
        }

        viewModel.forgotPasswordResult.observe(this) {
            if (it.isSuccessful || it.exception is FirebaseAuthInvalidUserException) {
                Toast.makeText(
                    this,
                    "Message sent to ${binding.edRegisterEmail.text}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                showSnackbar(getString(R.string.any_exception))
                binding.btRegisterForgot.stopLoading(binding.pbForgot)
            }
        }
    }

    private fun showSnackbar(it: String) {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(R.color.black))
            .setBackgroundTint(resources.getColor(R.color.white))
            .show()
    }
}