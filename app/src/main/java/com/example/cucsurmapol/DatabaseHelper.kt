package com.example.cucsurmapol

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.Base64

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cucsur_data.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PATH = "/databases/"
    }

    private val dbPath: String = context.applicationInfo.dataDir + DATABASE_PATH + DATABASE_NAME

    init {
//        val dbFile = File(dbPath)
//        if (!dbFile.exists() && !compareDatabaseHashes(context)) {
            copyDatabase(context)
//        }
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

    fun getSalonesAtEdificio(edificio: String): List<Salon>{
        val salones = mutableListOf<Salon>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM salon WHERE edificio_edificioid == '$edificio'", null)
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
        val db: SQLiteDatabase = this.readableDatabase;
        val cursor = db.rawQuery("SELECT * FROM salon WHERE nombre LIKE '%$search%'", null)
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
}
