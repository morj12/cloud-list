package com.morj12.cloudlist.presentation.view.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.morj12.cloudlist.utils.Credentials.isValidCredentials
import com.morj12.cloudlist.presentation.view.register.RegisterActivity
import com.morj12.cloudlist.databinding.ActivityMainBinding
import com.morj12.cloudlist.presentation.view.list.ListActivity
import com.morj12.cloudlist.utils.Constants.ANONYMOUS_EMAIL
import com.morj12.cloudlist.utils.Constants.EMAIL_KEY

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("finish")) {
            finishAffinity()
        }

        setupListeners()
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        observe()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkForUser()
    }

    private fun setupListeners() = with(binding) {
        edLoginEmail.doOnTextChanged { text, _, _, _ ->
            btLogin.isEnabled =
                isValidCredentials(text.toString(), edLoginPw.text.toString())
        }
        edLoginPw.doOnTextChanged { text, _, _, _ ->
            btLogin.isEnabled =
                isValidCredentials(edLoginEmail.text.toString(), text.toString())
        }
        btLogin.setOnClickListener {
            signUp(edLoginEmail.text.toString(), edLoginPw.text.toString())
        }

        btRegister.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
        }

        btLocalAcc.setOnClickListener { startListActivity(ANONYMOUS_EMAIL) }
    }

    override fun onBackPressed() = finishAffinity()

    private fun signUp(email: String, pw: String) = viewModel.signUp(email, pw)

    private fun observe() {
        viewModel.signUpResult.observe(this) {
            when {
                it.isSuccessful -> {
                    Toast.makeText(this@MainActivity, "Logged in", Toast.LENGTH_SHORT).show()
                    startListActivity(binding.edLoginEmail.text.toString())
                }
                else -> {
                    Toast.makeText(this@MainActivity, it.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        viewModel.currentUser.observe(this) {
            startListActivity(it)
        }
    }

    private fun startListActivity(email: String) {
        val intent = Intent(this, ListActivity::class.java).apply {
            putExtra(EMAIL_KEY, email)
        }
        startActivity(intent)
    }

}