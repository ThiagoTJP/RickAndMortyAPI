package com.example.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoritosDatabase(context: Context) : SQLiteOpenHelper(
    context,
    "favoritos.db", // Database name
    null,
    1
) {

    // Called only once when the database is created for the first time
    override fun onCreate(db: SQLiteDatabase?) {
        val crearTabla = """
            CREATE TABLE favoritos(
                id INTEGER PRIMARY KEY,
                name TEXT,
                species TEXT,
                image TEXT
            )
        """
        db?.execSQL(crearTabla) // execute SQL command to create the table
    }
    // Called automatically when the database version changes
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS favoritos") // delete the old table
        onCreate(db) // recreate the table
    }


    // Adds a new favorite character (avoids duplicates using CONFLICT_IGNORE)
    fun agregarFavorito(id: Int, name: String, species: String, image: String): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("id", id)
            put("name", name)
            put("species", species)
            put("image", image)
        }

        val resultado = db.insertWithOnConflict(
            "favoritos",
            null,
            valores,
            SQLiteDatabase.CONFLICT_IGNORE // prevents duplicate entries
        )
        db.close()
        return resultado != -1L // returns true if the insertion succeeded
    }


    // Deletes a character from favorites by its ID
    fun eliminarFavorito(id: Int): Boolean {
        val db = writableDatabase
        val filas = db.delete("favoritos", "id = ?", arrayOf(id.toString()))
        db.close()
        return filas > 0 // returns true if at least one row was deleted
    }

    // Checks if a character is already in favorites
    fun esFavorito(id: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM favoritos WHERE id = ?", arrayOf(id.toString()))
        val existe = cursor.moveToFirst()
        cursor.close()
        db.close()
        return existe
    }


    // Returns a list of all favorite characters as a list of maps
    fun obtenerFavoritos(): List<Map<String, String>> {
        val favoritos = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM favoritos", null)

        if (cursor.moveToFirst()) {
            do {
                val item = mapOf(
                    "id" to cursor.getInt(0).toString(),
                    "name" to cursor.getString(1),
                    "species" to cursor.getString(2),
                    "image" to cursor.getString(3)
                )
                favoritos.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return favoritos
    }
}
