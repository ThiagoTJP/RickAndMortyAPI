package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var btnViewFavorites: ImageButton // Renamed from btnVerFavoritos

    // Database instance
    private lateinit var db: FavoritosDatabase
    // Flag to check if we are showing favorites or all characters
    private var isShowingFavorites = false // Renamed from mostrandoFavoritos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.rvPersonajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        tvNoResults = findViewById(R.id.tvNoResults)
        btnViewFavorites = findViewById(R.id.btnVerFavoritos)

        // Initialize local SQLite database
        db = FavoritosDatabase(this)

        // Get characters by name
        btnSearch.setOnClickListener {
            val name = etSearch.text.toString().trim()

            // If we are in favorites mode, we search WITHIN the local favorites
            if (isShowingFavorites) {
                displayFavorites(name) // Call displayFavorites with the name filter
            } else {
                // Otherwise, we search the API
                getCharacterByName(name)
            }
        }

        // Toggle between favorites and all characters
        btnViewFavorites.setOnClickListener {
            isShowingFavorites = !isShowingFavorites

            etSearch.setText("") // Clear search field when changing mode

            if (isShowingFavorites) {
                // Show only favorite characters
                displayFavorites() // No initial filter
                btnViewFavorites.setImageResource(R.drawable.fav_lleno) // Change to filled icon
            } else {
                // Show all characters (no initial name filter)
                getCharacterByName("")
                btnViewFavorites.setImageResource(R.drawable.fav) // Change back to empty icon
            }
        }

        // Show all characters when app starts
        getCharacterByName("")
    }

    override fun onResume() {
        super.onResume()

        // This method runs every time the Activity returns to the foreground (e.g., from DetailActivity)

        // If we were viewing the favorites list, we reload it to reflect any changes made in DetailActivity.
        if (isShowingFavorites) {
            displayFavorites() // Reloads the full favorite list (no filter needed on resume)
        }

        // Notify the adapter that the item data (specifically the favorite icon state)
        // might have changed and needs to be refreshed.
        recyclerView.adapter?.notifyDataSetChanged()
    }

    // Fetch characters by name from the API
    private fun getCharacterByName(name: String) {
        Log.d(TAG, "Searching for character: $name")

        RetrofitClient.apiService.getCharacterByName(name)
            .enqueue(object : Callback<PersonajesRickMorty> {
                override fun onResponse(
                    call: Call<PersonajesRickMorty>,
                    response: Response<PersonajesRickMorty>
                ) {
                    if (response.isSuccessful) {
                        val characters = response.body()?.results ?: emptyList()
                        if (characters.isNotEmpty()) {
                            // Hide "no results" message
                            tvNoResults.visibility = View.GONE
                            // Set adapter with fetched characters
                            characterAdapter = CharacterAdapter(this@MainActivity, characters) { character ->
                                // When a character is clicked, go to detail screen
                                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                                intent.putExtra("characterId", character.id)
                                startActivity(intent)
                            }
                            recyclerView.adapter = characterAdapter
                        } else {
                            // Show "no results" message
                            tvNoResults.visibility = View.VISIBLE
                            recyclerView.adapter = null
                        }
                    } else {
                        // Show "no results" if API response failed
                        tvNoResults.visibility = View.VISIBLE
                        recyclerView.adapter = null
                    }
                }

                override fun onFailure(call: Call<PersonajesRickMorty>, t: Throwable) {
                    Log.e(TAG, "Error during request: ${t.message}")
                    tvNoResults.visibility = View.VISIBLE
                    recyclerView.adapter = null
                }
            })
    }

    // Display only the characters saved as favorites in the local database,
    // optionally filtered by 'name'
    private fun displayFavorites(filterName: String = "") {
        val favoritesFromDB = db.obtenerFavoritos() // Get ALL favorites

        // Convert data from database (Map<String, String>) to Personaje objects
        val favoriteCharacters = favoritesFromDB.map {
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

        // Apply the filter if a name was provided
        val filteredFavorites = if (filterName.isEmpty()) {
            favoriteCharacters
        } else {
            // Filter by name
            favoriteCharacters.filter {
                it.name.contains(filterName, ignoreCase = true)
            }
        }

        if (filteredFavorites.isNotEmpty()) {
            tvNoResults.visibility = View.GONE

            // 3. Use the FILTERED list for the adapter
            characterAdapter = CharacterAdapter(this, filteredFavorites) { character ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("characterId", character.id)
                startActivity(intent)
            }
            recyclerView.adapter = characterAdapter
        } else {
            // If there are no favorites or the filter finds nothing, show "no results" message
            tvNoResults.visibility = View.VISIBLE
            recyclerView.adapter = null
        }
    }
}