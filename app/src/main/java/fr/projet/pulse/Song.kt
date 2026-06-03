package com.pulseo  // ← Change de fr.projet.pulseo à com.pulseo

data class Song(
    var id: String = "",
    var name: String = "",
    var userId: String = "",
    var duration: Int = 0,
    var filePath: String = "",
    var dateAdded: Long = 0L  // ← Important: dateAdded (pas dateImported)
)