package com.morj12.cloudlist.presentation.view.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.morj12.cloudlist.R
import com.morj12.cloudlist.utils.Credentials.isValidCredentials
import com.morj12.cloudlist.presentation.view.register.RegisterActivity
import com.morj12.cloudlist.databinding.ActivityMainBinding
import com.morj12.cloudlist.presentation.view.list.ListActivity
import com.morj12.cloudlist.utils.Constants.ANONYMOUS_EMAIL
import com.morj12.cloudlist.utils.Constants.EMAIL_KEY
import com.morj12.cloudlist.utils.startLoading
import com.morj12.cloudlist.utils.stopLoading

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
            btLogin.startLoading(pbLogin)
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
                    startListActivity(binding.edLoginEmail.text.toString())
                }
                else -> {
                    var exception = ""
                    binding.btLogin.stopLoading(binding.pbLogin)
                    exception = if (shouldBeShownToUserException(it.exception))
                        "Invalid user email or password"
                    else
                        it.exception.toString()
                    showSnackbar(exception)
                }
            }
        }
        viewModel.currentUser.observe(this) {
            startListActivity(it)
        }
    }

    private fun showSnackbar(it: String) {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(R.color.black))
            .setBackgroundTint(resources.getColor(R.color.white))
            .show()
    }

    private fun shouldBeShownToUserException(exception: java.lang.Exception?): Boolean {
        return exception is FirebaseAuthInvalidUserException
    }

    private fun startListActivity(email: String) {
        val intent = Intent(this, ListActivity::class.java).apply {
            putExtra(EMAIL_KEY, email)
        }
        startActivity(intent)
    }


}