package com.pulseo

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MusicCardAdapter(
    private val context: Context,
    private val songs: List<Song>,
    private val onEditClick: (Song) -> Unit,
    private val onDeleteClick: (Song) -> Unit
) : RecyclerView.Adapter<MusicCardAdapter.SongViewHolder>() {

    private val colors = listOf(
        "#FF006E", // Pink
        "#00D4FF", // Cyan
        "#00FF88", // Green
        "#FFB700", // Orange
        "#9D00FF"  // Purple
    )

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardSong = itemView.findViewById<CardView>(R.id.cardSong)
        val tvName = itemView.findViewById<TextView>(R.id.tvCardSongName)
        val tvDuration = itemView.findViewById<TextView>(R.id.tvCardDuration)
        val tvDateAdded = itemView.findViewById<TextView>(R.id.tvCardDateAdded)
        val btnEdit = itemView.findViewById<Button>(R.id.btnCardEdit)
        val btnDelete = itemView.findViewById<Button>(R.id.btnCardDelete)

        fun bind(song: Song, colorIndex: Int) {
            tvName.text = song.name

            // Duration
            val minutes = song.duration / 60
            val seconds = song.duration % 60
            tvDuration.text = String.format("%d:%02d", minutes, seconds)

            // Date Added
            val dateFormat = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault())
            tvDateAdded.text = dateFormat.format(Date(song.dateImported))

            // Card color
            val color = Color.parseColor(colors[colorIndex % colors.size])
            cardSong.setCardBackgroundColor(color)

            // Edit button
            btnEdit.setOnClickListener {
                onEditClick(song)
            }

            // Delete button
            btnDelete.setOnClickListener {
                onDeleteClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.music_card_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], position)
    }

    override fun getItemCount() = songs.size
}