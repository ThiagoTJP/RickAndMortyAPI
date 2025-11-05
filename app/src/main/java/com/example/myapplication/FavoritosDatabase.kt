package com.example.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoritosDatabase(context: Context) : SQLiteOpenHelper(
    context,
    "favoritos.db",
    null,
    1
) {
    override fun onCreate(db: SQLiteDatabase?) {
        val crearTabla = """
            CREATE TABLE favoritos(
                id INTEGER PRIMARY KEY,
                name TEXT,
                species TEXT,
                image TEXT
            )
        """
        db?.execSQL(crearTabla)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS favoritos")
        onCreate(db)
    }

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
            SQLiteDatabase.CONFLICT_IGNORE // evita duplicados
        )
        db.close()
        return resultado != -1L
    }

    fun eliminarFavorito(id: Int): Boolean {
        val db = writableDatabase
        val filas = db.delete("favoritos", "id = ?", arrayOf(id.toString()))
        db.close()
        return filas > 0
    }

    fun esFavorito(id: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM favoritos WHERE id = ?", arrayOf(id.toString()))
        val existe = cursor.moveToFirst()
        cursor.close()
        db.close()
        return existe
    }

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
