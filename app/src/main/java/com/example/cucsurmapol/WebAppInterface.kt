package com.example.cucsurmapol

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class WebAppInterface(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)
    @JavascriptInterface
    fun sendBuildingInfo(info: String) {
        // Handle the received building info here
        Toast.makeText(context, "$info", Toast.LENGTH_SHORT).show()
    }
    @JavascriptInterface
    fun getEdificios(): String {
        val edificios = dbHelper.getAllEdificios()
        val jsonArray = JSONArray()
        for (edificio in edificios) {
            val jsonObject = JSONObject()
            jsonObject.put("id", edificio.id)
            jsonObject.put("nombre", edificio.nombre)
            jsonObject.put("tipo", edificio.tipo)
            jsonObject.put("pisos", edificio.pisos)
            jsonObject.put("lat", edificio.lat.toDouble())
            jsonObject.put("lon", edificio.lon.toDouble())
            jsonObject.put("image", edificio.image)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}