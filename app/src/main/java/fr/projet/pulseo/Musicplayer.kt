package com.pulseo

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class MusicPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSongIndex = 0
    private val songs = mutableListOf<Song>()
    private val handler = Handler(Looper.getMainLooper())
    private var updateCallback: ((currentTime: Long, totalTime: Long, isPlaying: Boolean) -> Unit)? = null

    fun setSongs(songList: List<Song>) {
        songs.clear()
        songs.addAll(songList)
        if (currentSongIndex >= songs.size) {
            currentSongIndex = 0
        }
    }

    fun play(song: Song) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(song.filePath)
                prepare()
                start()
                startProgressUpdate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playAtIndex(index: Int) {
        if (index >= 0 && index < songs.size) {
            currentSongIndex = index
            play(songs[index])
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        updateCallback?.invoke(
            mediaPlayer?.currentPosition?.toLong() ?: 0,
            mediaPlayer?.duration?.toLong() ?: 0,
            false
        )
    }

    fun resume() {
        mediaPlayer?.start()
        startProgressUpdate()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }

    fun next() {
        currentSongIndex = (currentSongIndex + 1) % songs.size
        playAtIndex(currentSongIndex)
    }

    fun previous() {
        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else songs.size - 1
        playAtIndex(currentSongIndex)
    }

    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentSong(): Song? = if (currentSongIndex < songs.size) songs[currentSongIndex] else null

    fun setOnProgressUpdateListener(callback: (currentTime: Long, totalTime: Long, isPlaying: Boolean) -> Unit) {
        updateCallback = callback
    }

    private fun startProgressUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                if (mediaPlayer?.isPlaying == true) {
                    val currentTime = mediaPlayer?.currentPosition?.toLong() ?: 0
                    val totalTime = mediaPlayer?.duration?.toLong() ?: 0
                    updateCallback?.invoke(currentTime, totalTime, true)
                    handler.postDelayed(this, 500) // Update every 500ms
                }
            }
        })
    }
}