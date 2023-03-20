package com.morj12.cloudlist.presentation.view.list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.morj12.cloudlist.App
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.ActivityListBinding
import com.morj12.cloudlist.presentation.view.main.MainActivity
import com.morj12.cloudlist.utils.Constants.ANONYMOUS_EMAIL
import com.morj12.cloudlist.utils.Constants.EMAIL_KEY

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding

    private val viewModel: ListViewModel by viewModels {
        ListViewModel.ListViewModelFactory((applicationContext as App).db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getUserEmail()
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (viewModel.mode == Mode.CLOUD)
            menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_signout -> viewModel.signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUserEmail() {
        val email = intent.getStringExtra(EMAIL_KEY)
        viewModel.setUserEmail(email ?: ANONYMOUS_EMAIL)
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.fl_fragment)) {
            is ChannelFragment -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("finish", "")
                startActivity(intent)
            }
            is CartFragment -> {
                viewModel.setChannel(null)
            }
            is ItemFragment -> {
                viewModel.setCart(null)
            }
        }
    }

    private fun observe() {
        viewModel.userEmail.observe(this) {
            when (it) {
                "" -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                ANONYMOUS_EMAIL -> {
                    loadCartFragment()
                }
                else -> {
                    loadChannelFragment()
                }
            }
        }
    }

    private fun loadCartFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, CartFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    private fun loadChannelFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, ChannelFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
}