package com.morj12.cloudlist.presentation.view.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.morj12.cloudlist.utils.Credentials.isValidCredentials
import com.morj12.cloudlist.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        setupListeners()
        subscribe()
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
            register(edRegisterEmail.text.toString(), edRegisterPw.text.toString())
        }

        btRegisterForgot.setOnClickListener {
            forgotPassword(edRegisterEmail.text.toString())
        }
    }

    private fun register(email: String, pw: String) = viewModel.register(email, pw)

    private fun forgotPassword(email: String) = viewModel.forgotPassword(email)

    private fun subscribe() {
        viewModel.registerResult.observe(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.forgotPasswordResult.observe(this) {
            if (it.isSuccessful) {
                Toast.makeText(
                    this,
                    "Message sent to ${binding.edRegisterEmail.text}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}