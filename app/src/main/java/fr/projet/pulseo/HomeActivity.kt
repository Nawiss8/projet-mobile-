package com.pulseo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var btnLogout: Button
    private lateinit var btnImportMusic: Button
    private lateinit var tvWelcome: TextView
    private lateinit var lvMusicList: ListView

    private lateinit var tvCurrentSongName: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var pbProgress: ProgressBar
    private lateinit var btnPrevious: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnNext: Button

    private val songsList = mutableListOf<Song>()
    private lateinit var songAdapter: SongAdapter
    private val musicPlayer = MusicPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize Views
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
        btnImportMusic = findViewById(R.id.btnImportMusic)
        lvMusicList = findViewById(R.id.lvMusicList)

        tvCurrentSongName = findViewById(R.id.tvCurrentSongName)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        pbProgress = findViewById(R.id.pbProgress)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)

        // Setup Adapter
        songAdapter = SongAdapter(this, songsList)
        lvMusicList.adapter = songAdapter

        // Affiche l'email de l'utilisateur connecté
        val currentUser = auth.currentUser
        tvWelcome.text = "Welcome, ${currentUser?.email ?: "User"} !"

        // Logout Button
        btnLogout.setOnClickListener {
            musicPlayer.stop()
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Import Music Button
        btnImportMusic.setOnClickListener {
            startActivity(Intent(this, ImportMusicActivity::class.java))
        }

        // Music Player Controls
        btnPlayPause.setOnClickListener {
            if (musicPlayer.isPlaying()) {
                musicPlayer.pause()
                btnPlayPause.text = "▶"
            } else {
                if (musicPlayer.getCurrentSong() != null) {
                    musicPlayer.resume()
                    btnPlayPause.text = "⏸"
                } else {
                    Toast.makeText(this, "No song selected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnNext.setOnClickListener {
            musicPlayer.next()
            updatePlayerUI()
        }

        btnPrevious.setOnClickListener {
            musicPlayer.previous()
            updatePlayerUI()
        }

        pbProgress.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicPlayer.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Setup progress update listener
        musicPlayer.setOnProgressUpdateListener { currentTime, totalTime, isPlaying ->
            tvCurrentTime.text = formatTime(currentTime)
            tvTotalTime.text = formatTime(totalTime)
            pbProgress.max = totalTime.toInt()
            pbProgress.progress = currentTime.toInt()

            if (isPlaying) {
                btnPlayPause.text = "⏸"
            }
        }

        // Click on song to play
        lvMusicList.setOnItemClickListener { _, _, position, _ ->
            val selectedSong = songsList[position]
            musicPlayer.setSongs(songsList)
            musicPlayer.playAtIndex(position)
            updatePlayerUI()
        }

        // Charger les musiques
        loadSongs()
    }

    private fun loadSongs() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Écouter les changements en temps réel
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

                    musicPlayer.setSongs(songsList)
                    songAdapter.notifyDataSetChanged()

                    if (songsList.isEmpty()) {
                        tvCurrentSongName.text = "No songs imported"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Error loading songs: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updatePlayerUI() {
        val currentSong = musicPlayer.getCurrentSong()
        if (currentSong != null) {
            tvCurrentSongName.text = currentSong.name
            btnPlayPause.text = "⏸"
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stop()
    }
}