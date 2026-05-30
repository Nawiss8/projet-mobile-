package com.pulseo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

class ImportMusicActivity : AppCompatActivity() {

    private lateinit var btnChooseFile: Button
    private lateinit var btnImport: Button
    private lateinit var btnBack: Button
    private lateinit var tvFileName: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDurationWarning: TextView
    private lateinit var etSongName: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var selectedFileUri: Uri? = null
    private var durationSeconds: Long = 0
    private val MAX_DURATION_SECONDS = 7 * 60 // 7 minutes max

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_music)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        initializeViews()
        setupListeners()
        requestPermissions()
    }

    private fun initializeViews() {
        btnChooseFile = findViewById(R.id.btnChooseFile)
        btnImport = findViewById(R.id.btnImport)
        btnBack = findViewById(R.id.btnBack)
        tvFileName = findViewById(R.id.tvFileName)
        tvDuration = findViewById(R.id.tvDuration)
        tvDurationWarning = findViewById(R.id.tvDurationWarning)
        etSongName = findViewById(R.id.etSongName)
    }

    private fun setupListeners() {
        btnChooseFile.setOnClickListener {
            openFilePicker()
        }

        btnImport.setOnClickListener {
            if (validateAndImport()) {
                // Ne pas finir immédiatement, attendre la sauvegarde async
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                selectedFileUri = uri
                val fileName = getFileName(uri)

                val songNameWithoutExt = fileName.substringBeforeLast(".")
                etSongName.setText(songNameWithoutExt)

                tvFileName.text = fileName
                checkFileDuration(uri)
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex("_display_name")
                it.moveToFirst()
                it.getString(nameIndex)
            } ?: "Unknown file"
        } catch (e: Exception) {
            "Unknown file"
        }
    }

    private fun checkFileDuration(uri: Uri) {
        try {
            val retriever = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                retriever.setDataSource(this, uri)
            } else {
                retriever.setDataSource(uri.path, emptyMap())
            }

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationSeconds = (duration?.toLong() ?: 0) / 1000

            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            tvDuration.text = String.format("%d:%02d", minutes, seconds)

            if (durationSeconds > MAX_DURATION_SECONDS) {
                tvDurationWarning.text = "❌ File too long! (Max 7 minutes)"
                btnImport.isEnabled = false
            } else {
                tvDurationWarning.text = "✅ Duration is OK"
                btnImport.isEnabled = true
            }

            retriever.release()
        } catch (e: Exception) {
            Toast.makeText(this, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            tvDuration.text = "-- : --"
            tvDurationWarning.text = "❌ Cannot read file"
            btnImport.isEnabled = false
        }
    }

    private fun validateAndImport(): Boolean {
        val songName = etSongName.text.toString().trim()

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show()
            return false
        }

        if (songName.isEmpty()) {
            Toast.makeText(this, "Please enter a song name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (durationSeconds > MAX_DURATION_SECONDS) {
            Toast.makeText(this, "File is too long!", Toast.LENGTH_SHORT).show()
            return false
        }

        // Désactiver le bouton pour éviter les doubles clics
        btnImport.isEnabled = false
        btnImport.text = "Importing..."

        // Sauvegarder en arrière-plan
        Thread {
            saveSongToDatabase(songName)
        }.start()

        return false
    }

    private fun saveSongToDatabase(songName: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            runOnUiThread {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                btnImport.isEnabled = true
                btnImport.text = "Import Song"
            }
            return
        }

        try {
            // 1. Copier le fichier audio dans le stockage interne
            val fileName = "song_${System.currentTimeMillis()}.mp3"
            val filesDir = filesDir
            val audioFile = File(filesDir, fileName)

            selectedFileUri?.let { uri ->
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(audioFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            // 2. Créer l'objet Song
            val songId = database.reference.child("songs").push().key ?: ""
            val song = Song(
                id = songId,
                userId = currentUser.uid,
                name = songName,
                duration = durationSeconds,
                filePath = audioFile.absolutePath,
                dateImported = System.currentTimeMillis()
            )

            // 3. Sauvegarder dans Realtime Database
            database.reference
                .child("songs")
                .child(songId)
                .setValue(song)
                .addOnSuccessListener {
                    runOnUiThread {
                        Toast.makeText(this, "✅ Song '$songName' imported successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    runOnUiThread {
                        Toast.makeText(this, "❌ Error: ${exception.message}", Toast.LENGTH_LONG).show()
                        btnImport.isEnabled = true
                        btnImport.text = "Import Song"
                    }
                }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                btnImport.isEnabled = true
                btnImport.text = "Import Song"
            }
        }
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
        private const val PERMISSION_REQUEST_CODE = 1002
    }
}