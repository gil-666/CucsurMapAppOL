package com.example.cucsurmapol

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class FeedbackUploader {

    private val client = OkHttpClient()

    fun uploadFeedback(zipFile: File, serverUrl: String, callback: (Boolean, String?) -> Unit) {
        val client = OkHttpClient.Builder()
            .connectTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        // Create a RequestBody for the file
        val requestFile = zipFile.asRequestBody("application/zip".toMediaTypeOrNull())

        // Create MultipartBody for the request
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", zipFile.name, requestFile)
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
                Log.i("imgay","fuck")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, response.body?.string())
                } else {
                    callback(false, "Server returned an error: ${response.code}")
                }
            }
        })
    }
}
