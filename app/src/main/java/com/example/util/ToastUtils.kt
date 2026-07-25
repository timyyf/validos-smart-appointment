package com.example.util

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private var currentToast: Toast? = null

    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            currentToast?.cancel()
            currentToast = Toast.makeText(context.applicationContext, message, duration)
            currentToast?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
