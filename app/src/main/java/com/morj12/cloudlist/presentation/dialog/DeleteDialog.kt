package com.morj12.cloudlist.presentation.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.DeleteDialogBinding
import com.morj12.cloudlist.domain.entity.Cart
import com.morj12.cloudlist.domain.entity.Item

object DeleteDialog {

    fun showDialog(context: Context, item: Any, onAccept: () -> Unit) {
        var dialog: AlertDialog? = null
        val binding = DeleteDialogBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context).apply {
            setView(binding.root)
        }
        binding.apply {
            when (item) {
                is Cart -> {
                    tvDialogText.text = context.getString(R.string.dialog_question_cart)
                }
                is Item -> {
                    tvDialogText.text = context.getString(R.string.dialog_question_item, item.name)
                }
            }
            btDialogYes.setOnClickListener {
                dialog?.dismiss()
                onAccept()
            }
            btDialogNo.setOnClickListener {
                dialog?.dismiss()
            }
        }
        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(null)
        dialog.show()
    }
}