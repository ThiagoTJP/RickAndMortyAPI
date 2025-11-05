package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.database.FavoritosDatabase

class CharacterAdapter(
    private val context: Context,
    private val characters: List<Personaje>,
    private val onClick: (Personaje) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.ViewHolder>() {

    private val db = FavoritosDatabase(context)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCharacter: ImageView = view.findViewById(R.id.ivCharacter)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSpecies: TextView = view.findViewById(R.id.tvSpecies)
        val ivFav: ImageView = view.findViewById(R.id.ivFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = characters.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val character = characters[position]

        holder.tvName.text = character.name
        holder.tvSpecies.text = character.species
        Glide.with(holder.itemView).load(character.image).into(holder.ivCharacter)

        // Actualizar ícono si ya está en favoritos
        val favorito = db.esFavorito(character.id)
        holder.ivFav.setImageResource(if (favorito) R.drawable.fav_lleno else R.drawable.fav)

        // Toggle favorito
        holder.ivFav.setOnClickListener {
            if (db.esFavorito(character.id)) {
                db.eliminarFavorito(character.id)
                holder.ivFav.setImageResource(R.drawable.fav)
            } else {
                db.agregarFavorito(character.id, character.name, character.species, character.image)
                holder.ivFav.setImageResource(R.drawable.fav_lleno)
            }
        }

        // Click general del personaje
        holder.itemView.setOnClickListener { onClick(character) }
    }
}
