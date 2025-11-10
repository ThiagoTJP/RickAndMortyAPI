package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.database.FavoritosDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity that shows detailed information about a selected character
class DetailActivity : AppCompatActivity() {

    private lateinit var db: FavoritosDatabase
    private lateinit var btnFavDetail: ImageButton
    private var personajeActual: Personaje? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Initialize favorite button and database
        btnFavDetail = findViewById(R.id.btnFavDetail)
        db = FavoritosDatabase(this)

        // Get the character ID sent from MainActivity
        val characterId = intent.getIntExtra("characterId", -1)

        if (characterId != -1) {
            getCharacterById(characterId)
        }
        // Set up "Back" button to close this activity and return to MainActivity
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish() // Close DetailActivity and back to MainActivity
        }
    }



    // Fetch character details from API by ID
    private fun getCharacterById(characterId: Int) {
        RetrofitClient.apiService.getCharacterById(characterId)
            .enqueue(object : Callback<Personaje> {
                override fun onResponse(call: Call<Personaje>, response: Response<Personaje>) {
                    if (response.isSuccessful) {
                        val c = response.body()!!
                        personajeActual = c

                        //Update UI with character details
                        findViewById<TextView>(R.id.tvNameDetail).text = c.name
                        findViewById<TextView>(R.id.tvStatus).text = "Status: ${c.status}"
                        findViewById<TextView>(R.id.tvSpecies).text = "Species: ${c.species}"
                        findViewById<TextView>(R.id.tvGender).text = "Gender: ${c.gender}"
                        findViewById<TextView>(R.id.tvOrigin).text = "Origin: ${c.origin.name}"
                        findViewById<TextView>(R.id.tvLocation).text = "Location: ${c.location.name}"

                        // Load character image
                        Glide.with(this@DetailActivity)
                            .load(c.image)
                            .into(findViewById(R.id.ivCharacterDetail))

                        // 🩷 Check if character is in favorites
                        val isFav = db.esFavorito(c.id)
                        btnFavDetail.setImageResource(
                            if (isFav) R.drawable.fav_lleno else R.drawable.fav
                        )

                        // Add favorite toggle functionality
                        btnFavDetail.setOnClickListener {
                            if (db.esFavorito(c.id)) {
                                db.eliminarFavorito(c.id)
                                btnFavDetail.setImageResource(R.drawable.fav)
                            } else {
                                db.agregarFavorito(c.id, c.name, c.species, c.image)
                                btnFavDetail.setImageResource(R.drawable.fav_lleno)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<Personaje>, t: Throwable) {
                    // Handle API failure (optional)
                }
            })
    }
}
