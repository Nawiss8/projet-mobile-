package com.pulseo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SongAdapter(
    context: Context,
    private val songs: MutableList<Song>
) : ArrayAdapter<Song>(context, 0, songs) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.song_list_item,
            parent,
            false
        )

        val song = getItem(position)
        if (song != null) {
            val tvSongName = view.findViewById<TextView>(R.id.tvSongName)
            val tvSongDuration = view.findViewById<TextView>(R.id.tvSongDuration)

            tvSongName.text = song.name

            val minutes = song.duration / 60
            val seconds = song.duration % 60
            tvSongDuration.text = String.format("%d:%02d", minutes, seconds)
        }

        return view
    }
}