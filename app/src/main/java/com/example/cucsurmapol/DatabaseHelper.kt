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

    data class Edificio(val id: Int, val nombre: String, val tipo: String, val pisos: String, val lat: String, val lon: String, val image: String)

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
                edificios.add(Edificio(id, nombre, tipo, pisos, lat, lon, imageData64))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return edificios
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
