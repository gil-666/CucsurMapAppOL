package com.example.cucsurmapol

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.SocketTimeoutException
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.CountDownLatch

class DatabaseHelper(context: Context, private val sharedViewModel: SharedViewModel) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cucsur_data.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PATH = "/databases/"
        const val SERVER_IP = "177.230.254.9"
        private const val REMOTE_DATABASE_URL = "http://$SERVER_IP/db/cucsur_data.db"
    }
    private var useRemote = true
    private val dbPath: String = context.applicationInfo.dataDir + DATABASE_PATH + DATABASE_NAME

    init {
        initializeDatabase(context)
        if (isDatabaseValid(File(dbPath)) || (!isInternetAvailable(context) && isDatabaseValid(File(dbPath)))) {
            // Load downloaded database
            Toast.makeText(context, "se encontro bd descargada", Toast.LENGTH_SHORT).show()
        } else {
            loadDatabase(context)
        }

    }

    private fun initializeDatabase(context: Context) {
        val initFile = File(context.filesDir, DATABASE_NAME)
        val parentDir = File(dbPath).parentFile
        if (parentDir != null) {
            if (!parentDir.exists()) {
                parentDir.mkdirs() // Create directories if not exist
            }
        }
        if (!initFile.exists()) {
            try {
                copyDatabase(context)
                initFile.createNewFile()
                Log.i("initializedatabase","app first time!")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }else{
            Log.i("initializedatabase","app not first time!")
        }
    }

    private fun isDatabaseValid(file: File): Boolean {
        if (!file.exists()) {
            return false
        }
        return try {
            val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.rawQuery("SELECT * FROM edificio WHERE type='table'", null)
            val valid = cursor.count > 0
            cursor.close()
            db.close()
            valid
        } catch (e: SQLiteException) {
            false
        }
    }
    fun loadDatabase(context: Context) {
        val latch = CountDownLatch(1)

        if (isInternetAvailable(context) && useRemote && sharedViewModel.dbLoaded.value != true && !isLocalHigherVersion(context)) {
            downloadDatabase(context, latch)
            Toast.makeText(context, "descargando bd del servidor....", Toast.LENGTH_SHORT).show()
        } else if (sharedViewModel.dbLoaded.value == true) {
            // Database is already loaded, no need to download again
            latch.countDown()
        } else if (isDatabaseValid(File(dbPath)) || !compareDatabaseHashes(context) && !isLocalHigherVersion(context)) {
            // Use the valid downloaded database
            Toast.makeText(context, "Sin conexion, usando la ultima bd descargada", Toast.LENGTH_SHORT).show()
            sharedViewModel.dbLoaded.postValue(true)
            latch.countDown()
        } else {
            // Fall back to copying the database from assets
            copyDatabase(context)
            Toast.makeText(context, "usando bd original", Toast.LENGTH_SHORT).show()
            sharedViewModel.dbLoaded.postValue(true)
            latch.countDown()
        }

        latch.await()
    }
    private fun copyDatabase(context: Context) {
        try {
            val inputStream: InputStream = context.assets.open(DATABASE_NAME)
            val outputStream: OutputStream = FileOutputStream(dbPath)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // No need to create table as we are using an existing database
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No need to upgrade table as we are using an existing database
    }

    data class Edificio(val id: Int, val nombre: String, val tipo: String, val pisos: String, val lat: String, val lon: String, val image: String, val v1: String,val v2: String,val v3: String, val v4:String)
    data class Salon(val salonid: Int, val nombre: String, val descripcion:String, val tipo: String, val piso:String, val edificio_edificioid:Int)
    data class Parametros(val par_salonid:Int, val v1:String, val v2:String, val v3: String, val v4: String)
    fun getAllEdificios(): List<Edificio> {
        val edificios = mutableListOf<Edificio>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM edificio", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val pisos = cursor.getString(cursor.getColumnIndexOrThrow("pisos"))
                val lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"))
                val lon = cursor.getString(cursor.getColumnIndexOrThrow("lon"))
                val imageData = cursor.getBlob(cursor.getColumnIndexOrThrow("icon"))
                val imageData64 =  Base64.getEncoder().encodeToString(imageData)

                //VERTICE DATA
                val v1 = cursor.getString(cursor.getColumnIndexOrThrow("v1"))
                val v2 = cursor.getString(cursor.getColumnIndexOrThrow("v2"))
                val v3 = cursor.getString(cursor.getColumnIndexOrThrow("v3"))
                val v4 = cursor.getString(cursor.getColumnIndexOrThrow("v4"))
                edificios.add(Edificio(id, nombre, tipo, pisos, lat, lon, imageData64, v1, v2, v3, v4))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return edificios
    }

    fun getAllSalones(): List<Salon>{
        val salones = mutableListOf<Salon>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM salon", null)
        if (cursor.moveToFirst()) {
            do {
                val salonid = cursor.getInt(cursor.getColumnIndexOrThrow("salonid"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val piso = cursor.getString(cursor.getColumnIndexOrThrow("piso"))
                val edificio_edificioid = cursor.getInt(cursor.getColumnIndexOrThrow("edificio_edificioid"))
                salones.add(Salon(salonid, nombre, descripcion, tipo, piso, edificio_edificioid))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return salones

    }

    fun getSalon(salonid:String): List<Salon>{
        val salones = mutableListOf<Salon>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM salon WHERE salonid = $salonid", null)
        if (cursor.moveToFirst()) {
            do {
                val salonid = cursor.getInt(cursor.getColumnIndexOrThrow("salonid"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val piso = cursor.getString(cursor.getColumnIndexOrThrow("piso"))
                val edificio_edificioid = cursor.getInt(cursor.getColumnIndexOrThrow("edificio_edificioid"))
                salones.add(Salon(salonid, nombre, descripcion, tipo, piso, edificio_edificioid))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return salones

    }

    fun getEdificio(id:String): List<Edificio> {
        val edificio = mutableListOf<Edificio>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM edificio WHERE id = $id", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val pisos = cursor.getString(cursor.getColumnIndexOrThrow("pisos"))
                val lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"))
                val lon = cursor.getString(cursor.getColumnIndexOrThrow("lon"))
                val imageData = cursor.getBlob(cursor.getColumnIndexOrThrow("icon"))
                val imageData64 =  Base64.getEncoder().encodeToString(imageData)

                //VERTICE DATA
                val v1 = cursor.getString(cursor.getColumnIndexOrThrow("v1"))
                val v2 = cursor.getString(cursor.getColumnIndexOrThrow("v2"))
                val v3 = cursor.getString(cursor.getColumnIndexOrThrow("v3"))
                val v4 = cursor.getString(cursor.getColumnIndexOrThrow("v4"))
                edificio.add(Edificio(id, nombre, tipo, pisos, lat, lon, imageData64, v1, v2, v3, v4))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return edificio
    }

    fun getSalonesAtEdificio(edificio: String): List<Salon>{
        val salones = mutableListOf<Salon>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM salon WHERE edificio_edificioid == '$edificio' ORDER BY indice", null)
        if (cursor.moveToFirst()) {
            do {
                val salonid = cursor.getInt(cursor.getColumnIndexOrThrow("salonid"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val piso = cursor.getString(cursor.getColumnIndexOrThrow("piso"))
                val edificio_edificioid = cursor.getInt(cursor.getColumnIndexOrThrow("edificio_edificioid"))
                salones.add(Salon(salonid, nombre, descripcion, tipo, piso, edificio_edificioid))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return salones
    }

    fun getSalonSearches(search:String):List<Salon>{
        val results = mutableListOf<Salon>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM salon WHERE nombre LIKE '%$search%' AND tipo !=" +
                " 'vacio' ORDER BY tipo", null)
        if (cursor.moveToFirst()){
            do {
                val salonid = cursor.getInt(cursor.getColumnIndexOrThrow("salonid"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                val piso = cursor.getString(cursor.getColumnIndexOrThrow("piso"))
                val edificio_edificioid = cursor.getInt(cursor.getColumnIndexOrThrow("edificio_edificioid"))
                results.add(Salon(salonid, nombre, descripcion, tipo, piso, edificio_edificioid))
            }while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }

    fun getCustomParametersSalon(id:String):List<Parametros>{
        val results = mutableListOf<Parametros>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM parametros WHERE par_salonid = $id", null)
        if (cursor.moveToFirst()){
            do {
                val par_salonid = cursor.getInt(cursor.getColumnIndexOrThrow("par_salonid"))
                val v1 = cursor.getString(cursor.getColumnIndexOrThrow("v1"))
                val v2 = cursor.getString(cursor.getColumnIndexOrThrow("v2"))
                val v3 = cursor.getString(cursor.getColumnIndexOrThrow("v3"))
                val v4 = cursor.getString(cursor.getColumnIndexOrThrow("v4"))
                results.add(Parametros(par_salonid,v1, v2, v3, v4))
            }while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }

    //COMPARE DATABASE HASH

    private fun compareDatabaseHashes(context: Context): Boolean {
        val assetDbHash = getDatabaseHash(context.assets.open(DATABASE_NAME))
        val existingDbHash = getDatabaseHash(FileInputStream(dbPath))
        return assetDbHash == existingDbHash
    }

    private fun getDatabaseHash(inputStream: InputStream): String? {
        try {
            val buffer = ByteArray(1024)
            val digest = MessageDigest.getInstance("SHA-256")
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            val hashBytes = digest.digest()
            val stringBuilder = StringBuilder()
            for (byte in hashBytes) {
                stringBuilder.append(String.format("%02x", byte))
            }
            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    //REMOTE DATABASE
    private fun isInternetAvailable(context: Context): Boolean {
        if(sharedViewModel.dbLoaded.value != true){
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            val isConnected = networkCapabilities != null &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            Log.println(Log.INFO, "InternetCheck", "checking internet connection...")

            if (!isConnected) {
                return false
            }

            return try {

                val process = Runtime.getRuntime().exec("ping -c 1 google.com")
                val exitValue = process.waitFor()
                exitValue == 0
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } catch (e: InterruptedException) {
                e.printStackTrace()
                false
            }
        }
        return false
    }

    private fun downloadDatabase(context: Context, latch: CountDownLatch) {
        val client = OkHttpClient.Builder()
            .connectTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(REMOTE_DATABASE_URL)
            .header("User-Agent", "CUCSURMapApp-${context.getString(R.string.app_version)} ${Build.MANUFACTURER}:${Build.MODEL}")
            .build()

        try{
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    e.printStackTrace()
                    latch.countDown()
                    if(sharedViewModel.dbLoaded.value != true){
                        (context as? Activity)?.runOnUiThread {
                            Toast.makeText(context, "Error al descargar la base remota, usando BD mas reciente", Toast.LENGTH_SHORT).show()
                        }

                        sharedViewModel.dbLoaded.postValue(true)
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (!response.isSuccessful) {
                        latch.countDown()
                        sharedViewModel.dbLoaded.postValue(true)
                        return
                    }
                    response.body?.let { body ->
                        val file = File(dbPath)
                        val parentDir = file.parentFile

                        // Ensure the directory exists
                        if (parentDir != null) {
                            if (!parentDir.exists()) {
                                parentDir.mkdirs() // Create directories if not exist
                            }
                        }

                        val inputStream: InputStream = body.byteStream()
                        val outputStream = FileOutputStream(file)

                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    latch.countDown()
                    sharedViewModel.dbLoaded.postValue(true)
                    (context as? Activity)?.runOnUiThread {
                        Toast.makeText(context, "Éxito!, ${getVersion()[0]}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }catch (e: SocketTimeoutException){
            e.printStackTrace()

            sharedViewModel.dbLoaded.postValue(true)
        }

    }

    fun getVersion(): List<String> {
        val info = mutableListOf<String>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM info", null)
        if (cursor.moveToFirst()) {
            do {
                val version = cursor.getString(cursor.getColumnIndexOrThrow("version"))
                info.add("Current DB Version: $version")

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return info
    }

    private fun isLocalHigherVersion(context: Context): Boolean {
        val assetVersion = getDatabaseVersionFromAssets(context)
        val currentVersion = getCurrentDatabaseVersion()

        return assetVersion > currentVersion
    }

    // Helper to get version from the asset database
    private fun getDatabaseVersionFromAssets(context: Context): Double {
        return try {
            val assetDbStream = context.assets.open(DATABASE_NAME)
            val tempFile = File(context.cacheDir, DATABASE_NAME)
            val outputStream = FileOutputStream(tempFile)

            assetDbStream.copyTo(outputStream)

            val tempDb = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = tempDb.rawQuery("SELECT version FROM info", null)
            val version = if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow("version")) else "0.0"
            cursor.close()
            tempDb.close()
            tempFile.delete() // Clean up the temporary file
            Log.i("ver check","asset ver $version")
            version.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }


    // Helper to get the current version from the local database
    private fun getCurrentDatabaseVersion(): Double {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT version FROM info", null)
            val version = if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow("version")) else "0.0"
            cursor.close()
            db.close()
            Log.i("ver check","current ver $version")
            version.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }
}
