package com.pulseo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.SeekBar
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
    private lateinit var btnManageMusic: Button
    private lateinit var btnTheme: Button
    private lateinit var tvWelcome: TextView
    private lateinit var lvMusicList: ListView
    private lateinit var tvCurrentSongName: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var pbProgress: SeekBar
    private lateinit var btnPrevious: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnNext: Button

    private val songsList = mutableListOf<Song>()
    private lateinit var songAdapter: SongAdapter
    private val musicPlayer = MusicPlayer()
    private var songsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        initUI()
        setupClickListeners()

        tvWelcome.text = "🎵 Pulseo"
        tvCurrentSongName.text = "📂 Chargement..."

        loadSongsOnce()
    }

    private fun initUI() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
        btnImportMusic = findViewById(R.id.btnImportMusic)
        btnManageMusic = findViewById(R.id.btnManageMusic)
        btnTheme = findViewById(R.id.btnTheme)
        lvMusicList = findViewById(R.id.lvMusicList)
        tvCurrentSongName = findViewById(R.id.tvCurrentSongName)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        pbProgress = findViewById(R.id.pbProgress)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)

        songAdapter = SongAdapter(this, songsList)
        lvMusicList.adapter = songAdapter
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            musicPlayer.stop()
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnImportMusic.setOnClickListener {
            startActivity(Intent(this, ImportMusicActivity::class.java))
        }

        btnManageMusic.setOnClickListener {
            startActivity(Intent(this, ManageMusicActivity::class.java))
        }

        btnTheme.setOnClickListener {
            ThemeHelper.toggleTheme(this)
            Toast.makeText(this, "Thème changé au redémarrage", Toast.LENGTH_SHORT).show()
        }

        btnPlayPause.setOnClickListener {
            if (musicPlayer.isPlaying()) {
                musicPlayer.pause()
                btnPlayPause.text = "▶"
            } else {
                if (musicPlayer.getCurrentSong() != null) {
                    musicPlayer.resume()
                    btnPlayPause.text = "⏸"
                }
            }
        }

        btnNext.setOnClickListener {
            musicPlayer.next()
            updateUI()
        }

        btnPrevious.setOnClickListener {
            musicPlayer.previous()
            updateUI()
        }

        pbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicPlayer.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        musicPlayer.setOnProgressUpdateListener { currentTime, totalTime, isPlaying ->
            runOnUiThread {
                tvCurrentTime.text = formatTime(currentTime)
                tvTotalTime.text = formatTime(totalTime)
                pbProgress.max = totalTime.toInt()
                pbProgress.progress = currentTime.toInt()
                if (isPlaying) {
                    btnPlayPause.text = "⏸"
                }
            }
        }

        lvMusicList.setOnItemClickListener { _, _, position, _ ->
            if (position < songsList.size) {
                val song = songsList[position]
                musicPlayer.setSongs(songsList)
                musicPlayer.playAtIndex(position)
                updateUI()
                Toast.makeText(this, "🎵 ${song.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSongsOnce() {
        val user = auth.currentUser
        if (user == null || songsLoaded) {
            return
        }

        songsLoaded = true

        database.reference
            .child("songs")
            .orderByChild("userId")
            .equalTo(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    songsList.clear()
                    for (snap in snapshot.children) {
                        val song = snap.getValue(Song::class.java)
                        if (song != null) {
                            song.id = snap.key ?: ""
                            songsList.add(song)
                        }
                    }
                    musicPlayer.setSongs(songsList)
                    songAdapter.notifyDataSetChanged()

                    if (songsList.isEmpty()) {
                        tvCurrentSongName.text = "📂 Importe une chanson"
                    } else {
                        tvCurrentSongName.text = "📂 ${songsList.size} chansons"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    tvCurrentSongName.text = "❌ Erreur chargement"
                }
            })
    }

    private fun updateUI() {
        val song = musicPlayer.getCurrentSong()
        if (song != null) {
            tvCurrentSongName.text = "▶️ ${song.name}"
            btnPlayPause.text = "⏸"
        }
    }

    private fun formatTime(ms: Long): String {
        val sec = (ms / 1000) % 60
        val min = (ms / 1000) / 60
        return String.format("%d:%02d", min, sec)
    }

    override fun onResume() {
        super.onResume()
        songsLoaded = false
        loadSongsOnce()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stop()
    }
}