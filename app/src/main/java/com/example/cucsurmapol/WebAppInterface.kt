package com.example.cucsurmapol

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun sendBuildingInfo(info: String) {
        // Handle the received building info here
        Toast.makeText(context, "$info", Toast.LENGTH_SHORT).show()
    }
}