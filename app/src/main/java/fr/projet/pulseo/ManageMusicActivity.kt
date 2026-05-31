package com.pulseo

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

class ManageMusicActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: Button
    private val songsList = mutableListOf<Song>()
    private lateinit var adapter: MusicCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_music)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        rvSongs = findViewById(R.id.rvSongs)
        btnBack = findViewById(R.id.btnBack)

        // Setup RecyclerView
        rvSongs.layoutManager = LinearLayoutManager(this)
        adapter = MusicCardAdapter(
            this,
            songsList,
            { song -> showEditDialog(song) },
            { song -> showDeleteConfirmation(song) }
        )
        rvSongs.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        loadSongs()
    }

    private fun loadSongs() {
        database.reference
            .child("songs")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    songsList.clear()

                    for (songSnapshot in snapshot.children) {
                        val song = songSnapshot.getValue(Song::class.java)
                        if (song != null) {
                            songsList.add(song)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ManageMusicActivity,
                        "Error loading songs",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showEditDialog(song: Song) {
        val editText = EditText(this).apply {
            setText(song.name)
            setSelection(song.name.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Song Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateSongName(song, newName)
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSongName(song: Song, newName: String) {
        database.reference
            .child("songs")
            .child(song.id)
            .child("name")
            .setValue(newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error updating", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation(song: Song) {
        AlertDialog.Builder(this)
            .setTitle("Delete?")
            .setMessage("Delete \"${song.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSong(song)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSong(song: Song) {
        try {
            val file = File(song.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        database.reference
            .child("songs")
            .child(song.id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
            }
    }
}