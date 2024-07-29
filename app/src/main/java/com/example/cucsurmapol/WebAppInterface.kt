package com.example.cucsurmapol

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.navigation.NavController
import org.json.JSONArray
import org.json.JSONObject

class WebAppInterface(private val context: Context,private val navController: NavController,private val sharedViewModel: SharedViewModel) {

    val dbHelper = DatabaseHelper(context,sharedViewModel)
    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun navToSalonInfo(info: String) {
        // Handle the received building info here
        Toast.makeText(context, "$info", Toast.LENGTH_SHORT).show()
        Log.println(Log.INFO,"suck my balls recieved: ",info);
        handler.post {
            sharedViewModel.setSalonid(info)
            navController.navigate(R.id.action_homeFragment_to_dashboardFragment)
        }
    }

    @JavascriptInterface
    fun navToMapSalon(id: String) {
        // Handle the received building info here
        Toast.makeText(context, "$id", Toast.LENGTH_SHORT).show()
        Log.println(Log.INFO,"navigated to map: ",id);
        handler.post {
            sharedViewModel.setSalonid(id)
            navController.navigate(R.id.action_dashboardFragment_to_homeFragment)
        }
    }

    fun createToast(text: String){
        Toast.makeText(context, "$text", Toast.LENGTH_SHORT).show()
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
            jsonObject.put("descripcion", salon.descripcion)
            jsonObject.put("tipo", salon.tipo)
            jsonObject.put("piso", salon.piso)
            jsonObject.put("edificio_edificioid", salon.edificio_edificioid)
            jsonArray.put(jsonObject)
        }
        Log.println(Log.INFO,"salon data", jsonArray.toString())
        return jsonArray.toString()
    }

    @JavascriptInterface
    fun getSalon(salonid:String):String{
        val salones = dbHelper.getSalon(salonid)
        val jsonArray = JSONArray()
        for (salon in salones) {
            val jsonObject = JSONObject()
            jsonObject.put("salonid", salon.salonid)
            jsonObject.put("nombre", salon.nombre)
            jsonObject.put("descripcion", salon.descripcion)
            jsonObject.put("tipo", salon.tipo)
            jsonObject.put("piso", salon.piso)
            jsonObject.put("edificio_edificioid", salon.edificio_edificioid)
            jsonArray.put(jsonObject)
        }
        Log.println(Log.INFO,"salon data", jsonArray.toString())
        return jsonArray.toString()
    }

    @JavascriptInterface
    fun getEdificio(id:String):String{
        val edificios = dbHelper.getEdificio(id)
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
        Log.println(Log.INFO,"got individual edificio: ",jsonArray.toString())
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
            jsonObject.put("descripcion", salon.descripcion)
            jsonObject.put("tipo", salon.tipo)
            jsonObject.put("piso", salon.piso)
            jsonObject.put("edificio_edificioid", salon.edificio_edificioid)
            jsonArray.put(jsonObject)
        }                                                       
        Log.println(Log.INFO,"salon data EDIFICIOSALONES", jsonArray.toString())
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
            jsonObject.put("descripcion", salon.descripcion)
            jsonObject.put("tipo", salon.tipo)
            jsonObject.put("piso", salon.piso)
            jsonObject.put("edificio_edificioid", salon.edificio_edificioid)
            jsonArray.put(jsonObject)
        }
        Log.println(Log.INFO,"salon data", jsonArray.toString())
        return jsonArray.toString()
    }

    @JavascriptInterface
    fun getSalonid(): String {
        Log.println(Log.INFO,"got salonid: ",sharedViewModel.info.value ?: "")
        return sharedViewModel.info.value ?: ""
    }

    @JavascriptInterface
    fun setSalonid(info: String) {
        sharedViewModel.setSalonid(info)
    }

    @JavascriptInterface
    fun getEdificioid():String{
        return sharedViewModel.edificioid.value ?: ""
        Log.println(Log.INFO,"suck my balls sent: ",sharedViewModel.edificioid.value ?:"");
    }

    @JavascriptInterface
    fun setEdificioid(id: String) {
        sharedViewModel.setEdificioid(id)
        Log.println(Log.INFO,"suck my balls recieved: ",id);
    }
}