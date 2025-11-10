package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.FavoritosDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val TAG = "RickMortyAPI"

    // UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var characterAdapter: CharacterAdapter
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var tvNoResults: TextView
    private lateinit var btnVerFavoritos: ImageButton

    // Database instance
    private lateinit var db: FavoritosDatabase
    // Flag to check if we are showing favorites or all characters
    private var mostrandoFavoritos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.rvPersonajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        tvNoResults = findViewById(R.id.tvNoResults)
        btnVerFavoritos = findViewById(R.id.btnVerFavoritos)

        // Initialize local SQLite database
        db = FavoritosDatabase(this)

        // get characters by name
        btnSearch.setOnClickListener {
            val name = etSearch.text.toString().trim()
            if (name.isNotEmpty()) getCharacterByName(name)
        }

        // Toggle between favorites and all characters
        btnVerFavoritos.setOnClickListener {
            mostrandoFavoritos = !mostrandoFavoritos

            if (mostrandoFavoritos) {
                // Show only favorite characters
                mostrarFavoritos()
                btnVerFavoritos.setImageResource(R.drawable.fav_lleno) // cambia a icono lleno
            } else {
                // Show all characters
                getCharacterByName("")
                btnVerFavoritos.setImageResource(R.drawable.fav) // vuelve al icono vacío
            }
        }

        // Show all characters when app starts
        getCharacterByName("")
    }

    // Fetch characters by name from the API
    private fun getCharacterByName(name: String) {
        Log.d(TAG, "Buscando al personaje $name")

        RetrofitClient.apiService.getCharacterByName(name)
            .enqueue(object : Callback<PersonajesRickMorty> {
                override fun onResponse(
                    call: Call<PersonajesRickMorty>,
                    response: Response<PersonajesRickMorty>
                ) {
                    if (response.isSuccessful) {
                        val characters = response.body()?.results ?: emptyList()
                        if (characters.isNotEmpty()) {
                            // Hide “no results” message
                            tvNoResults.visibility = View.GONE
                            // Set adapter with fetched characters
                            characterAdapter = CharacterAdapter(this@MainActivity, characters) { personaje ->
                                // When a character is clicked, go to detail screen
                                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                                intent.putExtra("characterId", personaje.id)
                                startActivity(intent)
                            }
                            recyclerView.adapter = characterAdapter
                        } else {
                            // Show “no results” message
                            tvNoResults.visibility = View.VISIBLE
                            recyclerView.adapter = null
                        }
                    } else {
                        // Show “no results” if API response failed
                        tvNoResults.visibility = View.VISIBLE
                        recyclerView.adapter = null
                    }
                }

                override fun onFailure(call: Call<PersonajesRickMorty>, t: Throwable) {
                    Log.e(TAG, "Hubo un error al hacer el request: ${t.message}")
                    tvNoResults.visibility = View.VISIBLE
                    recyclerView.adapter = null
                }
            })
    }

    // Display only the characters saved as favorites in the local database
    private fun mostrarFavoritos() {
        val favoritos = db.obtenerFavoritos()

        if (favoritos.isNotEmpty()) {
            tvNoResults.visibility = View.GONE
            // Convert data from database (Map<String, String>) to Personaje objects
            val personajesFavoritos = favoritos.map {
                Personaje(
                    id = it["id"]!!.toInt(),
                    name = it["name"]!!,
                    status = "",
                    species = it["species"]!!,
                    type = "",
                    gender = "",
                    image = it["image"]!!,
                    origin = Origen("", ""),
                    location = Location("", "")
                )
            }

            // Set adapter to display only favorites
            characterAdapter = CharacterAdapter(this, personajesFavoritos) { personaje ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("characterId", personaje.id)
                startActivity(intent)
            }
            recyclerView.adapter = characterAdapter
        } else {
            // If there are no favorites, show “no results” message
            tvNoResults.visibility = View.VISIBLE
            recyclerView.adapter = null
        }
    }
}
