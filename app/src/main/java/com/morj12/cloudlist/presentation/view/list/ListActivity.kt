package com.morj12.cloudlist.presentation.view.list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.morj12.cloudlist.databinding.ActivityListBinding
import com.morj12.cloudlist.presentation.view.main.MainActivity
import com.morj12.cloudlist.utils.Constants.ANONYMOUS_EMAIL
import com.morj12.cloudlist.utils.Constants.EMAIL_KEY

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding

    private lateinit var viewModel: ListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ListViewModel::class.java]
        getUserEmail()
        observe()

        setupListeners()
    }

    private fun getUserEmail() {
        val email = intent.getStringExtra(EMAIL_KEY)
        viewModel.setUserEmail(email ?: ANONYMOUS_EMAIL)
    }

    private fun greet(email: String) {
        binding.greeting.text = "Hello $email"
    }

    override fun onBackPressed() {
        viewModel.signOut()
        finish()
    }

    private fun observe() {
        viewModel.userEmail.observe(this) {
            if (it == "") startActivity(Intent(this, MainActivity::class.java)) else {
                greet(it)
            }
        }
    }

    private fun setupListeners() {
        binding.btListSignOut.setOnClickListener {
            onBackPressed()
        }
    }
}