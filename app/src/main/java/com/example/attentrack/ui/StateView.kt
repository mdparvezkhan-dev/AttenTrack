package com.example.attentrack.ui

import android.view.View
import com.example.attentrack.databinding.ViewStateBinding

// view_state.xml (loading / error + retry / empty) er helper. content = oi samoy lukiye rakhar view.

fun ViewStateBinding.showLoading(content: View) {
    root.visibility = View.VISIBLE
    progress.visibility = View.VISIBLE
    message.visibility = View.GONE
    retry.visibility = View.GONE
    content.visibility = View.GONE
}

fun ViewStateBinding.showError(text: String, content: View, onRetry: () -> Unit) {
    root.visibility = View.VISIBLE
    progress.visibility = View.GONE
    message.visibility = View.VISIBLE
    message.text = text
    retry.visibility = View.VISIBLE
    retry.setOnClickListener { onRetry() }
    content.visibility = View.GONE
}

fun ViewStateBinding.showEmpty(text: String, content: View) {
    root.visibility = View.VISIBLE
    progress.visibility = View.GONE
    message.visibility = View.VISIBLE
    message.text = text
    retry.visibility = View.GONE
    content.visibility = View.GONE
}

fun ViewStateBinding.hide(content: View) {
    root.visibility = View.GONE
    content.visibility = View.VISIBLE
}
