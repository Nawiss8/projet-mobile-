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
            android.R.layout.simple_list_item_2,
            parent,
            false
        )

        val song = getItem(position)
        if (song != null) {
            val textView1 = view.findViewById<TextView>(android.R.id.text1)
            val textView2 = view.findViewById<TextView>(android.R.id.text2)

            textView1.text = song.name
            textView1.setTextColor(android.graphics.Color.WHITE)

            val minutes = song.duration / 60
            val seconds = song.duration % 60
            textView2.text = String.format("%d:%02d", minutes, seconds)
            textView2.setTextColor(android.graphics.Color.GRAY)
        }

        view.setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
        return view
    }
}