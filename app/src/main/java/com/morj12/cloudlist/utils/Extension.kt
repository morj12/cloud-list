package com.morj12.cloudlist.utils

import android.view.View
import android.widget.ProgressBar

fun View.startLoading(progressBar: ProgressBar? = null) {
    this.animate().alpha(1f).setDuration(0).start()
    this.animate().alpha(0f).setDuration(300).start()
    progressBar?.visibility = View.VISIBLE
    progressBar?.animate()?.alpha(0f)?.setDuration(0)?.start()
    progressBar?.animate()?.alpha(1f)?.setDuration(300)?.start()
}

fun View.stopLoading(progressBar: ProgressBar? = null) {
    this.animate().alpha(0f).setDuration(0).start()
    this.animate().alpha(1f).setDuration(300).start()
    progressBar?.visibility = View.GONE
}