package com.example.cucsurmapol

import android.content.Context
import android.util.Log
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
            jsonObject.put("v1", edificio.v1)
            jsonObject.put("v2", edificio.v2)
            jsonObject.put("v3", edificio.v3)
            jsonObject.put("v4", edificio.v4)
            jsonArray.put(jsonObject)
        }
        Log.println(Log.INFO,"bruh error: 67",jsonArray.toString())
        return jsonArray.toString()
    }

    @JavascriptInterface
    fun getSalones():String{
        val salones = dbHelper.getAllSalones()
        val jsonArray = JSONArray()
        for (salon in salones) {
            val jsonObject = JSONObject()
            jsonObject.put("salonid", salon.salonid)
            jsonObject.put("nombre", salon.nombre)
            jsonObject.put("tipo", salon.tipo)
            jsonObject.put("piso", salon.piso)
            jsonObject.put("edificio_edificioid", salon.edificio_edificioid)
            jsonArray.put(jsonObject)
        }
        Log.println(Log.INFO,"salon data", jsonArray.toString())
        return jsonArray.toString()
    }

    @JavascriptInterface
    fun getEdificioSalones(edificio:String):String{
        val salones = dbHelper.getSalonesAtEdificio(edificio)
        val jsonArray = JSONArray()
        for (salon in salones) {
            val jsonObject = JSONObject()
            jsonObject.put("salonid", salon.salonid)
            jsonObject.put("nombre", salon.nombre)
            jsonObject.put("tipo", salon.tipo)
            jsonObject.put("piso", salon.piso)
            jsonObject.put("edificio_edificioid", salon.edificio_edificioid)
            jsonArray.put(jsonObject)
        }                                                       
        Log.println(Log.INFO,"salon data", jsonArray.toString())
        return jsonArray.toString()
    }

    @JavascriptInterface
    fun getSalonSearch(search:String):String{
        val salones = dbHelper.getSalonSearches(search)
        val jsonArray = JSONArray()
        for (salon in salones) {
            val jsonObject = JSONObject()
            jsonObject.put("salonid", salon.salonid)
            jsonObject.put("nombre", salon.nombre)
            jsonObject.put("tipo", salon.tipo)
            jsonObject.put("piso", salon.piso)
            jsonObject.put("edificio_edificioid", salon.edificio_edificioid)
            jsonArray.put(jsonObject)
        }
        Log.println(Log.INFO,"salon data", jsonArray.toString())
        return jsonArray.toString()
    }
}