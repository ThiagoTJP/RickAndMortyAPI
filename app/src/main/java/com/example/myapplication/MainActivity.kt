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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// MainActivity: displays a list of characters and allows searching by name
class MainActivity : AppCompatActivity() {

    private val TAG = "RickMortyAPI" // Tag for Logcat

    private lateinit var recyclerView: RecyclerView
    private lateinit var characterAdapter: CharacterAdapter
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var tvNoResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.rvPersonajes)
        recyclerView.layoutManager = LinearLayoutManager(this) // Linear vertical list

        // Initialize views
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        tvNoResults = findViewById(R.id.tvNoResults)

        // Listener for search button
        btnSearch.setOnClickListener {
            val name = etSearch.text.toString().trim()
            if(name.isNotEmpty()) {
                getCharacterByName(name)
            }
        }

        // Initially display all characters
        getCharacterByName("")
    }

    // Fetch characters by name and update the RecyclerView
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
                            tvNoResults.visibility = View.GONE
                            characterAdapter = CharacterAdapter(this@MainActivity, characters) { personaje ->
                                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                                intent.putExtra("characterId", personaje.id)
                                startActivity(intent)
                            }

                            recyclerView.adapter = characterAdapter
                        } else {
                            // No characters found for the query
                            tvNoResults.visibility = View.VISIBLE
                            recyclerView.adapter = null
                        }
                    } else {
                        // API response failed
                        tvNoResults.visibility = View.VISIBLE
                        recyclerView.adapter = null
                    }
                }

                override fun onFailure(call: Call<PersonajesRickMorty>, t: Throwable) {
                    // Handle network or API failure
                    Log.e(TAG, "Hubo un error al hacer el request: ${t.message}")
                    tvNoResults.visibility = View.VISIBLE
                    recyclerView.adapter = null
                }
            })
    }
}
