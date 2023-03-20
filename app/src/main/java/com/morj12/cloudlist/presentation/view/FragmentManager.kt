package com.morj12.cloudlist.presentation.view

import androidx.fragment.app.FragmentActivity
import com.morj12.cloudlist.R
import com.morj12.cloudlist.presentation.view.list.CartFragment
import com.morj12.cloudlist.presentation.view.list.ChannelFragment
import com.morj12.cloudlist.presentation.view.list.ItemFragment

object FragmentManager {

    fun loadItemFragment(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, ItemFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    fun loadCartFragment(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, CartFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    fun loadChannelFragment(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, ChannelFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

}